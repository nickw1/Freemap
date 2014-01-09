
(function (MapCSS) {
    'use strict';

    function restyle(style, tags, zoom, type, selector) {
        var s_default = {}, s_rights = {};

        if ((selector == 'canvas')) {
            s_default['fill-color'] = 'lightblue';
        }

        if (((selector == 'area' && tags['natural'] == 'land'))) {
            s_default['fill-color'] = '#f2efe9';
            s_default['z-index'] = 0;
        }

        if (((type == 'node' && (tags.hasOwnProperty('place'))))) {
            s_default['text-color'] = 'black';
        }

        if (((type == 'node' && (tags.hasOwnProperty('place')))) || ((type == 'node' && tags['railway'] == 'station') && zoom >= 13 && zoom <= 18)) {
            s_default['font-weight'] = 'bold';
            s_default['font-family'] = 'Helvetica';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['text-allow-overlap'] = 'true';
        }

        if (((type == 'node' && tags['railway'] == 'station') && zoom >= 13 && zoom <= 18)) {
            s_default['text-color'] = 'red';
            s_default['color'] = 'red';
            s_default['icon-image'] = '../data/symbols/rsmall.png';
            s_default['allow-overlap'] = 'true';
            s_default['z-index'] = 10;
            s_default['text-offset'] = 6;
        }

        if (((type == 'node' && tags['place'] == 'suburb') && zoom >= 14 && zoom <= 18) || ((type == 'node' && tags['place'] == 'hamlet'))) {
            s_default['font-size'] = '10';
        }

        if (((type == 'node' && tags['place'] == 'village') && zoom >= 14 && zoom <= 18)) {
            s_default['font-size'] = '12';
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 14 && zoom <= 18)) {
            s_default['font-size'] = '14';
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 14 && zoom <= 18)) {
            s_default['font-size'] = '16';
        }

        if (((type == 'node' && tags['place'] == 'village') && zoom >= 12 && zoom <= 13)) {
            s_default['font-size'] = '10';
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 12 && zoom <= 13)) {
            s_default['font-size'] = '12';
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 12 && zoom <= 13)) {
            s_default['font-size'] = '14';
        }

        if (((type == 'node' && tags['place'] == 'town') && zoom >= 10 && zoom <= 11)) {
            s_default['font-size'] = '10';
        }

        if (((type == 'node' && tags['place'] == 'city') && zoom >= 10 && zoom <= 11)) {
            s_default['font-size'] = '12';
        }

        if (((type == 'node' && tags['natural'] == 'peak'))) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-size'] = '10';
            s_default['font-family'] = 'Helvetica';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
            s_default['text-allow-overlap'] = 'true';
        }

        if (((type == 'node' && tags['amenity'] == 'pub'))) {
            s_default['text-color'] = 'black';
            s_default['font-weight'] = 'bold';
            s_default['font-size'] = '8';
            s_default['font-family'] = 'Helvetica';
            s_default['text'] = MapCSS.e_localize(tags, 'name');
        }

        if (((selector == 'line' && tags['highway'] == 'motorway') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'motorway_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 3;
            s_default['z-index'] = 8;
        }

        if (((selector == 'line' && tags['highway'] == 'trunk') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'trunk_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 3;
            s_default['z-index'] = 7;
        }

        if (((selector == 'line' && tags['highway'] == 'primary') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'primary_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 3;
            s_default['z-index'] = 6;
        }

        if (((selector == 'line' && tags['highway'] == 'secondary') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'secondary_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 3;
            s_default['z-index'] = 5;
        }

        if (((selector == 'line' && tags['highway'] == 'tertiary') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'tertiary_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 3;
            s_default['z-index'] = 4;
        }

        if (((selector == 'line' && tags['highway'] == 'unclassified') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'unclassified_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 3;
            s_default['z-index'] = 3;
        }

        if (((selector == 'line' && tags['highway'] == 'residential') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'service') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'residential_link') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'service_link') && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 2;
            s_default['z-index'] = 3;
        }

        if (((selector == 'line' && tags['highway'] == 'track') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'byway') && zoom >= 14 && zoom <= 18)) {
            s_default['color'] = 'white';
            s_default['width'] = 2;
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['casing-dashes'] = [4, 2];
            s_default['z-index'] = 2;
        }

        if (((selector == 'line' && tags['highway'] == 'path') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'footway') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'bridleway') && zoom >= 14 && zoom <= 18)) {
            s_default['color'] = 'black';
            s_default['width'] = 1;
            s_default['dashes'] = [2, 2];
            s_default['z-index'] = 2;
        }

        if (((selector == 'line' && tags['highway'] == 'path' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'footway' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['highway'] == 'bridleway' && (tags['bridge'] == '1' || tags['bridge'] == 'true' || tags['bridge'] == 'yes')) && zoom >= 14 && zoom <= 18)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 2;
            s_default['casing-dashes'] = [1, 0];
        }

        if (((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'public_footpath') && zoom >= 14 && zoom <= 18)) {
            s_rights['color'] = '#00ff00';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 9;
        }

        if (((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'public_bridleway') && zoom >= 14 && zoom <= 18)) {
            s_rights['color'] = '#c06000';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 9;
        }

        if (((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'public_byway') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'byway') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'restricted_byway') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'byway_open_to_all_traffic') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['designation'] == 'unknown_byway') && zoom >= 14 && zoom <= 18)) {
            s_rights['color'] = 'red';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 9;
        }

        if (((selector == 'line' && (tags.hasOwnProperty('highway')) && tags['foot'] == 'permissive') && zoom >= 14 && zoom <= 18)) {
            s_rights['color'] = 'cyan';
            s_rights['width'] = 3;
            s_rights['opacity'] = 0.5;
            s_rights['z-index'] = 9;
        }

        if (((selector == 'line' && (tags.hasOwnProperty('contour'))))) {
            s_default['color'] = '#fb9b67';
            s_default['width'] = 0.5;
            s_default['z-index'] = 1;
            s_default['text'] = MapCSS.e_localize(tags, 'contour');
            s_default['text-color'] = '#fb9b67';
            s_default['font-family'] = 'helvetica';
            s_default['font-size'] = '8';
        }

        if (((selector == 'area' && tags['natural'] == 'wood')) || ((selector == 'area' && tags['landuse'] == 'forest'))) {
            s_default['fill-color'] = '#c0ffc0';
            s_default['z-index'] = 1;
        }

        if (((selector == 'area' && tags['natural'] == 'water'))) {
            s_default['fill-color'] = 'lightblue';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'heath'))) {
            s_default['fill-color'] = '#ffffc0';
            s_default['z-index'] = 2;
        }

        if (((selector == 'area' && tags['natural'] == 'moor'))) {
            s_default['fill-color'] = '#ffc0ff';
            s_default['z-index'] = 2;
        }

        if (((selector == 'line' && tags['waterway'] == 'river'))) {
            s_default['color'] = 'lightblue';
            s_default['width'] = 2;
            s_default['z-index'] = 2;
        }

        if (((selector == 'line' && tags['waterway'] == 'stream'))) {
            s_default['color'] = 'lightblue';
            s_default['width'] = 1;
            s_default['z-index'] = 2;
        }

        if (((selector == 'line' && tags['railway'] == 'rail') && zoom >= 14 && zoom <= 18) || ((selector == 'line' && tags['railway'] == 'preserved_rail')) || ((selector == 'line' && tags['railway'] == 'preserved'))) {
            s_default['color'] = 'white';
            s_default['casing-color'] = 'black';
            s_default['casing-width'] = 1;
            s_default['dashes'] = [4, 4];
            s_default['casing-dashes'] = [1, 0];
            s_default['width'] = 2;
        }

        if (((selector == 'line' && tags['railway'] == 'rail') && zoom >= 10 && zoom <= 13) || ((selector == 'line' && tags['railway'] == 'preserved_rail')) || ((selector == 'line' && tags['railway'] == 'preserved'))) {
            s_default['color'] = 'white';
            s_default['casing-color'] = 'black';
            s_default['casing-width'] = 0.5;
            s_default['dashes'] = [4, 4];
            s_default['casing-dashes'] = [1, 0];
            s_default['width'] = 1;
        }

        if (((selector == 'line' && tags['highway'] == 'motorway') && zoom === 13) || ((selector == 'line' && tags['highway'] == 'motorway_link') && zoom === 13)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 1;
            s_default['color'] = '#809bc0';
            s_default['width'] = 3;
            s_default['z-index'] = 8;
        }

        if (((selector == 'line' && tags['highway'] == 'trunk') && zoom === 13) || ((selector == 'line' && tags['highway'] == 'trunk_link') && zoom === 13)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 3;
            s_default['z-index'] = 7;
        }

        if (((selector == 'line' && tags['highway'] == 'primary') && zoom === 13) || ((selector == 'line' && tags['highway'] == 'primary_link') && zoom === 13)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 3;
            s_default['z-index'] = 6;
        }

        if (((selector == 'line' && tags['highway'] == 'secondary') && zoom === 13) || ((selector == 'line' && tags['highway'] == 'secondary_link') && zoom === 13)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 3;
            s_default['z-index'] = 5;
        }

        if (((selector == 'line' && tags['highway'] == 'tertiary') && zoom >= 12 && zoom <= 13) || ((selector == 'line' && tags['highway'] == 'tertiary_link') && zoom >= 12 && zoom <= 13)) {
            s_default['casing-color'] = '#bbb';
            s_default['casing-width'] = 1;
            s_default['color'] = '#ffc';
            s_default['width'] = 2;
            s_default['z-index'] = 4;
        }

        if (((selector == 'line' && tags['highway'] == 'unclassified') && zoom === 13) || ((selector == 'line' && tags['highway'] == 'unclassified_link') && zoom === 13)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 1;
            s_default['color'] = 'white';
            s_default['width'] = 2;
            s_default['z-index'] = 3;
        }

        if (((selector == 'line' && tags['highway'] == 'unclassified') && zoom === 12) || ((selector == 'line' && tags['highway'] == 'unclassified_link') && zoom === 12)) {
            s_default['casing-color'] = '#999';
            s_default['casing-width'] = 0.5;
            s_default['color'] = 'white';
            s_default['width'] = 1;
            s_default['z-index'] = 3;
        }

        if (((selector == 'line' && tags['highway'] == 'motorway') && zoom >= 10 && zoom <= 12) || ((selector == 'line' && tags['highway'] == 'motorway_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 2;
            s_default['color'] = '#809bc0';
            s_default['width'] = 2;
            s_default['z-index'] = 8;
        }

        if (((selector == 'line' && tags['highway'] == 'trunk') && zoom >= 10 && zoom <= 12) || ((selector == 'line' && tags['highway'] == 'trunk_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 2;
            s_default['z-index'] = 7;
        }

        if (((selector == 'line' && tags['highway'] == 'primary') && zoom >= 10 && zoom <= 12) || ((selector == 'line' && tags['highway'] == 'primary_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 2;
            s_default['z-index'] = 6;
        }

        if (((selector == 'line' && tags['highway'] == 'secondary') && zoom >= 10 && zoom <= 12) || ((selector == 'line' && tags['highway'] == 'secondary_link') && zoom >= 10 && zoom <= 12)) {
            s_default['casing-color'] = '#a37b48';
            s_default['casing-width'] = 1;
            s_default['color'] = '#fee0b8';
            s_default['width'] = 2;
            s_default['z-index'] = 5;
        }

        if (((selector == 'line' && tags['highway'] == 'motorway') && zoom >= 1 && zoom <= 9) || ((selector == 'line' && tags['highway'] == 'motorway_link') && zoom >= 1 && zoom <= 9)) {
            s_default['casing-color'] = '#506077';
            s_default['casing-width'] = 2;
            s_default['color'] = '#809bc0';
            s_default['width'] = 1;
            s_default['z-index'] = 8;
        }

        if (((selector == 'line' && tags['highway'] == 'trunk') && zoom >= 1 && zoom <= 9) || ((selector == 'line' && tags['highway'] == 'trunk_link') && zoom >= 1 && zoom <= 9)) {
            s_default['casing-color'] = '#477147';
            s_default['casing-width'] = 1;
            s_default['color'] = '#cdeacd';
            s_default['width'] = 1;
            s_default['z-index'] = 7;
        }

        if (((selector == 'line' && tags['highway'] == 'primary') && zoom >= 1 && zoom <= 9) || ((selector == 'line' && tags['highway'] == 'primary_link') && zoom >= 1 && zoom <= 9)) {
            s_default['casing-color'] = '#8d4346';
            s_default['casing-width'] = 1;
            s_default['color'] = '#f4c3c4';
            s_default['width'] = 1;
            s_default['z-index'] = 6;
        }

        if (!K.Utils.isEmpty(s_default)) {
            style['default'] = s_default;
        }
        if (!K.Utils.isEmpty(s_rights)) {
            style['rights'] = s_rights;
        }
        return style;
    }
    
    var sprite_images = {
    }, external_images = ['../data/symbols/rsmall.png'], presence_tags = [], value_tags = ['highway', 'railway', 'amenity', 'contour', 'foot', 'waterway', 'name', 'natural', 'landuse', 'place', 'designation', 'bridge'];

    MapCSS.loadStyle('style', restyle, sprite_images, external_images, presence_tags, value_tags);
    MapCSS.preloadExternalImages('style');
})(MapCSS);
    
