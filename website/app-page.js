import {
    SECTIONS,
    fetchByPackage,
    groupByPackage,
    renderKey,
    relativeDate,
    humanSummary,
    localizedSummary,
    renderCardHeader,
    renderSection,
} from './core.js';
import { enableShareButton, renderShareButton } from './share.js';
import { setupI18n, t } from './i18n.js';

const SITE_ORIGIN = 'https://checksap.io';
const SITE_ICON = `${SITE_ORIGIN}/icon.png`;
const PERMISSIVE_STORAGE_KEY = 'sapio:showPermissive';
const MAX_DESCRIPTION_LENGTH = 300;

const LEGEND_ITEMS = [
    { cls: 'good',    key: 'legend_works' },
    { cls: 'average', key: 'legend_partial' },
    { cls: 'bad',     key: 'legend_broken' },
];

const detail = document.getElementById('app-detail');
const errorBox = document.getElementById('app-error');
const shareBanner = document.getElementById('share-banner');

let currentApp = null;
let showPermissive = localStorage.getItem(PERMISSIVE_STORAGE_KEY) === 'true';

setupI18n();
loadApp();

async function loadApp() {
    const packageName = readPackageName();

    if (!packageName) {
        showError();
        return;
    }

    try {
        const evaluations = await fetchByPackage(packageName);
        const app = groupByPackage(evaluations)[0];

        if (!app || app.entries.length === 0) {
            showError();
            return;
        }

        currentApp = app;
        renderApp();
        enablePermissiveToggle();
        enableShareButton(shareBanner, () => currentApp);

        if (!isPreRendered()) {
            applySeo(app);
        }
    } catch {
        showError();
    }
}

function isPreRendered() {
    return Boolean(document.body.dataset.package);
}

function readPackageName() {
    if (document.body.dataset.package) {
        return document.body.dataset.package;
    }

    const fromQuery = new URLSearchParams(location.search).get('app');

    if (fromQuery) {
        return fromQuery;
    }

    const match = location.pathname.match(/\/app\/([^/]+)\/?$/);

    return match ? decodeURIComponent(match[1]) : null;
}

// ─── Permissive toggle ───────────────────────────────────────────────────────

function enablePermissiveToggle() {
    const checkbox = detail.querySelector('#permissive-toggle');

    if (checkbox) {
        checkbox.checked = showPermissive;
    }

    detail.addEventListener('change', event => {
        if (event.target.id !== 'permissive-toggle') {
            return;
        }

        showPermissive = event.target.checked;
        localStorage.setItem(PERMISSIVE_STORAGE_KEY, showPermissive);
        applyPermissiveClass();
    });
}

function applyPermissiveClass() {
    document.documentElement.classList.toggle('show-permissive', showPermissive);
}

// ─── Rendering ───────────────────────────────────────────────────────────────

function renderApp() {
    detail.removeAttribute('aria-busy');
    applyPermissiveClass();

    if (isPreRenderedCardCurrent()) {
        syncRelativeDates();
        return;
    }

    repaintApp();
}

function isPreRenderedCardCurrent() {
    return detail.dataset.renderKey === renderKey(currentApp);
}

// The generator writes the same string relativeDate() produces, but it ages
// between hourly runs, so only a date that has actually drifted is rewritten.
function syncRelativeDates() {
    for (const node of detail.querySelectorAll('[data-updated-at]')) {
        const text = relativeDate(node.dataset.updatedAt);

        if (text && text !== node.textContent) {
            node.textContent = text;
        }
    }
}

function repaintApp() {
    detail.innerHTML = '';

    const card = document.createElement('article');
    card.className = 'app-card app-detail-card';
    card.appendChild(renderCardHeader(currentApp));
    card.appendChild(renderSummaries(currentApp));
    card.appendChild(renderSections(currentApp));

    detail.appendChild(renderLegend());
    detail.appendChild(card);
    detail.dataset.renderKey = renderKey(currentApp);

    renderShareBanner(currentApp);
}

function renderShareBanner(app) {
    const section = renderShareButton(app);
    shareBanner.innerHTML = '';
    shareBanner.hidden = section === null;

    if (section) {
        shareBanner.appendChild(section);
    }
}

function renderLegend() {
    const legend = document.createElement('div');
    legend.className = 'rating-legend';

    for (const item of LEGEND_ITEMS) {
        const entry = document.createElement('span');
        entry.className = 'legend-item';

        const dot = document.createElement('span');
        dot.className = `status-dot ${item.cls}`;

        const label = document.createElement('span');
        label.textContent = t(item.key);

        entry.appendChild(dot);
        entry.appendChild(label);
        legend.appendChild(entry);
    }

    legend.appendChild(permissiveToggleLabel());

    return legend;
}

function permissiveToggleLabel() {
    const label = document.createElement('label');
    label.className = 'permissive-label';

    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.id = 'permissive-toggle';
    checkbox.checked = showPermissive;

    const text = document.createElement('span');
    text.textContent = t('show_permissive');

    label.appendChild(checkbox);
    label.appendChild(text);

    return label;
}

// Both variants ship together so the toggle is a CSS class flip, never a repaint.
function renderSummaries(app) {
    const fragment = document.createDocumentFragment();

    for (const variant of ['standard', 'permissive']) {
        const summary = document.createElement('p');
        summary.className = `app-summary app-summary--${variant}`;
        summary.textContent = localizedSummary(app, variant === 'permissive');
        fragment.appendChild(summary);
    }

    return fragment;
}

function renderSections(app) {
    const fragment = document.createDocumentFragment();
    const sectionsRow = document.createElement('div');
    sectionsRow.className = 'sections-row';

    for (const section of SECTIONS) {
        const rendered = renderSection(section, app.entries);

        if (rendered) {
            sectionsRow.appendChild(rendered);
        }
    }

    if (hasOnlyPermissiveSections(sectionsRow)) {
        fragment.appendChild(permissiveOnlyHint());
    }

    fragment.appendChild(sectionsRow);

    return fragment;
}

function hasOnlyPermissiveSections(sectionsRow) {
    return sectionsRow.children.length > 0
        && [...sectionsRow.children].every(el => el.classList.contains('eval-section--permissive-only'));
}

function permissiveOnlyHint() {
    const hint = document.createElement('p');
    hint.className = 'app-summary permissive-only-hint';
    hint.textContent = t('permissive_only_hint');

    return hint;
}

function showError() {
    detail.hidden = true;
    errorBox.hidden = false;
    document.title = `${t('app_not_found_title')} — Sapio`;
}

// ─── SEO metadata ────────────────────────────────────────────────────────────
//
// Pre-rendered pages already carry localized metadata, a canonical pointing at
// their own language and their JSON-LD, so this only runs for /app.html?app=.

function applySeo(app) {
    const url = `${SITE_ORIGIN}/app/${encodeURIComponent(app.packageName)}`;
    const title = `${app.name} without Google Play Services — Sapio`;
    const description = clamp(humanSummary(app, false), MAX_DESCRIPTION_LENGTH);
    const image = app.iconUrl || SITE_ICON;

    document.title = title;
    setMetaName('description', description);
    setLinkRel('canonical', url);

    setMetaProperty('og:title', title);
    setMetaProperty('og:description', description);
    setMetaProperty('og:url', url);
    setMetaProperty('og:image', image);
    setMetaName('twitter:image', image);

    setStructuredData(app, url, image);
}

function setStructuredData(app, url, image) {
    const script = document.createElement('script');
    script.type = 'application/ld+json';
    script.textContent = JSON.stringify({
        '@context': 'https://schema.org',
        '@type': 'SoftwareApplication',
        name: app.name,
        identifier: app.packageName,
        operatingSystem: 'Android (without Google Play Services)',
        applicationCategory: 'MobileApplication',
        image,
        url,
    });

    document.head.appendChild(script);
}

function setMetaName(name, content) {
    const tag = document.head.querySelector(`meta[name="${name}"]`) ?? createHeadTag('meta', { name });
    tag.setAttribute('content', content);
}

function setMetaProperty(property, content) {
    const tag = document.head.querySelector(`meta[property="${property}"]`) ?? createHeadTag('meta', { property });
    tag.setAttribute('content', content);
}

function setLinkRel(rel, href) {
    const tag = document.head.querySelector(`link[rel="${rel}"]`) ?? createHeadTag('link', { rel });
    tag.setAttribute('href', href);
}

function createHeadTag(tagName, attributes) {
    const tag = document.createElement(tagName);

    for (const [key, value] of Object.entries(attributes)) {
        tag.setAttribute(key, value);
    }

    document.head.appendChild(tag);

    return tag;
}

function clamp(text, max) {
    return text.length <= max ? text : `${text.slice(0, max - 1).trimEnd()}…`;
}
