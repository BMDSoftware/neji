//
// views/annotation/tooltip.js
//   Annotated term tooltip view
//
/*global define, window */

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

    var AnnotationTooltipView = Backbone.View.extend({
        template: 'annotation/tooltip',

        cleanup: function () {
            app.debug('AnnotatedTooltipView.cleanup()', this);
            app.off(null, null, this);
        },

        initialize: function () {
            app.debug('AnnotatedTooltipView.initialize()', this);
            // { term, ids, ids_dict, term_positions }
            app.on('concept-tips:hide-all', this.hideAnnotationTooltip, this);
        },

        beforeRender: function () {                        
            app.debug('AnnotatedTooltipView.beforeRender()', this);            
            
            var model = this.options,
                // Build the tree and prepare model data for rendering
                //treeData = this.buildTree(model.term, model.ids, model.ids_dict),
                position = this.calcPosition(model.target);
            var classes = AnnotatedText.annotation_class(model.ids);
            
            // Build a concept map with class, ids, color and normalization
            var concepts = AnnotatedText.annotation_concepts(model.ids);
            
            var style = {
                    classes: classes,
                    color: AnnotatedText.annotation_color(classes),
                    concepts: concepts,
                    mirrored: position.mirrored,
                    posTop: position.top,
                    posLeft: position.left
                };
                
            this.options = _.extend(model, style);
        },

        afterRender: function () {
            app.debug('AnnotatedTooltipView.afterRender()', this);
            // Remove the surrounding <div>
            this.setElement(this.$el.children());

            // Bind close events
            var hideTip = _.bind(this.hideAnnotationTooltip, this, this.$el);
            this.$el.on('mouseleave', function () { _.delay(hideTip, 100); });
            this.$el.find('.close').on('click', hideTip);

            // Show the tooltip
            this.$el.appendTo('body');
            var detailsHeight = this.$('#concepts').height();
            this.$el.animate({
                width: this.options.width,
                height: 60 + detailsHeight
            }, 300, null, function () {
                app.trigger('concept:tooltip-open');
            });
            this.$('.concept-tooltip').animate({
                height: 35 + detailsHeight
            }, 300);
        },

        hideAnnotationTooltip: function (target) {
            if (this.options.target[0] === target) {
                return;  // do not hide current tip
            }

            this.$el.animate({width: 0, height: 0}, 300, _.bind(function () {
                this.$el.remove();
                this.undelegateEvents();
                this.cleanup();
                app.trigger('concept:tooltip-close');
            }, this));
        },

        calcPosition: function (target) {
            var targetPosition = target.position(),
                viewportWidth = $(window).width(),
                position = {
                    top: targetPosition.top - 12,
                    left: targetPosition.left - 11,
                    mirrored: false
                };

            if (targetPosition.left + this.options.width > viewportWidth &&
                viewportWidth > this.options.width) {
                position.mirrored = true;
                position.left -= (this.options.width - target.width() - 30);
            }

            return position;
        },

        /*buildTree: function (term, ids, ids_dict) {
            var entity_groups = AnnotatedText.concepts_by_group(ids),
                tree = $('<ul class="concept-group" />'),
                groupNode,
                groupTree,
                linkoutNode,
                linkoutTree,
                conceptCount = 0,
                linkoutCount = 0;

            $.sortedEach(entity_groups, function (group_name, gids) {  // Group Node
                groupNode = $('<li class="' + AnnotatedText.annotation_class(gids[0]) + '"><span>' + group_name + ' ( ' + _.keys(gids).length + ' )</span></li>');
                groupTree = $('<ul/>');

                // Sort IDs by preferred name
                var ids_by_name = {},
                    pref_name;
                $.each(gids, function (j, id) {
                    if (id !== ':::PRGE') {
                        if (!ids_dict[id]) {
                            app.error('AnnotationTooltip: No preferred name or external references available for "' + id + '"');
                            app.trigger('error:unexpected', {cause: 'Concept not found (' + id + ')' });
                            return;
                        }
                        pref_name = ids_dict[id].name;
                        if (ids_by_name.hasOwnProperty(pref_name)) {
                            app.debug('AnnotationTooltip: Overwriting EID "' + id + '" for term "' + ids_dict[id].name + '"');
                        }

                        ids_by_name[pref_name] = id;
                    }
                });

                $.sortedEach(ids_by_name, function (pref_name, id) {  // Preferred Name Node
                    conceptCount += 1;
                    linkoutNode = $('<li><i class="icon-book"></i> <span class="preferred-term" title="' + id + '">' + ids_dict[id].name + ' ( ' + _.keys(ids_dict[id].refs).length + ' )</span></li>');
                    linkoutTree = $('<ul/>');

                    ids_dict[id].refs.sort();
                    $.sortedEach(ids_dict[id].refs, function (j, ref) {  // Linkout Node
                        linkoutCount += 1;
                        linkoutTree.append($('<li class="linkout-node"><i class="icon-globe"></i> <a href="' + AnnotatedText.ref_to_link(ref) + '" target="_blank" data-conceptref="Dialog" data-bypass>' + ref + '</a></li>'));

                        linkoutNode.append(linkoutTree);
                        groupTree.append(linkoutNode);
                    });
                }, function (t1, t2) {  // sort term names in a case-insensitive way
                    return t1.toLowerCase() > t2.toLowerCase() ? 1 : -1;
                });

                groupNode.append(groupTree);
                tree.append(groupNode);
            });

            return {
                'tree': tree,
                'conceptCount': conceptCount,
                'linkoutCount': linkoutCount
            };
        },*/

        serialize: function () {
            var model = this.options;
        
            return {
                AnnotatedText: model.term,
                OccurrenceCount: model.term_positions[model.term.toLowerCase()].length,
                os: model.term_positions[model.term.toLowerCase()].length  > 1 ? 's' : '',
                ConceptCount: model.conceptCount,
                LinkoutCount: model.linkoutCount,
                cs: model.conceptCount > 1 ? 's' : '',
                ls: model.linkoutCount > 1 ? 's' : '',
                Concepts: $('<div>').append(model.tree).html(),
                mirrored: model.mirrored,
                posTop: model.posTop,
                posLeft: model.posLeft,
                classes: model.classes,
                color: model.color,
                ids: model.ids,
                concepts: model.concepts
            };
        }

    });

    return AnnotationTooltipView;
});