//
// models/annotated-pub.js
//   Annotated pubmed publication model
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

    var AnnotatedPublication = AnnotatedText.extend({
        // pmid, title, abstract,
        // authors, pubdate, journal, pubtype, country,
        // title_entities, abstract_entities, ids, duration

        initialize: function (attributes) {
            var entities;

            if (attributes.entities_abstract) {
                entities = _.union(attributes.entities_title, attributes.entities_abstract);
            } else {
                entities = attributes.entities_title;
            }

            this.set('entities', entities, {silent: true});

            // Call "super"
            AnnotatedText.prototype.initialize.apply(this, arguments);
        }
    });

    return AnnotatedPublication;
});