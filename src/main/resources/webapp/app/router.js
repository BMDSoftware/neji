//
// router.js
//   Application router.
//
/*global define */

define([
    // Libraries.
    'backbone',

    // Application.
    'app',

    // Application Controller.
    'controller',

    // Modules.
    'modules/tracker'

], function (Backbone, app, Controller, Tracker) {
    'use strict';

    var Router = Backbone.Router.extend({
        routes: {
            '': 'index',
            '!/': 'index',
            '!/annotate?t=:text': 'annotateText',
            '!/pmid/:pmid': 'annotatePubl',
            '!/help': 'staticHelp',
            'help__:section': 'staticHelp',
            'about__:section': 'staticAbout',
            'api__:section': 'staticApi',
            'widget__:section': 'staticWidget',
            '!/api': 'staticApi',
            '!/widget': 'staticWidget',
            '!/about': 'staticAbout',
            '!/contact': 'staticContact',
            '!/tour': 'tourHome',
            '!/tour/annotated': 'tourAnnotated',
            '*url': 'notFound'
        },

        initialize: function () {
            app.log('Router.initialize()', this);
            this.isLandingPage = true;

            // Log all route events and track pageviews
            this.on('route', function () {
                app.log('Route:', arguments);
                if (!this.isLandingPage) {
                    Tracker.trackPageView();
                } else {
                    this.isLandingPage = false;
                }
            }, this);

            // Respond to "reset" requests
            app.on('app:go-home', function () {
                this.navigate('!/', true);
            }, this);

            // Init application controller
            this.controller = new Controller();
        },

        notFound: function (url) {
            app.error(404, '"' + url + '"');
            return this.navigate('!/', true);
        },

        index: function () {
            this.controller.showHomepage();
        },

        tourHome: function () {
            this.controller.startHomeTour();
        },

        tourAnnotated: function () {
            this.controller.startAnnotationsTour();
        },

        annotateText: function (text) {
            this.controller.annotateText(text);
        },

        annotatePubl: function (pmid) {
            this.controller.annotatePublication(pmid);
        },

        staticHelp: function () {
            this.controller.showHelpDocs();
        },

        staticApi: function () {
            this.controller.showApiDocs();
        },

        staticWidget: function () {
            this.controller.showWidgetDocs();
        },

        staticAbout: function () {
            this.controller.showAboutDocs();
        },

        staticContact: function () {
            this.controller.showContactDocs();
        }
    });

    return Router;
});
