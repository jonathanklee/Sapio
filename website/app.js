import {
    SECTIONS,
    ENVS,
    fetchLatestPage,
    fetchSearch,
    groupByPackage,
    entryFor,
    renderCardHeader,
    renderSection,
} from './core.js';
import { setupI18n, t, format, getLang } from './i18n.js';

const DEBOUNCE_MS = 300;
const INITIAL_LATEST_COUNT = 3;
const LATEST_PAGE_SIZE = 10;
const API_FETCH_SIZE = 100;

const PERMISSIVE_STORAGE_KEY = 'sapio:showPermissive';

let showPermissive = localStorage.getItem(PERMISSIVE_STORAGE_KEY) === 'true';
let isLatestMode = false;
let currentApps = [];
let latestApps = [];
let visibleLatestCount = INITIAL_LATEST_COUNT;
let rawLatestEvaluations = [];
let latestApiPage = 0;
let latestApiDone = false;

// ─── DOM refs ────────────────────────────────────────────────────────────────

const searchInput    = document.getElementById('search-input');
const searchClear    = document.getElementById('search-clear');
const resultsGrid    = document.getElementById('results-grid');
const resultsTitle   = document.getElementById('results-title');
const resultsCount   = document.getElementById('results-count');
const resultsEmpty   = document.getElementById('results-empty');
const resultsError   = document.getElementById('results-error');
const permissiveToggle = document.getElementById('permissive-toggle');
const showMoreWrap   = document.getElementById('show-more-wrap');
const showMoreBtn    = document.getElementById('show-more-btn');

permissiveToggle.checked = showPermissive;

function setShowMore(visible) {
    showMoreWrap.style.display = visible ? 'flex' : 'none';
}

// ─── Rendering ───────────────────────────────────────────────────────────────

function renderAppCard(app) {
    const sectionsRow = document.createElement('div');
    sectionsRow.className = 'sections-row';

    for (const section of SECTIONS) {
        const cells = ENVS.map(env => entryFor(app.entries, section.microg, env.rooted));
        const rendered = renderSection(section, cells, showPermissive);
        if (rendered) {
            sectionsRow.appendChild(rendered);
        }
    }

    if (sectionsRow.children.length === 0) {
        return null;
    }

    const card = document.createElement('a');
    card.className = 'app-card app-card--link';
    card.href = `/app/${app.packageName}`;
    card.appendChild(renderCardHeader(app));
    card.appendChild(sectionsRow);

    return card;
}

// ─── State management ─────────────────────────────────────────────────────────

function showSkeletons() {
    resultsGrid.innerHTML = Array(3).fill('<div class="skeleton-card"></div>').join('');
    resultsGrid.hidden = false;
    resultsEmpty.hidden = true;
    resultsError.hidden = true;
    resultsCount.textContent = '';
    setShowMore(false);
}

function renderResults(apps) {
    currentApps = apps;
    resultsGrid.innerHTML = '';
    resultsError.hidden = true;
    setShowMore(false);

    const cards = apps
        .map(renderAppCard)
        .filter(card => card !== null);

    for (const card of cards) {
        resultsGrid.appendChild(card);
    }

    const hasResults = cards.length > 0;
    resultsEmpty.hidden = hasResults;
    resultsGrid.hidden = !hasResults;

    resultsCount.textContent = hasResults
        ? format(cards.length === 1 ? 'count_apps_one' : 'count_apps_other', { '%n': cards.length })
        : '';
}

function rerenderCurrentResults() {
    if (isLatestMode) {
        const renderable = renderableLatestApps();
        renderResults(renderable.slice(0, visibleLatestCount));
        setShowMore(visibleLatestCount < renderable.length || !latestApiDone);
    } else {
        renderResults(currentApps);
    }
}

function showError() {
    resultsGrid.innerHTML = '';
    resultsGrid.hidden = true;
    resultsEmpty.hidden = true;
    resultsError.hidden = false;
    resultsCount.textContent = '';
}

// ─── Data loading ─────────────────────────────────────────────────────────────

function filterToLatestSection(app) {
    if (app.entries.length <= 1) {
        return app;
    }

    const maxTime = Math.max(...app.entries.map(e => new Date(e.updatedAt).getTime()));
    const ONE_HOUR_MS = 60 * 60 * 1000;

    return {
        ...app,
        entries: app.entries.filter(e => maxTime - new Date(e.updatedAt).getTime() <= ONE_HOUR_MS),
    };
}

function renderableLatestApps() {
    const standardRotedValue = ENVS.find(e => e.cls === 'standard').rooted;

    return latestApps.filter(app =>
        showPermissive || app.entries.some(e => e.rooted === standardRotedValue)
    );
}

async function ensureEnoughApps(needed) {
    while (renderableLatestApps().length < needed && !latestApiDone) {
        latestApiPage++;
        const batch = await fetchLatestPage(latestApiPage, API_FETCH_SIZE);
        rawLatestEvaluations.push(...batch);
        latestApps = groupByPackage(rawLatestEvaluations).map(filterToLatestSection);

        if (batch.length < API_FETCH_SIZE) {
            latestApiDone = true;
        }
    }
}

async function loadLatest() {
    isLatestMode = true;
    showSkeletons();
    resultsTitle.textContent = t('results_latest');
    rawLatestEvaluations = [];
    latestApps = [];
    latestApiPage = 0;
    latestApiDone = false;
    visibleLatestCount = INITIAL_LATEST_COUNT;

    try {
        await ensureEnoughApps(INITIAL_LATEST_COUNT);
        if (!isLatestMode) {
            return;
        }

        const renderable = renderableLatestApps();
        renderResults(renderable.slice(0, visibleLatestCount));
        setShowMore(renderable.length > visibleLatestCount || !latestApiDone);
    } catch {
        showError();
    }
}

async function runSearch(query) {
    isLatestMode = false;
    showSkeletons();
    resultsTitle.textContent = format('results_for', { '%s': query });
    try {
        const raw = await fetchSearch(query);
        const apps = groupByPackage(raw);
        renderResults(apps);
    } catch {
        showError();
    }
}

// ─── Events ───────────────────────────────────────────────────────────────────

let debounceTimer = null;

searchInput.addEventListener('input', () => {
    const query = searchInput.value.trim();
    searchClear.hidden = query.length === 0;

    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
        if (query.length === 0) {
            loadLatest();
        } else if (query.length >= 2) {
            runSearch(query);
        }
    }, DEBOUNCE_MS);
});

searchClear.addEventListener('click', () => {
    searchInput.value = '';
    searchClear.hidden = true;
    loadLatest();
    searchInput.focus();
});

showMoreBtn.addEventListener('click', async () => {
    const nextCount = visibleLatestCount <= INITIAL_LATEST_COUNT
        ? LATEST_PAGE_SIZE
        : visibleLatestCount + LATEST_PAGE_SIZE;

    showMoreBtn.disabled = true;

    try {
        await ensureEnoughApps(nextCount);
    } finally {
        showMoreBtn.disabled = false;
    }

    if (!isLatestMode) {
        return;
    }

    const renderable = renderableLatestApps();
    visibleLatestCount = Math.min(nextCount, renderable.length);
    renderResults(renderable.slice(0, visibleLatestCount));
    setShowMore(visibleLatestCount < renderable.length || !latestApiDone);
});

permissiveToggle.addEventListener('change', () => {
    showPermissive = permissiveToggle.checked;
    localStorage.setItem(PERMISSIVE_STORAGE_KEY, showPermissive);
    rerenderCurrentResults();
});

for (const chip of document.querySelectorAll('.example-chip')) {
    chip.addEventListener('click', () => {
        searchInput.value = chip.dataset.query;
        searchInput.dispatchEvent(new Event('input'));
        searchInput.focus();
    });
}

// ─── Hero stats ─────────────────────────────────────────────────────────────────

async function loadStats() {
    try {
        const res = await fetch('/stats.json');
        if (!res.ok) { return; }

        const { apps, evaluations } = await res.json();
        document.getElementById('stat-apps').textContent = apps.toLocaleString(getLang());
        document.getElementById('stat-evaluations').textContent = evaluations.toLocaleString(getLang());
        document.getElementById('hero-stats').hidden = false;
    } catch {
        // Stats are non-essential; leave them hidden on failure.
    }
}

// ─── Init ─────────────────────────────────────────────────────────────────────

setupI18n();
loadLatest().then(() => {
    if (location.hash) {
        document.querySelector(location.hash)?.scrollIntoView();
    }
});
loadStats();
