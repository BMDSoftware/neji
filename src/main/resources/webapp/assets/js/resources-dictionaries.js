// REST webservices urls
var getDictionariesUrl = window.location.origin + "/manage/getDictionaries";
var removeDictionaryUrl = window.location.origin + "/manage/removeDictionary/id=";

// Current dictionaries table index
var dictionariesTableIndex = 0;

/**
 * Set page contents when page is loaded.
 */
$(document).ready(function() {  
   
   // Load dictionaries data
   loadDictionariesData(getDictionariesUrl);
   
   // Set form validation
   setFormValidation();
});

/**
 * Load dictionaries data into table.
 * @param getDictionariesUrl get dictionaries url
 */
function loadDictionariesData(getDictionariesUrl) {
    
    // Reset index
    dictionariesTableIndex = 0;
    
    $.ajax({
        url: getDictionariesUrl,
        type: 'GET',
        contentType: "application/json; charSet=UTF-8",
        dataType: 'json',   
        success: function (data) {        
                        
            var dictionaries  = {dictionaries: data};
                        
            // Set models table body
            var source = $("#dictionaries-table-template").html();
            var template = Handlebars.compile(source);
            var html = template(dictionaries);
            $("#dictionaries-table-body").html(html);
            
            // Add click events to dictionary buttons
            $.each(data, function(i, dictionary) {

                var deleteSelector = "#delete-" + dictionary.id;
                var $deleteButton = $(deleteSelector);
                $deleteButton.confirmation({
                    animation: true,
                    placement: 'left',
                    title: 'Are you sure?',
                    popout: true,
                    btnOkClass: 'btn-xs btn-danger',
                    onConfirm: function() {
                        removeDictionary(dictionary);
                    }
                });
                
                // Verify if button should be enabled
                if ((dictionary.services.length === 0) && (dictionary.models.length === 0)) {
                    
                    // Enable button
                    $deleteButton.prop("disabled", false);
                    
                    // Delete event and confirmation box
                    $deleteButton.confirmation({
                        animation: true,
                        placement: 'left',
                        title: 'Are you sure?',
                        popout: true,
                        btnOkClass: 'btn-xs btn-danger',
                        onConfirm: function() {
                            removeDictionary(dictionary);
                        }
                    });
                } else {
                    
                    // Disable button
                    $deleteButton.prop("disabled", true);
                }
            });            
        },
        error: function (xhr, status, error) {
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');
        }    
    });    
}

/**
 * HandleBars register helper to get next index.
 * @return table index
 */
Handlebars.registerHelper('getIndex', function() {
    return ++dictionariesTableIndex;
});

/**
 * Add dictionary event.
 */
function addDictionaryEvent() {
    
    // Name
    var name = $('#dictionary-name').val().trim();
    
    // File
    var file = $('#dictionary-file').get(0).files[0];
    
    // Build object and json representation
    var dictionary = new Object();
    dictionary.name = name;
    dictionary.file = file.name;
    dictionary.services = [];
    dictionary.models = [];
    var dictionaryJson = JSON.stringify(dictionary);
    
    // Build form data
    var formData = new FormData();
    formData.append('dictionary_data', dictionaryJson);
    formData.append('dictionary_file', file);  
    
    // Disable save button while saving
    var $saveButton = $("#dictionary-save-button");    
    disableButton($saveButton, "Saving ...");    
    
    // Add service throught a REST web service
    var addDictionaryUrl = window.location.origin + "/manage/addDictionary/";
    $.ajax({
        url: addDictionaryUrl,
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function() {
            
            // Reload dictionaries data
            loadDictionariesData(getDictionariesUrl);
            
            // Success alert
            createNotification('Success', "Dictionary '" + name + "' has been added with success.", 'success'); 
                
            // Close and reset modal window
            $('#add-dictionary-modal').modal('hide');
            
            // Enable save button
            enableButton($saveButton, "Save");
        },
        error: function (xhr, status, error) {
            
            // Enable save button
            enableButton($saveButton, "Save");
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');
        }
    });

}

// Disable button
function disableButton($button, text) {
    
    // Disable
    $button.attr("disabled", true);
    
    // Add a loading spinner
    $button.html("<i class='fa fa-spinner fa-pulse'></i> " + text);
}

// Enable button
function enableButton($button, text) {
    
    // Enable
    $button.attr("disabled", false);
    
    // Remove loading spinner
    $button.html("<i class='fa fa-check-circle'></i> " + text);
}

/**
 * Resets add dictionary modal.
 */
function resetAddDictionarylModal() {
    var $modal = $('#add-dictionary-modal');
    $modal.modal('hide');
    $modal.find('form')[0].reset();
    $('#add-dictionary-form').formValidation('resetForm', true);
    
    var $saveButton = $('#dictionary-save-button');
    $saveButton.attr("disabled", false);
}

/**
 * Remove a dicitionary.
 * @param dictionary dictionary
 */
function removeDictionary(dictionary) {
        
    url = removeDictionaryUrl + dictionary.id;
    $.ajax({
        url: url,
        type: 'POST',
        cache: false,
        contentType: false,
        processData: false,
        success: function() {        

            // Reload dictionaries data
            loadDictionariesData(getDictionariesUrl);

            // Success alert
            createNotification('Success', "Dictionary '" + dictionary.name + "' has been deleted with success.", 'success');                    
       },
       error: function (xhr, status, error) {

           // Error alert
           createNotification('Error', xhr.responseText, 'error');
       }
    });
}
    
/**
 * Sets validation of form fields.
 */
function setFormValidation() {
    $('#add-dictionary-form').formValidation({
        framework: 'bootstrap',
        excluded: [':disabled'],
        icon: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
        fields: {
            'dictionary-name': {
                validators: {
                    notEmpty: {
                        message: 'The name is required'
                    },
                    regexp: {
                        regexp: /^[a-zA-Z0-9._-]+$/,
                        message: 'The name can only consist of alphabetical, number, underscore(_), hifen(-) and dot(.) characters'
                    }
                }
            },
            'dictionary-file': {
                validators: {
                    notEmpty: {
                        message: 'The dictionary file is required'
                    }
                }
            }
        },
        onSuccess: function(e) {
            e.preventDefault();
            
            addDictionaryEvent();
        }
    });
}     


/**
 * Add dictionary button click event.
 */
$("#add-dictionary-button").on('click', function() {
    
    // Set add modal
    resetAddDictionarylModal();
    
    // Show modal
    $("#add-dictionary-modal").modal('show');
});
