//
// views/annotation/result.js
//   Annotation result widget view
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
    'modules/tutorials',
    'modules/semantic-groups',

    // Models.
    'models/annotated-text',
    
    // Views.
    'views/annotation/tooltip'

], function ($, _, _s, Backbone, app, Tutorials, SemanticGroups, AnnotatedText, AnnotationTooltipView) {
    'use strict';

    var AnnotationResultView = Backbone.View.extend({
        template: 'annotation/result',

        views: {
            '.annotated-text': undefined  // must be passed on view instantiation
        },

        events: {
            'click .go-home': 'goHome',
            'click .export [data-format]': 'exportAnnotatedResult',
            
            // Tooltip events
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
        },

        beforeRender: function () {
            app.debug('AnnotationResultView.beforeRender()', this);
        },

        afterRender: function () {
            app.debug('AnnotationResultView.afterRender()', this);
            var entities = this.model.get('entities'),
                term_positions = this.model.get('termPositions'),
                entity_groups;

            if (!entities || entities.length === 0) {
                this.$('.export-btn').hide();
                return;
            }
        },

//        startTour: function () {
//            var tour = Tutorials.newAnnotatedTextTour();
//            tour.restart();
//            tour.start(true);
//        },

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
                    crlf: this.model.get('crlf'),
                    fromFile: this.model.get('fromFile')
                });
            }
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

            /*if (cids.length === 1 && cids[0] === ':::PRGE') {
                app.alert('No external references available.');
                app.trigger('warning:user-error', {cause: 'No external references available for concept', value: term});
                return;
            }*/

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

//        focusAnnotatedTerm: function (ev) {
//            var term_positions, term_elements, term_text,
//                $this = $(ev.currentTarget),
//                view = this;
//
//            term_positions = $.map($this.attr('data-term-positions').split(','), function (pos) {
//                return parseInt(pos, 10);
//            }).sort(function (a, b) { return a - b; });
//
//            term_elements = $.map(term_positions, function (pos) {
//                return '.at-' + pos;
//            });
//
//            term_text = $this.prev().text().replace(/ \( \d+ \)$/, '').toLowerCase();
//
//            // TODO: refactor this function out
//            function showCantHighlightIntersectedWarning(term_text) {
//                app.error('ConceptTree: Skipping focus on "' + term_text + '" due to intersected annotation.');
//                $.scrollTo(0);
//                app.alert('Can\'t focus "' + term_text + '" because it intersects other annotation(s).');
//                app.trigger('error:known-bug', {cause: 'Skipped intersected annotation focus' });
//            }
//
//            if ($(term_elements[0]).length === 0) {
//                showCantHighlightIntersectedWarning(term_text);
//                return;
//            }
//
//            $.scrollTo(term_elements[0], 100, {offset: -80, onAfter: function () {
//                _.each(term_elements, function (elClass) {
//                    $(elClass).each(function () {
//                        var $termEl = $(this);
//                        if (_s.include(term_text, $termEl.text().toLowerCase())) {
//                            // Mute all annotations
//                            var highlightClasses = _.filter($('body').attr('class').split(' '), function (cls) {
//                                return _s.startsWith(cls, 'highlight-');
//                            }).join(' ');
//                            $('.annotation-toggles-container, .concept-tree-container').addClass(highlightClasses);
//                            $('body').removeClass(highlightClasses);
//
//                            // Highlight selected terms
//                            $termEl.effect('highlight', {'color': '#ffff99'}, 2000, function () {
//                                // Re-highlight all annotations
//                                $('body').addClass(highlightClasses);
//                                $('.annotation-toggles-container, .concept-tree-container').removeClass(highlightClasses);
//                            });
//                        }
//                    });
//                });
//            }});
//
//            app.trigger('concept:focus', {
//                term: term_text
//            });
//        },

        goHome: function () {
            app.trigger('app:go-home');
        },

//        buildConceptTree: function (entity_groups, ids_dict, term_positions) {
//            // Build concept tree by semantic group
//            var tree = $('<ul/>'),
//                groupNode,
//                groupTree,
//                termNode,
//                termTree,
//                linkoutNode,
//                linkoutTree,
//                groupsOrder = {};
//
//            _.each(SemanticGroups.groups, function (group, i) {
//                groupsOrder[group.name] = i;
//            });
//
//            $.sortedEach(entity_groups, function (group_name, terms) {  // Group Node
//                groupNode = $('<li class="top-level"><i class="icon-folder-open"></i> <span>' + group_name + ' ( ' + _.keys(terms).length + ' )</span> <i class="semantic-group-collapser icon-minus-sign" title="Collapse all ' + group_name + ' nodes" style="display:none"></i> <i class="semantic-group-expander icon-plus-sign" title="Expand all ' + group_name + ' nodes"></i></li>');
//                groupTree = $('<ul/>');
//
//                $.sortedEach(terms, function (term, ids) {  // Term Node
//                    groupNode.addClass(AnnotatedText.annotation_class(ids[0]));
//
//                    termNode = $('<li><i class="icon-tag"></i> <span>' + term + ' ( ' + (_.keys(ids).length === 1 && ids[0] === ':::PRGE' ? '0' : _.keys(ids).length) + ' )</span> <i class="icon-screenshot focus-annotated-term" title="Find on text" data-term-positions="' + term_positions[term.toLowerCase()] + '"></i></li>');
//                    termTree = $('<ul/>');
//
//                    // Sort IDs by preferred name
//                    var ids_by_name = {},
//                        pref_name;
//                    $.each(ids, function (j, id) {
//                        if (id !== ':::PRGE') {
//                            if (!ids_dict[id]) {
//                                app.error('ConceptTree: No preferred name or external references available for "' + id + '"');
//                                app.trigger('error:unexpected', {cause: 'Concept not found (' + id + ')' });
//                                return;
//                            }
//                            pref_name = ids_dict[id].name;
//                            if (ids_by_name.hasOwnProperty(pref_name)) {
//                                app.debug('ConceptTree: Overwriting EID "' + id + '" for term "' + ids_dict[id].name + '"');
//                            }
//
//                            ids_by_name[pref_name] = id;
//                        }
//                    });
//
//                    $.sortedEach(ids_by_name, function (pref_name, id) {  // Preferred name Node
//                        linkoutNode = $('<li><i class="icon-book"></i> <span class="preferred-term" title="' + id + '">' + ids_dict[id].name + ' ( ' + _.keys(ids_dict[id].refs).length + ' )</span></li>');
//                        linkoutTree = $('<ul/>');
//
//                        ids_dict[id].refs.sort();
//                        $.each(ids_dict[id].refs, function (k, ref) {  // Linkout Node
//                            linkoutTree.append($('<li><i class="icon-globe"></i> <a href="' + AnnotatedText.ref_to_link(ref) + '" target="_blank" data-conceptref="Tree" data-bypass>' + ref + '</a></li>'));
//                        });
//
//                        linkoutNode.append(linkoutTree);
//                        termTree.append(linkoutNode);
//                    });
//
//                    termNode.append(termTree);
//                    groupTree.append(termNode);
//                }, function (t1, t2) {  // sort term names in a case-insensitive way
//                    return t1.toLowerCase() > t2.toLowerCase() ? 1 : -1;
//                });
//
//                groupNode.append(groupTree);
//                tree.append(groupNode);
//            }, function (a, b) {  // Group names comparator
//                return groupsOrder[a] - groupsOrder[b];
//            });
//
//            this.$('.concept-tree')
//                .append(tree)
//                .treeview({
//                    'collapsed': true,
//                    'animated': 'fast',
//                    'control': '.tree-control',
//                    'toggle': toggle_tree_item
//                });
//        },

        serialize: function () {
            return {
                pmid: this.model.get('pmid'),
                conceptCount: this.model.get('entities').length,
                duration: this._formatTime(this.model.get('duration'))
            };
        },

        _formatTime: function (millis) {
            return ((millis / 1000) % 60) + 's';
        }
    });

    return AnnotationResultView;
});