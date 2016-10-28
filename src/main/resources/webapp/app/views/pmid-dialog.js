//
// views/pmid-dialog.js
//   PMID request modal dialog view
//
/*global define */

define([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',

    // Application.
    'app',

    // Modules.
    'modules/text-samples'

], function ($, _, Backbone, app, Samples) {
    'use strict';

    var PmidDialogModalView = Backbone.View.extend({
        template: 'pmid-dialog',

        events: {
            'click #annotate-pmid': 'annotatePmid',
            'submit form': 'annotatePmid'
        },

        cleanup: function () {
            app.debug('PmidDialogModalView.cleanup()', this);
        },

        initialize: function () {
            app.debug('PmidDialogModalView.initialize()', this);
        },

        beforeRender: function () {
            app.debug('PmidDialogModalView.beforeRender()', this);
        },

        afterRender: function () {
            app.debug('PmidDialogModalView.afterRender()', this);
            // Remove the surrounding <div>
            this.setElement(this.$el.children());

            // Show the modal dialog
            $('body').append(this.$el);
            this.$el.modal();

            // Focus PMID field when shown
            this.$el.on('shown', _.bind(function () {
                this.$('#pmid').focus();
            }, this));

            // Destroy the modal dialog when hidden
            this.$el.on('hidden', _.bind(function () {
                this.$el.remove();
                this.undelegateEvents();
                this.cleanup();
            }, this));
        },

        annotatePmid: function (ev) {
            ev.stopPropagation();
            ev.preventDefault();

            var userInput = this.$('#pmid').val(),
                pmid = parseInt(userInput, 10);

            if (userInput === '') {
                this.$('#pmid').attr('placeholder', 'Enter a PMID');
                this.$('#pmid').focus();
                return;
            }

            this.$el.modal('hide');

            if (_.isFinite(pmid) && pmid > 0 && userInput === '' + pmid) {
                Backbone.history.navigate('!/pmid/' + pmid, {trigger: true});
            } else {
                app.alert({title: 'Invalid PMID:', msg: '"' + userInput + '" is not a valid PMID. Please try again.'});
                app.trigger('warning:user-error', {cause: 'Invalid PMID', value: pmid});
            }
        },

        serialize: function () {
            return {
                pmids: Samples.pmids
            };
        }
    });

    return PmidDialogModalView;
});