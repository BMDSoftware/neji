//
// views/annotation/publication.js
//   Annotated PubMed publication widget view
//
/*global define */

define([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',

    // Application.
    'app',

    // Application.
    'models/annotated-pub'

], function ($, _, Backbone, app, AnnotatedPublication) {
    'use strict';

    var AnnotatedPublicationView = Backbone.View.extend({
        template: 'annotation/publication',

        cleanup: function () {
            app.debug('AnnotatedPublicationView.cleanup()', this);
        },

        initialize: function () {
            app.debug('AnnotatedPublicationView.initialize()', this);
        },

        beforeRender: function () {
            app.debug('AnnotatedPublicationView.beforeRender()', this);

            var title = this.model.get('title'),
                abstract = this.model.get('abstract'),
                entities_title = this.model.get('entities_title'),
                entities_abstract = this.model.get('entities_abstract'),
                highlighted_title = AnnotatedPublication.highlight_text(title, entities_title),
                highlighted_abstract = null;

            if (abstract) {
                highlighted_abstract = AnnotatedPublication.highlight_text(abstract, entities_abstract);
            }

            this.model.set({
                'highlighted-title': highlighted_title,
                'highlighted-abstract': highlighted_abstract
            });

            app.trigger('app:set-title', title);
        },

        afterRender: function () {
            app.debug('AnnotatedPublicationView.afterRender()', this);
            app.trigger('result-view:rendered');
        },

        serialize: function () {
            return {
                pmid: this.model.get('pmid'),
                doi: this.model.get('doi'),
                title: this.model.get('highlighted-title'),
                abstract: this.model.get('highlighted-abstract'),
                authors: this.model.get('authors') ? this.model.get('authors').join(', ') : undefined,
                country: this.model.get('country'),
                journal: this.model.get('journal'),
                pubdate: this.model.get('pubdate'),
                pubtype: this.model.get('pubtype') ? this.model.get('pubtype').join(', ') : undefined
            };
        }
    });

    return AnnotatedPublicationView;
});