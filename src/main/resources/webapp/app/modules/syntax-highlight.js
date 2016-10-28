//
// syntax-highlight.js
//   Syntax highlight JSON code
//
/*global define, JSON */

define(function () {
    'use strict';

    function prettyPrint (obj) {
        return JSON.stringify(obj, undefined, 2);
    }

    function syntaxHighlight (json) {
        if (typeof json != 'string') {
             json = prettyPrint(json);
        }
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            var cls = 'number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'key';
                } else {
                    cls = 'string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'boolean';
            } else if (/null/.test(match)) {
                cls = 'null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    }

    return {
        prettyPrint: prettyPrint,
        highlight: syntaxHighlight
    };
});
