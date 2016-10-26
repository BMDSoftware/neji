//
// tutorials.js
//   App tour tutorials definition.
//
/*global define, localStorage */

define([
    // Libraries.
    'jquery',
    'underscore',
    'backbone',
    'bootstrap.tour',

    // Application.
    'app',

    // Modules
    'modules/semantic-groups',
    'modules/text-samples'

], function ($, _, Backbone, Tour, app, SemanticGroups, TextSamples) {
    'use strict';

    function hasLocalStorage() {
        try {
            localStorage.setItem('test', 'test');
            localStorage.removeItem('test');
            return true;
        } catch(e) {
            return false;
        }
    }

    var Tutorials = {
        newHomeTour: function () {
            var tour = new Tour({
                name: 'becasAnnotateTextTour',
                useLocalStorage: hasLocalStorage(),
                keyboard: false,
                afterSetState: function (key, value) {
                    if (key === 'current_step') {
                        switch (value) {
                            // 1st step
                            case 0:
                                _.defer(function () {
                                    $('#textarea').val('');
                                    app.trigger('concept-types:select-all');
                                    $('.next').focus();
                                });
                                break;
                            // 2nd step
                            case 1:
                                $('.try-sample').focus();
                                break;
                            // last step
                            case 2:
                                var btn = $('.annotate-text'),
                                    btnOnclick = btn[0].onclick;

                                app.on('home-tour:ended', function () {
                                    btn[0].onclick = btnOnclick;
                                }, tour);

                                btn[0].onclick = function (e) {
                                    e.stopPropagation();
                                    e.preventDefault();
                                    $('#textarea').val(TextSamples.samples[0]);
                                    Backbone.history.navigate('#!/tour/annotated', true);
                                };
                                break;
                        }
                    }
                },
                onStart: function (tour) {
                    app.on('tour:end', function () {
                        this.end();
                    }, tour);
                    // Hide 'next' button on first tour step if user unselects all groups
                    app.on('semantic-groups:change', function (groups) {
                        if (tour.getCurrentStep() === 0) {
                            var selected = _.reduce(groups, function (count, group) {
                                return (group === true) ? count + 1 : count;
                            });
                            if (selected != SemanticGroups.groups.length) {
                                $('.popover .next').hide();
                            } else {
                                $('.popover .next').show();
                            }
                        }
                    }, tour);

                    // End 'annotate' tour when user annotates something
                    app.on('annotate-text:setup annotate-publication:setup', function () {
                        tour.end();
                    }, tour);

                    app.trigger('home-tour:started');
                },
                onEnd: function (tour) {
                    app.off(null, null, tour);
                    app.trigger('home-tour:ended');
                }
            });

            // 1st step: Choose concept types
            tour.addStep({
                element: '#sidebar',
                placement: 'right',
                title: 'Choose concept types',
                content: '<span class="becas">becas</span> recognizes and annotates biomedical concepts in text.<br />' +
                         'You start by choosing the types of concepts you want to find.<br />' +
                         'Go ahead and click next now.'
            });

            // 2nd step: Enter some text
            tour.addStep({
                element: '.try-sample',
                placement: 'top',
                showNext: false,
                reflex: true,
                title: 'Enter biomedical text',
                content: 'You can upload text files, paste text or choose a PubMed publication.<br />' +
                         'For this tour, start by trying a sample.'
            });

            // 3rd step: Click annotate
            tour.addStep({
                element: '.annotate-text',
                placement: 'left',
                showPrev: false,
                showNext: false,
                showEnd: false,
                title: 'Annotate',
                content: 'Now press enter or click the Annotate button.'
            });

            return tour;
        },

        newAnnotatedTextTour: function () {
            var tour = new Tour({
                name: 'becasAnnotatedTextTour',
                useLocalStorage: hasLocalStorage(),
                afterSetState: function (key, value) {
                    if (key === 'current_step') {
                        _.defer(function () {
                            $('.next').focus();
                        });
                    }
                },
                onStart: function (tour) {
                    app.on('tour:end', function () {
                        this.end();
                    }, tour);

                    app.trigger('result-tour:started');
                },
                onEnd: function (tour) {
                    app.off(null, null, tour);
                    app.trigger('result-tour:ended');
                }
            });

            // 1st step: Examine annotated text
            tour.addStep({
                element: '.annotated-text .annotation:nth(1)',
                placement: 'bottom',
                title: 'Explore annotated text',
                content: 'Here is your text annotated with known biomedical concepts.<br />' +
                         'Find more about concepts by placing your mouse over them.'
            });

            // 2rd step: Highlight/mute concepts
            tour.addStep({
                element: '#sidebar',
                placement: 'right',
                title: 'Highlight and mute concepts',
                content: 'Use the highlight controls to show or mute specific concepts in the text.'
            });

            // 3nd step: Explore concept tree
            tour.addStep({
                element: '.concept-tree-wrapper',
                placement: 'top',
                title: 'Explore concepts',
                content: 'Every entity found in text is categorized in the concept tree bellow.<br />' +
                         'You can explore the tree and follow concept references.'
            });

            // 4th step: Export results
            tour.addStep({
                element: '.export-btn',
                placement: 'left',
                reflex: true,
                title: 'Export results',
                content: 'Reuse results in other applications by exporting annotations in your preferred format.'
            });

            // 5th step: Learn more
            tour.addStep({
                element: '[data-navitem="help"]',
                placement: 'bottom',
                reflex: true,
                title: 'Learn more',
                content: 'You can learn more about <span class="becas">becas</span> in the <a href="#!/help">Help page</a>.'
            });

            return tour;
        }
    };
    
    return Tutorials;
});
