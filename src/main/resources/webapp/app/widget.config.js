/*!
 * becas widget - biomedical concept annotator
 *
 * Copyright 2014 UA.PT Bioinformatics
 * All rights reserved.
 * http://bioinformatics.ua.pt/becas/
 */

/*global require, window */

require.config({
    // Initialize the widget with the main application file.
    deps: ['widget/main'],

    paths: {
        // Libraries.
        jquery: '../assets/js/libs/jquery',
        underscore: '../assets/js/libs/lodash', // use lodash instead of underscore
        backbone: '../assets/js/libs/backbone',
        bootstrap: '../assets/js/libs/bootstrap',
        handlebars: '../assets/js/libs/handlebars',
        easyxdm: '../assets/js/libs/easyXDM',
        json2: '../assets/js/libs/json2',

        // jQuery Plugins.
        'jquery.sortedEach': '../assets/js/plugins/jquery.sortedEach',

        // Underscore Plugins.
        'underscore.string': '../assets/js/plugins/underscore.string',

        // Backbone Plugins.
        'backbone.layoutmanager': '../assets/js/plugins/backbone.layoutmanager'
    },

    map: {
        // Map modules shared by app and widget to proper paths
        '*': {
            'app': 'widget/app'
        }
    },

    shim: {
        // Backbone library depends on underscore and jQuery.
        backbone: {
            deps: ['underscore', 'jquery'],
            exports: 'Backbone'
        },

        // Backbone.LayoutManager depends on Backbone.
        'backbone.layoutmanager': ['backbone'],

        // Handlebars
        handlebars: {
            exports: 'Handlebars'
        },

        // Lodash needs to be exported
        'underscore': {
            exports: '_'
        },

        // Underscore.String depends on Underscore.
        'underscore.string': ['underscore'],

        // All jQuery plugins depend on jQuery.
        'jquery.sortedEach': ['jquery']
    }

});
