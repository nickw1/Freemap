#!/bin/bash
echo "LOCK" > /home/www-data/POPULATE_LOCK
chmod go-x /var/www/fm/ws
#wget http://nick.dev.openstreetmap.org/downloads/planet/england_wales_relevant.osm.bz2
/usr/local/bin/osm2pgsql -U gis -d gis2 -s -S popdb_files/ways2.style /home/nick/public_html/downloads/planet/england_wales_relevant.osm.bz2
chmod go+x /var/www/fm/ws
rm /home/www-data/POPULATE_LOCK 
mv -f england_wales_relevant.osm.bz2 england_wales_relevant.prev.osm.bz2
/usr/bin/psql -U gis gis2 < popdb_files/fixcuttings.sql
