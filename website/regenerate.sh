#!/bin/sh
#
# Sapio website refresh — runs on the Pi via cron.
# Pulls latest source from git, regenerates per-app pages,
# sitemap, stats, and deploys everything to the web root.

set -e

REPO="$HOME/Sapio"

git -C "$REPO" pull -q
python3 "$REPO/website/tools/refresh.py"

echo "$(date '+%Y-%m-%d %H:%M:%S') refresh OK"
