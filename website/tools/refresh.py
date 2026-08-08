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

API_BASE = "https://server.checksap.io/api"
SITE_ORIGIN = "https://checksap.io"
WEBSITE_DIR = Path(__file__).resolve().parent.parent
WEB_DIR = Path("/var/www/sapio-website")
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

    print(f"Generating {len(pages)} app pages…")
    template = (WEB_DIR / "app.html").read_text(encoding="utf-8")
    reset_app_dir()
    for app in pages:
        write_app_page(app, template)

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
            buckets[pkg] = {"name": ev["name"], "packageName": pkg, "by_env": {}}

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



# ─── Server-rendered card ──────────────────────────────────────────────────────
#
# app.html ships an empty shell that app-page.js fills from the API. That is
# fine for browsers, but a crawler that does not run JS sees nothing. So the
# same card is rendered here, with the class names the stylesheet already
# expects. app-page.js replaces it wholesale once it runs.

RATING_CLASS = {1: "good", 2: "average", 3: "bad"}
ENV_LABELS = [(3, "standard"), (4, "permissive")]


def render_legend():
    items = "".join(
        f'<span class="legend-item"><span class="status-dot {cls}"></span>'
        f"<span>{label}</span></span>"
        for cls, label in (("good", "Perfect"), ("average", "Partial"), ("bad", "Unusable"))
    )
    return f'<div class="rating-legend">{items}</div>'


def render_cell(env_label, entry):
    cls = RATING_CLASS.get(entry["rating"], "unknown")
    label = RATING_LABEL.get(entry["rating"], "—")

    version = ""
    if entry.get("versionName"):
        version = f'<span class="rating-date">v{escape_html(entry["versionName"])}</span>'

    broken = ""
    if entry["rating"] == 2 and entry["brokenFeatures"]:
        chips = "".join(
            f'<span class="broken-chip">{escape_html(lbl)}</span>'
            for lbl in broken_labels(entry)
        )
        if chips:
            broken = (
                '<div class="broken-features">'
                '<span class="broken-features-title">Doesn\'t work</span>'
                f'<div class="broken-chips">{chips}</div></div>'
            )

    return (
        '<div class="eval-cell">'
        f'<span class="cell-env-badge {env_label}">{env_label}</span>'
        '<div class="rating-row">'
        f'<span class="status-dot {cls}"></span>'
        '<div class="rating-text-col">'
        f'<span class="rating-label {cls}">{label}</span>{version}'
        "</div></div>"
        f"{broken}</div>"
    )


def render_sections(app):
    blocks = []

    for section in SECTIONS:
        cells = [
            render_cell(env_label, entry)
            for rooted, env_label in ENV_LABELS
            if (entry := entry_for(app["entries"], section["microg"], rooted))
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


def render_card(app):
    return (
        f"{render_legend()}"
        '<article class="app-card app-detail-card">'
        '<div class="card-header">'
        '<div class="app-icon app-icon-placeholder">?</div>'
        '<div class="app-meta">'
        f'<span class="app-name">{escape_html(app["name"])}</span>'
        f'<span class="app-package">{escape_html(app["packageName"])}</span>'
        "</div></div>"
        f'<p class="app-summary">{escape_html(human_summary(app))}</p>'
        f"{render_sections(app)}"
        "</article>"
    )

def render_page(app, template):
    pkg = app["packageName"]
    name = app["name"]
    url = f"{SITE_ORIGIN}/app/{pkg}"
    title = f"{name} without Google Play Services — Sapio"
    description = human_summary(app)[:300]

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
    page = page.replace("<body>", f'<body data-package="{attr(pkg)}">')

    # Fill the shell so crawlers without JS get the actual evaluation.
    page = re.sub(
        r'(<div id="app-detail"[^>]*>).*?(</div>\s*\n\s*<div id="app-error")',
        lambda m: m.group(1) + render_card(app) + "\n        " + m.group(2),
        page,
        flags=re.S,
    )
    page = page.replace('<div id="app-detail" class="app-detail" aria-busy="true">',
                        '<div id="app-detail" class="app-detail">')
    return page


def reset_app_dir():
    app_dir = WEB_DIR / "app"
    shutil.rmtree(app_dir, ignore_errors=True)
    app_dir.mkdir(parents=True, exist_ok=True)


def write_app_page(app, template):
    out_dir = WEB_DIR / "app" / app["packageName"]
    out_dir.mkdir(parents=True, exist_ok=True)
    (out_dir / "index.html").write_text(render_page(app, template), encoding="utf-8")


def write_sitemap(pages):
    entries = [f"  <url><loc>{SITE_ORIGIN}/</loc></url>"]

    for app in pages:
        lastmod = last_modified(app)
        lastmod_tag = f"<lastmod>{lastmod}</lastmod>" if lastmod else ""
        entries.append(
            f"  <url><loc>{SITE_ORIGIN}/app/{app['packageName']}</loc>{lastmod_tag}</url>"
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
