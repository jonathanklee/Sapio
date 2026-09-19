import { t, format, relativeDate } from './i18n.js';

const API_BASE = 'https://server.checksap.io/api';
const MEDIA_BASE = 'https://server.checksap.io';
const MAX_PAGE_SIZE = '100';
const ICON_FIELD = { 'populate[icon][fields][0]': 'url' };

const RATING_CLASS = { 1: 'good', 2: 'average', 3: 'bad' };
const RATING_LABEL_EN = { 1: 'Perfect', 2: 'Partial', 3: 'Unusable' };
const PARTIAL_RATING = 2;

const BROKEN_FEATURE_LABELS = {
    notifications:      'Notifications',
    in_app_purchase:    'In-app purchases',
    login:              'Login',
    maps:               'Maps',
    location:           'Location',
    payments:           'Contactless payment',
    cast:               'Screen casting',
    augmented_reality:  'Augmented reality',
};

const SECTIONS = [
    { microg: 1, label: 'microG',   badgeCls: 'microg' },
    { microg: 2, label: 'bareAOSP', badgeCls: 'aosp'   },
];

const ENVS = [
    { rooted: 3, label: 'standard',   cls: 'standard',   labelKey: 'env_standard' },
    { rooted: 4, label: 'permissive', cls: 'permissive', labelKey: 'env_permissive' },
];

const STANDARD_ROOTED = ENVS.find(env => env.cls === 'standard').rooted;

// ─── API ─────────────────────────────────────────────────────────────────────

async function fetchSearch(query) {
    return fetchApplications({
        ...ICON_FIELD,
        'filters[$or][0][name][$contains]': query,
        'filters[$or][1][packageName][$contains]': query,
        'sort': 'name',
        'pagination[pageSize]': MAX_PAGE_SIZE,
    });
}

async function fetchByPackage(packageName) {
    return fetchApplications({
        ...ICON_FIELD,
        'filters[packageName][$eq]': packageName,
        'sort': 'updatedAt:Desc',
        'pagination[pageSize]': MAX_PAGE_SIZE,
    });
}

// No icon: the callers already have one, and these payloads are the largest
// the site asks for.
async function fetchByPackages(packageNames) {
    const filters = Object.fromEntries(
        packageNames.map((name, index) => [`filters[packageName][$in][${index}]`, name])
    );
    const evaluations = [];

    for (let page = 1; ; page++) {
        const batch = await fetchApplications({
            ...filters,
            'sort': 'updatedAt:Desc',
            'pagination[page]': String(page),
            'pagination[pageSize]': MAX_PAGE_SIZE,
        });
        evaluations.push(...batch);

        if (batch.length < Number(MAX_PAGE_SIZE)) {
            return evaluations;
        }
    }
}

async function fetchLatestPage(page, pageSize) {
    return fetchApplications({
        ...ICON_FIELD,
        'sort': 'updatedAt:Desc',
        'pagination[page]': String(page),
        'pagination[pageSize]': String(pageSize),
    });
}

async function fetchApplications(params) {
    const query = new URLSearchParams(params);
    const response = await fetch(`${API_BASE}/sapio-applications?${query}`);

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
    }

    const json = await response.json();

    return json.data.map(item => item.attributes);
}

// ─── Data helpers ─────────────────────────────────────────────────────────────

function groupByPackage(evaluations) {
    const appMap = new Map();

    for (const evaluation of evaluations) {
        const app = appBucketFor(appMap, evaluation);
        adoptIcon(app, evaluation);
        collectEntry(app, evaluation);
    }

    return [...appMap.values()].map(app => ({
        name: app.name,
        packageName: app.packageName,
        iconUrl: app.iconUrl,
        entries: [...app.entriesByEnv.values()].map(currentEntry),
    }));
}

function appBucketFor(appMap, evaluation) {
    if (!appMap.has(evaluation.packageName)) {
        appMap.set(evaluation.packageName, {
            name: evaluation.name,
            packageName: evaluation.packageName,
            iconUrl: null,
            entriesByEnv: new Map(),
        });
    }

    return appMap.get(evaluation.packageName);
}

function adoptIcon(app, evaluation) {
    const path = evaluation.icon?.data?.attributes?.url;

    if (!app.iconUrl && path) {
        app.iconUrl = MEDIA_BASE + path;
    }
}

function collectEntry(app, evaluation) {
    const envKey = `${evaluation.microg}-${evaluation.rooted}`;
    const envEntries = app.entriesByEnv.get(envKey) ?? [];

    envEntries.push({
        microg: evaluation.microg,
        rooted: evaluation.rooted,
        rating: evaluation.rating,
        updatedAt: evaluation.updatedAt,
        versionName: evaluation.versionName ?? null,
        brokenFeatures: evaluation.brokenFeatures ?? null,
    });

    app.entriesByEnv.set(envKey, envEntries);
}

function currentEntry(envEntries) {
    const history = [...envEntries]
        .sort((a, b) => new Date(a.updatedAt) - new Date(b.updatedAt));

    return { ...history[history.length - 1], history };
}

// ─── History backfill ─────────────────────────────────────────────────────────
//
// The feed only ever holds the evaluations of the API page it fetched, so the
// history groupByPackage() derived there is a fragment: the ones a card
// superseded sit far down the global "latest" list. The whole package is
// fetched instead, in one request for the batch of cards about to be drawn.

const HISTORY_CHUNK_SIZE = 25;
const historyByPackage = new Map();

async function loadHistories(apps) {
    const missing = apps
        .map(app => app.packageName)
        .filter(packageName => !historyByPackage.has(packageName));

    if (missing.length > 0) {
        await cacheHistories(missing);
    }

    return apps.map(applyCachedHistory);
}

// Kept for the session: the same cards are re-rendered every time the
// permissive toggle flips, and no evaluation lands in between.
//
// Chunked because every package name goes in the query string, and a broad
// search matches enough of them to push one URL past what a server accepts as
// a request header. The chunks go out together, so it stays one round trip.
async function cacheHistories(packageNames) {
    for (const packageName of packageNames) {
        historyByPackage.set(packageName, []);
    }

    const chunks = [];

    for (let start = 0; start < packageNames.length; start += HISTORY_CHUNK_SIZE) {
        chunks.push(fetchByPackages(packageNames.slice(start, start + HISTORY_CHUNK_SIZE)));
    }

    for (const evaluations of await Promise.all(chunks)) {
        for (const app of groupByPackage(evaluations)) {
            historyByPackage.set(app.packageName, app.entries);
        }
    }
}

function applyCachedHistory(app) {
    const cached = historyByPackage.get(app.packageName) ?? [];

    return {
        ...app,
        entries: app.entries.map(entry => ({
            ...entry,
            history: entryFor(cached, entry.microg, entry.rooted)?.history ?? entry.history,
        })),
    };
}

function entryFor(entries, microg, rooted) {
    return entries.find(e => e.microg === microg && e.rooted === rooted) ?? null;
}

function renderKey(app) {
    const entries = [...app.entries]
        .sort((a, b) => (a.microg - b.microg) || (a.rooted - b.rooted))
        .map(entry => [
            entry.microg,
            entry.rooted,
            entry.rating,
            entry.updatedAt ?? '',
            entry.versionName ?? '',
            (entry.brokenFeatures ?? []).join(','),
            historyFingerprint(entry),
        ].join(':'));

    return [app.packageName, app.name, ...entries].join('|');
}

// An evaluation added to an environment also replaces its current entry,
// which the fingerprint already
// covers, so this exists to catch an older one being edited or removed under
// an unchanged head.
function historyFingerprint(entry) {
    return historyPoints(entry)
        .map(point => [point.rating, ...(point.brokenFeatures ?? [])].join(''))
        .join(',');
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// ─── Summary text ─────────────────────────────────────────────────────────────

function evaluationScenarios(app, withPermissive) {
    const envs = withPermissive ? ENVS : ENVS.filter(env => env.cls === 'standard');
    const scenarios = [];

    for (const section of SECTIONS) {
        for (const env of envs) {
            const entry = entryFor(app.entries, section.microg, env.rooted);

            if (entry) {
                scenarios.push({ section, env, entry });
            }
        }
    }

    return scenarios;
}

function brokenFeatureKeys(entry) {
    if (entry.rating !== PARTIAL_RATING) {
        return [];
    }

    return (entry.brokenFeatures ?? []).filter(key => BROKEN_FEATURE_LABELS[key]);
}

function englishBrokenSuffix(entry) {
    const labels = brokenFeatureKeys(entry).map(key => BROKEN_FEATURE_LABELS[key]);

    return labels.length === 0 ? '' : ` (no ${labels.join(', ').toLowerCase()})`;
}

function localizedBrokenSuffix(entry) {
    const labels = brokenFeatureKeys(entry).map(key => t(`feat_${key}`));

    return labels.length === 0 ? '' : ` (${t('summary_no_prefix')} ${labels.join(', ').toLowerCase()})`;
}

function humanSummary(app, withPermissive) {
    const parts = evaluationScenarios(app, withPermissive).map(({ section, env, entry }) => {
        const scenario = withPermissive ? `${section.label} · ${env.label}` : section.label;
        const rating = RATING_LABEL_EN[entry.rating] ?? '—';

        return `${scenario}: ${rating}${englishBrokenSuffix(entry)}`;
    });

    if (parts.length === 0) {
        return `No evaluation yet for ${app.name} without Google Play Services.`;
    }

    return `${app.name} without Google Play Services — ${parts.join(' · ')}.`;
}

function localizedSummary(app, withPermissive) {
    const parts = evaluationScenarios(app, withPermissive).map(({ section, env, entry }) => {
        const scenario = withPermissive ? `${section.label} · ${t(env.labelKey)}` : section.label;

        return `${scenario}: ${t(`rating_${entry.rating}`)}${localizedBrokenSuffix(entry)}`;
    });

    if (parts.length === 0) {
        return format('summary_none', { '%name': app.name });
    }

    return format('summary_frame', { '%name': app.name, '%parts': parts.join(' · ') });
}

// ─── Rendering ───────────────────────────────────────────────────────────────

function renderCardHeader(app) {
    const header = document.createElement('div');
    header.className = 'card-header';
    header.appendChild(renderAppIcon(app));

    const meta = document.createElement('div');
    meta.className = 'app-meta';
    meta.innerHTML = `
        <span class="app-name">${escapeHtml(app.name)}</span>
        <span class="app-package">${escapeHtml(app.packageName)}</span>
    `;
    header.appendChild(meta);

    return header;
}

function renderAppIcon(app) {
    if (!app.iconUrl) {
        return iconPlaceholder();
    }

    const img = document.createElement('img');
    img.className = 'app-icon';
    img.src = app.iconUrl;
    img.alt = app.name;
    img.loading = 'lazy';
    img.onerror = () => img.replaceWith(iconPlaceholder());

    return img;
}

function iconPlaceholder() {
    const placeholder = document.createElement('div');
    placeholder.className = 'app-icon app-icon-placeholder';
    placeholder.textContent = '?';

    return placeholder;
}

// Opt-in: a chart is only honest once the whole package has been fetched, which
// the app page does outright and the feed does through loadHistories().
function renderSection(section, entries, withHistory = false) {
    const cells = ENVS
        .map(env => ({ env, entry: entryFor(entries, section.microg, env.rooted) }))
        .filter(({ entry }) => entry !== null);

    if (cells.length === 0) {
        return null;
    }

    const block = document.createElement('div');
    block.className = 'eval-section';

    if (cells.every(({ env }) => env.cls === 'permissive')) {
        block.classList.add('eval-section--permissive-only');
    }

    block.appendChild(sectionBadge(section));

    const cellsRow = document.createElement('div');
    cellsRow.className = cells.length === 1 ? 'cells-row cells-row--single' : 'cells-row';

    for (const { env, entry } of cells) {
        cellsRow.appendChild(renderCell(env, entry, withHistory));
    }

    block.appendChild(cellsRow);

    return block;
}

function sectionBadge(section) {
    const badge = document.createElement('span');
    badge.className = `section-badge ${section.badgeCls}`;
    badge.textContent = section.label;

    return badge;
}

function renderCell(env, entry, withHistory) {
    const cell = document.createElement('div');
    cell.className = `eval-cell eval-cell--${env.cls}`;
    cell.appendChild(envBadge(env));
    cell.appendChild(ratingLine(entry, withHistory));

    const broken = brokenFeatureKeys(entry);

    if (broken.length > 0) {
        cell.appendChild(renderBrokenFeatures(broken));
    }

    return cell;
}

function ratingLine(entry, withHistory) {
    const line = document.createElement('div');
    line.className = 'rating-line';
    line.appendChild(ratingRow(entry));

    const chart = withHistory ? renderHistoryChart(entry) : null;

    if (chart) {
        line.appendChild(chart);
    }

    return line;
}

function envBadge(env) {
    const badge = document.createElement('span');
    badge.className = `cell-env-badge ${env.cls}`;
    badge.textContent = t(env.labelKey);

    return badge;
}

function ratingRow(entry) {
    const row = document.createElement('div');
    row.className = 'rating-row';
    const cls = RATING_CLASS[entry.rating] ?? 'unknown';

    const dot = document.createElement('span');
    dot.className = `status-dot ${cls}`;

    const textCol = document.createElement('div');
    textCol.className = 'rating-text-col';
    textCol.appendChild(ratingLabel(entry, cls));

    if (entry.versionName) {
        textCol.appendChild(ratingDetail(`v${entry.versionName}`));
    }

    const dateText = relativeDate(entry.updatedAt);

    if (dateText) {
        const date = ratingDetail(dateText);
        date.dataset.updatedAt = entry.updatedAt;
        textCol.appendChild(date);
    }

    row.appendChild(dot);
    row.appendChild(textCol);

    return row;
}

function ratingLabel(entry, cls) {
    const label = document.createElement('span');
    label.className = `rating-label ${cls}`;
    label.textContent = RATING_CLASS[entry.rating] ? t(`rating_${entry.rating}`) : '—';

    return label;
}

function ratingDetail(text) {
    const detail = document.createElement('span');
    detail.className = 'rating-date';
    detail.textContent = text;

    return detail;
}

// ─── History chart ───────────────────────────────────────────────────────────
//
// Mirrored by tools/refresh.py: the two must draw the same chart.

const SVG_NS = 'http://www.w3.org/2000/svg';
const HISTORY_MAX_POINTS = 8;
const HISTORY_WIDTH = 80;
const HISTORY_HEIGHT = 44;
const HISTORY_PAD_X = 5;
const HISTORY_PAD_Y = 6;
const HISTORY_POINT_RADIUS = 3.5;

function historyPoints(entry) {
    return (entry.history ?? []).slice(-HISTORY_MAX_POINTS);
}

function renderHistoryChart(entry) {
    const points = historyPoints(entry);

    if (points.length < 2) {
        return null;
    }

    const chart = document.createElementNS(SVG_NS, 'svg');
    chart.setAttribute('class', 'history-chart');
    chart.setAttribute('viewBox', `0 0 ${HISTORY_WIDTH} ${HISTORY_HEIGHT}`);
    chart.setAttribute('role', 'img');
    chart.setAttribute('aria-label', t('history_label'));
    chart.appendChild(historyLine(points));

    for (const [index, point] of points.entries()) {
        chart.appendChild(historyDot(point, index, points.length));
    }

    return chart;
}

function historyLine(points) {
    const line = document.createElementNS(SVG_NS, 'polyline');
    line.setAttribute('class', 'history-line');
    line.setAttribute('points', points
        .map((point, index) => `${historyX(index, points.length)},${historyY(point.rating)}`)
        .join(' '));

    return line;
}

function historyDot(point, index, count) {
    const dot = document.createElementNS(SVG_NS, 'circle');
    dot.setAttribute('class', `history-point ${RATING_CLASS[point.rating] ?? 'unknown'}`);
    dot.setAttribute('cx', historyX(index, count));
    dot.setAttribute('cy', historyY(point.rating));
    dot.setAttribute('r', HISTORY_POINT_RADIUS);

    const title = document.createElementNS(SVG_NS, 'title');
    title.textContent = historyTooltip(point);
    dot.appendChild(title);

    return dot;
}

// An absolute date, unlike the cell's: a tooltip is not rewritten by
// syncRelativeDates(), so a relative one would go stale on a pre-rendered page.
function historyTooltip(point) {
    const rating = RATING_CLASS[point.rating]
        ? `${t(`rating_${point.rating}`)}${localizedBrokenSuffix(point)}`
        : '—';
    const parts = [rating];

    if (point.versionName) {
        parts.push(`v${point.versionName}`);
    }

    if (point.updatedAt) {
        parts.push(point.updatedAt.slice(0, 10));
    }

    return parts.join(' · ');
}

function historyX(index, count) {
    const step = (HISTORY_WIDTH - HISTORY_PAD_X * 2) / (count - 1);

    return roundCoordinate(HISTORY_PAD_X + index * step);
}

function historyY(rating) {
    const level = RATING_CLASS[rating] ? rating - 1 : 1;

    return roundCoordinate(HISTORY_PAD_Y + level * (HISTORY_HEIGHT - HISTORY_PAD_Y * 2) / 2);
}

function roundCoordinate(value) {
    return Math.round(value * 100) / 100;
}

// ─── Broken features ─────────────────────────────────────────────────────────

function renderBrokenFeatures(featureKeys) {
    const container = document.createElement('div');
    container.className = 'broken-features';

    const title = document.createElement('span');
    title.className = 'broken-features-title';
    title.textContent = t('doesnt_work');
    container.appendChild(title);

    const chips = document.createElement('div');
    chips.className = 'broken-chips';

    for (const key of featureKeys) {
        const chip = document.createElement('span');
        chip.className = 'broken-chip';
        chip.textContent = t(`feat_${key}`);
        chips.appendChild(chip);
    }

    container.appendChild(chips);

    return container;
}

export {
    SECTIONS,
    STANDARD_ROOTED,
    BROKEN_FEATURE_LABELS,
    fetchLatestPage,
    fetchSearch,
    fetchByPackage,
    groupByPackage,
    loadHistories,
    entryFor,
    renderKey,
    relativeDate,
    humanSummary,
    localizedSummary,
    renderCardHeader,
    renderSection,
};
