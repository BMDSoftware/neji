/*!
 * becas widget - biomedical concept annotator
 *
 * Copyright 2014 UA.PT Bioinformatics
 * All rights reserved.
 * http://bioinformatics.ua.pt/becas/
 */

(function (window, document, easyXDM) {
    var
    // Use local copy of easyXDM, to prevent conflict with host page
    _becasEasyXDM = easyXDM, //.noConflict('_becasEasyXDM'),

    // Configuration constants
    _BECAS_HOST = ('https:' === document.location.protocol ? 'https' : 'http') + '://bioinformatics.ua.pt/becas/',
    _BECAS_WIDGET_ADDRESS = _BECAS_HOST + 'widget.html',
    _DEFAULT_WIDTH = '100%',
    _DEFAULT_HEIGHT = '60px',
    _DEP_JSON2_ADDRESS = _BECAS_HOST + 'assets/js/libs/json2.min.js',
    _DEP_SWF_HELPER = _BECAS_HOST + 'assets/swf/easyxdm.swf',
    _DEP_HTML_HELPER = _BECAS_HOST + 'assets/html/name.html',

    _curWidgetId = 0,

    // The widget namespace
    becas = {},

    // Some DOM helper functions
    _isObject = function (obj) {
        return Object.prototype.toString.call(obj) === '[object Object]';
    },

    _isInteger = function (obj) {
        return ('' + parseInt(obj, 10) === '' + obj);
    },

    _isString = function (obj) {
        return Object.prototype.toString.call(obj) === '[object String]';
    },

    _isFunction = function (obj) {
        return Object.prototype.toString.call(obj) === '[object Function]';
    },

    _isDOMElement = function (obj) {
        return obj.appendChild && _isFunction(obj.appendChild);
    };

    // Include JSON polyfill in old browsers
    _becasEasyXDM.DomHelper.requiresJSON(_DEP_JSON2_ADDRESS);

    /**
     * becas.Widget constructor
     *  Options:
     *      container: {String || DOMElement} - ID of the container element to hold the widget
     *      width:     {String} - Width of the widget iframe (e.g. "100%" or "700px")
     */
    becas.Widget = function (options) {
        var
        // Unique identifier for this widget
        _widgetId = 'becas-widget-' + (_curWidgetId++),
        // The RPC interface
        _rpc,
        // Whether the RPC interface is ready or not
        _rpcReady = false,
        // Queue to hold annotation requests till the RPC is ready
        _requestQueue = [],

        // becas.Widget initializer
        _initialize = function (options) {
            // Validate options
            if (options !== Object(options)) {
                throw new Error('Missing initialization options.');
            }
            if (!options.container) {
                throw new Error('Missing `container` option.');
            }
            if (_isString(options.container)) {
                if (!document.getElementById(options.container)) {
                    throw new Error('Couldn\'t find DOM element with ID= "' + options.container + '"');
                }
            } else if (!_isDOMElement(options.container)) {
                throw new Error('`container` "' + options.container + '" is not a DOM element.');
            }
            if (!options.width) {
                options.width = _DEFAULT_WIDTH;
            } else if (Object.prototype.toString.call(options.width) !== '[object String]') {
                throw new Error('Expected `width` option to be a string (Ex: "100%"" or "700px").');
            }

            // Initialize RPC interface to communicate with widget
            _becasEasyXDM.whenReady(function () {
                _rpc = _buildRPC(options);
            });
        },

        // RPC interface builder
        _buildRPC = function (options) {
            return new _becasEasyXDM.Rpc({
                remote: _BECAS_WIDGET_ADDRESS,
                local: _DEP_HTML_HELPER,
                remoteHelper: _DEP_HTML_HELPER,
                swf: _DEP_SWF_HELPER,
                container: options.container,
                props: {
                    id: _widgetId,
                    style: {
                        width: options.width,
                        height: _DEFAULT_HEIGHT,
                        border: 0,
                        backgroundColor: 'transparent'
                    },
                    // IE hacks
                    frameBorder: 'no',
                    allowTransparency: 'true'
                },
                onReady: function (success) {
                    if (!success) {
                        if (window.console && window.console.error && window.console.error.call) {
                            window.console.error.call(window.console, 'Failed to initialize communication channel with widget.');
                        }
                    }

                    _rpcReady = true;
                    // Dispatch pending requests
                    var callback = _requestQueue.shift();
                    while (callback) {
                        callback.call(_rpc);
                        callback =  _requestQueue.shift();
                    }
                }
            },
            {
                local: {
                    // Host page methods exposed to widget
                    resizeWidgetFrame: function (height, successFn, errorFn) {
                        document.getElementById(_widgetId).style.height = height + 'px';
                    }
                },
                remote: {
                    // Remote widget method stubs
                    annotateText: {

                    },
                    annotatePublication: {

                    }
                }
            });
        },

        // Helper to use the RPC interface only after it is ready
        _onRPCReady = function (callback) {
            if (_rpcReady) {
                callback.call(_rpc);
            } else {
                _requestQueue.push(callback);
            }
        },

        // Helper to validate common annotation parameters
        _validateParams = function (options) {
            if (options.groups && !_isObject(options.groups)) {
                throw Error('Expected `groups` parameter to be an object.');
            }
            if (options.success && !_isFunction(options.success)) {
                throw Error("Expected `success` parameter to be a function.");
            }
            if (options.error && !_isFunction(options.error)) {
                throw Error("Expected `error` parameter to be a function.");
            }
        };

        // Initialize widget
        _initialize.call(this, options);

        // Exposed methods
        return {
            annotateText: function (options) {
                if (!options || !_isString(options.text)) {
                    throw new Error('Expected `text` parameter.');
                }
                _validateParams(options);

                _onRPCReady(function () {
                    this.annotateText(options.text, options.groups, options.success, options.error);
                });
            },

            annotatePublication: function (options) {
                if (!options || !_isInteger(options.pmid)) {
                    throw new Error('Expected integer `pmid` parameter.');
                }
                _validateParams(options);

                _onRPCReady(function () {
                    this.annotatePublication(options.pmid, options.groups, options.success, options.error);
                });
            },

            destroy: function () {
                _onRPCReady(function () {
                    this.destroy();
                });
            }
        };
    };

    // Expose widget namespace to the host page
    window.becas = becas;

})(window, window.document, window.easyXDM);
