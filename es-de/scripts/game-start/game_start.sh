#!/bin/bash

FOREGROUND_USER=0

# Start he overlay service if it was not running yet
am broadcast --user $FOREGROUND_USER -a com.handheld.exp.OVERLAY_SERVICE \
  -n com.handheld.exp/.receivers.OverlayServiceReceiver \
  \

# Inform the overlay that a game was started
# via ES-DE and send respective game data
am broadcast --user $FOREGROUND_USER -a com.handheld.exp.GAME \
  --es command "start" \
  --es game_path "${1}" \
  --es game_name "${2}" \
  --es system_name "${3}" \
  --es system_full_name "${4}" \
  \