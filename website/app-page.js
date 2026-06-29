import {
    SECTIONS,
    ENVS,
    BROKEN_FEATURE_LABELS,
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
const shareBanner = document.getElementById('share-banner');

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

    const shareSection = renderShareButton(currentApp);
    shareBanner.innerHTML = '';
    if (shareSection) {
        shareBanner.appendChild(shareSection);
        shareBanner.hidden = false;
    } else {
        shareBanner.hidden = true;
    }
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

// ─── Share ───────────────────────────────────────────────────────────────────────

function renderShareButton(app) {
    const hasEntry = entryFor(app.entries, 1, 3) || entryFor(app.entries, 2, 3);
    if (!hasEntry) {
        return null;
    }

    const section = document.createElement('div');
    section.className = 'share-section';

    const ctaRow = document.createElement('div');
    ctaRow.className = 'share-cta-row';

    const icon = document.createElement('span');
    icon.className = 'share-cta-icon';
    icon.textContent = '📣';

    const title = document.createElement('span');
    title.className = 'share-cta-title';
    title.textContent = t('share_title');

    ctaRow.appendChild(icon);
    ctaRow.appendChild(title);

    const text = document.createElement('p');
    text.className = 'share-cta-text';
    text.textContent = t('share_cta');

    const btn = document.createElement('button');
    btn.className = 'btn btn-primary share-btn';
    btn.textContent = t('share');
    btn.addEventListener('click', () => handleShare(app, btn));

    section.appendChild(ctaRow);
    section.appendChild(text);
    section.appendChild(btn);
    return section;
}

async function handleShare(app, btn) {
    btn.disabled = true;

    try {
        const text = buildShareText(app);
        const canvas = await drawShareCard(app);
        showShareModal(canvas, text);
    } catch (err) {
        console.error('Share card error:', err);
    } finally {
        btn.disabled = false;
    }
}

function showShareModal(canvas, text) {
    const overlay = document.createElement('div');
    overlay.className = 'share-overlay';
    overlay.addEventListener('click', e => { if (e.target === overlay) { overlay.remove(); } });

    const modal = document.createElement('div');
    modal.className = 'share-modal';

    const closeBtn = document.createElement('button');
    closeBtn.className = 'share-modal-close';
    closeBtn.textContent = '×';
    closeBtn.setAttribute('aria-label', 'Close');
    closeBtn.addEventListener('click', () => overlay.remove());

    const img = document.createElement('img');
    img.src = canvas.toDataURL('image/png');
    img.className = 'share-card-preview';
    img.alt = 'Compatibility card';

    const textarea = document.createElement('textarea');
    textarea.className = 'share-text-preview';
    textarea.readOnly = true;
    textarea.value = text;
    textarea.rows = 4;

    const actions = document.createElement('div');
    actions.className = 'share-modal-actions';

    const downloadBtn = document.createElement('a');
    downloadBtn.className = 'btn btn-outline share-modal-btn';
    downloadBtn.textContent = t('share_download');
    downloadBtn.href = canvas.toDataURL('image/png');
    downloadBtn.download = 'sapio-compatibility.png';

    const copyBtn = document.createElement('button');
    copyBtn.className = 'btn btn-outline share-modal-btn';
    copyBtn.textContent = t('share_copy');
    copyBtn.addEventListener('click', async () => {
        await navigator.clipboard.writeText(text).catch(() => {});
        copyBtn.textContent = t('copied_to_clipboard');
        setTimeout(() => { copyBtn.textContent = t('share_copy'); }, 1500);
    });

    actions.appendChild(downloadBtn);
    actions.appendChild(copyBtn);

    canvasToBlob(canvas).then(blob => {
        const file = new File([blob], 'sapio-compatibility.png', { type: 'image/png' });
        if (navigator.canShare?.({ text, files: [file] })) {
            const nativeBtn = document.createElement('button');
            nativeBtn.className = 'btn btn-primary share-modal-btn';
            nativeBtn.textContent = '↑ ' + t('share');
            nativeBtn.addEventListener('click', () => {
                navigator.share({ text, files: [file] }).catch(() => {});
            });
            actions.appendChild(nativeBtn);
        }
    });

    modal.appendChild(closeBtn);
    modal.appendChild(img);
    modal.appendChild(textarea);
    modal.appendChild(actions);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    setTimeout(() => { textarea.focus(); textarea.select(); }, 50);
}

function buildShareText(app) {
    const microg = entryFor(app.entries, 1, 3);
    const bareAosp = entryFor(app.entries, 2, 3);

    const parts = [];
    if (microg) { parts.push(`microG ${ratingSymbol(microg.rating)}${brokenPartSuffix(microg)}`); }
    if (bareAosp) { parts.push(`bareAOSP ${ratingSymbol(bareAosp.rating)}${brokenPartSuffix(bareAosp)}`); }

    const header = `Android compatibility for ${app.name}`;
    const ratingSuffix = parts.length > 0 ? `: ${parts.join(', ')}` : '';

    return `${header}${ratingSuffix}\n\nhttps://sapio.ovh/app/${encodeURIComponent(app.packageName)} #degoogle #privacy #android #sapio`;
}

function ratingSymbol(rating) {
    if (rating === 1) { return '✓'; }
    if (rating === 2) { return '~'; }
    if (rating === 3) { return '✗'; }
    return '?';
}

function brokenPartSuffix(entry) {
    if (entry.rating !== 2 || !entry.brokenFeatures?.length) {
        return '';
    }

    const labels = entry.brokenFeatures
        .filter(key => BROKEN_FEATURE_LABELS[key])
        .map(key => `no ${BROKEN_FEATURE_LABELS[key].toLowerCase()}`);

    return labels.length > 0 ? ` (${labels.join(', ')})` : '';
}

function canvasToBlob(canvas) {
    return new Promise(resolve => canvas.toBlob(resolve, 'image/png'));
}

function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'share-toast';
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}

// ─── Share card canvas ────────────────────────────────────────────────────────

const CARD_SCALE = 3;

async function drawShareCard(app) {
    const W = 200 * CARD_SCALE;
    const H = 115 * CARD_SCALE;

    await document.fonts.ready;

    const canvas = document.createElement('canvas');
    canvas.width = W;
    canvas.height = H;
    const ctx = canvas.getContext('2d');

    const [sapioImg, appImg] = await Promise.all([
        loadCardImage('/icon.png', false),
        app.iconUrl ? loadCardImage(app.iconUrl, true).catch(() => null) : Promise.resolve(null),
    ]);

    const padX = 12 * CARD_SCALE;
    const padTop = 8 * CARD_SCALE;
    const padBot = 4 * CARD_SCALE;
    const cW = W - padX * 2;

    ctx.fillStyle = '#212121';
    ctx.fillRect(0, 0, W, H);

    let y = padTop;
    y = paintCardHeader(ctx, y, W, padX, cW, sapioImg);
    paintCardContent(ctx, y, W, padX, cW, app, appImg);
    paintCardFooter(ctx, W, H, padX, cW, padBot);

    return canvas;
}

function paintCardHeader(ctx, y, W, padX, cW, sapioImg) {
    const sapioSz = 18 * CARD_SCALE;

    if (sapioImg) {
        ctx.drawImage(sapioImg, padX + cW - sapioSz, y, sapioSz, sapioSz);
    }

    ctx.textBaseline = 'top';
    ctx.textAlign = 'center';

    ctx.fillStyle = 'rgba(255,255,255,1)';
    ctx.font = `500 ${Math.round(8.5 * CARD_SCALE)}px Roboto, sans-serif`;
    ctx.fillText(t('card_title'), W / 2, y + 2);

    ctx.fillStyle = 'rgba(255,255,255,0.7)';
    ctx.font = `${Math.round(5 * CARD_SCALE)}px Roboto, sans-serif`;
    ctx.fillText(t('card_subtitle'), W / 2, y + Math.round(8.5 * CARD_SCALE) + 6);

    return y + sapioSz + 8 * CARD_SCALE;
}

function paintCardContent(ctx, y, W, padX, cW, app, appImg) {
    const microg = entryFor(app.entries, 1, 3);
    const bareAosp = entryFor(app.entries, 2, 3);

    const pills = [];
    if (microg) { pills.push({ label: 'microG', rating: microg.rating }); }
    if (bareAosp) { pills.push({ label: 'bareAOSP', rating: bareAosp.rating }); }

    const circleSz = 44 * CARD_SCALE;
    const nameH = 9 * CARD_SCALE;
    const pkgH = 5 * CARD_SCALE;
    const pillH = 14 * CARD_SCALE;
    const pillGap = 3 * CARD_SCALE;
    const spacerH = 6 * CARD_SCALE;

    const pillAreaH = pills.length > 0 ? pillH * 2 + pillGap : 0;
    const colH = nameH + pkgH + (pills.length > 0 ? spacerH + pillAreaH : 0);
    const mainH = Math.max(circleSz, colH);

    const circleY = y + (mainH - circleSz) / 2;
    paintCircleIcon(ctx, padX, circleY, circleSz, appImg);

    const colPadX = 48 * CARD_SCALE;
    const colMidX = W / 2;
    const colW = cW - colPadX * 2;

    ctx.textBaseline = 'top';
    ctx.textAlign = 'center';

    let cy = y + (mainH - colH) / 2;

    ctx.fillStyle = 'rgba(255,255,255,0.9)';
    ctx.font = `500 ${nameH}px Roboto, sans-serif`;
    ctx.fillText(truncateText(ctx, app.name, colW), colMidX, cy);
    cy += nameH;

    ctx.fillStyle = 'rgba(255,255,255,0.65)';
    ctx.font = `${pkgH}px Roboto, sans-serif`;
    ctx.fillText(truncateText(ctx, app.packageName, colW), colMidX, cy);
    cy += pkgH;

    if (pills.length > 0) {
        cy += spacerH;
        if (pills.length === 1) {
            const pillY = cy + (pillAreaH - pillH) / 2;
            paintRatingPill(ctx, pills[0].label, pills[0].rating, colMidX, pillY, pillH, colW);
        } else {
            for (const pill of pills) {
                paintRatingPill(ctx, pill.label, pill.rating, colMidX, cy, pillH, colW);
                cy += pillH + pillGap;
            }
        }
    }
}

function paintCircleIcon(ctx, padX, circleY, circleSz, appImg) {
    const cx = padX + circleSz / 2;
    const cy = circleY + circleSz / 2;

    ctx.fillStyle = 'rgba(144,202,249,0.18)';
    ctx.beginPath();
    ctx.arc(cx, cy, circleSz / 2, 0, Math.PI * 2);
    ctx.fill();

    if (appImg) {
        const iconSz = 36 * CARD_SCALE;
        const offset = (circleSz - iconSz) / 2;

        ctx.save();
        ctx.beginPath();
        ctx.arc(cx, cy, iconSz / 2, 0, Math.PI * 2);
        ctx.clip();
        ctx.drawImage(appImg, padX + offset, circleY + offset, iconSz, iconSz);
        ctx.restore();
    }
}

function paintRatingPill(ctx, label, rating, centerX, y, pillH, colW) {
    const padH = 7 * CARD_SCALE;
    const labelW = 40 * CARD_SCALE;
    const spacing = 4 * CARD_SCALE;
    const dotSz = 7 * CARD_SCALE;
    const pillW = Math.min(padH + labelW + spacing + dotSz + padH, colW);
    const pillX = centerX - pillW / 2;

    ctx.fillStyle = 'rgba(255,255,255,0.08)';
    ctx.beginPath();
    ctx.roundRect(pillX, y, pillW, pillH, Math.min(10 * CARD_SCALE, pillH / 2));
    ctx.fill();

    const dotColor = rating === 1 ? '#4CAF50' : rating === 2 ? '#FFC107' : '#F44336';
    const dotR = dotSz / 2;
    const dotX = pillX + pillW - padH - dotR;
    const dotY = y + pillH / 2;

    ctx.fillStyle = dotColor;
    ctx.beginPath();
    ctx.arc(dotX, dotY, dotR, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = 'rgba(255,255,255,0.9)';
    ctx.font = `${6 * CARD_SCALE}px Roboto, sans-serif`;
    ctx.textAlign = 'left';
    ctx.textBaseline = 'middle';
    ctx.fillText(label, pillX + padH, dotY);
    ctx.textBaseline = 'top';
}

function paintCardFooter(ctx, W, H, padX, cW, padBot) {
    const sapioLabelH = Math.round(5.5 * CARD_SCALE);
    const botPad = 2 * CARD_SCALE;
    const botLineH = Math.round(5 * CARD_SCALE);
    const footerY = H - padBot - sapioLabelH - botPad - botLineH;

    ctx.fillStyle = 'rgba(255,255,255,1)';
    ctx.font = `500 ${sapioLabelH}px Roboto, sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ctx.fillText('Sapio', W / 2, footerY);

    const botY = footerY + sapioLabelH + botPad;

    ctx.fillStyle = 'rgba(255,255,255,0.8)';
    ctx.font = `${Math.round(4 * CARD_SCALE)}px Roboto, sans-serif`;
    ctx.textAlign = 'center';
    ctx.fillText(t('hero_eyebrow'), W / 2, botY);

    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();

    ctx.font = `${Math.round(5 * CARD_SCALE)}px Roboto, sans-serif`;
    ctx.textAlign = 'right';
    ctx.fillText(`${dd}/${mm}/${yyyy}`, padX + cW, botY);
}

function loadCardImage(src, withCors) {
    return new Promise((resolve, reject) => {
        const img = new Image();
        if (withCors) { img.crossOrigin = 'anonymous'; }
        img.onload = () => resolve(img);
        img.onerror = reject;
        img.src = src;
    });
}

function truncateText(ctx, text, maxWidth) {
    if (ctx.measureText(text).width <= maxWidth) { return text; }
    let out = text;
    while (out.length > 0 && ctx.measureText(`${out}…`).width > maxWidth) {
        out = out.slice(0, -1);
    }
    return `${out}…`;
}

// ─── States ──────────────────────────────────────────────────────────────────────

function showError() {
    detail.hidden = true;
    errorBox.hidden = false;
    document.title = `${t('app_not_found_title')} — Sapio`;
}
