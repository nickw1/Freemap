
var MapCSS = {
    styles: {},
    availableStyles: [],
    images: {},
    locales: [],
    presence_tags: [],
    value_tags: [],
    cache: {},
    debug: {hit: 0, miss: 0},

    onError: function () {
    },

    onImagesLoad: function () {
    },

    /**
     * Incalidate styles cache
     */
    invalidateCache: function () {
        this.cache = {};
    },

    e_min: function (/*...*/) {
        return Math.min.apply(null, arguments);
    },

    e_max: function (/*...*/) {
        return Math.max.apply(null, arguments);
    },

    e_any: function (/*...*/) {
        var i;

        for (i = 0; i < arguments.length; i++) {
            if (typeof(arguments[i]) !== 'undefined' && arguments[i] !== '') {
                return arguments[i];
            }
        }

        return '';
    },

    e_num: function (arg) {
        if (!isNaN(parseFloat(arg))) {
            return parseFloat(arg);
        } else {
            return '';
        }
    },

    e_str: function (arg) {
        return arg;
    },

    e_int: function (arg) {
        return parseInt(arg, 10);
    },

    e_tag: function (obj, tag) {
        if (obj.hasOwnProperty(tag) && obj[tag] !== null) {
            return tag;
        } else {
            return '';
        }
    },

    e_prop: function (obj, tag) {
        if (obj.hasOwnProperty(tag) && obj[tag] !== null) {
            return obj[tag];
        } else {
            return '';
        }
    },

    e_sqrt: function (arg) {
        return Math.sqrt(arg);
    },

    e_boolean: function (arg, if_exp, else_exp) {
        if (typeof(if_exp) === 'undefined') {
            if_exp = 'true';
        }

        if (typeof(else_exp) === 'undefined') {
            else_exp = 'false';
        }

        if (arg === '0' || arg === 'false' || arg === '') {
            return else_exp;
        } else {
            return if_exp;
        }
    },

    e_metric: function (arg) {
        if (/\d\s*mm$/.test(arg)) {
            return 1000 * parseInt(arg, 10);
        } else if (/\d\s*cm$/.test(arg)) {
            return 100 * parseInt(arg, 10);
        } else if (/\d\s*dm$/.test(arg)) {
            return 10 * parseInt(arg, 10);
        } else if (/\d\s*km$/.test(arg)) {
            return 0.001 * parseInt(arg, 10);
        } else if (/\d\s*in$/.test(arg)) {
            return 0.0254 * parseInt(arg, 10);
        } else if (/\d\s*ft$/.test(arg)) {
            return 0.3048 * parseInt(arg, 10);
        } else {
            return parseInt(arg, 10);
        }
    },

    e_zmetric: function (arg) {
        return MapCSS.e_metric(arg);
    },

    e_localize: function (tags, text) {
        var locales = MapCSS.locales, i, tag;

        for (i = 0; i < locales.length; i++) {
            tag = text + ':' + locales[i];
            if (tags[tag]) {
                return tags[tag];
            }
        }

        return tags[text];
    },

    e_join: function () {
        if (arguments.length === 2 && Object.prototype.toString.call(arguments[1]) === '[object Array]') {
            return arguments[1].join(arguments[0]);
        } else {
            var tagString = "";

            for (var i = 1; i < arguments.length; i++)
                tagString = tagString.concat(arguments[0]).concat(arguments[i]);

            return tagString.substr(arguments[0].length);
        }
    },

    e_split: function (sep, text) {
        return text.split(sep);
    },

    e_get: function(arr, index) {
        if (Object.prototype.toString.call(arr) !== '[object Array]')
            return "";

        if (!/^[0-9]+$/.test(index) || index >= arr.length())
            return "";

        return arr[index];
    },

    e_set: function(arr, index, text) {
        if (Object.prototype.toString.call(arr) !== '[object Array]')
            return [];

        if (!/^[0-9]+$/.test(index))
            return [];

        arr[index] = text;

        return arr;
    },

    e_count: function(arr) {
        if (Object.prototype.toString.call(arr) !== '[object Array]')
            return 0;

        return arr.length();
    },

    e_list: function() {
        return arguments;
    },

    e_append: function(lst, v) {
        if (Object.prototype.toString.call(lst) !== '[object Array]')
            return [];

        lst.push(v);

        return lst;
    },

    e_contains: function(lst, v) {
        if (Object.prototype.toString.call(lst) !== '[object Array]')
            return false;

        return (lst.indexOf(v) >= 0);
    },

    e_sort: function(lst) {
        if (Object.prototype.toString.call(lst) !== '[object Array]')
            return [];

        lst.sort();

        return lst;
    },

    e_reverse: function(lst) {
        if (Object.prototype.toString.call(lst) !== '[object Array]')
            return [];

        lst.reverse();

        return lst;
    },

    loadStyle: function (style, restyle, sprite_images, external_images, presence_tags, value_tags) {
        var i;
        sprite_images = sprite_images || [];
        external_images = external_images || [];

        if (presence_tags) {
            for (i = 0; i < presence_tags.length; i++) {
                if (this.presence_tags.indexOf(presence_tags[i]) < 0) {
                    this.presence_tags.push(presence_tags[i]);
                }
            }
        }

        if (value_tags) {
            for (i = 0; i < value_tags.length; i++) {
                if (this.value_tags.indexOf(value_tags[i]) < 0) {
                    this.value_tags.push(value_tags[i]);
                }
            }
        }

        MapCSS.styles[style] = {
            restyle: restyle,
            images: sprite_images,
            external_images: external_images,
            textures: {},
            sprite_loaded: !sprite_images,
            external_images_loaded: !external_images.length
        };

        MapCSS.availableStyles.push(style);
    },

    /**
     * Call MapCSS.onImagesLoad callback if all sprite and external
     * images was loaded
     */
    _onImagesLoad: function (style) {
        if (MapCSS.styles[style].external_images_loaded &&
                MapCSS.styles[style].sprite_loaded) {
            MapCSS.onImagesLoad();
        }
    },

    preloadSpriteImage: function (style, url) {
        var images = MapCSS.styles[style].images,
            img = new Image();

        delete MapCSS.styles[style].images;

        img.onload = function () {
            var image;
            for (image in images) {
                if (images.hasOwnProperty(image)) {
                    images[image].sprite = img;
                    MapCSS.images[image] = images[image];
                }
            }
            MapCSS.styles[style].sprite_loaded = true;
            MapCSS._onImagesLoad(style);
        };
        img.onerror = function (e) {
            MapCSS.onError(e);
        };
        img.src = url;
    },

    preloadExternalImages: function (style, urlPrefix) {
        var external_images = MapCSS.styles[style].external_images;
        delete MapCSS.styles[style].external_images;

        urlPrefix = urlPrefix || '';
        var len = external_images.length, loaded = 0, i;

        function loadImage(url) {
            var img = new Image();
            img.onload = function () {
                loaded++;
                MapCSS.images[url] = {
                    sprite: img,
                    height: img.height,
                    width: img.width,
                    offset: 0
                };
                if (loaded === len) {
                    MapCSS.styles[style].external_images_loaded = true;
                    MapCSS._onImagesLoad(style);
                }
            };
            img.onerror = function () {
                loaded++;
                if (loaded === len) {
                    MapCSS.styles[style].external_images_loaded = true;
                    MapCSS._onImagesLoad(style);
                }
            };
            img.src = url;
        }

        for (i = 0; i < len; i++) {
            loadImage(urlPrefix + external_images[i]);
        }
    },

    getImage: function (ref) {
        var img = MapCSS.images[ref];

        if (img && img.sprite) {
            var canvas = document.createElement('canvas');
            canvas.width = img.width;
            canvas.height = img.height;

            canvas.getContext('2d').drawImage(img.sprite,
                    0, img.offset, img.width, img.height,
                    0, 0, img.width, img.height);

            img = MapCSS.images[ref] = canvas;
        }

        return img;
    },

    getTagKeys: function (tags, zoom, type, selector) {
        var keys = [], i;
        for (i = 0; i < this.presence_tags.length; i++) {
            if (tags.hasOwnProperty(this.presence_tags[i])) {
                keys.push(this.presence_tags[i]);
            }
        }

        for (i = 0; i < this.value_tags.length; i++) {
            if (tags.hasOwnProperty(this.value_tags[i])) {
                keys.push(this.value_tags[i] + ':' + tags[this.value_tags[i]]);
            }
        }

        return [zoom, type, selector, keys.join(':')].join(':');
    },

    restyle: function (styleNames, tags, zoom, type, selector) {
        var i, key = this.getTagKeys(tags, zoom, type, selector), actions = this.cache[key] || {};

        if (!this.cache.hasOwnProperty(key)) {
            this.debug.miss += 1;
            for (i = 0; i < styleNames.length; i++) {
                actions = MapCSS.styles[styleNames[i]].restyle(actions, tags, zoom, type, selector);
            }
            this.cache[key] = actions;
        } else {
            this.debug.hit += 1;
        }

        return actions;
    }
};
