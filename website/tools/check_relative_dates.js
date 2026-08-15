// Proves RELATIVE_PATTERNS in refresh.py matches Intl.RelativeTimeFormat, so a
// pre-rendered date is byte-identical to the one relativeDate() would compute
// and app-page.js never has to rewrite it.
//
//   node tools/check_relative_dates.js
//
// Run it after touching either side. Exits non-zero on the first mismatch.

import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const HERE = dirname(fileURLToPath(import.meta.url));
const LANGS = ['en', 'fr', 'de', 'it', 'es'];
const UNIT_MS = {
    year: 31536000000,
    month: 2592000000,
    day: 86400000,
    hour: 3600000,
    minute: 60000,
};

// Straight off relativeDate() in i18n.js, so the reference includes its unit
// choice and truncation, not just the formatting.
function expected(diffMs, lang) {
    const rtf = new Intl.RelativeTimeFormat(lang, { numeric: 'always', style: 'short' });

    for (const [unit, ms] of Object.entries(UNIT_MS)) {
        if (Math.abs(diffMs / ms) >= 1 || unit === 'minute') {
            return rtf.format(Math.trunc(diffMs / ms), unit);
        }
    }

    return rtf.format(0, 'minute');
}

const ages = [];
for (const ms of Object.values(UNIT_MS)) {
    for (const n of [0, 1, 2, 3, 5, 11, 21, 30, 99]) {
        ages.push(n * ms, n * ms + Math.floor(ms / 2));
    }
}
ages.push(1, 59_000, 61_000, 86_399_000);

// Strictly in the past. The generator only ever formats a timestamp older than
// itself, and 0 is the one value where ICU flips to future wording ("in 0 min."
// rather than "0 min. ago"), which no real evaluation can reach.
const AGES = [...new Set(ages)].filter(age => age > 0);

const NOW = 1786752000000;
const cases = [];
for (const lang of LANGS) {
    for (const age of AGES) {
        cases.push({ lang, iso: new Date(NOW - age).toISOString() });
    }
}

const actual = JSON.parse(execFileSync('python3', [join(HERE, 'relative_date_probe.py')], {
    input: JSON.stringify({ now_ms: NOW, cases }),
    encoding: 'utf-8',
}));

let failures = 0;
cases.forEach((c, i) => {
    const want = expected(new Date(c.iso).getTime() - NOW, c.lang);
    if (actual[i] !== want) {
        failures++;
        if (failures <= 20) {
            console.error(`${c.lang} ${c.iso}: python ${JSON.stringify(actual[i])} != icu ${JSON.stringify(want)}`);
        }
    }
});

if (failures > 0) {
    console.error(`\n${failures} of ${cases.length} mismatched`);
    process.exit(1);
}

console.log(`${cases.length} cases across ${LANGS.length} languages match Intl.RelativeTimeFormat`);
