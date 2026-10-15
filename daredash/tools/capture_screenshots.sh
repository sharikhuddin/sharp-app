#!/usr/bin/env bash
# Real UI screenshots from the installed game, not mockups or generated images.
set -euo pipefail
mkdir -p play-store/screenshots
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell wm size 1080x2400
adb shell wm density 420
adb shell am force-stop com.daredash.party || true
adb shell am start -n com.daredash.party/.MainActivity
sleep 10
adb shell screencap -p /sdcard/daredash-home.png
adb pull /sdcard/daredash-home.png play-store/screenshots/01-home.png >/dev/null
find_and_tap() {
  local needle="$1" target_coords=""
  for i in $(seq 1 7); do
    adb shell uiautomator dump /sdcard/dd-ui.xml >/dev/null 2>&1 || true
    adb exec-out cat /sdcard/dd-ui.xml > /tmp/dd-ui.xml || true
    target_coords=$(python3 - "$needle" <<'PY'
import sys, re, xml.etree.ElementTree as ET
try:
    root = ET.parse('/tmp/dd-ui.xml').getroot()
    needle = sys.argv[1].lower()
    for node in root.iter('node'):
        if needle in node.attrib.get('text', '').lower():
            bounds = re.findall(r'\d+', node.attrib.get('bounds',''))
            if len(bounds)==4:
                a,b,c,d=map(int,bounds)
                print(f'{(a+c)//2} {(b+d)//2}')
                break
except (OSError,ET.ParseError,ValueError):
    pass
PY
)
    if [ -n "$target_coords" ]; then
      adb shell input tap $target_coords
      return 0
    fi
    adb shell input swipe 530 1800 530 610 350
    sleep 1
  done
  echo "Cannot find UI element: $needle" >&2
  return 1
}
find_and_tap "LET'S PLAY"
sleep 3
adb shell screencap -p /sdcard/daredash-game.png
adb pull /sdcard/daredash-game.png play-store/screenshots/02-challenge.png >/dev/null
find_and_tap "Finish game"
sleep 2
adb shell screencap -p /sdcard/daredash-winner.png
adb pull /sdcard/daredash-winner.png play-store/screenshots/03-results.png >/dev/null
ls -lah play-store/screenshots
