import { mkdir, writeFile, rm } from 'node:fs/promises';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

import {
    fetchAll,
    groupByPackage,
    humanSummary,
    evaluationLines,
    escapeHtml,
} from '../core.js';

const SITE_ORIGIN = 'https://sapio.ovh';
const SITE_ICON = `${SITE_ORIGIN}/icon.png`;
const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const APP_DIR = join(ROOT, 'app');

main();

async function main() {
    const evaluations = await fetchAll();
    const allApps = groupByPackage(evaluations);
    const pages = allApps
        .filter(hasSecureEvaluation)
        .filter(hasSafePackageName);

    await resetAppDir();
    await Promise.all(pages.map(writeAppPage));
    await writeSitemap(pages);
    await writeRobots();
    await writeStats(allApps.length, evaluations.length);

    console.log(`Generated ${pages.length} app pages, sitemap.xml, robots.txt and stats.json in ${ROOT}`);
}

async function writeStats(apps, evaluations) {
    await writeFile(join(ROOT, 'stats.json'), JSON.stringify({ apps, evaluations }), 'utf8');
}

function hasSecureEvaluation(app) {
    return evaluationLines(app, false).length > 0;
}

function hasSafePackageName(app) {
    return /^[A-Za-z0-9_.]+$/.test(app.packageName);
}

async function resetAppDir() {
    await rm(APP_DIR, { recursive: true, force: true });
    await mkdir(APP_DIR, { recursive: true });
}

async function writeAppPage(app) {
    const dir = join(APP_DIR, app.packageName);
    await mkdir(dir, { recursive: true });
    await writeFile(join(dir, 'index.html'), renderPage(app), 'utf8');
}

function renderPage(app) {
    const url = `${SITE_ORIGIN}/app/${app.packageName}`;
    const title = `${app.name} without Google Play Services — Sapio`;
    const description = clamp(humanSummary(app, false), 300);
    const image = app.iconUrl || SITE_ICON;

    return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${escapeHtml(title)}</title>
    <meta name="description" content="${attr(description)}">

    <link rel="canonical" href="${attr(url)}">
    <link rel="icon" href="/favicon.ico" sizes="any">
    <link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png">
    <link rel="apple-touch-icon" href="/apple-touch-icon.png">

    <meta property="og:type" content="website">
    <meta property="og:site_name" content="Sapio">
    <meta property="og:title" content="${attr(title)}">
    <meta property="og:description" content="${attr(description)}">
    <meta property="og:url" content="${attr(url)}">
    <meta property="og:image" content="${attr(image)}">
    <meta name="twitter:card" content="summary">
    <meta name="twitter:image" content="${attr(image)}">

    <script type="application/ld+json">${jsonLd(app, url, image)}</script>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="/style.css">
</head>
<body data-package="${attr(app.packageName)}">

    <header class="header">
        <nav class="nav container">
            <a href="/index.html" class="nav-logo">
                <img src="/icon.png" alt="Sapio" class="nav-icon">
                <span>Sapio</span>
            </a>
            <ul class="nav-links">
                <li><a href="/index.html#about" data-i18n="nav_about">About</a></li>
                <li><a href="https://github.com/jonathanklee/Sapio" target="_blank" rel="noopener">GitHub</a></li>
                <li><select id="lang-select" class="lang-select" aria-label="Language"></select></li>
            </ul>
        </nav>
    </header>

    <main class="app-page container">
        <a href="/index.html" class="back-link" data-i18n="back_all">← All evaluations</a>

        <div id="app-toolbar" class="app-toolbar" hidden>
            <label class="unsafe-label">
                <input type="checkbox" id="unsafe-toggle">
                <span data-i18n="show_unsafe">Show unsafe environments</span>
            </label>
        </div>

        <div id="app-detail" class="app-detail">
            ${renderStaticDetail(app)}
        </div>

        <div id="app-error" class="state-box state-error" hidden>
            <p class="state-title" data-i18n="app_not_found_title">App not found</p>
            <p class="state-hint"><span data-i18n="app_not_found_hint">We couldn't find an evaluation for this app.</span> <a href="/index.html" data-i18n="browse_all">Browse all evaluations</a>.</p>
        </div>
    </main>

    <section class="contribute-section">
        <div class="container contribute-inner">
            <h2 class="contribute-title" data-i18n="contribute_title">Want to contribute?</h2>
            <p class="contribute-text" data-i18n="contribute_text">
                Evaluations come from the community, through the app. Install Sapio on your
                deGoogled device to rate apps and share their compatibility with everyone.
            </p>
            <div class="contribute-actions">
                <a href="https://f-droid.org/packages/com.klee.sapio/" target="_blank" rel="noopener" class="btn btn-primary" data-i18n="contribute_fdroid">
                    Get it on F-Droid
                </a>
                <a href="https://github.com/jonathanklee/Sapio/releases" target="_blank" rel="noopener" class="btn btn-outline" data-i18n="contribute_github">
                    GitHub releases
                </a>
            </div>
        </div>
    </section>

    <footer class="footer">
        <div class="container footer-inner">
            <p class="footer-disclaimer">
                <strong data-i18n="disclaimer_label">Disclaimer</strong> — <span data-i18n="disclaimer_text">Evaluations are community-contributed and may be inaccurate, incomplete, or device-specific. Sapio and its maintainers are not responsible for any issues arising from relying on these evaluations.</span>
            </p>
            <p class="footer-credits"><span data-i18n="footer_credits_by">Brain icons by</span> <a href="https://www.flaticon.com/authors/freepik" target="_blank" rel="noopener">Freepik</a> — Flaticon</p>
        </div>
    </footer>

    <script type="module" src="/app-page.js"></script>

</body>
</html>
`;
}

function renderStaticDetail(app) {
    const lines = evaluationLines(app, false)
        .map(line => `<li>${escapeHtml(line.scenario)}: ${escapeHtml(line.rating)}${brokenSuffix(line)}</li>`)
        .join('\n            ');

    return `<article class="app-card app-detail-card">
        <div class="card-header">
            ${renderStaticIcon(app)}
            <div class="app-meta">
                <span class="app-name">${escapeHtml(app.name)}</span>
                <span class="app-package">${escapeHtml(app.packageName)}</span>
            </div>
        </div>
        <p class="app-summary">${escapeHtml(humanSummary(app, false))}</p>
        <ul class="static-evals">
            ${lines}
        </ul>
    </article>`;
}

function renderStaticIcon(app) {
    if (!app.iconUrl) {
        return '<div class="app-icon app-icon-placeholder">?</div>';
    }

    return `<img class="app-icon" src="${attr(app.iconUrl)}" alt="${attr(app.name)}">`;
}

function brokenSuffix(line) {
    if (line.broken.length === 0) {
        return '';
    }

    return ` (no ${escapeHtml(line.broken.join(', ').toLowerCase())})`;
}

// ─── Sitemap & robots ────────────────────────────────────────────────────────────

async function writeSitemap(apps) {
    const urls = [
        urlEntry(`${SITE_ORIGIN}/`, null),
        ...apps.map(app => urlEntry(`${SITE_ORIGIN}/app/${app.packageName}`, lastModified(app))),
    ];

    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls.join('\n')}
</urlset>
`;

    await writeFile(join(ROOT, 'sitemap.xml'), xml, 'utf8');
}

function urlEntry(loc, lastmod) {
    const lastmodTag = lastmod ? `<lastmod>${lastmod}</lastmod>` : '';
    return `  <url><loc>${loc}</loc>${lastmodTag}</url>`;
}

function lastModified(app) {
    const latest = app.entries
        .map(entry => entry.updatedAt)
        .filter(Boolean)
        .sort()
        .pop();

    return latest ? latest.slice(0, 10) : null;
}

async function writeRobots() {
    const robots = `User-agent: *
Allow: /

Sitemap: ${SITE_ORIGIN}/sitemap.xml
`;

    await writeFile(join(ROOT, 'robots.txt'), robots, 'utf8');
}

// ─── Helpers ─────────────────────────────────────────────────────────────────────

function jsonLd(app, url, image) {
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

    return JSON.stringify(data).replace(/</g, '\\u003c');
}

function attr(value) {
    return escapeHtml(value).replace(/"/g, '&quot;');
}

function clamp(text, max) {
    return text.length <= max ? text : `${text.slice(0, max - 1).trimEnd()}…`;
}
