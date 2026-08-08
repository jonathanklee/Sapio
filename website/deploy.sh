#!/bin/sh
#
# Deploy website source files to the Pi web root.
# Run this locally after pushing new website code.

set -e

PI="raspi"
WEB_DIR="/var/www/sapio-website"
WEBSITE_DIR="$(dirname "$0")"

SOURCE_FILES="
    index.html
    app.html
    app.js
    app-page.js
    core.js
    i18n.js
    style.css
    fonts.css
    favicon.ico
    favicon-16x16.png
    favicon-32x32.png
    apple-touch-icon.png
    icon.png
    og-image.png
"

for f in $SOURCE_FILES; do
    src="$WEBSITE_DIR/$f"
    if [ -f "$src" ]; then
        scp -q "$src" "$PI:$WEB_DIR/$f"
    fi

done

# Self-hosted webfonts - a directory, so it needs its own copy step.
ssh "$PI" "mkdir -p $WEB_DIR/fonts"
scp -q "$WEBSITE_DIR"/fonts/*.woff2 "$PI:$WEB_DIR/fonts/"

scp -q "$WEBSITE_DIR/tools/refresh.py" "$PI:~/Sapio/website/tools/refresh.py"
scp -q "$WEBSITE_DIR/tools/i18n_extract.py" "$PI:~/Sapio/website/tools/i18n_extract.py"

echo "Deploy OK"
