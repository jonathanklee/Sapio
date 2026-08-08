#!/usr/bin/env python3
"""Read the translation table out of i18n.js.

i18n.js stays the single source of truth: the browser reads it directly, and
the static generator parses it here rather than keeping a second copy that
would quietly drift out of sync.
"""

import re
from pathlib import Path

LANGS = ["en", "fr", "de", "it", "es"]

_ENTRY = re.compile(r"^\s*([a-z_0-9]+)\s*:\s*\{(.+?)\},?\s*$", re.M)
_PAIR = re.compile(
    r"\b(en|fr|de|it|es)\s*:\s*"
    r"""(?:'((?:[^'\\]|\\.)*)'|"((?:[^"\\]|\\.)*)")"""
)


def _unescape(value):
    return (value
            .replace("\\'", "'")
            .replace('\\"', '"')
            .replace("\\\\", "\\"))


def load_translations(i18n_path):
    source = Path(i18n_path).read_text(encoding="utf-8")

    start = source.index("const TRANSLATIONS")
    end = source.index("\n};", start)
    table = source[start:end]

    translations = {}
    for entry in _ENTRY.finditer(table):
        key, body = entry.group(1), entry.group(2)
        langs = {
            lang: _unescape(single or double)
            for lang, single, double in _PAIR.findall(body)
        }
        if langs:
            translations[key] = langs

    if not translations:
        raise SystemExit("i18n.js: no translations parsed - has the format changed?")

    return translations


def translator(translations, lang):
    """Return t(key), falling back to English then to the key itself."""
    def t(key):
        entry = translations.get(key)
        if not entry:
            return key

        return entry.get(lang) or entry.get("en") or key

    return t


if __name__ == "__main__":
    here = Path(__file__).resolve().parent.parent
    tr = load_translations(here / "i18n.js")
    print(f"{len(tr)} keys parsed")

    missing = {
        lang: [k for k, v in tr.items() if lang not in v]
        for lang in LANGS
    }
    for lang, keys in missing.items():
        state = "complete" if not keys else f"{len(keys)} missing: {', '.join(keys[:5])}"
        print(f"  {lang}: {state}")
