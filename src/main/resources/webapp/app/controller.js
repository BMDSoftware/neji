//
// controller.js
//   Application Controller.
//
/*global define, window, encodeURIComponent, decodeURIComponent */

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
    'modules/text-samples',

    // Views.
    'views/alert',
    'views/home',
    'views/static',
    'views/annotation/controls',
    'views/annotation/result',
    'views/annotation/text',
    'views/annotation/publication'

], function ($, _, _s, Backbone, app, AnnotationService, Tracker, TextSamples,
             AlertView, HomeView, StaticView, HighlightControlsView,
             AnnotationResultView, AnnotatedTextView, AnnotatedPublicationView) {
    'use strict';

    var Controller = function() {
        this.initialize.apply(this, arguments);
    };

    _.extend(Controller.prototype, {
        initialize: function () {
            app.log('Controller.initialize()', this);

            this.views = {
                '#alerts-container': new AlertView({cid: 'alert-view'}),
                '#main-content': new HomeView({cid: 'home-view'}),
                '#sidebar': new HighlightControlsView({cid: 'controls-view'})
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
            Tracker.trackTiming('Application', 'Load', loadTime, window.location.href, 100);
        },

        attachTo: function (app) {
            // Respond to alert events
            app.on('alert:show', function (options) {
                app.log('Alert', options);
                this.views['#alerts-container'].show(options);
            }, this);
            app.on('alert:hide', function (view) {
                this.views['#alerts-container'].hide(view);
            }, this);

            // Respond to window title change events
            app.on('app:set-title', app.setTitle, this);

            // Respond to annotation requests
            app.on('annotate-text:setup annotate-publication:setup', function () {
                // Ensure the home view is rendered
                this._renderHomepage(true);
            }, this);

            // Respond to annotation results
            app.on('annotate-text:success', function (annotatedText) {
                this._renderAnnotatedText(annotatedText);
            }, this);
            app.on('annotate-publication:success',function (annotatedPublication) {
                this._renderAnnotatedPublication(annotatedPublication);
            }, this);

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
                app.trigger('app:go-home');

                if (err.status === 413) {
                    app.alert('You provided too much text to annotate. Please try again with less text.');
                    app.trigger('warning:user-error', {cause: 'Too much text'});
                } else if (err.status === 503) {
                    app.alert('Sorry, the annotation service is not available at the moment. Check internet connectivity and try again.');
                    app.trigger('error:unexpected', {cause: 'Service Unavailable (Annotate Text)', value: err.status});
                } else {
                    app.alert('Sorry, an unexpected error prevented your request. Please try again.');
                    app.trigger('error:unexpected', {cause: 'Service Error (Annotate Text)', value: err.status});
                }
            }, this);
            app.on('annotate-publication:error', function (err) {
                if (err === 'no-groups') {
                    // User made an error, ignore
                    return;
                }

                app.error('PMID Annotation error [readyState: ' + err.readyState + ', status: ' + err.status + ']', err);
                app.trigger('app:go-home');

                if (err.status === 404) {
                    app.alert({title: 'Not found:', msg: 'There is no PubMed publication with PMID #' + err.pmid + '. Please try again.'});
                    app.trigger('warning:user-error', {cause: 'PMID not found in Pubmed', value: err.pmid});
                } else if (err.status === 503) {
                    app.alert('Sorry, the annotation service is not available at the moment. Check internet connectivity and try again.');
                    app.trigger('error:unexpected', {cause: 'Service Unavailable (Annotate Pub)', value: err.status});
                } else {
                    app.alert('Sorry, an unexpected error prevented your request. Please try again.');
                    app.trigger('error:unexpected', {cause: 'Service Error (Annotate Pub)', value: err.status});
                }
            }, this);
        },

        showHomepage: function () {
            this._renderHomepage();
            app.trigger('homepage:reset');
            app.trigger('annotation:abort');
            app.trigger('tour:end');
            app.trigger('concept-tips:hide-all');
        },

        startHomeTour: function () {
            this.showHomepage();
            _.defer(function () {
                app.trigger('home-tour:start');
            });
        },

        startAnnotationsTour: function () {
            app.trigger('tour:end');
            var sample = encodeURIComponent(TextSamples.samples[0]);
            this.annotateText(sample, true);
        },

        annotateText: function (text, inTour) {
            app.trigger('annotate-text:setup', {
                text: decodeURIComponent(text.replace(/\+/g, '%20')),
                crlf: _s.include(text, '\r\n'),
                fromFile: false,
                inTour: inTour || false
            });
        },

        annotatePublication: function (pmid) {
            var clean_pmid = parseInt(pmid, 10);

            // Validate PMID
            if ('' + clean_pmid !== pmid) {
                app.alert({title: 'Invalid PMID:', msg: '"' + pmid + '" is not a valid PMID. Please try again.'});
                app.trigger('app:go-home');
                app.trigger('warning:user-error', {cause: 'Invalid PMID', value: pmid});
                return;
            }

            app.trigger('annotate-publication:setup', {pmid: pmid});
        },

        showHelpDocs: function () {
            this._renderStatic('help');
        },

        showApiDocs: function () {
            this._renderStatic('api');
        },

        showWidgetDocs: function () {
            this._renderStatic('widget');
        },

        showAboutDocs: function () {
            this._renderStatic('about');
        },

        showContactDocs: function () {
            this._renderStatic('contact');
        },

        _initMainLayout: function (views) {
            return app.useLayout('main', {
                container: 'body',

                views: views,

                afterRender: function () {
                    app.debug('Layout.afterRender()');
                    $('#page-container').show();
                }
            });
        },

        _renderStatic: function (page) {
            app.trigger('annotation:abort');
            app.trigger('tour:end');
            app.trigger('concept-tips:hide-all');

            this._switchToStaticView(page);
            app.trigger('alert:hide');

            app.layout.setView('#main-content', new StaticView({
                template: 'static/' + page
            })).render();

            app.trigger('static:render', {page: page});
        },

        _renderHomepage: function (loading) {
            app.trigger('tour:end');
            this._switchToAppView();

            var homeView = this.layout.getView(function (view) {
                return view.options.cid === 'home-view';
            });

            if (!homeView) {
                this.layout.setView('#main-content', this.views['#main-content']).render();
                this.views['#main-content'].setLoading(loading);
            }
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
            this._switchToAppView();

            $('#main-content').fadeOut(function () {
                app.layout.setView('#main-content', new AnnotationResultView({
                    model: annotatedContentView.model,

                    views: {
                        '.annotated-text': annotatedContentView
                    }
                })).render();
                $(this).fadeIn();
            });

            app.trigger('result-view:render');
        },

        _setNavItemActive: function (page) {
            $('li[data-navitem]').removeClass('active');
            $('li[data-navitem="' + page + '"]').addClass('active');
        },

        _switchToAppView: function () {
            this._setNavItemActive('app');
            $('[role="main"]').removeClass('span11').addClass('span10');
            $('#sidebar').show();
        },

        _switchToStaticView: function (page) {
            this._setNavItemActive(page);
            $('#sidebar').hide();
            $('[role="main"]').removeClass('span10').addClass('span11');
        }
    });

    return Controller;
});
