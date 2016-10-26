//
// widget/views/result.js
//   Widget annotation result widget view
//
/*global define, document, setTimeout, clearTimeout */

define([
    // Libraries.
    'jquery',
    'underscore',
    'underscore.string',
    'backbone',

    // Application.
    'app',

    // Modules.
    'modules/semantic-groups',

    // Views.
    'views/annotation/tooltip'

], function ($, _, _s, Backbone, app, SemanticGroups, AnnotationTooltipView) {
    'use strict';

    var AnnotationResultView = Backbone.View.extend({
        template: 'result',

        views: {
            '.annotated-text': undefined  // must be passed on view instantiation
        },

        events: {
            'mouseenter .annotated-text [data-concept-ids]': 'prepareAnnotationTooltip',
            'mouseleave .annotated-text [data-concept-ids]': 'forgetAnnotationTooltip',
            'click .annotated-text [data-concept-ids]': 'showImmediatelyAnnotationTooltip'
        },

        cleanup: function () {
            app.debug('AnnotationResultView.cleanup()', this);
            this.off(null, null, this);
            app.off(null, null, this);
            $(document).off('.annotationTips');
        },

        initialize: function () {
            app.debug('AnnotationResultView.initialize()', this);
            // Hide tooltips when clicking out of tip in touch devices
            $(document).on('click.annotationTips', ':not(.concept-tooltip)',
                _.bind(this.forgetAnnotationTooltip, this));
        },

        beforeRender: function () {
            app.debug('AnnotationResultView.beforeRender()', this);
        },

        afterRender: function () {
            app.debug('AnnotationResultView.afterRender()', this);
        },

        prepareAnnotationTooltip: function (ev) {
            ev.stopPropagation();

            // Prevent showing of tips on parent annotations
            $(ev.currentTarget).parent().trigger('mouseleave', ev);

            // Show tip after user hovers on annotation for some time
            var timeoutId = setTimeout(_.bind(function () {
                    this.showAnnotationTooltip(ev);
                }, this), 600);

            $(ev.currentTarget).attr('data-hoverTimeoutId', timeoutId);
        },

        forgetAnnotationTooltip: function (ev) {
            ev.stopPropagation();

            // Hide any visible tips
            app.trigger('concept-tips:hide-all', ev.currentTarget);

            // Cancel showing of scheduled tip
            var timeoutId = $(ev.currentTarget).attr('data-hoverTimeoutId');
            clearTimeout(timeoutId);

            // Cleanup timeout ID from DOM
            $(ev.currentTarget).removeAttr('data-hoverTimeoutId');
        },

        showImmediatelyAnnotationTooltip: function (ev) {
            this.forgetAnnotationTooltip(ev);
            this.showAnnotationTooltip(ev);
        },

        showAnnotationTooltip: function (ev) {
            ev.stopPropagation();
            ev.preventDefault();

            var $this = $(ev.currentTarget),
                term = $this.attr('data-term') || $this.text(),
                cids = $this.attr('data-concept-ids').split(';'),
                annotationTooltipView;

            if (cids.length === 1 && cids[0] === ':::PRGE') {
                app.alert('No external references available.');
                app.trigger('warning:user-error', {cause: 'No external references available for concept', value: term});
                return;
            }

            annotationTooltipView = new AnnotationTooltipView({
                'target': $this,
                'term': term,
                'ids': cids,
                'ids_dict': this.model.get('ids'),
                'term_positions': this.model.get('termPositions')
            });

            annotationTooltipView.render();

            app.trigger('concept:view', {
                term: term,
                ids: cids
            });
        },

        serialize: function () {
            return {
                pmid: this.model.get('pmid')
            };
        }
    });

    return AnnotationResultView;
});