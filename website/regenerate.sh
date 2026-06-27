#!/bin/sh
#
# Regenerate the static Sapio per-app pages + sitemap and sync them to the Pi.
# Runs on this machine (Node 20); the Pi only serves the files.

set -e
export PATH=/usr/local/bin:/usr/bin:/bin

cd "$HOME/sapio-website"

node tools/generate.mjs
rsync -az --delete app/ raspi:/var/www/sapio-website/app/
rsync -az sitemap.xml robots.txt stats.json raspi:/var/www/sapio-website/

echo "$(date '+%Y-%m-%d %H:%M:%S') regenerate + deploy OK"
