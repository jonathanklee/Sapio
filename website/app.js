import {
    SECTIONS,
    ENVS,
    fetchLatest,
    fetchSearch,
    groupByPackage,
    entryFor,
    renderCardHeader,
    renderSection,
} from './core.js';
import { setupI18n, t, format, getLang } from './i18n.js';

const DEBOUNCE_MS = 300;

let showUnsafe = false;
let currentApps = [];

// ─── DOM refs ────────────────────────────────────────────────────────────────

const searchInput    = document.getElementById('search-input');
const searchClear    = document.getElementById('search-clear');
const resultsGrid    = document.getElementById('results-grid');
const resultsTitle   = document.getElementById('results-title');
const resultsCount   = document.getElementById('results-count');
const resultsEmpty   = document.getElementById('results-empty');
const resultsError   = document.getElementById('results-error');
const unsafeToggle   = document.getElementById('unsafe-toggle');

// ─── Rendering ───────────────────────────────────────────────────────────────

function renderAppCard(app) {
    const sectionsRow = document.createElement('div');
    sectionsRow.className = 'sections-row';

    for (const section of SECTIONS) {
        const cells = ENVS.map(env => entryFor(app.entries, section.microg, env.rooted));
        const rendered = renderSection(section, cells, showUnsafe);
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
    resultsGrid.innerHTML = Array(5).fill('<div class="skeleton-card"></div>').join('');
    resultsGrid.hidden = false;
    resultsEmpty.hidden = true;
    resultsError.hidden = true;
    resultsCount.textContent = '';
}

function renderResults(apps) {
    currentApps = apps;
    resultsGrid.innerHTML = '';
    resultsError.hidden = true;

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
    renderResults(currentApps);
}

function showError() {
    resultsGrid.innerHTML = '';
    resultsGrid.hidden = true;
    resultsEmpty.hidden = true;
    resultsError.hidden = false;
    resultsCount.textContent = '';
}

// ─── Data loading ─────────────────────────────────────────────────────────────

async function loadLatest() {
    showSkeletons();
    resultsTitle.textContent = t('results_latest');
    try {
        const raw = await fetchLatest();
        const apps = groupByPackage(raw).slice(0, 20);
        renderResults(apps);
    } catch {
        showError();
    }
}

async function runSearch(query) {
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

unsafeToggle.addEventListener('change', () => {
    showUnsafe = unsafeToggle.checked;
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
loadLatest();
loadStats();
