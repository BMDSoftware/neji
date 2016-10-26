//
// views/home.js
//   Homepage widget view
//
/*global define, document, prompt, FileReader, encodeURIComponent */

define([
    // Libraries.
    'jquery',
    'underscore',
    'underscore.string',
    'backbone',

    // Application.
    'app',

    // Modules.
    'modules/text-samples',
    'modules/tutorials',

    // Views.
    'views/pmid-dialog'

], function ($, _, _s, Backbone, app, ts, Tutorials, PmidDialogModalView) {
    'use strict';

    var HomeView = Backbone.View.extend({
        template: 'home',

        events: {
            'click #annotator-widget button[type="reset"]': 'focusTextArea',
            'click #annotator-widget button.try-sample': 'trySample',
            'click #annotator-widget button.upload-file': 'uploadFile',
            'change #annotator-widget input[type="file"]': 'handleFileSelected',
            'click #annotator-widget button.annotate-pmid': 'annotateByPmid',
            'click #annotator-widget button[type="submit"]': 'annotateText',
            'change #annotator-widget #textarea': 'validateTextLength',
            'keyup #annotator-widget #textarea': 'validateTextLength',
            'blur #annotator-widget #textarea': 'validateTextLength'
        },

        cleanup: function () {
            app.debug('HomeView.cleanup()', this);
            app.off(null, null, this);
            this.$('#textarea').unbind();
            this.eventsBound = false;
        },

        initialize: function () {
            app.debug('HomeView.initialize()', this);
            this.textSamples = ts.samples;
            this.curSample = 0;
            this.dndSupported = 'draggable' in document.createElement('span');
            this.filereaderSupported = typeof FileReader != 'undefined';
            this.inTour = false;
            this.afterRenderDeferred = $.Deferred();
            this._bindAppEventListeners();
        },

        _initDragAndDropFileUpload: function () {
            var placeholderText = 'Type or paste some text here ...',
                $textArea = this.$('#textarea'),
                text,
                self = this;

            if (this.dndSupported) {
                placeholderText = 'Type, paste or drag a text file to this area ...';

                $textArea[0].ondragenter = function (e) {
                    $(this).addClass('dnd-over');
                    text = $(this).val();
                    $(this).val('');
                    $(this).attr('placeholder', 'Drop file to annotate ...');

                    e.preventDefault();
                    return false;
                };

                $textArea[0].ondragleave = function (e) {
                    $(this).removeClass('dnd-over');
                    $(this).val(text);
                    $textArea.attr('placeholder', placeholderText);

                    e.preventDefault();
                    return false;
                };

                $textArea[0].ondragover = function (e) {
                    // Make it work in Firefox
                    e.preventDefault();
                    return false;
                };

                $textArea[0].ondrop = function (e) {
                    $(this).removeClass('dnd-over');
                    $textArea.attr('placeholder', placeholderText);

                    if (e.dataTransfer.files.length > 0) {
                        // File upload
                        e.stopPropagation();
                        e.preventDefault();
                        if (e.dataTransfer.files.length > 1) {
                            app.alert('Please upload only one file at a time. Please try again.');
                            app.trigger('warning:user-error', {cause: 'Tried to upload multiple files',
                                                               value: e.dataTransfer.files.length});
                            return;
                        }
                        self.handleFileUpload(e.dataTransfer.files[0]);
                    } else {
                        // Text drop
                        _.defer(function () {
                            self.annotateText.call(self);
                        });
                    }
                };
            }

            $textArea.attr('placeholder', placeholderText);
        },

        _bindAppEventListeners: function () {
            app.on('annotate-text:setup annotate-publication:setup', function () {
                this.afterRenderDeferred.done(function () {
                    this.setLoading(true);
                });
            }, this);

            app.on('annotate-text:success annotate-text:error annotate-publication:success annotate-publication:error', function () {
                this.afterRenderDeferred.done(function () {
                    this.setLoading(false);
                });
            }, this);

            app.on('annotate-text:error annotate-publication:error', function (err, highlightControls) {
                if (err === 'no-groups' && highlightControls) {
                    this.$('#annotator-widget button[type="submit"]').effect('transfer', {'to': highlightControls}, 500, function () {
                        highlightControls.effect('highlight', {'color': '#ffff99'}, 1500);
                    });
                    app.trigger('app:go-home');
                }
            }, this);

            app.on('homepage:reset', function () {
                this.setLoading(false);
                this.focusTextArea();
            }, this);

            app.on('home-tour:start', this.startTour, this);
            app.on('home-tour:ended', function () {
                this.inTour = false;
                if ('!/tour' === Backbone.history.fragment) {
                    app.trigger('app:go-home');
                }
            }, this);

            this.eventsBound = true;
        },

        _initFileUploadButton: function () {
            if (!this.filereaderSupported) {
                this.$('#annotator-widget button.upload-file').hide();
            }
        },

        beforeRender: function () {
            app.debug('HomeView.beforeRender()', this);

            if (!this.eventsBound) {
                this._bindAppEventListeners();
            }

            app.trigger('app:set-title');
        },

        afterRender: function () {
            app.debug('HomeView.afterRender()', this);
            this._initDragAndDropFileUpload();
            this._initFileUploadButton();
            this.scrollToTop();
            this.focusTextArea();

            this.afterRenderDeferred.resolveWith(this);
        },

        scrollToTop: function () {
            $.scrollTo(0);
        },

        focusTextArea: function () {
            this.$('#textarea').focus();
        },

        startTour: function () {
            this.curSample = 0;
            var tour = Tutorials.newHomeTour();
            tour.restart();
            tour.start(true);
            this.inTour = true;
        },

        trySample: function (ev) {
            ev.stopPropagation();
            ev.preventDefault();
            this.$('#textarea').val(this.textSamples[this.curSample]);
            this.curSample += 1;
            this.curSample = (this.curSample === this.textSamples.length) ? 0 : this.curSample;

            if (!this.inTour) {
                $(ev.target).html('<i class="icon-play"></i> Try another sample');
            }

            var annotateButton = this.$('button[type="submit"]');
            annotateButton.effect('highlight', {'color': '#acead0'}, function () {
                annotateButton.focus();
            }, 2000);
        },

        uploadFile: function (ev) {
            ev.stopPropagation();
            ev.preventDefault();

            this.$('input[type="file"]').trigger('click');
        },

        handleFileSelected: function (ev) {
            if (ev.currentTarget.files.length == 1) {
                this.handleFileUpload(ev.currentTarget.files[0]);
            }
        },

        handleFileUpload: function(file) {
            var reader = new FileReader(),
                self = this;

            if (_s.startsWith(file.type, 'text/')) {

                // Check file size
                if (file.size > app.maxinput) {
                    app.alert('The file is too large (' + file.size + ' bytes). Please try again with a smaller file.');
                    app.trigger('warning:user-error', {cause: 'File too large (' + file.size + ')'});
                    return;
                }

                reader.onerror = function (e) {
                    app.error('Error while reading file', e, file);

                    if(e.target.error.name == "NotReadableError") {
                        app.alert('The file could not be read.');
                    } else {
                        app.alert('An error occurred while trying to read the file. Please try again.');
                    }

                    app.trigger('error:unexpected', {cause: 'Error reading file'});
                };

                reader.onload = function (e) {
                    var fileContents = e.target.result,
                        trimmedText = _s.trim(fileContents);

                    self.$('#textarea').val(trimmedText);

                    if (trimmedText.length === 0) {
                        app.alert('The file is empty, please try again with another file.');
                        this.focusTextArea();
                        app.trigger('warning:user-error', {cause: 'Empty text'});
                    } else if (trimmedText.length > app.maxinput) {
                        app.alert('Too much text. Please try again with less text.');
                        this.focusTextArea();
                        app.trigger('warning:user-error', {cause: 'Too much text (' + trimmedText.length + ')'});
                    } else {  // annotate
                        Backbone.history.navigate('#!/annotate?t=' + encodeURIComponent(trimmedText), false);
                        app.trigger('annotate-text:setup', {
                            text: trimmedText,
                            crlf: _s.include(trimmedText, '\r\n'),
                            fromFile: true
                        });
                    }
                };

                reader.readAsText(file, 'UTF-8');
            } else if (file.type === '') {
                app.alert('Only plain text files, of type "text/*" are supported. Please try again.');
                app.trigger('warning:user-error', {cause: 'Tried to upload unknown type file'});
            } else {
                app.alert('Please upload a plain text file. "' + file.type + '" files are not supported.');
                app.trigger('warning:user-error', {cause: 'Tried to upload non-text file', value: file.type});
            }
        },

        annotateByPmid: function (ev) {
            ev.stopPropagation();
            ev.preventDefault();

            new PmidDialogModalView().render();
        },

        annotateText: function (ev) {
            if (ev) {
                ev.stopPropagation();
                ev.preventDefault();
            }

            var text = this.$('#textarea').val(),
                trimmedText = _s.trim(text);

            if (!text || trimmedText.length === 0) {
                app.alert({msg: 'Please enter some text, upload a file or try a sample.', icon: 'icon-arrow-down'});
                this.focusTextArea();
                app.trigger('warning:user-error', {cause: 'Empty text'});
            } else if (trimmedText.length > app.maxinput) {
                app.alert('Too much text. Please try again with less text.');
                this.focusTextArea();
                app.trigger('warning:user-error', {cause: 'Too much text (' + trimmedText.length + ')'});
            } else {  // annotate
                Backbone.history.navigate('#!/annotate?t=' + encodeURIComponent(trimmedText), false);
                app.trigger('annotate-text:setup', {
                    text: trimmedText,
                    crlf: _s.include(trimmedText, '\r\n'),
                    fromFile: false
                });
            }
        },

        setLoading: function (isLoading) {
            app.debug('HomeView.setLoading(' + isLoading + ')');
            if (isLoading) {
                this.$('#textarea').attr('disabled', 'disabled');
                this.$('#annotator-widget button').attr('disabled', 'disabled');
                this.$('.loading-spinner').show();
            } else {
                this.$('.loading-spinner').hide();
                this.$('#annotator-widget button').removeAttr('disabled');
                this.$('#textarea').removeAttr('disabled');
            }
        },

        validateTextLength: function (ev) {
            var $textarea = this.$('#textarea'),
                text = $textarea.val();

            if (text.length > app.maxinput) {
                $textarea.val(text.substring(0, app.maxinput));
                ev.preventDefault();
                return false;
            }
        }
    });

    return HomeView;
});