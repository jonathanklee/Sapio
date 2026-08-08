#!/usr/bin/env python3
"""
Sapio static generator — runs on the server, no Node required.

Fetches all evaluations from the API and writes to WEB_DIR:
  - app/<packageName>/index.html  (per-app SEO pages)
  - sitemap.xml
  - stats.json
  - robots.txt
"""

import json
import re
import shutil
import urllib.request
from pathlib import Path

from i18n_extract import LANGS, load_translations, translator

API_BASE = "https://server.checksap.io/api"
SITE_ORIGIN = "https://checksap.io"
MEDIA_BASE = "https://server.checksap.io"
WEBSITE_DIR = Path(__file__).resolve().parent.parent
WEB_DIR = Path("/var/www/sapio-website")
DEFAULT_LANG = "en"
PAGE_SIZE = 100

RATING_LABEL = {1: "Perfect", 2: "Partial", 3: "Unusable"}
BROKEN_FEATURE_LABELS = {
    "notifications":     "Notifications",
    "in_app_purchase":   "In-app purchases",
    "login":             "Login",
    "maps":              "Maps",
    "location":          "Location",
    "payments":          "Contactless payment",
    "cast":              "Screen casting",
    "augmented_reality": "Augmented reality",
}
SECTIONS = [
    {"microg": 1, "label": "microG"},
    {"microg": 2, "label": "bareAOSP"},
]


def main():
    print("Fetching evaluations…")
    evaluations = fetch_all_evaluations()
    all_apps = group_by_package(evaluations)

    pages = [
        app for app in all_apps
        if has_secure_evaluation(app) and has_safe_package_name(app)
    ]

    translations = load_translations(WEB_DIR / "i18n.js")

    print(f"Generating {len(pages)} app pages in {len(LANGS)} languages…")
    template = (WEB_DIR / "app.html").read_text(encoding="utf-8")
    reset_app_dir()
    for app in pages:
        write_app_page(app, template, translations)

    write_home_pages(translations, len(all_apps), len(evaluations))

    write_sitemap(pages)
    write_robots()
    write_stats(len(all_apps), len(evaluations))

    print(f"Done — {len(pages)} app pages, {len(all_apps)} apps, {len(evaluations)} evaluations.")


def fetch_all_evaluations():
    evaluations = []
    page = 1

    while True:
        url = (
            f"{API_BASE}/sapio-applications"
            f"?sort=updatedAt%3ADesc"
            f"&populate%5Bicon%5D=*"
            f"&pagination%5Bpage%5D={page}"
            f"&pagination%5BpageSize%5D={PAGE_SIZE}"
        )
        with urllib.request.urlopen(url, timeout=30) as response:
            data = json.loads(response.read())

        batch = [item["attributes"] for item in data["data"]]
        evaluations.extend(batch)

        if len(batch) < PAGE_SIZE:
            break

        page += 1

    return evaluations


def group_by_package(evaluations):
    buckets = {}

    for ev in evaluations:
        pkg = ev["packageName"]
        if pkg not in buckets:
            buckets[pkg] = {"name": ev["name"], "packageName": pkg,
                            "iconUrl": None, "by_env": {}}

        if not buckets[pkg]["iconUrl"]:
            icon = ((ev.get("icon") or {}).get("data") or {})
            path = (icon.get("attributes") or {}).get("url")
            if path:
                buckets[pkg]["iconUrl"] = MEDIA_BASE + path

        env_key = f"{ev['microg']}-{ev['rooted']}"
        candidate = {
            "microg":        ev["microg"],
            "rooted":        ev["rooted"],
            "rating":        ev["rating"],
            "updatedAt":     ev.get("updatedAt", ""),
            "versionName":   ev.get("versionName"),
            "brokenFeatures": ev.get("brokenFeatures") or [],
        }
        existing = buckets[pkg]["by_env"].get(env_key)
        if not existing or candidate["updatedAt"] > existing["updatedAt"]:
            buckets[pkg]["by_env"][env_key] = candidate

    return [
        {
            "name":        b["name"],
            "packageName": b["packageName"],
            "iconUrl":     b["iconUrl"],
            "entries":     list(b["by_env"].values()),
        }
        for b in buckets.values()
    ]


def has_secure_evaluation(app):
    return any(e["rooted"] == 3 for e in app["entries"])


def has_safe_package_name(app):
    return bool(re.fullmatch(r"[A-Za-z0-9_.]+", app["packageName"]))


def last_modified(app):
    dates = [e["updatedAt"] for e in app["entries"] if e.get("updatedAt")]
    return sorted(dates)[-1][:10] if dates else None


def entry_for(entries, microg, rooted):
    return next(
        (e for e in entries if e["microg"] == microg and e["rooted"] == rooted),
        None,
    )


def broken_labels(entry):
    if entry["rating"] != 2 or not entry.get("brokenFeatures"):
        return []

    return [
        BROKEN_FEATURE_LABELS[k]
        for k in entry["brokenFeatures"]
        if k in BROKEN_FEATURE_LABELS
    ]


def human_summary(app):
    parts = []

    for section in SECTIONS:
        entry = entry_for(app["entries"], section["microg"], 3)
        if not entry:
            continue

        rating = RATING_LABEL.get(entry["rating"], "—")
        broken = broken_labels(entry)
        broken_suffix = f" (no {', '.join(b.lower() for b in broken)})" if broken else ""
        parts.append(f"{section['label']}: {rating}{broken_suffix}")

    if not parts:
        return f"No evaluation yet for {app['name']} without Google Play Services."

    return f"{app['name']} without Google Play Services — {' · '.join(parts)}."


def escape_html(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def attr(text):
    return escape_html(text).replace('"', "&quot;")



# ─── Localisation ──────────────────────────────────────────────────────────────

def lang_prefix(lang):
    """English lives at the root; the others get a /xx prefix."""
    return "" if lang == DEFAULT_LANG else f"/{lang}"


def app_url(pkg, lang):
    return f"{SITE_ORIGIN}{lang_prefix(lang)}/app/{pkg}"


def home_url(lang):
    return f"{SITE_ORIGIN}{lang_prefix(lang)}/"


def hreflang_block(url_for):
    """The reciprocal cluster Google needs, plus x-default on English."""
    links = [
        f'    <link rel="alternate" hreflang="{lang}" href="{attr(url_for(lang))}">'
        for lang in LANGS
    ]
    links.append(
        f'    <link rel="alternate" hreflang="x-default" href="{attr(url_for(DEFAULT_LANG))}">'
    )
    return "\n".join(links)


def localize_internal_links(page, lang):
    """Keep in-site links inside the language tree.

    Without this, clicking "All evaluations" from /fr/app/x lands on the
    English home page - the URL is authoritative, so the visitor would
    silently switch language mid-visit.
    """
    if lang == DEFAULT_LANG:
        return page

    prefix = lang_prefix(lang)
    for target in ('href="/index.html#about"', 'href="/index.html"',
                   'href="/#about"', 'href="/"'):
        path = target[len('href="'):-1]
        page = page.replace(target, f'href="{prefix}{path}"')

    return page


def apply_static_translations(page, t):
    """Server-side equivalent of applyStaticTranslations() in i18n.js."""
    page = re.sub(
        r'(<([a-z0-9]+)[^>]*\bdata-i18n="([a-z_0-9]+)"[^>]*>)(.*?)(</\2>)',
        lambda m: m.group(1) + escape_html(t(m.group(3))) + m.group(5),
        page,
        flags=re.S,
    )
    return re.sub(
        r'(data-i18n-placeholder="([a-z_0-9]+)"[^>]*?placeholder=")[^"]*(")',
        lambda m: m.group(1) + attr(t(m.group(2))) + m.group(3),
        page,
    )


def localized_summary(app, t):
    parts = []

    for section in SECTIONS:
        entry = entry_for(app["entries"], section["microg"], 3)
        if not entry:
            continue

        rating = t(f"rating_{entry['rating']}")
        broken = [t(f"feat_{k}") for k in entry["brokenFeatures"]
                  if k in BROKEN_FEATURE_LABELS]
        suffix = ""
        if broken:
            suffix = f" ({t('summary_no_prefix')} {', '.join(b.lower() for b in broken)})"
        parts.append(f"{section['label']}: {rating}{suffix}")

    if not parts:
        return t("summary_none").replace("%name", app["name"])

    return t("summary_frame").replace("%name", app["name"]).replace("%parts", " · ".join(parts))


# ─── Server-rendered card ──────────────────────────────────────────────────────
#
# app.html ships an empty shell that app-page.js fills from the API. That is
# fine for browsers, but a crawler that does not run JS sees nothing. So the
# same card is rendered here, with the class names the stylesheet already
# expects. app-page.js replaces it wholesale once it runs.

RATING_CLASS = {1: "good", 2: "average", 3: "bad"}
ENV_LABELS = [(3, "standard", "env_standard"), (4, "permissive", "env_permissive")]
STANDARD_ROOTED = 3


def render_legend(t):
    items = "".join(
        f'<span class="legend-item"><span class="status-dot {cls}"></span>'
        f"<span>{escape_html(t(key))}</span></span>"
        for cls, key in (("good", "legend_works"), ("average", "legend_partial"),
                         ("bad", "legend_broken"))
    )
    # The toggle is part of the legend client-side; omitting it here makes it
    # pop in once app-page.js runs. Unchecked, matching the default.
    toggle = (
        '<label class="permissive-label">'
        '<input type="checkbox" id="permissive-toggle">'
        f'<span>{escape_html(t("show_permissive"))}</span>'
        "</label>"
    )
    return f'<div class="rating-legend">{items}{toggle}</div>'


def render_cell(env_label, env_key, entry, t):
    cls = RATING_CLASS.get(entry["rating"], "unknown")
    label = t(f"rating_{entry['rating']}")

    version = ""
    if entry.get("versionName"):
        version = f'<span class="rating-date">v{escape_html(entry["versionName"])}</span>'

    # The client renders a relative date it computes at runtime. Emitting the
    # same number of lines here keeps the row height identical, otherwise the
    # text column grows on hydration and the centred dot visibly drops.
    # Absolute rather than relative: a generated "5 hours ago" would go stale.
    date = ""
    iso = (entry.get("updatedAt") or "")[:10]
    parts = iso.split("-")
    if len(parts) == 3:
        date = (f'<time class="rating-date" datetime="{iso}">'
                f'{parts[2]}/{parts[1]}/{parts[0]}</time>')

    broken = ""
    if entry["rating"] == 2 and entry["brokenFeatures"]:
        chips = "".join(
            f'<span class="broken-chip">{escape_html(t("feat_" + k))}</span>'
            for k in entry["brokenFeatures"] if k in BROKEN_FEATURE_LABELS
        )
        if chips:
            broken = (
                '<div class="broken-features">'
                f'<span class="broken-features-title">{escape_html(t("doesnt_work"))}</span>'
                f'<div class="broken-chips">{chips}</div></div>'
            )

    return (
        '<div class="eval-cell">'
        # Hidden by default, exactly as envBadge() does client-side: the
        # environment name only disambiguates once permissive cells are shown.
        f'<span class="cell-env-badge {env_label} env-badge--hidden">'
        f'{escape_html(t(env_key))}</span>'
        '<div class="rating-row">'
        f'<span class="status-dot {cls}"></span>'
        '<div class="rating-text-col">'
        f'<span class="rating-label {cls}">{label}</span>{version}{date}'
        "</div></div>"
        f"{broken}</div>"
    )


def render_sections(app, t):
    blocks = []

    for section in SECTIONS:
        # Standard only. "Show permissive environments" is a client-side
        # preference the generator cannot know, and it defaults to off, so
        # rendering permissive cells here makes them flash on screen until
        # app-page.js re-renders without them.
        cells = [
            render_cell(env_label, env_key, entry, t)
            for rooted, env_label, env_key in ENV_LABELS
            if rooted == STANDARD_ROOTED
            and (entry := entry_for(app["entries"], section["microg"], rooted))
        ]
        if not cells:
            continue

        row_cls = "cells-row cells-row--single" if len(cells) == 1 else "cells-row"
        blocks.append(
            '<div class="eval-section">'
            f'<span class="section-badge {"microg" if section["microg"] == 1 else "aosp"}">'
            f'{section["label"]}</span>'
            f'<div class="{row_cls}">{"".join(cells)}</div></div>'
        )

    return f'<div class="sections-row">{"".join(blocks)}</div>'



def render_app_icon(app):
    """Emit the real icon when known.

    The client fetches it from the API and only then sets the src, so the
    placeholder was visible until that round-trip finished. With the URL in
    the HTML the browser starts loading it during parse.
    """
    url = app.get("iconUrl")
    if not url:
        return '<div class="app-icon app-icon-placeholder">?</div>'

    return (f'<img class="app-icon" src="{attr(url)}" '
            f'alt="{attr(app["name"])}" width="50" height="50">')


def render_card(app, t):
    return (
        f"{render_legend(t)}"
        '<article class="app-card app-detail-card">'
        '<div class="card-header">'
        f'{render_app_icon(app)}'
        '<div class="app-meta">'
        f'<span class="app-name">{escape_html(app["name"])}</span>'
        f'<span class="app-package">{escape_html(app["packageName"])}</span>'
        "</div></div>"
        f'<p class="app-summary">{escape_html(localized_summary(app, t))}</p>'
        f"{render_sections(app, t)}"
        "</article>"
    )

def render_page(app, template, lang, t):
    pkg = app["packageName"]
    name = app["name"]
    url = app_url(pkg, lang)
    title = f"{name} {t('card_subtitle')} — Sapio"
    description = localized_summary(app, t)[:300]

    json_ld = json.dumps({
        "@context": "https://schema.org",
        "@type": "SoftwareApplication",
        "name": name,
        "identifier": pkg,
        "operatingSystem": "Android (without Google Play Services)",
        "applicationCategory": "MobileApplication",
        "url": url,
    }).replace("<", "\\u003c")

    page = template
    page = page.replace(
        "<title>App compatibility — Sapio</title>",
        f"<title>{escape_html(title)}</title>",
    )
    page = re.sub(
        r'(<meta name="description" content=")[^"]*(")',
        f"\\g<1>{attr(description)}\\g<2>",
        page,
    )
    page = re.sub(
        r'(<link rel="canonical" href=")[^"]*(")',
        f"\\g<1>{attr(url)}\\g<2>",
        page,
    )
    page = re.sub(
        r'(<meta property="og:title" content=")[^"]*(")',
        f"\\g<1>{attr(title)}\\g<2>",
        page,
    )
    page = re.sub(
        r'(<meta property="og:description" content=")[^"]*(")',
        f"\\g<1>{attr(description)}\\g<2>",
        page,
    )
    page = page.replace(
        "</head>",
        f'    <meta property="og:url" content="{attr(url)}">\n'
        f'    <script type="application/ld+json">{json_ld}</script>\n'
        "</head>",
    )
    page = page.replace('<html lang="en">', f'<html lang="{lang}">', 1)
    page = apply_static_translations(page, t)
    page = localize_internal_links(page, lang)
    page = page.replace(
        "</head>",
        hreflang_block(lambda l: app_url(pkg, l)) + "\n</head>",
        1,
    )
    page = page.replace("<body>", f'<body data-package="{attr(pkg)}" data-lang="{lang}">')

    # Fill the shell so crawlers without JS get the actual evaluation.
    page = re.sub(
        r'(<div id="app-detail"[^>]*>).*?(</div>\s*\n\s*<div id="app-error")',
        lambda m: m.group(1) + render_card(app, t) + "\n        " + m.group(2),
        page,
        flags=re.S,
    )
    page = page.replace('<div id="app-detail" class="app-detail" aria-busy="true">',
                        '<div id="app-detail" class="app-detail">')
    return page


def reset_app_dir():
    """Clear every language tree, so apps that disappear do not linger."""
    for lang in LANGS:
        base = WEB_DIR / lang if lang != DEFAULT_LANG else WEB_DIR
        shutil.rmtree(base / "app", ignore_errors=True)
        (base / "app").mkdir(parents=True, exist_ok=True)


def write_app_page(app, template, translations):
    for lang in LANGS:
        t = translator(translations, lang)
        prefix = WEB_DIR / lang if lang != DEFAULT_LANG else WEB_DIR
        out_dir = prefix / "app" / app["packageName"]
        out_dir.mkdir(parents=True, exist_ok=True)
        (out_dir / "index.html").write_text(
            render_page(app, template, lang, t), encoding="utf-8"
        )


# Digit grouping per language, matching what a reader expects locally.
GROUPING = {"en": ",", "fr": "\u202f", "de": ".", "it": ".", "es": "."}


def format_count(value, lang):
    return f"{value:,}".replace(",", GROUPING.get(lang, ","))


def inject_stats(page, lang, apps, evaluations):
    """Bake the counters into the HTML.

    They used to be fetched from stats.json and unhidden on arrival, which
    made the hero grow by 45px after paint. The generator already knows these
    numbers, so there is nothing to wait for. Written idempotently: the English
    index.html is both the template and the output.
    """
    page = page.replace('<div class="hero-stats" id="hero-stats" hidden>',
                        '<div class="hero-stats" id="hero-stats">')
    page = re.sub(r'(<strong id="stat-apps">)[^<]*(</strong>)',
                  lambda m: m.group(1) + format_count(apps, lang) + m.group(2), page)
    page = re.sub(r'(<strong id="stat-evaluations">)[^<]*(</strong>)',
                  lambda m: m.group(1) + format_count(evaluations, lang) + m.group(2), page)
    return page


def write_home_pages(translations, apps, evaluations):
    """English index.html is deployed as-is; the others are generated beside it."""
    template = (WEB_DIR / "index.html").read_text(encoding="utf-8")

    for lang in LANGS:
        t = translator(translations, lang)
        page = template.replace('<html lang="en">', f'<html lang="{lang}">', 1)
        page = apply_static_translations(page, t)
        page = localize_internal_links(page, lang)
        page = inject_stats(page, lang, apps, evaluations)
        page = re.sub(
            r'(<link rel="canonical" href=")[^"]*(")',
            f"\\g<1>{attr(home_url(lang))}\\g<2>",
            page,
        )
        page = page.replace("</head>", hreflang_block(home_url) + "\n</head>", 1)
        page = page.replace("<body>", f'<body data-lang="{lang}">', 1)

        # Assets are referenced relatively, so prefixed pages need absolute paths.
        if lang != DEFAULT_LANG:
            for asset in ("style.css", "fonts.css", "app.js", "icon.png", "favicon.ico"):
                page = page.replace(f'"{asset}"', f'"/{asset}"')
            out = WEB_DIR / lang / "index.html"
            out.parent.mkdir(parents=True, exist_ok=True)
        else:
            out = WEB_DIR / "index.html"

        out.write_text(page, encoding="utf-8")


def write_sitemap(pages):
    entries = [f"  <url><loc>{home_url(lang)}</loc></url>" for lang in LANGS]

    for app in pages:
        lastmod = last_modified(app)
        lastmod_tag = f"<lastmod>{lastmod}</lastmod>" if lastmod else ""
        for lang in LANGS:
            entries.append(
                f"  <url><loc>{app_url(app['packageName'], lang)}</loc>{lastmod_tag}</url>"
            )

    xml = (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        + "\n".join(entries)
        + "\n</urlset>\n"
    )
    (WEB_DIR / "sitemap.xml").write_text(xml, encoding="utf-8")


def write_robots():
    (WEB_DIR / "robots.txt").write_text(
        f"User-agent: *\nAllow: /\n\nSitemap: {SITE_ORIGIN}/sitemap.xml\n",
        encoding="utf-8",
    )


def write_stats(apps, evaluations):
    (WEB_DIR / "stats.json").write_text(
        json.dumps({"apps": apps, "evaluations": evaluations}),
        encoding="utf-8",
    )


if __name__ == "__main__":
    main()
