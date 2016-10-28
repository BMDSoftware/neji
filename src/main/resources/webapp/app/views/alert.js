//
// views/alert.js
//   Alert widget view
//
/*global define, clearTimeout */

define([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',

    // Application.
    'app'

], function ($, _, Backbone, app) {
    'use strict';

    var AlertView = Backbone.View.extend({
        template: 'alert',

        model: new (Backbone.Model.extend())({}),

        events: {
            'click .close': 'hide'
        },

        cleanup: function () {
            app.debug('AlertView.cleanup()', this);
        },

        initialize: function () {
            app.debug('AlertView.initialize()', this);
        },

        beforeRender: function () {
            app.debug('AlertView.beforeRender()', this);
        },

        afterRender: function () {
            app.debug('AlertView.afterRender()', this);
        },

        show: function (options) {
            app.debug('AlertView.show()', options);

            clearTimeout(this.autoCloser);
            this.model.clear().set(options);
            this.render();
            $('#alerts-container').slideDown(_.bind(function () {
                this.autoCloser = _.delay(this.hide, 5000, this);
            }, this));
        },

        hide: function (view) {
            app.debug('AlertView.hide()', view);

            var this_ = view || this;
            clearTimeout(this_.autoCloser);
            $('#alerts-container').slideUp();
        },

        serialize: function () {
            return this.model.toJSON();
        }
    });

    return AlertView;
});