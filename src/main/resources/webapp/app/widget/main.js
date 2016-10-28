//
// widget/main.js
//   Initialize the widget.
//
/*global require, window, document, clearTimeout, setTimeout, easyXDM */

require([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',

    // Application.
    'app',

    // Main Controller.
    'widget/controller',

    // JSON polyfill for old browsers
    'json2',
    // easyXDM lib for cross-browser iframe communication
    'easyxdm'

], function ($, _, Backbone, app, Controller) {
    'use strict';

    var widgetReady = false,
        requestQueue = [],
        dispatchQueue = function () {
            var callback = requestQueue.shift();
            while (callback) {
                callback.call(app);
                callback =  requestQueue.shift();
            }
        },
        onWidgetReady = function (callback) {
            if (widgetReady) {
                callback.call(app);
            } else {
                requestQueue.push(callback);
            }
        };

    easyXDM.whenReady(function () {
        app.log('Bootstrapping widget', app);

        var rpc = new easyXDM.Rpc({
            local: app.root + 'assets/html/name.html',
            swf: app.root + 'assets/swf/easyxdm.swf',
            onReady: function () {
                // Host page resize callback.
                var resizeTimer,
                    resizedByWidget = false,
                    resizeIFrame = function (scrollHeight) {
                    _.defer(function () {
                        var height;
                        if (scrollHeight) {
                            height = $('body')[0].scrollHeight;
                        } else {
                            height = $('body').height();
                        }
                        rpc.resizeWidgetFrame(height);
                    });
                };

                // Initialize widget.
                app.controller = new Controller({
                    hostPage: rpc.origin
                });

                // Notify host page of widget resize events.
                app.on('result-view:rendered concept:tooltip-open', function () {
                    resizedByWidget = true;
                    resizeIFrame(true);
                }, this);
                app.on('concept:tooltip-close', function () {
                    resizedByWidget = true;
                    resizeIFrame(false);
                }, this);
                $(window).resize(function() {
                    clearTimeout(resizeTimer);
                    resizeTimer = setTimeout(function () {
                        resizeIFrame(resizedByWidget);
                        resizedByWidget = false;
                    }, 100);
                });

                // Track following of external links.
                $(document).on('click', 'a[data-conceptref]', function (evt) {
                    app.trigger('concept:follow-ref', {
                        concept: $(this).text(),
                        source: $(this).data('conceptref')
                    });
                });

                widgetReady = true;
                dispatchQueue();
            }
        },
        {
            local: {
                // Widget methods exposed to host page
                annotateText: function (text, groups, successFn, errorFn) {
                    onWidgetReady.call(this, function () {
                        app.on('annotate-text:success', function (annotatedText) {
                            app.off('annotate-text:success', null, this);
                            if (_.isFunction(successFn)) {
                                successFn({
                                    text: annotatedText.get('text'),
                                    entities: annotatedText.get('entities'),
                                    ids: annotatedText.get('ids'),
                                    duration: annotatedText.get('duration')
                                });
                            }
                        }, this);
                        app.on('annotate-text:error', function (err) {
                            app.off('annotate-text:error', null, this);
                            if (_.isFunction(errorFn)) {
                                errorFn(err);
                            }
                        }, this);

                        try {
                            app.controller.annotateText(text, groups);
                        } catch (err) {
                            app.off('annotate-text:success annotate-text:error', null, this);
                            if (_.isFunction(errorFn)) {
                                errorFn(err);
                            }
                        }
                    });
                },
                annotatePublication: function (pmid, groups, successFn, errorFn) {
                    onWidgetReady.call(this, function () {
                        app.on('annotate-publication:success', function (annotatedPublication) {
                            app.off('annotate-publication:success', null, this);
                            if (_.isFunction(successFn)) {
                                successFn({
                                    pmid: annotatedPublication.get('pmid'),
                                    doi: annotatedPublication.get('doi'),
                                    title: annotatedPublication.get('title'),
                                    abstract: annotatedPublication.get('abstract'),
                                    authors: annotatedPublication.get('authors'),
                                    publ_date: annotatedPublication.get('pubdate'),
                                    publ_type: annotatedPublication.get('pubtype'),
                                    jrn_title: annotatedPublication.get('journal'),
                                    country: annotatedPublication.get('country'),
                                    entities_title: annotatedPublication.get('entities_title'),
                                    entities_abstract: annotatedPublication.get('entities_abstract'),
                                    ids: annotatedPublication.get('ids'),
                                    duration: annotatedPublication.get('duration')
                                });
                            }
                        }, this);
                        app.on('annotate-publication:error', function (err) {
                            app.off('annotate-publication:error', null, this);
                            if (_.isFunction(errorFn)) {
                                errorFn(err);
                            }
                        }, this);

                        try {
                            app.controller.annotatePublication(pmid, groups);
                        } catch (err) {
                            app.off('annotate-publication:success annotate-publication:error', null, this);
                            if (_.isFunction(errorFn)) {
                                errorFn(err);
                            }
                        }
                    });
                }
            },
            remote: {
                // Remote host page method stubs
                resizeWidgetFrame: {

                }
            }
        });
    });
});
