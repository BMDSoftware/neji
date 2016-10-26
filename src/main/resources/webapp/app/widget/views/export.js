//
// widget/views/export.js
//   Widget export controls view
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

    var ExportView = Backbone.View.extend({
        template: 'export',

        model: new Backbone.Model({
            pmid: undefined,
            entities: []
        }),

        events: {
            'click a[data-format]': 'exportAnnotatedResult'
        },

        cleanup: function () {
            app.debug('ExportView.cleanup()', this);
            app.off(null, null, this);
        },

        initialize: function () {
            app.debug('ExportView.initialize()', this);

            app.on('annotate-text:success annotate-publication:success', function (annotatedModel) {
                this.model = annotatedModel;
                this.render();
            }, this);
        },

        beforeRender: function () {
            app.debug('ExportView.beforeRender()', this);
        },

        afterRender: function () {
            app.debug('ExportView.afterRender()', this);
        },

        exportAnnotatedResult: function (ev) {
            ev.preventDefault();
            this.$('[data-toggle="dropdown"]').parent().removeClass('open');

            var format = $(ev.currentTarget).data('format'),
                pmid = this.model.get('pmid'),
                text = this.model.get('text');

            if (pmid) {
                app.trigger('export-pub:setup', {
                    pmid: pmid,
                    format: format
                });
            } else {
                app.trigger('export-text:setup', {
                    text: text,
                    format: format,
                    fromFile: this.model.get('fromFile')
                });
            }
        },

        serialize: function () {
            return {
                hasConcepts: this.model.get('entities'),
                pmid: this.model.get('pmid')
            };
        }
    });

    return ExportView;
});