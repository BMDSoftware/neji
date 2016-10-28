//
// views/static.js
//   Static page view
//
/*global define, window */

define([
    // Libraries.
    'jquery',
    'underscore',
    'underscore.string',
    'backbone',

    // Application.
    'app',

    // Modules.
    'modules/syntax-highlight'

], function ($, _, _s, Backbone, app, Highlighter) {
    'use strict';

    var StaticView = Backbone.View.extend({
        // Template is set by router

        cleanup: function () {
            app.debug('StaticView["' + this._getTemplateName() + '"].cleanup()', this);
        },

        initialize: function () {
            app.debug('StaticView["' + this._getTemplateName() + '"].initialize()', this);
        },

        beforeRender: function () {
            app.debug('StaticView["' + this._getTemplateName() + '"].beforeRender()', this);

            var title = this._getTemplateName();
            if (title === 'api') {
                title = 'API';
            } else {
                title = _s.capitalize(this._getTemplateName());
            }
            app.trigger('app:set-title', title);
        },

        afterRender: function () {
            app.debug('StaticView["' + this._getTemplateName() + '"].afterRender()', this);

            // Setup scrollspy for the sidebar
            $('body').scrollspy({
                target: '.docs-sidebar',
                offset: 10
            });

            // Use animated scrolling instead of native
            this.$('ul.docs-sidenav li a').bind('click', function (e) {
                e.preventDefault();
                $.scrollTo(this.hash, 1000);
            });

            // Scroll to specific section if needed
            if (_s.contains(window.location.hash, '__')) {
                $.scrollTo(window.location.hash);
            } else {  // or scroll to top
                $.scrollTo(0);
            }

            // Prettify code snippets
            this.$('.prettyprint').each(function () {
                $(this).html(Highlighter.highlight($(this).text()));
            });
        },

        _getTemplateName: function () {
            return _s.words(this.options.template, '/')[1];
        },

        serialize: function () {
            return {
                'baseUrl': app.baseUrl,
                'protocolRelativebaseUrl': app.protocolRelativebaseUrl,
                'authSuffix': '?email=$EMAIL&tool=$TOOL'
            };
        }
    });

    return StaticView;
});