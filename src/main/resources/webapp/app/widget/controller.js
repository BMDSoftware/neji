//
// widget/controller.js
//   Widget Controller.
//
/*global define, window, encodeURIComponent, decodeURIComponent, easyXDM */

define([
    // Libraries.
    'jquery',
    'underscore',
    'underscore.string',
    'backbone',

    // Application.
    'app',

    // Modules.
    'modules/annotation-service',
    'modules/tracker',

    // Views.
    'widget/views/home',
    'widget/views/controls',
    'widget/views/export',
    'widget/views/result',
    'views/annotation/text',
    'views/annotation/publication'

], function ($, _, _s, Backbone, app, AnnotationService, Tracker,
             HomeView, HighlightControlsView, ExportControlsView,
             AnnotationResultView, AnnotatedTextView, AnnotatedPublicationView) {
    'use strict';

    var Controller = function() {
        this.initialize.apply(this, arguments);
    };

    _.extend(Controller.prototype, {
        initialize: function (options) {
            app.log('Controller.initialize()', this);

            this.views = {
                '#main-content': new HomeView({cid: 'home-view'}),
                '#highlight-controls': new HighlightControlsView({cid: 'controls-view'}),
                '#export-controls': new ExportControlsView({cid: 'export-view'})
            };

            // Initialize global event listeners
            this.attachTo(app);

            // Initialize annotation services listeners
            AnnotationService.attachListeners(app);

            // Initialize event tracker listeners
            Tracker.attachListeners(app);

            // Initialize main layout
            this.layout = this._initMainLayout(this.views);

            // Track loading time
            var loadTime = new Date() - window._htmlLoad;
            Tracker.trackTiming('Widget', 'Load', loadTime, options.hostPage, 100);
        },

        attachTo: function (app) {
            // Respond to error message alerts
            app.on('alert:show', function (msg) {
                this._renderHomepage();
            }, this);

            // Respond to annotation requests
            app.on('annotate-text:annotate annotate-publication:annotate', function () {
                // Ensure the loading view is rendered
                this._renderHomepage();
            }, this);

            // Respond to annotation results
            app.on('annotate-text:success', this._renderAnnotatedText, this);
            app.on('annotate-publication:success', this._renderAnnotatedPublication, this);

            // Respond to errors
            app.on('annotate-text:error', function (err) {
                if (err === 'no-groups') {
                    // User made an error, ignore
                    return;
                }
                if (err.statusText === 'abort') {
                    // User aborted annotation, ignore
                    return;
                }

                app.error('Text Annotation error [readyState: ' + err.readyState + ', status: ' + err.status + ']', err);

                if (err.status === 413) {
                    app.alert('You provided too much text to annotate. Please try again with less text.');
                } else if (err.status === 503) {
                    app.alert('Sorry, the annotation service is not available at the moment. Please try again later.');
                } else {
                    app.alert('Sorry, the annotation service returned an error. Please try again.');
                }

                app.trigger('error:unexpected', {cause: 'Service Error (Annotate Text)', value: err.status});
            }, this);
            app.on('annotate-publication:error', function (err) {
                if (err === 'no-groups') {
                    // User made an error, ignore
                    return;
                }

                app.error('PMID Annotation error [readyState: ' + err.readyState + ', status: ' + err.status + ']', err);

                if (err.status === 404) {
                    app.alert('There is no PubMed publication with PMID #' + err.pmid + '. Please try again.');
                    app.trigger('warning:user-error', {cause: 'PMID not found in Pubmed', value: err.pmid});
                } else if (err.status === 503) {
                    app.alert('Sorry, the annotation service is not available at the moment. Please try again later.');
                } else {
                    app.alert('Sorry, the annotation service returned an error. Please try again.');
                }
            }, this);
        },

        annotateText: function (text, groups) {
            // Validate text
            if (!_.isString(text) || _s.trim(text).length === 0) {
                app.alert('"' + text + '" is not valid text. Please try again.');
                throw new Error('"' + text + '" is not valid text.');
            }
            app.trigger('annotate-text:annotate', {text: text, groups: groups});
        },

        annotatePublication: function (pmid, groups) {
            var clean_pmid = parseInt(pmid, 10);

            // Validate PMID
            if ('' + clean_pmid !== '' + pmid) {
                app.alert('"' + pmid + '" is not a valid PMID. Please try again.');
                throw new Error('"' + pmid + '" is not a valid PMID.');
            }
            app.trigger('annotate-publication:annotate', {pmid: pmid, groups: groups});
        },

        _initMainLayout: function (views) {
            return app.useLayout('widget', {
                container: 'body',

                views: views,

                afterRender: function () {
                    app.debug('Layout.afterRender()');
                }
            });
        },

        _renderHomepage: function () {
            var homeView = this.layout.getView(function (view) {
                return view.options.cid === 'home-view';
            });

            if (!homeView) {
                this.layout.setView('#main-content', this.views['#main-content']).render();
                $('#highlight-controls, footer').hide();
            }

            $('#page-container').show();
        },

        _renderAnnotatedPublication: function (annotatedPublication) {
            this._renderAnnotationResultView(new AnnotatedPublicationView({
                model: annotatedPublication
            }));
        },

        _renderAnnotatedText: function (annotatedText) {
            this._renderAnnotationResultView(new AnnotatedTextView({
                model: annotatedText
            }));
        },

        _renderAnnotationResultView: function (annotatedContentView) {
            $('#main-content').fadeOut(function () {
                app.layout.setView('#main-content', new AnnotationResultView({
                    model: annotatedContentView.model,

                    views: {
                        '.annotated-text': annotatedContentView
                    }
                })).render();
                $('#highlight-controls, footer').show();
                $(this).fadeIn();
            });

            app.trigger('result-view:render');
        }
    });

    return Controller;
});
