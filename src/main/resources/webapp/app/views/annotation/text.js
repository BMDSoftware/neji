//
// view/annotation/text.js
//   Annotated text widget view
//
/*global define */

define([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',

    // Application.
    'app',

    // Models.
    'models/annotated-text'

], function ($, _, Backbone, app, AnnotatedText) {
    'use strict';

    var AnnotatedTextView = Backbone.View.extend({
        template: 'annotation/text',

        cleanup: function () {
            app.debug('AnnotatedTextView.cleanup()', this);
        },

        initialize: function () {
            app.debug('AnnotatedTextView.initialize()', this);
        },

        beforeRender: function () {
            app.debug('AnnotatedTextView.beforeRender()', this);
            var text = this.model.get('text'),
                entities = this.model.get('entities'),
                highlighted_text = AnnotatedText.highlight_text(text, entities);

            this.model.set('highlighted-text', highlighted_text);

            app.trigger('app:set-title', 'Annotated Text');
        },

        afterRender: function () {
            app.debug('AnnotatedTextView.afterRender()', this);
            app.trigger('result-view:rendered');
        },

        serialize: function () {
            return {
                text: this.model.get('highlighted-text')
            };
        }
    });

    return AnnotatedTextView;
});