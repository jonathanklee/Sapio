import { BROKEN_FEATURE_LABELS, STANDARD_ROOTED, entryFor } from './core.js';
import { t } from './i18n.js';

const SITE_ORIGIN = 'https://checksap.io';
const CARD_SCALE = 3;
const CARD_FONT = 'Roboto, sans-serif';
const CARD_FILENAME = 'sapio-compatibility.png';
const PARTIAL_RATING = 2;

const MICROG = 1;
const BARE_AOSP = 2;

const DOT_COLORS = { 1: '#4CAF50', 2: '#FFC107', 3: '#F44336' };
const RATING_SYMBOLS = { 1: '✓', 2: '~', 3: '✗' };

// Delegated: the button is pre-rendered in app.html and survives untouched
// whenever the card skips its repaint, so binding it directly would miss it.
function enableShareButton(container, currentApp) {
    container.addEventListener('click', event => {
        const button = event.target.closest('.share-btn');

        if (button) {
            openShareModal(currentApp(), button);
        }
    });
}

function renderShareButton(app) {
    if (standardEntries(app).length === 0) {
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

    const button = document.createElement('button');
    button.className = 'btn btn-primary share-btn';
    button.textContent = t('share');

    section.appendChild(ctaRow);
    section.appendChild(text);
    section.appendChild(button);

    return section;
}

function standardEntries(app) {
    return [MICROG, BARE_AOSP]
        .map(microg => ({ microg, entry: entryFor(app.entries, microg, STANDARD_ROOTED) }))
        .filter(({ entry }) => entry !== null);
}

function sectionLabel(microg) {
    return microg === MICROG ? 'microG' : 'bareAOSP';
}

async function openShareModal(app, button) {
    button.disabled = true;

    try {
        const canvas = await drawShareCard(app);
        showShareModal(canvas, buildShareText(app));
    } catch (error) {
        console.error('Share card error:', error);
    } finally {
        button.disabled = false;
    }
}

// ─── Modal ───────────────────────────────────────────────────────────────────

function showShareModal(canvas, text) {
    const overlay = document.createElement('div');
    overlay.className = 'share-overlay';
    overlay.addEventListener('click', event => {
        if (event.target === overlay) {
            overlay.remove();
        }
    });

    const modal = document.createElement('div');
    modal.className = 'share-modal';
    modal.appendChild(closeButton(overlay));
    modal.appendChild(cardPreview(canvas));

    const textarea = shareTextarea(text);
    modal.appendChild(textarea);

    const actions = modalActions(canvas, text);
    modal.appendChild(actions);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    appendNativeShare(actions, canvas, text);
    textarea.focus();
    textarea.select();
}

function closeButton(overlay) {
    const button = document.createElement('button');
    button.className = 'share-modal-close';
    button.textContent = '×';
    button.setAttribute('aria-label', 'Close');
    button.addEventListener('click', () => overlay.remove());

    return button;
}

function cardPreview(canvas) {
    const img = document.createElement('img');
    img.src = canvas.toDataURL('image/png');
    img.className = 'share-card-preview';
    img.alt = 'Compatibility card';

    return img;
}

function shareTextarea(text) {
    const textarea = document.createElement('textarea');
    textarea.className = 'share-text-preview';
    textarea.readOnly = true;
    textarea.value = text;
    textarea.rows = 4;

    return textarea;
}

function modalActions(canvas, text) {
    const actions = document.createElement('div');
    actions.className = 'share-modal-actions';

    const download = document.createElement('a');
    download.className = 'btn btn-outline share-modal-btn';
    download.textContent = t('share_download');
    download.href = canvas.toDataURL('image/png');
    download.download = CARD_FILENAME;

    actions.appendChild(download);
    actions.appendChild(copyButton(text));

    return actions;
}

function copyButton(text) {
    const button = document.createElement('button');
    button.className = 'btn btn-outline share-modal-btn';
    button.textContent = t('share_copy');
    button.addEventListener('click', async () => {
        await navigator.clipboard.writeText(text).catch(() => {});
        button.textContent = t('copied_to_clipboard');
        setTimeout(() => { button.textContent = t('share_copy'); }, 1500);
    });

    return button;
}

async function appendNativeShare(actions, canvas, text) {
    const blob = await canvasToBlob(canvas);
    const file = new File([blob], CARD_FILENAME, { type: 'image/png' });

    if (!navigator.canShare?.({ text, files: [file] })) {
        return;
    }

    const button = document.createElement('button');
    button.className = 'btn btn-primary share-modal-btn';
    button.textContent = `↑ ${t('share')}`;
    button.addEventListener('click', () => {
        navigator.share({ text, files: [file] }).catch(() => {});
    });

    actions.appendChild(button);
}

function canvasToBlob(canvas) {
    return new Promise(resolve => canvas.toBlob(resolve, 'image/png'));
}

// ─── Share text ──────────────────────────────────────────────────────────────

function buildShareText(app) {
    const parts = standardEntries(app).map(({ microg, entry }) =>
        `${sectionLabel(microg)} ${RATING_SYMBOLS[entry.rating] ?? '?'}${brokenSuffix(entry)}`
    );

    const header = `Android compatibility for ${app.name}`;
    const ratings = parts.length > 0 ? `: ${parts.join(', ')}` : '';
    const url = `${SITE_ORIGIN}/app/${encodeURIComponent(app.packageName)}`;

    return `${header}${ratings}\n\n${url} #degoogle #privacy #android #sapio`;
}

function brokenSuffix(entry) {
    if (entry.rating !== PARTIAL_RATING) {
        return '';
    }

    const labels = (entry.brokenFeatures ?? [])
        .filter(key => BROKEN_FEATURE_LABELS[key])
        .map(key => `no ${BROKEN_FEATURE_LABELS[key].toLowerCase()}`);

    return labels.length === 0 ? '' : ` (${labels.join(', ')})`;
}

// ─── Card canvas ─────────────────────────────────────────────────────────────

async function drawShareCard(app) {
    const width = 200 * CARD_SCALE;
    const height = 115 * CARD_SCALE;

    await loadCardFont();

    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;

    const ctx = canvas.getContext('2d');
    const [sapioIcon, appIcon] = await Promise.all([
        loadCardImage('/icon.png', false),
        app.iconUrl ? loadCardImage(app.iconUrl, true).catch(() => null) : null,
    ]);

    const padX = 12 * CARD_SCALE;
    const padTop = 8 * CARD_SCALE;
    const padBottom = 4 * CARD_SCALE;
    const contentWidth = width - padX * 2;

    ctx.fillStyle = '#212121';
    ctx.fillRect(0, 0, width, height);

    const contentY = paintCardHeader(ctx, padTop, width, padX, contentWidth, sapioIcon);
    paintCardContent(ctx, contentY, width, padX, contentWidth, app, appIcon);
    paintCardFooter(ctx, width, height, padX, contentWidth, padBottom);

    return canvas;
}

// ctx.font does not trigger webfont fetching, so Roboto is requested up front.
async function loadCardFont() {
    await document.fonts.load(`400 ${9 * CARD_SCALE}px Roboto`).catch(() => {});
}

function loadCardImage(src, withCors) {
    return new Promise((resolve, reject) => {
        const img = new Image();

        if (withCors) {
            img.crossOrigin = 'anonymous';
        }

        img.onload = () => resolve(img);
        img.onerror = reject;
        img.src = src;
    });
}

function paintCardHeader(ctx, y, width, padX, contentWidth, sapioIcon) {
    const iconBox = 18 * CARD_SCALE;
    const titleSize = Math.round(8.5 * CARD_SCALE);

    if (sapioIcon) {
        paintSapioIcon(ctx, sapioIcon, padX + contentWidth - iconBox, y, iconBox);
    }

    ctx.textBaseline = 'top';
    ctx.textAlign = 'center';

    ctx.fillStyle = 'rgba(255,255,255,1)';
    ctx.font = `400 ${titleSize}px ${CARD_FONT}`;
    ctx.fillText(t('card_title'), width / 2, y + 2);

    ctx.fillStyle = 'rgba(255,255,255,0.7)';
    ctx.font = `${Math.round(5 * CARD_SCALE)}px ${CARD_FONT}`;
    ctx.fillText(t('card_subtitle'), width / 2, y + titleSize + 6);

    return y + iconBox + 8 * CARD_SCALE;
}

// The Android card draws the adaptive-icon foreground, whose artwork covers
// 58.35% of its box. icon.png is full-bleed, so it is inset to match.
const SAPIO_ARTWORK_RATIO = 0.5835;

function paintSapioIcon(ctx, img, boxX, boxY, boxSize) {
    const artwork = boxSize * SAPIO_ARTWORK_RATIO;
    const inset = (boxSize - artwork) / 2;

    ctx.drawImage(img, boxX + inset, boxY + inset, artwork, artwork);
}

function paintCardContent(ctx, y, width, padX, contentWidth, app, appIcon) {
    const pills = standardEntries(app).map(({ microg, entry }) => ({
        label: sectionLabel(microg),
        rating: entry.rating,
    }));

    const circleSize = 44 * CARD_SCALE;
    const nameHeight = 9 * CARD_SCALE;
    const packageHeight = 5 * CARD_SCALE;
    const pillHeight = 14 * CARD_SCALE;
    const pillGap = 3 * CARD_SCALE;
    const spacer = 6 * CARD_SCALE;

    const pillArea = pills.length > 0 ? pillHeight * 2 + pillGap : 0;
    const columnHeight = nameHeight + packageHeight + (pills.length > 0 ? spacer + pillArea : 0);
    const rowHeight = Math.max(circleSize, columnHeight);

    paintCircleIcon(ctx, padX, y + (rowHeight - circleSize) / 2, circleSize, appIcon);

    const columnWidth = contentWidth - 48 * CARD_SCALE * 2;
    const centerX = width / 2;
    let cursorY = y + (rowHeight - columnHeight) / 2;

    ctx.textBaseline = 'top';
    ctx.textAlign = 'center';

    ctx.fillStyle = 'rgba(255,255,255,0.9)';
    ctx.font = `400 ${nameHeight}px ${CARD_FONT}`;
    ctx.fillText(truncateText(ctx, app.name, columnWidth), centerX, cursorY);
    cursorY += nameHeight;

    ctx.fillStyle = 'rgba(255,255,255,0.65)';
    ctx.font = `${packageHeight}px ${CARD_FONT}`;
    ctx.fillText(truncateText(ctx, app.packageName, columnWidth), centerX, cursorY);
    cursorY += packageHeight;

    if (pills.length === 0) {
        return;
    }

    cursorY += spacer;

    if (pills.length === 1) {
        paintRatingPill(ctx, pills[0], centerX, cursorY + (pillArea - pillHeight) / 2, pillHeight, columnWidth);
        return;
    }

    for (const pill of pills) {
        paintRatingPill(ctx, pill, centerX, cursorY, pillHeight, columnWidth);
        cursorY += pillHeight + pillGap;
    }
}

function paintCircleIcon(ctx, padX, circleY, circleSize, appIcon) {
    const centerX = padX + circleSize / 2;
    const centerY = circleY + circleSize / 2;

    ctx.fillStyle = 'rgba(144,202,249,0.18)';
    ctx.beginPath();
    ctx.arc(centerX, centerY, circleSize / 2, 0, Math.PI * 2);
    ctx.fill();

    if (!appIcon) {
        return;
    }

    const iconSize = 36 * CARD_SCALE;
    const offset = (circleSize - iconSize) / 2;

    ctx.save();
    ctx.beginPath();
    ctx.arc(centerX, centerY, iconSize / 2, 0, Math.PI * 2);
    ctx.clip();
    ctx.drawImage(appIcon, padX + offset, circleY + offset, iconSize, iconSize);
    ctx.restore();
}

function paintRatingPill(ctx, pill, centerX, y, pillHeight, columnWidth) {
    const padding = 7 * CARD_SCALE;
    const labelWidth = 40 * CARD_SCALE;
    const spacing = 4 * CARD_SCALE;
    const dotSize = 7 * CARD_SCALE;
    const pillWidth = Math.min(padding * 2 + labelWidth + spacing + dotSize, columnWidth);
    const pillX = centerX - pillWidth / 2;

    ctx.fillStyle = 'rgba(255,255,255,0.08)';
    ctx.beginPath();
    ctx.roundRect(pillX, y, pillWidth, pillHeight, Math.min(10 * CARD_SCALE, pillHeight / 2));
    ctx.fill();

    const dotRadius = dotSize / 2;
    const dotY = y + pillHeight / 2;

    ctx.fillStyle = DOT_COLORS[pill.rating] ?? DOT_COLORS[3];
    ctx.beginPath();
    ctx.arc(pillX + pillWidth - padding - dotRadius, dotY, dotRadius, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = 'rgba(255,255,255,0.9)';
    ctx.font = `${6 * CARD_SCALE}px ${CARD_FONT}`;
    ctx.textAlign = 'left';
    ctx.textBaseline = 'middle';
    ctx.fillText(pill.label, pillX + padding, dotY);
    ctx.textBaseline = 'top';
}

function paintCardFooter(ctx, width, height, padX, contentWidth, padBottom) {
    const labelHeight = Math.round(5.5 * CARD_SCALE);
    const gap = 2 * CARD_SCALE;
    const lineHeight = Math.round(5 * CARD_SCALE);
    const footerY = height - padBottom - labelHeight - gap - lineHeight;

    ctx.fillStyle = 'rgba(255,255,255,1)';
    ctx.font = `400 ${labelHeight}px ${CARD_FONT}`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    ctx.fillText('Sapio', width / 2, footerY);

    const bottomY = footerY + labelHeight + gap;

    ctx.fillStyle = 'rgba(255,255,255,0.8)';
    ctx.font = `${Math.round(4 * CARD_SCALE)}px ${CARD_FONT}`;
    ctx.fillText(t('hero_eyebrow'), width / 2, bottomY);

    ctx.font = `${Math.round(5 * CARD_SCALE)}px ${CARD_FONT}`;
    ctx.textAlign = 'right';
    ctx.fillText(today(), padX + contentWidth, bottomY);
}

function today() {
    const now = new Date();
    const day = String(now.getDate()).padStart(2, '0');
    const month = String(now.getMonth() + 1).padStart(2, '0');

    return `${day}/${month}/${now.getFullYear()}`;
}

function truncateText(ctx, text, maxWidth) {
    if (ctx.measureText(text).width <= maxWidth) {
        return text;
    }

    let truncated = text;

    while (truncated.length > 0 && ctx.measureText(`${truncated}…`).width > maxWidth) {
        truncated = truncated.slice(0, -1);
    }

    return `${truncated}…`;
}

export { enableShareButton, renderShareButton };
