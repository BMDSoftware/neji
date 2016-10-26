//
// annotation-service.js
//   Interface to becas annotation service
//
/*global define, window */

define([
    // Libraries.
    'jquery',
    'underscore',

    // Models.
    'models/annotated-text',
    'models/annotated-pub'

], function ($, _, AnnotatedText, AnnotatedPublication) {
    'use strict';

    function getContextPath() {
        var path = window.location.pathname;
        return path + (/\/$/.test(path) ? '' : '/');
    }

    var
    endpoints = {
        annotateText: getContextPath()+'annotate',
        annotatePublication: getContextPath()+'pubmed-annotate/annotate',
        exportText: getContextPath()+'export',
        exportPublication: getContextPath()+'pubmed-export/export'
    },

    buildAuthRequestQuery = function (params) {
        return '?tool=' + params.name + '&email=' + params.email;
    },

    AnnotationService = {
        annotateText: function (options, auth) {
            _.extend(options, {'echo': true});
            return $.ajax({
                type: 'POST',
                url: endpoints.annotateText + buildAuthRequestQuery(auth),
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(options)
            });
        },

        annotatePublication: function (options, auth) {            
            return $.ajax({
                type: 'POST',
                url: endpoints.annotatePublication + options.pmid + buildAuthRequestQuery(auth),
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(options)
            });
        },

        attachListeners: function (app) {
            _attachTextListeners(app);
            _attachPublicationListeners(app);
            _attachAbortAnnotationListeners(app);
        }
    },

    _annotationRequests = {},

    _attachTextListeners = function (app) {
        app.on('annotate-text:annotate', function (options) {
            var startTime = new Date(),
                requestId = 'text:' + options.text,
                request = AnnotationService.annotateText(options, app)
                    .done(function (result) {
                        // Delete current request from request map
                        // (NOTE: do not use .always(), must run before done/fail!)
                        delete _annotationRequests[requestId];

                        var elapsedTime = new Date() - startTime,
                            annotationResult = new AnnotatedText({
                                text: result.text,
                                entities: result.entities,
                                ids: result.ids,
                                duration: elapsedTime,
                                fromFile: options.fromFile,
                                crlf: options.crlf,
                                inTour: options.inTour
                            });

                        app.trigger('annotate-text:success', annotationResult);
                    })
                    .fail(function (err) {
                        // Delete current request from request map
                        // (NOTE: do not use .always(), must run before done/fail!)
                        delete _annotationRequests[requestId];

                        if (err.statusText === 'abort') {
                            // User aborted annotation, ignore
                            return;
                        }

                        app.trigger('annotate-text:error', err);
                    });

            _annotationRequests[requestId] = request;
        }, this);

        app.on('export-text:export', function (options) {
            _.extend(options, {groups: JSON.stringify(options.groups)});
            app.postToExportFrame(endpoints.exportText + buildAuthRequestQuery(app), options);
        }, this);
    },

    _attachPublicationListeners = function (app) {
        app.on('annotate-publication:annotate', function (options) {
            var startTime = new Date(),
                requestId = 'pmid:' + options.pmid,
                request = AnnotationService.annotatePublication(options, app)
                    .done(function (result) {
                        // Delete current request from request map
                        // (NOTE: do not use .always(), must run before done/fail!)
                        delete _annotationRequests[requestId];
                        var elapsedTime = new Date() - startTime,
                            annotationResult = new AnnotatedPublication({
                                pmid: parseInt(result.pmid, 10),
                                doi: result.doi,
                                entities_title: result.entities_title,
                                entities_abstract: result.entities_abstract,
                                ids: result.ids,
                                title: result.title,
                                abstract: result.abstract_,
                                authors: result.authors,
                                journal: result.jrn_title,
                                pubdate: result.publ_date,
                                pubtype: result.publ_type,
                                country: result.state || result.country,
                                duration: elapsedTime,
                                inTour: options.inTour
                            });

                        app.trigger('annotate-publication:success', annotationResult);
                    })
                    .fail(function (err) {
                        // Delete current request from request map
                        // (NOTE: do not use .always(), must run before done/fail!)
                        delete _annotationRequests[requestId];

                        if (err.statusText === 'abort') {
                            // User aborted annotation, ignore
                            return;
                        }

                        err.pmid = options.pmid;
                        app.trigger('annotate-publication:error', err);
                    });

            _annotationRequests[requestId] = request;
        }, this);

        app.on('export-pub:export', function (options) {
            _.extend(options, {groups: JSON.stringify(options.groups)});
            app.postToExportFrame(endpoints.exportPublication + options.pmid + buildAuthRequestQuery(app), options);
        }, this);
    },

    _attachAbortAnnotationListeners = function (app) {
        app.on('annotation:abort', function () {
            _.each(_annotationRequests, function (request, key, map) {
                app.log('Aborting ongoing annotation request', request);
                request.abort();
            });
        });
    };

    return AnnotationService;
});
