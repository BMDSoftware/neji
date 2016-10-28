//
// widget/views/controls.js
//   Widget highlighting controls widget view
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
    'modules/semantic-groups'

], function ($, _, _s, Backbone, app, SemanticGroups) {
    'use strict';

    var HighlightControlsView = Backbone.View.extend({
        template: 'controls',

        model: new Backbone.Model({
            'groups': [],
            'groups_map': {},
            'disabled_all': false
        }),

        events: {
            'click #annotation-toggles li button': 'toggleClass',
            'click #highlight-all-annotations': 'showAll',
            'click #hide-all-annotations': 'hideAll'
        },

        cleanup: function () {
            app.debug('HighlightControlsView.cleanup()', this);
            app.off(null, null, this);
            this.model.off(null, null, this);
        },

        initialize: function () {
            app.debug('HighlightControlsView.initialize()', this);

            this._initModelWithGroups();
            this.model.on('change', this.render, this);

            this._bindAppEventListeners();
        },

        _initModelWithGroups: function () {
            var groups = this.model.get('groups'),
                groupsMap = this.model.get('groups_map');

            _.each(SemanticGroups.groups, function (group) {
                group.active = true;
                group.disabled = false;

                groups.push(group);
                groupsMap[group.id] = group;
            });

            var ambiguous = {
                id: 'AMBIGUOUS',
                name: 'Ambiguous',
                class_: 'ambiguous',
                active: true,
                disabled: true
            };
            groups.push(ambiguous);
            groupsMap[ambiguous.id] = ambiguous;
        },

        _bindAppEventListeners: function () {
            app.on('concept-types:select-all', this.showAll, this);
            app.on('concept-types:select-none', this.hideAll, this);

            app.on('annotate-text:success annotate-publication:success', function (annotatedText) {
                this.updateVisibleControls(annotatedText);
            }, this);

            app.on('export-text:setup', function (options) {
                this.addSelectedGroupsAndTrigger('export-text:export', options);
            }, this);

            app.on('export-pub:setup', function (options) {
                this.addSelectedGroupsAndTrigger('export-pub:export', options);
            }, this);
        },

        addSelectedGroupsAndTrigger: function (eventName, options) {
            var groups = this._getAnnotationGroups(true);
            if ('AMBIGUOUS' in groups) {
                delete groups.AMBIGUOUS;
            }

            if (_.any(groups)) {
                app.trigger(eventName, _.extend({
                    groups: groups
                }, options));
            } else {
                _.defer(_.bind(function () {
                    app.trigger(eventName.split(':')[0] + ':error', 'no-groups', this.$('.annotation-toggles-container'));
                }, this));
                window.alert('Please select at least one concept type to export annotations.');
                app.trigger('warning:user-error', {cause: 'No Concept Groups Selected'});
            }
        },

        beforeRender: function () {
            app.debug('HighlightControlsView.beforeRender()', this);

            // Set toolbar "All" and "None" buttons status on model
            var groups = this._getEnabledAnnotationGroups();

            if (!this._isAmbiguousDisplayed()) {
                delete groups.AMBIGUOUS;
            }

            this.model.set({
                'toolbar_visible': _.keys(groups).length > 1,
                'toolbar_ALL': _.all(groups),
                'toolbar_NONE': !_.any(groups)
            },
                {silent: true});
        },

        afterRender: function () {
            app.debug('HighlightControlsView.afterRender()', this);

            var annotationClass;

            // Add annotation highlighting classes to body
            _.each(this._getAnnotationGroups(), function (active, group) {
                annotationClass = 'highlight-' + this.$('[data-group="' + group + '"]').next().attr('class');

                if (_s.contains(annotationClass, 'disabled')) {
                    return; // skip
                }

                if (active) {
                    $('body').addClass(annotationClass);
                } else {
                    $('body').removeClass(annotationClass);
                }
            }, this);
        },

        updateVisibleControls: function (annotatedText) {
            var conceptGroups = annotatedText.get('groups'),
                anySelected = _.any(conceptGroups),
                hasAmbiguous = annotatedText.get('hasAmbiguousConcepts');

            _.each(this.model.get('groups'), function (group) {
                if (group.id !== 'AMBIGUOUS') {
                    group.disabled = !(group.id in conceptGroups);
                }
            }, this);

            this.model.set('disabled_all', !anySelected, {silent: true});
            this._setAmbiguousCheckboxVisibility(hasAmbiguous);
        },

        toggleClass: function (ev) {
            var group = $(ev.currentTarget).data('group'),
                selected = $(ev.currentTarget).hasClass('active');
            this.model.get('groups_map')[group].active = !selected;
            this.model.trigger('change');
        },

        showAll: function () {
            var groups = this.model.get('groups');

            _.each(groups, function (group) {
                group.active = true;
            }, this);

            this.model.trigger('change');
        },

        hideAll: function () {
            var groups = this.model.get('groups');

            _.each(groups, function (group) {
                group.active = false;
            }, this);

            this.model.trigger('change');
        },

        _getAnnotationGroups: function (selected) {
            var groups = {};
            _.each(this.model.get('groups'), function (group) {
                if ((selected !== undefined ? (group.active === selected) : true)) {
                    groups[group.id] = group.active;
                }
            }, this);
            return groups;
        },

        _getEnabledAnnotationGroups: function (enabled) {
            var groups = {};
            enabled = enabled === undefined ? true : enabled;
            _.each(this.model.get('groups'), function (group) {
                if (group.disabled !== enabled) {
                    groups[group.id] = group.active;
                }
            }, this);
            return groups;
        },

        _isAmbiguousDisplayed: function () {
            return !(this.model.get('groups_map').AMBIGUOUS.disabled);
        },

        _setAmbiguousCheckboxVisibility: function (visible) {
            var groups = this.model.get('groups_map');
            groups.AMBIGUOUS.disabled = !visible;
            this.model.trigger('change');
        },

        serialize: function () {
            return this.model.toJSON();
        }
    });

    return HighlightControlsView;
});
