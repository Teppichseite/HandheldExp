#!/bin/bash

FOREGROUND_USER=0

# Close the overlay, if this is not the case yet
am broadcast --user $FOREGROUND_USER -a com.handheld.exp.OVERLAY --es command "close"

# Inform the overlay that a game was ended somehow
# and the user returned back to ES-DE
am broadcast --user $FOREGROUND_USER -a com.handheld.exp.GAME --es command "end"