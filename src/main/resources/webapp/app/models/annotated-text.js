//
// models/annotated-text.js
//   Annotated text model
//
/*jshint loopfunc: true */
/*global define */

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

    function sort_by_text_length(a, b) {
        return b.text.length - a.text.length;
    }

    var annotation_template = '<span class="at-<%= pos %> annotation <%= groups %> <%= color %>" data-concept-ids="<%= ids %>" <%= dataTerm %> <%= style %>><%= text %></span>',
        make_annotation = _.template(annotation_template),

        AnnotatedText = Backbone.Model.extend({

        initialize: function (attributes) {
            // text, entities, ids, duration, fromFile

            // Find groups of concepts present in text
            this.set('groups', this._getConceptGroups(), {silent: true});

            // Detect presence of ambiguous concepts
            this.set('hasAmbiguousConcepts', this._hasAmbiguousConcepts(), {silent: true});

            // Save term positions
            this.set('termPositions', this._getTermPositions(), {silent: true});
        },

        _getConceptGroups: function () {
            var groups = {},
                group,
                cids;
            _.each(this.get('entities'), function (entity) {
                cids = entity.split('|')[1].split(';');
                _.each(cids, function (cid) {
                    
                    // Get group
                    var matched = false;
                    _.each(SemanticGroups.groups, function (groupElement) {
                        if (cid.match(groupElement.regex)) {
                            group = groupElement.name;
                            matched = true;
                            return false;
                        }
                    });
                    
                    if (matched && !(group in groups)) {
                        groups[group] = 0;
                    }
                    groups[group] += 1;
                });
            }, this);

            return groups;
        },

        _hasAmbiguousConcepts: function () {
            var entities = this.get('entities'),
                cids,
                i,
                j,
                last;

            for (i = 0; i < entities.length; i += 1) {
                cids = entities[i].split('|')[1].split(';');
                if (cids.length > 1) {
                    last = cids[0].substr(-4);
                    for (j = 1; j < cids.length; j += 1) {
                        if (cids[j].substr(-4) !== last) {
                            return true;
                        }
                    }
                }
            }

            return false;
        },

        _getTermPositions: function () {
            var entities = this.get('entities'),
                posMap = {},
                term,
                pos;

            if (!entities) {
                return posMap;
            }

            _.each(entities, function (entity) {
                entity = entity.split('|');
                term = entity[0].toLowerCase();  // case-insensitive positions
                pos = entity[2];

                if (posMap.hasOwnProperty(term)) {
                    posMap[term].push(pos);
                } else {
                    posMap[term] = [pos];
                }
            });

            return posMap;
        }
    },
        {
            annotation_class: function (cids) {
                var class_,
                    matched = false,
                    ambiguous = false,
                    cid,
                    cur,
                    last,
                    i;

                if (_.isArray(cids)) {
                    if (cids.length > 1) {
                        last = this.annotation_class(cids[0]);
                        class_ = last;
                        for (i = 1; i < cids.length; i += 1) {
                            cur = this.annotation_class(cids[i]);
                            if (cur !== last) {
                                class_ += ' ' + cur;
                                ambiguous = true;
                            }
                        }

                        return class_ + (ambiguous ? ' ambiguous' : '');
                    }

                    cid = cids[0];
                } else {
                    cid = cids;
                }

                _.each(SemanticGroups.groups, function (group) {
                    if (cid.match(group.regex)) {
                        class_ = group.class_;
                        matched = true;
                        return false;
                    }
                });

                if (matched) {
                    return class_;
                }

                throw 'Unrecognized Concept-ID: ' + cid;
            },

            annotation_group: function (cid) {
                var name,
                    matched = false;

                _.each(SemanticGroups.groups, function (group) {
                    if (cid.match(group.regex)) {
                        name = group.name;
                        matched = true;
                        return false;
                    }
                });

                if (matched) {
                    return name;
                }

                throw 'Unrecognized Concept-ID ' + cid;
            },
            
            annotation_color: function (annotationGroups) {
                var color,
                    matched = false;

                var groups = annotationGroups.split(" ");
                
                // If more than one annotaion than its ambiguous, and returns an empty string
                // because groups already have the 'ambiguous' class for css color
                if (groups.length > 1) {
                    return "";
                }

                _.each(SemanticGroups.groups, function (group) {
                    if (group.class_ === groups[0]) {
                        color = group.color;
                        matched = true;
                        return false;
                    }
                });

                if (matched) {
                    return color;
                }

                return "";
            },
            
            annotation_norm: function (annotationGroups) {
                var norm,
                    matched = false;

                var groups = annotationGroups.split(" ");
                
                // If more than one annotaion than its ambiguous, and returns an empty string
                // because groups already have the 'ambiguous' class for css color
                if (groups.length > 1) {
                    return "";
                }

                _.each(SemanticGroups.groups, function (group) {
                    if (group.class_ === groups[0]) {
                        norm = group.norm;
                        matched = true;
                        return false;
                    }
                });

                if (matched) {
                    return norm;
                }

                return "";
            },
            
            annotation_concepts: function(cids) {
                var concepts = [];
                
                // Group cids by group
                var cidsGroup = {};
                _.each(cids, function (cid) {
                    var group = this.annotation_group(cid);
                    
                    if (!(group in cidsGroup))
                        cidsGroup[group] = [];
                    
                    cidsGroup[group].push(cid);
                }, this);
                                                
                // Build concept entries
                _.each(cidsGroup, function (value, key) {
                    var concept = new Object();
                    concept.group = key;
                    concept.color = this.annotation_color(key);
                    concept.norm = this.annotation_norm(key);
                    concept.cids = value;
                    concepts.push(concept);
                }, this);
                                            
                return concepts;
            },

            entities_map: function (entities) {
                var emap = {},
                    term,
                    eids,
                    pos,
                    entry,
                    merged = false;

                if (!entities) {
                    return emap;
                }
                _.each(entities, function (entity) {
                    entity = entity.split('|');
                    term = entity[0];
                    eids = entity[1].split(';');
                    pos = entity[2];

                    entry = {text: term, eids: eids};

                    if (!emap.hasOwnProperty(pos)) {
                        emap[pos] = [entry];
                    } else {
                        _.each(emap[pos], function (ent) {
                            if (ent.text === entry.text) {
                                // app.log('merging entities', ent, entry);
                                ent.eids = _.union(ent.eids, entry.eids);
                                ent.eids.sort(sort_by_text_length);
                                merged = true;
                                return false;
                            }
                        });

                        if (merged) {
                            merged = false;
                        } else {
                            emap[pos].push(entry);
                            emap[pos].sort(sort_by_text_length);
                        }
                    }
                });

                return emap;
            },

            highlight_text: function (text, entities) {
                var emap = this.entities_map(entities),
                    terms,
                    nextTerms,
                    indexes,
                    annotated_text = '',
                    last_idx = 0,
                    annotation,
                    style,
                    curText,
                    wholeTerm,
                    term,
                    i,
                    j;

                // app.debug('Entities map', emap);
                // app.debug('Annotated text:', $('<span />').html(annotated_text)[0]);

                text = _s.trim(text);

                indexes = _.map(_.keys(emap), function (idx) {
                    return parseInt(idx, 10);
                });
                indexes.sort(function (a, b) {
                    return a - b;
                });

                // For each annotated term
                _.each(indexes, function (pos, idx) {
                    terms = emap[pos];

                    // Append non-annotated text
                    if (pos >= last_idx) {
                        annotated_text += _.escape(text.substring(last_idx, pos));
                    }

                    // Build annotations
                    annotation = '';
                    for (i = terms.length - 1; i >= 0; i -= 1) {
                        // Apply padding for nested annotations
                        style = terms.length > 1 && i < terms.length - 1 ?
                                    'style="padding:' + (terms.length - i) + 'px 2px"' :
                                    '';

                        // Check for intersections
                        if (pos < last_idx) {
                            // Intersected annotation
                            curText = terms[i].text.substring(last_idx - pos);
                            wholeTerm = terms[i].text;
                            style = 'style="border-left:0"';

                            // app.debug('Intersected annotation', last_idx - pos);

                            if (curText === '') {
                                break;
                                // Do not append empty annotations added
                                //  by previous annotation as a nested concept.
                            }
                        } else {
                            curText = terms[i].text;
                            wholeTerm = undefined;
                        }
                        curText = _.escape(curText);
                        wholeTerm = _.escape(wholeTerm);

                        // Check for nested annotations
                        //  On the beginning of the term
                        if (i < terms.length - 1) {
                            term = annotation + _.escape(_s.strRight(curText, terms[i + 1].text));
                        } else {
                            term = curText;
                        }
                        //  Anywhere afer the beginning
                        if (idx + 1 < indexes.length &&
                            indexes[idx + 1] < pos + terms[i].text.length) {

                            if (indexes[idx + 1] + emap[indexes[idx + 1]][0].text.length <= pos + terms[i].text.length) {
                                // Nested annotation offset from parent beginning
                                // app.debug('Nested annotation offset from parent beginning', emap[indexes[idx + 1]]);

                                nextTerms = emap[indexes[idx + 1]];

                                var annotationGroups = this.annotation_class(nextTerms[0].eids);
                                var annotationColor = this.annotation_color(annotationGroups);

                                term = _s.strLeft(term, _.escape(nextTerms[0].text)) + make_annotation({
                                    text: _.escape(nextTerms[0].text),
                                    pos: indexes[idx + 1],
                                    groups: annotationGroups,
                                    ids: _.escape(nextTerms[0].eids.join(';')),
                                    dataTerm: '',
                                    style: nextTerms.length > 1 && 0 < nextTerms.length - 1 ?
                                            'style="padding:' + (nextTerms.length - 0) + 'px 2px"' :
                                            '',
                                    color: annotationColor
                                }) + _s.strRight(term, _.escape(nextTerms[0].text));
                                
                                // Override nested annotation padding
                                style = 'style="padding: 2px 2px"';

                            } else {
                                // Intersected annotation (emphasize common part)
                                term = term.substring(0, indexes[idx + 1] - pos) +
                                        '<em>' + term.substring(indexes[idx + 1] - pos) + '</em>';
                            }
                        }

                        // Get annotation color
                        var annotationGroups = this.annotation_class(terms[i].eids);
                        var annotationColor = this.annotation_color(annotationGroups);

                        annotation = make_annotation({
                            text: term,
                            pos: pos,
                            groups: annotationGroups,
                            color: annotationColor,
                            ids: _.escape(terms[i].eids.join(';')),
                            dataTerm: wholeTerm ?
                                        'data-term="' + wholeTerm + '"' :
                                        '',
                            style: style
                        });                        
                    }

                    // Append annotated text
                    annotated_text += annotation;

                    // Increase last idx pointer
                    last_idx = pos + terms[0].text.length;
                }, this);

                // Append non-annotated text
                annotated_text += _.escape(text.substring(last_idx, text.length));

                // Preserve whitespace (line breaks, tabs and multiple spaces)
                return annotated_text
                        .replace(/(\r\n)|\r|\n/g, '<br/>')
                        .replace(/ {2}|\t/g, '&nbsp; ');
            },

            concepts_tree: function (entities) {
                var egroups = {},
                    term,
                    eids,
                    pos,
                    group;

                if (!entities) {
                    return egroups;
                }

                _.each(entities, function (entity) {
                    entity = entity.split('|');
                    term = entity[0];
                    eids = entity[1].split(';');
                    pos = entity[2];

                    _.each(eids, function (eid) {
                        group = this.annotation_group(eid);

                        if (egroups.hasOwnProperty(group)) {
                            if (egroups[group].hasOwnProperty(term)) {
                                if ($.inArray(eid, egroups[group][term]) === -1) {
                                    egroups[group][term].push(eid);
                                } //else {
                                    //window.console.log('Discarding re-occuring eid:', group, term, eid);
                                //}
                            } else {  // term not in egroups[group]
                                egroups[group][term] = [eid];
                            }

                        } else {  // group not in egroups
                            egroups[group] = {};
                            egroups[group][term] = [eid];
                        }
                    }, this);
                }, this);

                // Remove same-term-different-case terms
                // TODO: optimize algorithm to avoid this removal step after tree construction
                _.each(egroups, function (terms) {
                    var lowercasedTermsCache = {},
                        lowercaseTerm;

                    _.each(terms, function (ids, term) {
                        lowercaseTerm = term.toLowerCase();

                        if (lowercasedTermsCache.hasOwnProperty(lowercaseTerm)) {
                            delete terms[term];
                            // window.console.log('Removing different-case duplicate "' + term + '"');
                        } else {
                            lowercasedTermsCache[lowercaseTerm] = null;
                        }
                    });
                });

                return egroups;
            },

            concepts_by_group: function (ids) {
                var egroups = {}, group;

                if (!ids || ids.length === 0) {
                    return egroups;
                }

                _.each(ids, function (id) {
                    if (id === ':::PRGE') {
                        return;
                    }

                    group = this.annotation_group(id);

                    if (egroups.hasOwnProperty(group)) {
                        egroups[group].push(id);
                    } else {  // group not in egroups
                        egroups[group] = [id];
                    }
                }, this);

                return egroups;
            },

            ref_to_link: function (ref) {
                return 'api/concept/redirect/' + ref;
            }
        });

    return AnnotatedText;
});