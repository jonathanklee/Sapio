#!/usr/bin/env python3
"""Feeds relative_date() to check_relative_dates.js. Reads JSON on stdin."""

import json
import sys

from refresh import relative_date

payload = json.load(sys.stdin)
print(json.dumps([
    relative_date(case["iso"], case["lang"], payload["now_ms"])
    for case in payload["cases"]
]))
