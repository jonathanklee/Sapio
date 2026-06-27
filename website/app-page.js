import {
    SECTIONS,
    ENVS,
    fetchByPackage,
    groupByPackage,
    entryFor,
    humanSummary,
    localizedSummary,
    renderCardHeader,
    renderSection,
} from './core.js';
import { setupI18n, t } from './i18n.js';

const SITE_ORIGIN = 'https://sapio.ovh';
const SITE_ICON = `${SITE_ORIGIN}/icon.png`;

const detail = document.getElementById('app-detail');
const errorBox = document.getElementById('app-error');
const toolbar = document.getElementById('app-toolbar');
const unsafeToggle = document.getElementById('unsafe-toggle');

let currentApp = null;
let showUnsafe = false;

// ─── Entry point ───────────────────────────────────────────────────────────────

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
        enableUnsafeToggle();
        renderApp();
        applySeo(app);
    } catch {
        showError();
    }
}

function enableUnsafeToggle() {
    toolbar.hidden = false;
    unsafeToggle.addEventListener('change', () => {
        showUnsafe = unsafeToggle.checked;
        renderApp();
    });
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

// ─── Rendering ──────────────────────────────────────────────────────────────────

function renderApp() {
    detail.removeAttribute('aria-busy');
    detail.innerHTML = '';

    const card = document.createElement('article');
    card.className = 'app-card app-detail-card';
    card.appendChild(renderCardHeader(currentApp));
    card.appendChild(renderSummary(currentApp, showUnsafe));
    card.appendChild(renderSections(currentApp, showUnsafe));

    detail.appendChild(card);
}

function renderSections(app, withUnsafe) {
    const sectionsRow = document.createElement('div');
    sectionsRow.className = 'sections-row';

    for (const section of SECTIONS) {
        const cells = ENVS.map(env => entryFor(app.entries, section.microg, env.rooted));
        const rendered = renderSection(section, cells, withUnsafe);
        if (rendered) {
            sectionsRow.appendChild(rendered);
        }
    }

    if (sectionsRow.children.length === 0) {
        return unsafeOnlyHint();
    }

    return sectionsRow;
}

function unsafeOnlyHint() {
    const hint = document.createElement('p');
    hint.className = 'app-summary';
    hint.textContent = t('unsafe_only_hint');

    return hint;
}

function renderSummary(app, withUnsafe) {
    const summary = document.createElement('p');
    summary.className = 'app-summary';
    summary.textContent = localizedSummary(app, withUnsafe);

    return summary;
}

// ─── SEO / social metadata ───────────────────────────────────────────────────────

function applySeo(app) {
    const url = `${SITE_ORIGIN}/app/${encodeURIComponent(app.packageName)}`;
    const title = `${app.name} without Google Play Services — Sapio`;
    const description = clamp(humanSummary(app, /* withUnsafe */ false), 300);
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
    const data = {
        '@context': 'https://schema.org',
        '@type': 'SoftwareApplication',
        name: app.name,
        identifier: app.packageName,
        operatingSystem: 'Android (without Google Play Services)',
        applicationCategory: 'MobileApplication',
        image,
        url,
    };

    const script = document.createElement('script');
    script.type = 'application/ld+json';
    script.textContent = JSON.stringify(data);
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

// ─── States ──────────────────────────────────────────────────────────────────────

function showError() {
    detail.hidden = true;
    errorBox.hidden = false;
    document.title = `${t('app_not_found_title')} — Sapio`;
}
