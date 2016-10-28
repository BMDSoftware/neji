/**
 * jQuery sorted each
 *
 * @author: Tiago Nunes
 */
/*global jQuery, window, document*/

(function ($, window, document) {
    'use strict';

    $.extend($, {
        sortedEach: function (obj, callback, comparator) {
            var name, names = [], i = 0,
                length = obj.length,
                isObj = length === undefined || $.isFunction(obj);

            if (isObj) {
                for (name in obj) {
                    if (obj.hasOwnProperty(name)) {
                        names.push(name);
                    }
                }
                if (comparator) {
                    names.sort(comparator);
                } else {
                    names.sort();
                }

                while (i < names.length) {
                    if (callback.call(obj[names[i]], names[i], obj[names[i]]) === false) {
                        break;
                    }
                    i += 1;
                }
            } else {
                while (i < length) {
                    if (callback.call(obj[i], i, obj[i]) === false) {
                        break;
                    }
                    i += 1;
                }
            }

            return obj;
        }
    });

})(jQuery, window, document);
