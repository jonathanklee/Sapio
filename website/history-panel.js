import { SECTIONS, ENVS, RATING_CLASS, brokenFeatureKeys, entryFor } from './core.js?v=20261006';
import { t } from './i18n.js?v=20261006';

// Mirrors OperatingSystemLabel in the Android app: both must name a system the same way.
const OS_NAMES = {
    eos: '/e/OS',
    iodeos: 'iodéOS',
    calyxos: 'CalyxOS',
    grapheneos: 'GrapheneOS',
    lineageos: 'LineageOS',
};

const ANDROID_RELEASES = {
    26: '8.0', 27: '8.1', 28: '9', 29: '10', 30: '11', 31: '12',
    32: '12L', 33: '13', 34: '14', 35: '15', 36: '16', 37: '17',
};

const PHONE_ICON_PATH = 'M17,1.01L7,1c-1.1,0 -2,0.9 -2,2v18c0,1.1 0.9,2 2,2h10c1.1,0 2,-0.9 2,-2V3c0,-1.1 -0.9,-1.99 -2,-1.99zM17,19H7V5h10v14z';
const SVG_NS = 'http://www.w3.org/2000/svg';

function openHistoryPanel(app, microg, rooted) {
    const entry = entryFor(app.entries, microg, rooted);

    if (!entry) {
        return;
    }

    const dialog = historyDialog(app, entry);
    document.body.appendChild(dialog);
    dialog.showModal();
}

function historyDialog(app, entry) {
    const dialog = document.createElement('dialog');
    dialog.className = 'history-dialog';
    dialog.setAttribute('aria-labelledby', 'history-dialog-title');
    dialog.appendChild(dialogHeader());
    dialog.appendChild(dialogSubtitle(app, entry));
    dialog.appendChild(historyList(entry.history));

    dialog.addEventListener('click', event => {
        if (event.target === dialog) {
            dialog.close();
        }
    });

    dialog.addEventListener('close', () => dialog.remove());

    return dialog;
}

function dialogHeader() {
    const header = document.createElement('div');
    header.className = 'history-dialog-header';

    const title = document.createElement('h2');
    title.id = 'history-dialog-title';
    title.className = 'history-dialog-title';
    title.textContent = t('history_label');

    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'history-dialog-close';
    close.setAttribute('aria-label', t('history_close'));
    close.textContent = '×';
    close.addEventListener('click', () => close.closest('dialog').close());

    header.appendChild(title);
    header.appendChild(close);

    return header;
}

function dialogSubtitle(app, entry) {
    const section = SECTIONS.find(candidate => candidate.microg === entry.microg);
    const env = ENVS.find(candidate => candidate.rooted === entry.rooted);

    const subtitle = document.createElement('p');
    subtitle.className = 'history-dialog-subtitle';
    subtitle.textContent = [app.name, section?.label, env ? t(env.labelKey) : null]
        .filter(Boolean)
        .join(' · ');

    return subtitle;
}

function historyList(history) {
    const list = document.createElement('ol');
    list.className = 'history-list';

    for (const point of [...history].reverse()) {
        list.appendChild(historyItem(point));
    }

    return list;
}

function historyItem(point) {
    const cls = RATING_CLASS[point.rating] ?? 'unknown';

    const item = document.createElement('li');
    item.className = 'history-item';

    const dot = document.createElement('span');
    dot.className = `status-dot ${cls}`;

    const details = document.createElement('div');
    details.className = 'history-item-details';
    details.appendChild(ratingLabel(point, cls));

    if (point.versionName) {
        details.appendChild(detailLine(`v${point.versionName}`));
    }

    if (point.updatedAt) {
        details.appendChild(detailLine(point.updatedAt.slice(0, 10)));
    }

    const operatingSystem = operatingSystemLabel(point);

    if (operatingSystem) {
        details.appendChild(operatingSystemLine(operatingSystem));
    }

    const broken = brokenFeatureKeys(point);

    if (broken.length > 0) {
        details.appendChild(brokenChips(broken));
    }

    item.appendChild(dot);
    item.appendChild(details);

    return item;
}

function ratingLabel(point, cls) {
    const label = document.createElement('span');
    label.className = `rating-label ${cls}`;
    label.textContent = RATING_CLASS[point.rating] ? t(`rating_${point.rating}`) : '—';

    return label;
}

function detailLine(text) {
    const line = document.createElement('span');
    line.className = 'rating-date';
    line.textContent = text;

    return line;
}

function operatingSystemLabel(point) {
    if (!point.os || !point.androidVersion) {
        return null;
    }

    const release = ANDROID_RELEASES[point.androidVersion] ?? `API ${point.androidVersion}`;
    const android = `Android ${release}`;
    const name = OS_NAMES[point.os];

    return name ? `${name} · ${android}` : android;
}

function operatingSystemLine(text) {
    const line = document.createElement('span');
    line.className = 'history-os';
    line.appendChild(phoneIcon());
    line.appendChild(document.createTextNode(text));

    return line;
}

function phoneIcon() {
    const icon = document.createElementNS(SVG_NS, 'svg');
    icon.setAttribute('class', 'history-os-icon');
    icon.setAttribute('viewBox', '0 0 24 24');
    icon.setAttribute('aria-hidden', 'true');

    const path = document.createElementNS(SVG_NS, 'path');
    path.setAttribute('d', PHONE_ICON_PATH);
    icon.appendChild(path);

    return icon;
}

function brokenChips(featureKeys) {
    const chips = document.createElement('div');
    chips.className = 'broken-chips';

    for (const key of featureKeys) {
        const chip = document.createElement('span');
        chip.className = 'broken-chip';
        chip.textContent = t(`feat_${key}`);
        chips.appendChild(chip);
    }

    return chips;
}

export { openHistoryPanel };
