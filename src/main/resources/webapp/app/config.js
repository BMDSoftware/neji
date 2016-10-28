/*!
 * becas - biomedical concept annotator
 *
 * Copyright 2014 UA.PT Bioinformatics
 * All rights reserved.
 * http://bioinformatics.ua.pt/becas/
 */

/*global require, window */

require.config({
    // Initialize the application with the main application file.
    deps: ['main'],

    paths: {
        // Libraries.
        jquery: '../assets/js/libs/jquery',
        underscore: '../assets/js/libs/lodash', // use lodash instead of underscore
        backbone: '../assets/js/libs/backbone',
        bootstrap: '../assets/js/libs/bootstrap',
        handlebars: '../assets/js/libs/handlebars',
        json2: '../assets/js/libs/json2',

        // jQuery Plugins.
        'jquery.effects': '../assets/js/plugins/jquery-ui.effects',
        'jquery.scrollTo': '../assets/js/plugins/jquery.scrollTo',
        'jquery.treeview': '../assets/js/plugins/jquery.treeview',
        'jquery.sortedEach': '../assets/js/plugins/jquery.sortedEach',
        'jquery.cookie': '../assets/js/plugins/jquery.cookie',

        // Underscore Plugins.
        'underscore.string': '../assets/js/plugins/underscore.string',

        // Backbone Plugins.
        'backbone.layoutmanager': '../assets/js/plugins/backbone.layoutmanager',

        // Bootstrap Plugins.
        'bootstrap.tour': '../assets/js/plugins/bootstrap.tour'
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

        // Bootstrap Tour attaches itself to the window.
        'bootstrap.tour': {
            exports: 'Tour'
        },

        // All jQuery plugins depend on jQuery.
        'jquery.effects': ['jquery'],
        'jquery.scrollTo': ['jquery'],
        'jquery.treeview': ['jquery'],
        'jquery.sortedEach': ['jquery']
    }

});
