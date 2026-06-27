import { t, format, relativeDate } from './i18n.js';

const API_BASE = 'https://server.sapio.ovh/api';

const RATING = {
    1: { cls: 'good'    },
    2: { cls: 'average' },
    3: { cls: 'bad'     },
};

// English labels — used by the generator (static pages) and the SEO summary.
const RATING_LABEL_EN = { 1: 'Perfect', 2: 'Partial', 3: 'Unusable' };

const BROKEN_FEATURE_LABELS = {
    notifications:      'Notifications',
    in_app_purchase:    'In-app purchases',
    login:              'Login',
    maps:               'Maps',
    location:           'Location',
    payments:           'Physical payments',
    cast:               'Screen casting',
    augmented_reality:  'Augmented reality',
};

const SECTIONS = [
    { microg: 1, label: 'microG',   badgeCls: 'microg' },
    { microg: 2, label: 'bareAOSP', badgeCls: 'aosp'   },
];

const ENVS = [
    { rooted: 3, label: 'secure', cls: 'secure', labelKey: 'env_secure' },
    { rooted: 4, label: 'unsafe', cls: 'unsafe', labelKey: 'env_unsafe' },
];

// ─── API ─────────────────────────────────────────────────────────────────────

async function fetchLatest() {
    const params = new URLSearchParams({
        'pagination[pageSize]': '80',
        'sort': 'updatedAt:Desc',
        'populate[icon][fields][0]': 'url',
    });

    return fetchApplications(params);
}

async function fetchSearch(query) {
    const params = new URLSearchParams({
        'filters[$or][0][name][$contains]': query,
        'filters[$or][1][packageName][$contains]': query,
        'sort': 'name',
        'populate[icon][fields][0]': 'url',
        'pagination[pageSize]': '100',
    });

    return fetchApplications(params);
}

async function fetchByPackage(packageName) {
    const params = new URLSearchParams({
        'filters[packageName][$eq]': packageName,
        'sort': 'updatedAt:Desc',
        'populate[icon][fields][0]': 'url',
        'pagination[pageSize]': '100',
    });

    return fetchApplications(params);
}

async function fetchApplications(params) {
    const res = await fetch(`${API_BASE}/sapio-applications?${params}`);
    if (!res.ok) { throw new Error(`HTTP ${res.status}`); }

    const json = await res.json();
    return json.data.map(item => item.attributes);
}

async function fetchAll() {
    const pageSize = 100;
    const evaluations = [];

    for (let page = 1; ; page++) {
        const params = new URLSearchParams({
            'sort': 'updatedAt:Desc',
            'populate[icon][fields][0]': 'url',
            'pagination[page]': String(page),
            'pagination[pageSize]': String(pageSize),
        });

        const batch = await fetchApplications(params);
        evaluations.push(...batch);

        if (batch.length < pageSize) {
            break;
        }
    }

    return evaluations;
}

// ─── Data helpers ─────────────────────────────────────────────────────────────

function groupByPackage(evaluations) {
    const appMap = new Map();

    for (const ev of evaluations) {
        const app = appBucketFor(appMap, ev);
        adoptIcon(app, ev);
        keepMostRecentEntry(app, ev);
    }

    return [...appMap.values()].map(app => ({
        name: app.name,
        packageName: app.packageName,
        iconUrl: app.iconUrl,
        entries: [...app.entriesByEnv.values()],
    }));
}

function appBucketFor(appMap, ev) {
    if (!appMap.has(ev.packageName)) {
        appMap.set(ev.packageName, {
            name: ev.name,
            packageName: ev.packageName,
            iconUrl: null,
            entriesByEnv: new Map(),
        });
    }

    return appMap.get(ev.packageName);
}

function adoptIcon(app, ev) {
    if (!app.iconUrl && ev.icon?.data?.attributes?.url) {
        app.iconUrl = `https://server.sapio.ovh${ev.icon.data.attributes.url}`;
    }
}

function keepMostRecentEntry(app, ev) {
    const envKey = `${ev.microg}-${ev.rooted}`;
    const candidate = {
        microg: ev.microg,
        rooted: ev.rooted,
        rating: ev.rating,
        updatedAt: ev.updatedAt,
        versionName: ev.versionName ?? null,
        brokenFeatures: ev.brokenFeatures ?? null,
    };

    const existing = app.entriesByEnv.get(envKey);
    const isNewer = !existing
        || new Date(candidate.updatedAt) > new Date(existing.updatedAt);

    if (isNewer) {
        app.entriesByEnv.set(envKey, candidate);
    }
}

function entryFor(entries, microg, rooted) {
    return entries.find(e => e.microg === microg && e.rooted === rooted) ?? null;
}

function formatVersionAndDate(versionName, updatedAt) {
    const dateStr = relativeDate(updatedAt) ?? '';
    return versionName ? `v${versionName} · ${dateStr}` : dateStr;
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// ─── Summary text (shared by the page body, meta tags and the generator) ────────

function evaluationLines(app, withUnsafe) {
    const envs = withUnsafe ? ENVS : ENVS.filter(env => env.cls === 'secure');
    const lines = [];

    for (const section of SECTIONS) {
        for (const env of envs) {
            const entry = entryFor(app.entries, section.microg, env.rooted);
            if (!entry) { continue; }

            lines.push({
                scenario: withUnsafe ? `${section.label} · ${env.label}` : section.label,
                rating: RATING_LABEL_EN[entry.rating] ?? '—',
                broken: brokenLabels(entry),
            });
        }
    }

    return lines;
}

function brokenLabels(entry) {
    if (entry.rating !== 2 || !entry.brokenFeatures?.length) {
        return [];
    }

    return entry.brokenFeatures
        .map(key => BROKEN_FEATURE_LABELS[key])
        .filter(Boolean);
}

function humanSummary(app, withUnsafe) {
    const parts = evaluationLines(app, withUnsafe).map(line => {
        const broken = line.broken.length > 0 ? ` (no ${line.broken.join(', ').toLowerCase()})` : '';
        return `${line.scenario}: ${line.rating}${broken}`;
    });

    if (parts.length === 0) {
        return `No evaluation yet for ${app.name} without Google Play Services.`;
    }

    return `${app.name} without Google Play Services — ${parts.join(' · ')}.`;
}

// Localized counterpart of humanSummary — for the visible summary on the page.
function localizedSummary(app, withUnsafe) {
    const envs = withUnsafe ? ENVS : ENVS.filter(env => env.cls === 'secure');
    const parts = [];

    for (const section of SECTIONS) {
        for (const env of envs) {
            const entry = entryFor(app.entries, section.microg, env.rooted);
            if (!entry) { continue; }

            const scenario = withUnsafe ? `${section.label} · ${t(env.labelKey)}` : section.label;
            parts.push(`${scenario}: ${t(`rating_${entry.rating}`)}${localizedBroken(entry)}`);
        }
    }

    if (parts.length === 0) {
        return format('summary_none', { '%name': app.name });
    }

    return format('summary_frame', { '%name': app.name, '%parts': parts.join(' · ') });
}

function localizedBroken(entry) {
    if (entry.rating !== 2 || !entry.brokenFeatures?.length) {
        return '';
    }

    const labels = entry.brokenFeatures
        .filter(key => BROKEN_FEATURE_LABELS[key])
        .map(key => t(`feat_${key}`));

    if (labels.length === 0) {
        return '';
    }

    return ` (${t('summary_no_prefix')} ${labels.join(', ').toLowerCase()})`;
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

function renderSection(section, cells, showUnsafe) {
    const visibleCells = visibleCellsFor(cells, showUnsafe);
    if (visibleCells.length === 0) {
        return null;
    }

    const block = document.createElement('div');
    block.className = 'eval-section';
    block.appendChild(sectionBadge(section));

    const cellsRow = document.createElement('div');
    cellsRow.className = visibleCells.length === 1 ? 'cells-row cells-row--single' : 'cells-row';

    for (const { env, entry } of visibleCells) {
        cellsRow.appendChild(renderCell(env, entry, showUnsafe));
    }

    block.appendChild(cellsRow);
    return block;
}

function visibleCellsFor(cells, showUnsafe) {
    const visibleEnvs = showUnsafe ? ENVS : ENVS.filter(e => e.cls === 'secure');

    return visibleEnvs
        .map((env, i) => ({ env, entry: cells[i] }))
        .filter(({ entry }) => entry !== null);
}

function sectionBadge(section) {
    const badge = document.createElement('span');
    badge.className = `section-badge ${section.badgeCls}`;
    badge.textContent = section.label;

    return badge;
}

function renderCell(env, entry, showEnvBadge) {
    const cell = document.createElement('div');
    cell.className = 'eval-cell';
    cell.appendChild(envBadge(env, showEnvBadge));
    cell.appendChild(ratingRow(entry));

    if (entry?.rating === 2 && entry.brokenFeatures?.length > 0) {
        cell.appendChild(renderBrokenFeatures(entry.brokenFeatures));
    }

    return cell;
}

function envBadge(env, visible) {
    const badge = document.createElement('span');
    badge.className = `cell-env-badge ${env.cls}`;
    badge.textContent = t(env.labelKey);
    badge.classList.toggle('env-badge--hidden', !visible);

    return badge;
}

function ratingRow(entry) {
    const row = document.createElement('div');
    row.className = 'rating-row';

    if (!entry) {
        return row;
    }

    const cls = RATING[entry.rating]?.cls ?? 'unknown';

    const dot = document.createElement('span');
    dot.className = `status-dot ${cls}`;

    const textCol = document.createElement('div');
    textCol.className = 'rating-text-col';

    const label = document.createElement('span');
    label.className = `rating-label ${cls}`;
    label.textContent = RATING[entry.rating] ? t(`rating_${entry.rating}`) : '—';

    const date = document.createElement('span');
    date.className = 'rating-date';
    date.textContent = formatVersionAndDate(entry.versionName, entry.updatedAt);

    textCol.appendChild(label);
    textCol.appendChild(date);
    row.appendChild(dot);
    row.appendChild(textCol);

    return row;
}

function renderBrokenFeatures(features) {
    const container = document.createElement('div');
    container.className = 'broken-features';

    const title = document.createElement('span');
    title.className = 'broken-features-title';
    title.textContent = t('doesnt_work');
    container.appendChild(title);

    const chips = document.createElement('div');
    chips.className = 'broken-chips';

    for (const key of features) {
        if (!BROKEN_FEATURE_LABELS[key]) { continue; }

        const chip = document.createElement('span');
        chip.className = 'broken-chip';
        chip.textContent = `× ${t(`feat_${key}`)}`;
        chips.appendChild(chip);
    }

    container.appendChild(chips);
    return container;
}

function iconPlaceholder() {
    const el = document.createElement('div');
    el.className = 'app-icon app-icon-placeholder';
    el.textContent = '?';

    return el;
}

export {
    RATING,
    BROKEN_FEATURE_LABELS,
    SECTIONS,
    ENVS,
    fetchLatest,
    fetchSearch,
    fetchByPackage,
    fetchAll,
    groupByPackage,
    entryFor,
    relativeDate,
    formatVersionAndDate,
    escapeHtml,
    evaluationLines,
    humanSummary,
    localizedSummary,
    renderCardHeader,
    renderSection,
    renderCell,
    renderBrokenFeatures,
};
