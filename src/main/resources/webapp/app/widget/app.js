//
// widget/app.js
//   Widget application object.
//   Acts as an event bus to facilitate communication between modules.
//
/*global define, window, document */

define([
    // Libraries.
    'jquery',
    'underscore',
    'underscore.string',
    'backbone',
    'handlebars',
    'modules/tracker',
    'bootstrap',

    // Plugins.
    'jquery.sortedEach',
    'backbone.layoutmanager'

], function ($, _, _s, Backbone, Handlebars, Tracker) {
    'use strict';

    // Provide a global location to place configuration settings.
    var app = {
        // Annotation service auth parameters
        name: 'becas-widget',
        email: 'bioinformatics@ua.pt',
        // Max input text size.
        maxinput: 50000,
        // The root path to run the widget.
        root: $('base').attr('href') || '/',
        // Logging level
        logLevel: Tracker.Level.DEBUG
    },

    // Localize or create a new JavaScript Template object.
        JST = window.JST = window.JST || {},
        JSTRuntimeCompilations = {},

    // Override Backbone event handling to log all events
        originalTrigger = Backbone.Events.trigger;

    Backbone.Events.trigger = function () {
        app.log('Event:', arguments);
        return originalTrigger.apply(this, arguments);
    };

    // Set logging level
    Tracker.setLevel(app.logLevel);

    // Configure LayoutManager with Backbone Boilerplate defaults.
    Backbone.LayoutManager.configure({
        // Allow LayoutManager to augment Backbone.View.prototype.
        manage: true,

        paths: {
            layout: 'app/widget/templates/layouts/',
            template: 'app/widget/templates/'
        },

        fetch: function (path) {
            var preCompiled = true;

            // Prefix path with templates prefix if it wasn't prefixed already.
            if (this.paths && !_s.startsWith(path, this.paths.template)) {
                path = this.paths.template + path;
            }

            // Concatenate the file extension.
            path = path + '.html';

            // If not cached, fetch the template synchronouly.
            if (!JST[path]) {
                $.ajax({ url: app.root + path, async: false }).then(function (contents) {
                    preCompiled = false;
                    JST[path] = Handlebars.compile(contents);
                    JSTRuntimeCompilations[path] = true;
                });
            }

            if (preCompiled && !JSTRuntimeCompilations[path]) {
                return Handlebars.template(JST[path]);
            } else {
                return JST[path];
            }
        }
    });

    // Mix Backbone.Events, modules, and layout management into the app object.
    return _.extend(app, {
        // Helper for using layouts.
        useLayout: function (name, options) {
            this.debug('app.useLayout(name, options)', name, options);

            // If already using this Layout, then don't re-inject into the DOM.
            if (this.layout && this.layout.options.template === name) {
                // If using this Layout with different views update only the views.
                if (options.views && !_.isEqual(this.layout.getViews(), options.views)) {
                    this.layout.setViews(options.views).render();
                }
                return this.layout;
            }

            // If a layout already exists, remove it from the DOM.
            if (this.layout) {
                this.debug('Removing layout from DOM', this.layout);
                this.layout.remove();
            }

            // Create a new Layout with options.
            var layout = new Backbone.Layout(_.extend({
                template: name,
                className: 'layout ' + name,
                id: 'layout',

                cleanup: function () {
                    app.debug('Layout.cleanup()', this);
                },

                initialize: function () {
                    app.debug('Layout.initialize()', this);
                },

                beforeRender: function () {
                    app.debug('Layout.beforeRender()', this);
                },

                afterRender: function () {
                    app.debug('Layout.afterRender()', this);
                }
            }, options));

            // Insert into the DOM.
            this.debug('Appending to DOM', layout.el);
            $(options.container || '#main').empty().append(layout.el);

            // Render the layout.
            this.debug('Rendering layout', name);
            layout.render();

            // Cache the refererence.
            this.layout = layout;

            // Return the reference, for chainability.
            return layout;
        },

        // Helper to POST data to the data export frame.
        postToExportFrame: function (url, postdata) {
            var frameName = '__export_frame',
                form = document.createElement('form');
            form.setAttribute('method', 'post');
            form.setAttribute('action', url);
            form.setAttribute('target', frameName);

            _.each(postdata, function (val, key) {
                var input = document.createElement('input');
                input.type = 'hidden';
                input.name = key;
                input.value = val;
                form.appendChild(input);
            });

            document.body.appendChild(form);
            form.submit();
            document.body.removeChild(form);
        },

        // Alert helper
        alert: function (msg) {
            this.trigger('alert:show', msg);
        },

        // Log helper
        log: function () {
            Tracker.log.apply(Tracker, arguments);
        },

        // Error logging helper
        error: function () {
            Tracker.error.apply(Tracker, arguments);
        },

        // debugging helper
        debug: function () {
            Tracker.debug.apply(Tracker, arguments);
        }

    }, Backbone.Events);
});
