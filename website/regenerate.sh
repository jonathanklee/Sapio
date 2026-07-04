#!/bin/sh
#
# Sapio website refresh — runs on the Pi via cron.
# Refreshes per-app pages, sitemap and stats from the API.
# Source file deployment is done separately via deploy.sh.

set -e

REPO="$HOME/Sapio"

python3 "$REPO/website/tools/refresh.py"

echo "$(date '+%Y-%m-%d %H:%M:%S') refresh OK"
