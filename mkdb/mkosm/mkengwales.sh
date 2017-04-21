#!/bin/bash
bash ./mkrelevant2.ll.sh england
bash ./mkrelevant2.ll.sh wales
bash ./merge.sh england
bash ./merge.sh wales
bunzip2 wales_relevant.osm.bz2
OSMOSIS=/home/nick/errol/osmosis-0.42/bin/osmosis
bzcat england_relevant.osm.bz2 | ${OSMOSIS} --rx file=- outPipe.0=england --rx file=wales_relevant.osm outPipe.0=wales --merge inPipe.0=england inPipe.1=wales --sort --wx file=- | bzip2 > /home/nick/public_html/downloads/planet/england_wales_relevant.osm.bz2
