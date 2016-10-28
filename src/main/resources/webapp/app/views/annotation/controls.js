//
// views/annotation/controls.js
//   Highlighting controls widget view
//
/*global define */

var globalGroups = {};

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
        template: 'annotation/controls',

        model: new Backbone.Model({
            'groups': [],
            'groups_map': {},
            'disabled_all': false,
            'title': 'Annotate',
            'starttour_visible': true
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
            this.model.on('change', this._notifyModelChange, this);

            this._bindAppEventListeners();
        },

        _initModelWithGroups: function () {
            var groups = this.model.get('groups'),
                groupsMap = this.model.get('groups_map');
        
            // Get service groups
            var color = 0;
            _.each(getGroups(), function (normalizedName, groupName) {

                var group = {
                    id: groupName,
                    name: groupName,
                    class_: groupName,
                    color: "color"+(color%11),
                    regex: new RegExp(":"+groupName+"$"),
                    norm: normalizedName,
                    active: true,
                    disabled: true
                };
                
                groups.push(group);
                groupsMap[group.id] = group;
                globalGroups[group.id] = group;
                color++;
            });
            
            // Set service semantic groups
            SemanticGroups.groups = jQuery.extend(true, {}, groups);
            
            var ambiguous = {
                id: 'AMBIGUOUS',
                name: 'Ambiguous',
                class_: 'ambiguous',
                color: 'ambiguous',
                norm: 'Ambiguous',
                active: true,
                disabled: true
            };
            groups.push(ambiguous);
            groupsMap[ambiguous.id] = ambiguous;
            
            globalGroups[ambiguous.id] = ambiguous;
            
        },

        _bindAppEventListeners: function () {
            app.on('concept-types:select-all', this.showAll, this);
            app.on('concept-types:select-none', this.hideAll, this);

            app.on('annotate-text:setup', function (options) {
                this.addSelectedGroupsAndTrigger('annotate-text:annotate', options);
            }, this);

            app.on('annotate-publication:setup', function (options) {
                this.addSelectedGroupsAndTrigger('annotate-publication:annotate', options);
            }, this);

            app.on('annotate-text:success annotate-publication:success', function (annotatedText) {
                this.updateVisibleControls(annotatedText);
            }, this);

            app.on('result-view:render', function () {
                this.setTitle('Highlight');
            }, this);

            app.on('export-text:setup', function (options) {
                this.addSelectedGroupsAndTrigger('export-text:export', options);
            }, this);

            app.on('export-pub:setup', function (options) {
                this.addSelectedGroupsAndTrigger('export-pub:export', options);
            }, this);

            app.on('homepage:reset', function () {
                this.setTitle('Annotate');
                this.enableDisabledAnnotationGroups();
                if (this._isAmbiguousDisplayed()) {
                    this._setAmbiguousCheckboxVisibility(false);
                }
            }, this);

            app.on('home-tour:started result-tour:started', function () {
                this.model.set('starttour_visible', false);
            }, this);
        },

        _notifyModelChange: function () {
            var groups = this._getAnnotationGroups(true);
            if ('AMBIGUOUS' in groups) {
                delete groups.AMBIGUOUS;
            }

            app.trigger('semantic-groups:change', groups);
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
                app.alert({msg: 'Please select at least one concept type.', icon: 'icon-arrow-left'});
                app.trigger('warning:user-error', {cause: 'No Concept Groups Selected'});
            }
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

        disableUnselectedAnnotationGroups: function () {
            _.each(this.model.get('groups'), function (group) {
                if (!group.active) {
                    group.disabled = true;
                }
            }, this);

            this.model.trigger('change');
        },

        enableDisabledAnnotationGroups: function () {
            _.each(this.model.get('groups'), function (group) {
                if (group.id !== 'AMBIGUOUS') {
                    group.disabled = false;
                }
            }, this);

            this.model.set('disabled_all', false, {silent: true});
            this.model.trigger('change');
        },

        toggleClass: function (ev) {
            var group = $(ev.currentTarget).data('group'),
                selected = $(ev.currentTarget).hasClass('active');
            this.model.get('groups_map')[group].active = !selected;
            this.model.trigger('change');
            updateAnnotations(group, !selected);
        },

        showAll: function () {
            var groups = this.model.get('groups');

            _.each(groups, function (group) {
                group.active = true;
            }, this);            
            
            this.model.trigger('change');
            updateAllAnnotations(true);
        },

        hideAll: function () {
            var groups = this.model.get('groups');

            _.each(groups, function (group) {
                group.active = false;
            }, this);
                        
            this.model.trigger('change');
            updateAllAnnotations(false);
        },

        setTitle: function (title) {
            if (title !== this.$('#highlight-controls-title').text()) {
                _.defer(function (el) {
                    el.$('#highlight-controls-title').fadeOut(_.bind(function () {
                        this.$('#highlight-controls-title').text(title).fadeIn();
                        this.model.set('title', title);
                    }, el));
                }, this);
            }
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

function getGroups() {
    
    // Get service name
    var url = window.location.href;
    var regex = /https:\/\/[\w\:\.\-\d]+\/annotate\/([^\/#!]+)/g;
    var service = regex.exec(url)[1];

    // Get groups
    var groups = {};
    
    url = window.location.origin + '/services/getGroups/name=' + service;
    $.ajax({
        url: url,
        type: 'GET',
        contentType: "application/json; charSet=UTF-8",
        dataType: 'json',
        async: false,
        success: function (data) {
            groups = data;
        },
        error: function (xhr, status, error) {
            alert("Error: " + error);
        }
    });
    
    return groups; 
}

function updateAllAnnotations(active) {        
    for (var group in globalGroups) {
        updateAnnotations(group, active);
    }
}

function updateAnnotations(group, active) {
    var annotations = document.getElementsByClassName(group);
    var annotation;
    
    for (i=0 ; i<annotations.length ; i++) {
        
        annotation = annotations[i];
        
        if (!active) {
            annotation.className = annotation.className.replace(/color[0-9]+/g, "");
            annotation.setAttribute("data-concept-id", annotation.getAttribute("data-concept-ids"));
            annotation.removeAttribute("data-concept-ids");
        } else {
            annotation.className += globalGroups[group].color;
            annotation.setAttribute("data-concept-ids", annotation.getAttribute("data-concept-id"));
            annotation.removeAttribute("data-concept-id");
        }
    }
}