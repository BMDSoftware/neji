//
// widget/views/home.js
//   Widget "home" view
//
/*global define */

define([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',

    // Application.
    'app'

], function ($, _, Backbone, app) {
    'use strict';

    var HomeView = Backbone.View.extend({
        template: 'home',

        model: new Backbone.Model(),

        cleanup: function () {
            app.debug('HomeView.cleanup()', this);
            app.off(null, null, this);
        },

        initialize: function () {
            app.debug('HomeView.initialize()', this);

            app.on('alert:show', function (msg) {
                this.model.set('error', msg);
                this.render();
            }, this);
        },

        beforeRender: function () {
            app.debug('HomeView.beforeRender()', this);
        },

        afterRender: function () {
            app.debug('HomeView.afterRender()', this);
        },

        serialize: function () {
            return this.model.toJSON();
        }
    });

    return HomeView;
});