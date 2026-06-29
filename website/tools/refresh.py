#!/usr/bin/env python3
"""
Sapio static generator — runs on the server, no Node required.

Fetches all evaluations from the API and writes to WEB_DIR:
  - app/<packageName>/index.html  (per-app SEO pages)
  - sitemap.xml
  - stats.json
  - robots.txt
  - website source files (copied from the git repo)
"""

import json
import os
import re
import shutil
import urllib.request
from pathlib import Path

API_BASE = "https://server.sapio.ovh/api"
SITE_ORIGIN = "https://sapio.ovh"
WEBSITE_DIR = Path(__file__).resolve().parent.parent
WEB_DIR = Path("/var/www/sapio-website")
PAGE_SIZE = 100

SOURCE_FILES = [
    "index.html", "app.html", "app.js", "app-page.js",
    "core.js", "i18n.js", "style.css",
    "favicon.ico", "favicon-16x16.png", "favicon-32x32.png",
    "apple-touch-icon.png", "icon.png", "og-image.png",
]

RATING_LABEL = {1: "Perfect", 2: "Partial", 3: "Unusable"}
BROKEN_FEATURE_LABELS = {
    "notifications":     "Notifications",
    "in_app_purchase":   "In-app purchases",
    "login":             "Login",
    "maps":              "Maps",
    "location":          "Location",
    "payments":          "Physical payments",
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
    template = (WEBSITE_DIR / "app.html").read_text(encoding="utf-8")
    reset_app_dir()
    for app in pages:
        write_app_page(app, template)

    write_sitemap(pages)
    write_robots()
    write_stats(len(all_apps), len(evaluations))
    deploy_source_files()

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


def deploy_source_files():
    for filename in SOURCE_FILES:
        src = WEBSITE_DIR / filename
        if src.exists():
            shutil.copy2(src, WEB_DIR / filename)


if __name__ == "__main__":
    main()
