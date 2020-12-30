
(function (MapCSS) {
    'use strict';

    function restyle(style, tags, zoom, type, selector) {
        var s_default = {}, s_rights = {};

        if ((selector == 'canvas')) {
            s_default['fill-color'] = 'lightblue';
        }

        if (((selector == 'area' && tags['natural'] == 'nosea'))) {
            s_default['fill-color'] = '#f2efe9';
            s_default['z-index'] = 0;
        }

        if (((type == 'node' && (tags.hasOwnProperty('place')) && tags['place'] !== 'locality'))) {
            s_default['text-color'] = 'black';
        }

        if (((type == 'node' && (tags.hasOwnProperty('place')) && tags['place'] !== 'locality')) || ((type == 'node' && tags['railway'] == 'station') && zoom >= 13 && zoom <= 20)) {
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'DejaVu Sans Book';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['text-allow-overlap'] = 'true';
        }

        if (((type == 'node' && tags['railway'] == 'station') && zoom >= 13 && zoom <= 20)) {
            s_default['text-color'] = 'red';
            s_default['color'] = 'red';
            s_default['icon-image'] = 'img/rsmall.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['text-offset'] = 6;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['man_made'] == 'mast') && zoom >= 13 && zoom <= 20) || ((type == 'node' && tags['man_made'] == 'communications_tower') && zoom >= 13 && zoom <= 20)) {
            s_default['icon-image'] = 'img/mast.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['power'] == 'tower') && zoom >= 13 && zoom <= 20)) {
            s_default['icon-image'] = 'img/powertower.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['barrier'] == 'stile') && zoom >= 15 && zoom <= 20)) {
            s_default['icon-image'] = 'img/osm_stile_small.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['barrier'] == 'gate') && zoom >= 15 && zoom <= 20)) {
            s_default['icon-image'] = 'img/osm_gate_small.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['tourism'] == 'viewpoint') && zoom >= 14 && zoom <= 20)) {
            s_default['icon-image'] = 'img/viewpoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['man_made'] == 'windmill') && zoom >= 14 && zoom <= 20)) {
            s_default['icon-image'] = 'img/osm_windmill.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['man_made'] == 'water_tower') && zoom >= 14 && zoom <= 20)) {
            s_default['icon-image'] = 'img/osm_water_tower.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['tourism'] == 'camp_site') && zoom >= 14 && zoom <= 20)) {
            s_default['icon-image'] = 'img/osm_camping.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['tourism'] == 'hostel') && zoom >= 14 && zoom <= 20)) {
            s_default['icon-image'] = 'img/osmosnimki.hostel.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && tags['amenity'] == 'parking') && zoom >= 15 && zoom <= 20)) {
            s_default['icon-image'] = 'img/carpark.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 21;
            s_default['opacity'] = 1;
        }

        if (((type == 'node' && (tags.hasOwnProperty('place'))))) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'DejaVu Sans Book';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
        }

        if (((type == 'node' && tags['place'] == 'suburb') && zoom >= 17 && zoom <= 20) || ((type == 'node' && tags['place'] == 'hamlet') && zoom >= 17 && zoom <= 20)) {
            s_default['font-size'] = '18';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 10;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'village') && zoom >= 17 && zoom <= 20)) {
            s_default['font-size'] = '20';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 17 && zoom <= 20)) {
            s_default['font-size'] = '22';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 17 && zoom <= 20)) {
            s_default['font-size'] = '24';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 14;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'suburb') && zoom >= 15 && zoom <= 16) || ((type == 'node' && tags['place'] == 'hamlet') && zoom >= 15 && zoom <= 16)) {
            s_default['font-size'] = '14';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 10;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'village') && zoom >= 15 && zoom <= 16)) {
            s_default['font-size'] = '16';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 15 && zoom <= 16)) {
            s_default['font-size'] = '18';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 15 && zoom <= 16)) {
            s_default['font-size'] = '20';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 14;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'suburb') && zoom === 14) || ((type == 'node' && tags['place'] == 'hamlet') && zoom === 14)) {
            s_default['font-size'] = '10';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 10;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'village') && zoom === 14)) {
            s_default['font-size'] = '12';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom === 14)) {
            s_default['font-size'] = '14';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom === 14)) {
            s_default['font-size'] = '16';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 14;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'village') && zoom >= 12 && zoom <= 13)) {
            s_default['font-size'] = '10';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 10;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 12 && zoom <= 13)) {
            s_default['font-size'] = '12';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 12 && zoom <= 13)) {
            s_default['font-size'] = '14';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 10 && zoom <= 11)) {
            s_default['font-size'] = '10';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 10;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 10 && zoom <= 11)) {
            s_default['font-size'] = '12';
            s_default['icon-image'] = 'img/waypoint.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-allow-overlap'] = 'true';
            s_default['text-offset'] = 12;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['natural'] == 'peak'))) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'DejaVu Sans Book';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['text-allow-overlap'] = 'true';
            s_default['allow-overlap'] = 'true';
            s_default['icon-image'] = 'img/peak_small.png';
            s_default['text-offset'] = 12;
        }

        if (((type == 'node' && tags['natural'] == 'peak') && zoom >= 13 && zoom <= 14)) {
            s_default['font-size'] = '10';
        }

        if (((type == 'node' && tags['natural'] == 'peak') && zoom >= 15 && zoom <= 20)) {
            s_default['font-size'] = '14';
        }

        if (((type == 'node' && tags['amenity'] == 'pub') && zoom >= 14 && zoom <= 20)) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'DejaVu Sans Book';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['icon-image'] = 'img/pub.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-offset'] = 8;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['amenity'] == 'pub') && zoom === 14) || ((type == 'node' && tags['amenity'] == 'cafe') && zoom === 14) || ((type == 'node' && tags['place'] == 'locality') && zoom === 14) || ((type == 'node' && tags['amenity'] == 'restaurant') && zoom === 14) || ((type == 'node' && tags['railway'] == 'station') && zoom === 14)) {
            s_default['font-size'] = '8';
        }

        if (((type == 'node' && tags['amenity'] == 'pub') && zoom >= 15 && zoom <= 16) || ((type == 'node' && tags['amenity'] == 'cafe') && zoom >= 15 && zoom <= 16) || ((type == 'node' && tags['place'] == 'locality') && zoom >= 15 && zoom <= 16) || ((type == 'node' && tags['amenity'] == 'restaurant') && zoom >= 15 && zoom <= 16) || ((type == 'node' && tags['railway'] == 'station') && zoom >= 15 && zoom <= 16)) {
            s_default['font-size'] = '12';
        }

        if (((type == 'node' && tags['amenity'] == 'pub') && zoom >= 17 && zoom <= 20) || ((type == 'node' && tags['amenity'] == 'cafe') && zoom >= 17 && zoom <= 20) || ((type == 'node' && tags['place'] == 'locality') && zoom >= 17 && zoom <= 20) || ((type == 'node' && tags['amenity'] == 'restaurant') && zoom >= 17 && zoom <= 20) || ((type == 'node' && tags['railway'] == 'station') && zoom >= 17 && zoom <= 20)) {
            s_default['font-size'] = '16';
        }

        if (((type == 'node' && tags['amenity'] == 'cafe') && zoom >= 14 && zoom <= 20)) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'DejaVu Sans Book';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['icon-image'] = 'img/cafe.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-offset'] = 8;
            s_default['z-index'] = 21;
        }

        if (((type == 'node' && tags['amenity'] == 'restaurant') && zoom >= 14 && zoom <= 20)) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'DejaVu Sans Book';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['icon-image'] = 'img/restaurant.png';
            s_default['allow-overlap'] = 'true';
            s_default['text-offset'] = 8;
            s_default['z-index'] = 21;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom === 14) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom === 14)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 3;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom === 14) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom === 14)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 3;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom === 14) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom === 14)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 3;
            s_default['z-index'] = 7;
        }

        if (((type == 'way' && tags['highway'] == 'secondary') && zoom === 14) || ((type == 'way' && tags['highway'] == 'secondary_link') && zoom === 14)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 3;
            s_default['z-index'] = 6;
        }

        if (((type == 'way' && tags['highway'] == 'tertiary') && zoom === 14) || ((type == 'way' && tags['highway'] == 'tertiary_link') && zoom === 14)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 3;
            s_default['z-index'] = 5;
        }

        if (((type == 'way' && tags['highway'] == 'unclassified') && zoom === 14) || ((type == 'way' && tags['highway'] == 'unclassified_link') && zoom === 14)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 3;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'residential') && zoom === 14) || ((type == 'way' && tags['highway'] == 'service') && zoom === 14) || ((type == 'way' && tags['highway'] == 'residential_link') && zoom === 14) || ((type == 'way' && tags['highway'] == 'service_link') && zoom === 14)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 2;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'track') && zoom === 14) || ((type == 'way' && tags['highway'] == 'byway') && zoom >= 14 && zoom <= 20)) {
            s_default['color'] = 'white';
            s_default['width'] = 2;
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['casing-dashes'] = [4, 2];
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom === 15) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom === 15)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 4;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom === 15) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom === 15)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 4;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom === 15) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom === 15)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 4;
            s_default['z-index'] = 7;
        }

        if (((type == 'way' && tags['highway'] == 'secondary') && zoom === 15) || ((type == 'way' && tags['highway'] == 'secondary_link') && zoom === 15)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 4;
            s_default['z-index'] = 6;
        }

        if (((type == 'way' && tags['highway'] == 'tertiary') && zoom === 15) || ((type == 'way' && tags['highway'] == 'tertiary_link') && zoom === 15)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 4;
            s_default['z-index'] = 5;
        }

        if (((type == 'way' && tags['highway'] == 'unclassified') && zoom === 15) || ((type == 'way' && tags['highway'] == 'unclassified_link') && zoom === 15)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 4;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'residential') && zoom === 15) || ((type == 'way' && tags['highway'] == 'service') && zoom === 15) || ((type == 'way' && tags['highway'] == 'residential_link') && zoom === 15) || ((type == 'way' && tags['highway'] == 'service_link') && zoom === 15)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 3;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'track') && zoom === 15) || ((type == 'way' && tags['highway'] == 'byway') && zoom === 15)) {
            s_default['color'] = 'white';
            s_default['width'] = 3;
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['casing-dashes'] = [4, 2];
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom === 16) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom === 16)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 6;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom === 16) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom === 16)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 6;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom === 16) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom === 16)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 6;
            s_default['z-index'] = 7;
        }

        if (((type == 'way' && tags['highway'] == 'secondary') && zoom === 16) || ((type == 'way' && tags['highway'] == 'secondary_link') && zoom === 16)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 6;
            s_default['z-index'] = 6;
        }

        if (((type == 'way' && tags['highway'] == 'tertiary') && zoom === 16) || ((type == 'way' && tags['highway'] == 'tertiary_link') && zoom === 16)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 6;
            s_default['z-index'] = 5;
        }

        if (((type == 'way' && tags['highway'] == 'unclassified') && zoom === 16) || ((type == 'way' && tags['highway'] == 'unclassified_link') && zoom === 16)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 6;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'residential') && zoom === 16) || ((type == 'way' && tags['highway'] == 'service') && zoom === 16) || ((type == 'way' && tags['highway'] == 'residential_link') && zoom === 16) || ((type == 'way' && tags['highway'] == 'service_link') && zoom === 16)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 4;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'track') && zoom === 16) || ((type == 'way' && tags['highway'] == 'byway') && zoom === 16)) {
            s_default['color'] = 'white';
            s_default['width'] = 4;
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['casing-dashes'] = [4, 2];
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom >= 17)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 8;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom >= 17)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 8;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom >= 17)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 8;
            s_default['z-index'] = 7;
        }

        if (((type == 'way' && tags['highway'] == 'secondary') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'secondary_link') && zoom >= 17)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 8;
            s_default['z-index'] = 6;
        }

        if (((type == 'way' && tags['highway'] == 'tertiary') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'tertiary_link') && zoom >= 17)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 8;
            s_default['z-index'] = 5;
        }

        if (((type == 'way' && tags['highway'] == 'unclassified') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'unclassified_link') && zoom >= 17)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 8;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'residential') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'service') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'residential_link') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'service_link') && zoom >= 17)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 6;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'track') && zoom >= 17) || ((type == 'way' && tags['highway'] == 'byway') && zoom >= 17)) {
            s_default['color'] = 'white';
            s_default['width'] = 6;
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['casing-dashes'] = [4, 2];
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['highway'] == 'path') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'footway') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'bridleway') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'steps') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'pedestrian') && zoom >= 14 && zoom <= 20)) {
            s_default['color'] = 'black';
            s_default['width'] = 1;
            s_default['dashes'] = [2, 2];
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['highway'] == 'cycleway') && zoom >= 14 && zoom <= 20)) {
            s_default['color'] = 'blue';
            s_default['width'] = 1;
            s_default['dashes'] = [2, 2];
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['highway'] == 'path' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'footway' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'bridleway' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'cycleway' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['highway'] == 'pedestrian' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 20)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 2;
            s_default['casing-dashes'] = [1, 0];
        }

        if (((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'public_footpath') && zoom >= 14 && zoom <= 20)) {
            s_rights['color'] = '#00ff00';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 20;
        }

        if (((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'public_bridleway') && zoom >= 14 && zoom <= 20)) {
            s_rights['color'] = '#c06000';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 20;
        }

        if (((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'public_byway') && zoom >= 14 && zoom <= 20) || ((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'byway') && zoom >= 14 && zoom <= 20) || ((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'restricted_byway') && zoom >= 14 && zoom <= 20) || ((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'byway_open_to_all_traffic') && zoom >= 14 && zoom <= 20) || ((type == 'way' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'unknown_byway') && zoom >= 14 && zoom <= 20)) {
            s_rights['color'] = 'red';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 20;
        }

        if (((type == 'way' && (tags.hasOwnProperty('highway')) && tags['foot'] == 'permissive') && zoom >= 14 && zoom <= 20)) {
            s_rights['color'] = 'cyan';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 20;
        }

        if (((type == 'way' && (tags.hasOwnProperty('contour'))) && zoom >= 13 && zoom <= 20)) {
            s_default['color'] = '#fb9b67';
            s_default['width'] = 0.5;
            s_default['z-index'] = 2;
            s_default['text'] = MapCSS.e_localize(tags, 'contour');
            s_default['text-color'] = '#fb9b67';
            s_default['font-family'] = 'helvetica';
        }

        if (((type == 'way' && (tags.hasOwnProperty('contour'))) && zoom === 13)) {
            s_default['font-size'] = '8';
        }

        if (((type == 'way' && (tags.hasOwnProperty('contour'))) && zoom === 14)) {
            s_default['font-size'] = '10';
        }

        if (((type == 'way' && (tags.hasOwnProperty('contour'))) && zoom >= 15 && zoom <= 16)) {
            s_default['font-size'] = '12';
        }

        if (((type == 'way' && (tags.hasOwnProperty('contour'))) && zoom >= 17 && zoom <= 20)) {
            s_default['font-size'] = '14';
        }

        if (((type == 'way' && (tags.hasOwnProperty('highway'))) && zoom >= 16 && zoom <= 20)) {
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['text-color'] = 'black';
            s_default['font-family'] = 'helvetica';
            s_default['font-weight'] = 'bold';
            s_default['font-size'] = '12';
        }

        if (((type == 'way' && tags['barrier'] == 'hedge') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['barrier'] == 'fence') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['barrier'] == 'wall') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['barrier'] == 'ditch') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['barrier'] == 'retaining_wall') && zoom >= 14 && zoom <= 20)) {
            s_default['color'] = '#404040';
            s_default['width'] = 0.5;
            s_default['z-index'] = 2;
        }

        if (((type == 'way' && tags['power'] == 'line') && zoom >= 13 && zoom <= 20)) {
            s_default['color'] = '#808080';
            s_default['width'] = 1;
            s_default['z-index'] = 21;
        }

        if (((selector == 'area' && tags['natural'] == 'wood')) || ((selector == 'area' && tags['landuse'] == 'forest'))) {
            s_default['fill-color'] = '#c0e0c0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'water')) || ((selector == 'area' && tags['landuse'] == 'reservoir'))) {
            s_default['fill-color'] = 'lightblue';
            s_default['z-index'] = 3;
        }

        if (((selector == 'area' && tags['natural'] == 'heath'))) {
            s_default['fill-color'] = '#f0e0a0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'moor'))) {
            s_default['fill-color'] = '#ffc0ff';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['leisure'] == 'park')) || ((selector == 'area' && tags['leisure'] == 'common'))) {
            s_default['fill-color'] = '#e0ffe0';
            s_default['z-index'] = 1;
        }

        if (((selector == 'area' && tags['landuse'] == 'residential')) || ((selector == 'area' && tags['landuse'] == 'commercial')) || ((selector == 'area' && tags['landuse'] == 'retail'))) {
            s_default['fill-color'] = '#c0c0c0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['landuse'] == 'industrial'))) {
            s_default['fill-color'] = '#a0a0a0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['landuse'] == 'military'))) {
            s_default['fill-color'] = '#ffe0e0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'scree') && zoom >= 13) || ((selector == 'area' && tags['natural'] == 'bare_rock') && zoom >= 13)) {
            s_default['fill-image'] = 'img/Bare_rock-125.png';
            s_default['z-index'] = 3;
        }

        if (((selector == 'area' && tags['landuse'] == 'field') && zoom >= 14)) {
            s_default['casing-color'] = '#404040';
            s_default['casing-width'] = 0.25;
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'beach') && zoom >= 13)) {
            s_default['fill-color'] = '#ffffc0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'wetland') && zoom >= 13)) {
            s_default['fill-color'] = '#f0f0ff';
            s_default['z-index'] = 2;
        }

        if (((type == 'way' && tags['waterway'] == 'river'))) {
            s_default['color'] = 'lightblue';
            s_default['width'] = 2;
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['waterway'] == 'stream'))) {
            s_default['color'] = 'lightblue';
            s_default['width'] = 1;
            s_default['z-index'] = 3;
        }

        if (((type == 'way' && tags['railway'] == 'rail') && zoom >= 14 && zoom <= 20) || ((type == 'way' && tags['railway'] == 'preserved_rail')) || ((type == 'way' && tags['railway'] == 'preserved'))) {
            s_default['color'] = 'white';
            s_default['casing-color'] = 'black';
            s_default['casing-width'] = 1;
            s_default['dashes'] = [4, 4];
            s_default['casing-dashes'] = [1, 0];
            s_default['width'] = 2;
        }

        if (((type == 'way' && tags['railway'] == 'rail') && zoom >= 10 && zoom <= 13) || ((type == 'way' && tags['railway'] == 'preserved_rail')) || ((type == 'way' && tags['railway'] == 'preserved'))) {
            s_default['color'] = 'white';
            s_default['casing-color'] = 'black';
            s_default['casing-width'] = 0.5;
            s_default['dashes'] = [4, 4];
            s_default['casing-dashes'] = [1, 0];
            s_default['width'] = 1;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom === 13) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom === 13)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 3;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom === 13) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom === 13)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 3;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom === 13) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom === 13)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 3;
            s_default['z-index'] = 7;
        }

        if (((type == 'way' && tags['highway'] == 'secondary') && zoom === 13) || ((type == 'way' && tags['highway'] == 'secondary_link') && zoom === 13)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 3;
            s_default['z-index'] = 6;
        }

        if (((type == 'way' && tags['highway'] == 'tertiary') && zoom >= 12 && zoom <= 13) || ((type == 'way' && tags['highway'] == 'tertiary_link') && zoom >= 12 && zoom <= 13)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 2;
            s_default['z-index'] = 5;
        }

        if (((type == 'way' && tags['highway'] == 'unclassified') && zoom === 13) || ((type == 'way' && tags['highway'] == 'unclassified_link') && zoom === 13)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 2;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'unclassified') && zoom === 12) || ((type == 'way' && tags['highway'] == 'unclassified_link') && zoom === 12)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 0.5;
            s_default['color'] = 'white';
            s_default['width'] = 1;
            s_default['z-index'] = 4;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom >= 10 && zoom <= 12) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 2;
            s_default['color'] = '#809bc0';
            s_default['width'] = 2;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom >= 10 && zoom <= 12) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 2;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom >= 10 && zoom <= 12) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 2;
            s_default['z-index'] = 7;
        }

        if (((type == 'way' && tags['highway'] == 'secondary') && zoom >= 10 && zoom <= 12) || ((type == 'way' && tags['highway'] == 'secondary_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 2;
            s_default['z-index'] = 6;
        }

        if (((type == 'way' && tags['highway'] == 'motorway') && zoom >= 1 && zoom <= 9) || ((type == 'way' && tags['highway'] == 'motorway_link') && zoom >= 1 && zoom <= 9)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 2;
            s_default['color'] = '#809bc0';
            s_default['width'] = 1;
            s_default['z-index'] = 9;
        }

        if (((type == 'way' && tags['highway'] == 'trunk') && zoom >= 1 && zoom <= 9) || ((type == 'way' && tags['highway'] == 'trunk_link') && zoom >= 1 && zoom <= 9)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 1;
            s_default['z-index'] = 8;
        }

        if (((type == 'way' && tags['highway'] == 'primary') && zoom >= 1 && zoom <= 9) || ((type == 'way' && tags['highway'] == 'primary_link') && zoom >= 1 && zoom <= 9)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 1;
            s_default['z-index'] = 7;
        }

        if (Object.keys(s_default).length) {
            style['default'] = s_default;
        }
        if (Object.keys(s_rights).length) {
            style['rights'] = s_rights;
        }
        return style;
    }
    
    var sprite_images = {
    }, external_images = ['img/Bare_rock-125.png', 'img/cafe.png', 'img/carpark.png', 'img/mast.png', 'img/osm_camping.png', 'img/osm_gate_small.png', 'img/osm_stile_small.png', 'img/osm_water_tower.png', 'img/osm_windmill.png', 'img/osmosnimki.hostel.png', 'img/peak_small.png', 'img/powertower.png', 'img/pub.png', 'img/restaurant.png', 'img/rsmall.png', 'img/viewpoint.png', 'img/waypoint.png'], presence_tags = [], value_tags = ['highway', 'railway', 'leisure', 'amenity', 'contour', 'tourism', 'man_made', 'waterway', 'designation', 'power', 'name', 'natural', 'landuse', 'place', 'foot', 'barrier', 'bridge'];

    MapCSS.loadStyle('free-map', restyle, sprite_images, external_images, presence_tags, value_tags);
    MapCSS.preloadExternalImages('free-map');
})(MapCSS);
    