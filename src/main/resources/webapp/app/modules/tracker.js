//
// tracker.js
//   Event tracker (uses Google Analytics)
//
/*global define, window, location */

define([
    // Libraries.
    'underscore',
    'underscore.string',

    'modules/text-samples'

], function (_, _s, TextSamples) {
    'use strict';

    var Level = {
            NONE: 0,
            ERROR: 1,
            INFO: 2,
            DEBUG: 3
        },

        Tracker = {
            Level: Level,

            _level: Level.DEBUG,

            // Log every interesting event
            attachListeners: function (app) {
                //
                // Track Annotation Events
                //

                // TEXT annotation
                app.on('annotate-text:success', function (params) {
                    var text = _s.trim(params.get('text')),
                        textSample = _s.truncate(text, 15),
                        evt = ['Annotate'],     // category
                        timing = ['Annotate'];  // category

                    if (_.contains(TextSamples.samples, text)) {
                        // Text samples
                        evt.push('Sample');     // action
                        timing.push('Sample');  // variable
                    } else if (params.get('fromFile')) {
                        // File upload
                        evt.push('File');     // action
                        timing.push('File');  // variable
                    } else {
                        // Free text input
                        evt.push('Text');     // action
                        timing.push('Text');  // variable
                    }

                    evt.push(textSample);   // label
                    evt.push(text.length);  // value

                    timing.push(params.get('duration'));  // time
                    timing.push(textSample);              // label
                    timing.push(100);                     // sample rate

                    this.trackEvent.apply(this, evt);
                    this.trackTiming.apply(this, timing);
                }, this);

                // PUBLICATION annotation
                app.on('annotate-publication:success', function (params) {
                    var pmid = '' + params.get('pmid'),
                        textLength = params.get('title').length;

                    if (params.get('abstract', '') !== undefined) {
                        textLength += params.get('abstract').length;
                    }

                    this.trackEvent('Annotate', 'Publication', pmid, textLength);
                    this.trackTiming('Annotate', 'Publication', params.get('duration'), pmid, 100);
                }, this);


                //
                // Track Export Events
                //

                // TEXT export
                app.on('export-text:export', function (params) {
                    var text = _s.trim(params.text),
                        format = params.format,
                        type;

                    if (_.contains(TextSamples.samples, text)) {
                        type = 'Sample';
                    } else if (params.fromFile) {
                        type = 'File';
                    } else {
                        type = 'Text';
                    }

                    this.trackEvent('Export', type, format, text.length);
                }, this);

                // PUBLICATION export
                app.on('export-pub:export', function (params) {
                    var pmid = '' + params.pmid;

                    this.trackEvent('Export', 'Publication', pmid);
                }, this);


                //
                // Track Concept Interaction Events
                //

                // // Focus on text
                // app.on('concept:focus', function (params) {
                //     this.trackEvent('Concept', 'Focus on text', params.term);
                // }, this);

                // // View concept popover
                // app.on('concept:view', function (params) {
                //     this.trackEvent('Concept', 'View Details', params.term, params.ids.length);
                // }, this);

                // Follow external reference
                app.on('concept:follow-ref', function (params) {
                    // Source:
                    //  Dialog = 0
                    //  Tree = 1
                    var source = params.source === 'Dialog' ? 0 : 1;
                    this.trackEvent('Concept', 'Follow Reference', params.concept, source);
                }, this);


                //
                // Track Application Tour
                //
                app.on('home-tour:started', function () {
                    this.trackEvent('Tour', 'Start', 'Home');
                }, this);

                app.on('home-tour:ended', function () {
                    this.trackEvent('Tour', 'End', 'Home');
                }, this);

                app.on('result-tour:started', function () {
                    this.trackEvent('Tour', 'Start', 'Result');
                }, this);

                app.on('result-tour:ended', function () {
                    this.trackEvent('Tour', 'End', 'Result');
                }, this);

                //
                // Track Static Pages Rendering
                //
                app.on('static:render', function (params) {
                    this.trackEvent('Static', params.page);
                }, this);


                //
                // Track User Error Warnings
                //
                app.on('warning:user-error', function (params) {
                    this.trackEvent('Warning', params.cause, params.value);
                }, this);


                //
                // Track Application Errors
                //
                app.on('error:known-bug', function (params) {
                    this.trackEvent('Error', 'Known Bug', params.cause);
                }, this);
                app.on('error:unexpected', function (params) {
                    this.trackEvent('Error', 'Unexpected', params.cause, params.value);
                }, this);
            },

            // GA page view tracking
            trackPageView: function () {
                window._gaq.push(['_trackPageview', location.hash]);
                this.log('Track pageview', location.hash);
            },

            // GA event tracking
            trackEvent: function (category, action, opt_label, opt_value, opt_noninteraction) {
                var evt = _.toArray(arguments);
                evt.unshift('_trackEvent');

                window._gaq.push(evt);
                this.log('Track event:', evt);
            },

            // GA timing tracking
            trackTiming: function (category, variable, time, opt_label, opt_sample) {
                var evt = _.toArray(arguments);
                evt.unshift('_trackTiming');

                window._gaq.push(evt);
                this.log('Track timing:', evt);
            },

            // Log helper
            log: function () {
                if (this._level >= this.Level.INFO && window.console && window.console.log && _.isFunction(window.console.log)) {
                    window.console.log.apply(window.console, this._argsWithTimestamp(arguments));
                }
            },

            // Error logging helper
            error: function () {
                if (this._level >= this.Level.ERROR && window.console && window.console.error && _.isFunction(window.console.error)) {
                    window.console.error.apply(window.console, this._argsWithTimestamp(arguments));
                }
            },

            // debugging helper
            debug: function () {
                if (this._level >= this.Level.DEBUG && window.console && window.console.debug && _.isFunction(window.console.debug)) {
                    var debugMsg = this._argsWithTimestamp(arguments);
                    debugMsg.splice(1, 0, '[Debug]');
                    window.console.debug.apply(window.console, debugMsg);
                }
            },

            setLevel: function (level) {
                this._level = level;
            },

            _argsWithTimestamp: function (args) {
                var dt = new Date(),
                    timestamp = [dt.getHours(), dt.getMinutes(), dt.getSeconds(), dt.getMilliseconds()].join(':');
                return _.union(['[' + timestamp] + ']', _.toArray(args));
            }
    };

    return Tracker;
});
