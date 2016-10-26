// REST webservices urls
var getModelsUrl = window.location.origin + "/manage/getModels";
var getDictionariesUrl = window.location.origin + "/manage/getDictionaries";
var removeModelUrl = window.location.origin + "/manage/removeModel/id=";

// Current models table index
var modelsTableIndex = 0;

// Flag that indicates what operation add or edit is active in modal
// (add: true, edit: false);
var addEditModalFlag;
var modelId;

/**
 * Set page contents when page is loaded.
 */
$(document).ready(function() {
   
   // Load models data
   loadModelsData(getModelsUrl);
   
   // Load add model modal data
   loadAddModelModal(getDictionariesUrl);
   
   // Set form validation
   setFormValidation();   
});

/**
 * Load models data into table.
 * @param getModelsUrl get models url
 */
function loadModelsData(getModelsUrl) {
    
    // Reset index
    modelsTableIndex = 0;
    
    $.ajax({
        url: getModelsUrl,
        type: 'GET',
        contentType: "application/json; charSet=UTF-8",
        dataType: 'json',   
        success: function (data) {        
                        
            var models  = {models: data};
                        
            // Set models table body
            var source = $("#models-table-template").html();
            var template = Handlebars.compile(source);
            var html = template(models);
            $("#models-table-body").html(html);
            
            // Add click events to dictionary buttons
            $.each(data, function(i, model) {
                var editSelector = "#edit-" + model.id;
                var $editButton = $(editSelector);
                var deleteSelector = "#delete-" + model.id;
                var $deleteButton = $(deleteSelector);
                
                // Edit button
                $editButton.on('click', function(){
                    setEditModal(model);
                    $('#add-edit-model-modal').modal('show');
                });
                
                // Verify if delete button should be enabled
                if (model.services.length === 0) {
                    
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
                            removeModel(model);
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
 * Load add models modal data.
 * @param getDictionariesUrl get dictionaries url
 */
function loadAddModelModal(getDictionariesUrl) {    
    $.ajax({
        url: getDictionariesUrl,
        type: 'GET',
        contentType: "application/json; charSet=UTF-8",
        dataType: 'json',   
        success: function (data) {        
                        
            var dictionaries  = {dictionaries: data};
                        
            // Set available dictionaries in modal
            var source = $("#available-dictionaries-template").html();
            var template = Handlebars.compile(source);
            var html = template(dictionaries);
            $("#available-dictionaries").html(html);
            
            // Set selected dictionaries in modal
            source = $("#selected-dictionaries-template").html();
            template = Handlebars.compile(source);
            html = template(dictionaries);
            $("#selected-dictionaries").html(html);
            
            // Add click events to dictionary buttons
            $.each(data, function(i, dictionary) {
                var nameWithoutSpaces = dictionary.name.toString().replace(/\s/g, '');
                var availableSelector = "#available-dictionary-" + nameWithoutSpaces;
                var selectedSelector = "#selected-dictionary-" + nameWithoutSpaces;
                
                $(availableSelector).on('click', function(){
                    $(this).hide();
                    $(selectedSelector).show();
                });
                
                $(selectedSelector).on('click', function(){
                    $(this).hide();
                    $(availableSelector).show();
                });
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
    return ++modelsTableIndex;
});

/**
 * Add model event.
 */
function addModelEvent() {
    
    // Name
    var name = $('#model-name').val().trim();
    
    // Files
    var file = $('#model-file').get(0).files[0];
    var configuration = $('#model-configuration-file').get(0).files[0];
    var properties = $('#model-properties-file').get(0).files[0];
    
    // Normalization dicitionaries
    var dictionaries = [];    
    $("#selected-dictionaries").children('button').each( function () {       
       var $dictionary = $(this);
       if ($dictionary.is(':visible')) {           
           var dictionaryName = $dictionary.contents().get(2).nodeValue.trim();
           dictionaries.push(dictionaryName);
       }       
    });
    
    // Build object and json representation
    var model = new Object();
    model.name = name;
    model.file = file.name;
    model.dictionaries = dictionaries;
    model.services = [];
    var modelJson = JSON.stringify(model);
    
    // Build form data
    var formData = new FormData();
    formData.append('model_file', file);
    formData.append('configuration_file', configuration);
    formData.append('properties_file', properties);
    formData.append('model_data', modelJson);
    
    // Disable save button while saving
    var $saveButton = $("#model-save-button");
     disableButton($saveButton, "Saving ...");
    
    // Add service throught a REST web service
    var addModelUrl = window.location.origin + "/manage/addModel/";
    $.ajax({
        url: addModelUrl,
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function() {
            
            // Reload models data
            loadModelsData(getModelsUrl);
            
            // Success alert
            createNotification('Success', "Model '" + name + "' has been added with success", 'success');            
        
            // Close and reset modal window
            $('#add-edit-model-modal').modal('hide');
            
            // Enable button
            enableButton($saveButton, "Save");
        },
        error: function (xhr, status, error) {
            
            // Enable button
            enableButton($saveButton, "Save");
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');            
        }
    });
}

/**
 * Edit model event.
 * @param modelId model id
 */
function editModelEvent(modelId) {
    
    // Name
    var name = $('#model-name').val();
    
    // Normalization dicitionaries
    var dictionaries = [];    
    $("#selected-dictionaries").children('button').each( function () {       
       var $dictionary = $(this);
       if ($dictionary.is(':visible')) {           
           var dictionaryName = $dictionary.contents().get(2).nodeValue.trim();
           dictionaries.push(dictionaryName);
       }       
    });
    
    // Build object and json representation
    var model = new Object();
    model.id = modelId;
    model.name = name;
    model.dictionaries = dictionaries;
    var modelJson = JSON.stringify(model);
    
    // Disable save button while saving
    var 
    $saveButton = $("#model-save-button");
    disableButton($saveButton, "Saving ...");
    
    // Add service throught a REST web service
    var addModelUrl = window.location.origin + "/manage/editModel";
    $.ajax({
        url: addModelUrl,
        type: 'POST',
        cache: false,
        data: modelJson,
        dataType: 'json',
        contentType: "application/json; charset=UTF-8",
        processData: false,
        success: function() {
            
            // Reload services data
            loadModelsData(getModelsUrl);  
        
            // Success alert
            createNotification('Success', "Model '" + name + "' has been edited with success", 'success');            
        
            // Close and reset modal window
            $('#add-edit-model-modal').modal('hide');
            
            // Enable button
            enableButton($saveButton, "Save");
        },
        error: function (xhr, status, error) {
            
            // Enable button
            enableButton($saveButton, "Save");
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');            
        }
    });
}

/**
 * Remove a model.
 * @param model model
 */
function removeModel(model) {
        
    url = removeModelUrl + model.id;
    $.ajax({
        url: url,
        type: 'POST',
        cache: false,
        contentType: false,
        processData: false,
        success: function() {        

            // Reload dictionaries data
            loadModelsData(getModelsUrl);

            // Success alert
            createNotification('Success', "Model '" + model.name + "' has been deleted with success.", 'success');                    
        },
        error: function (xhr, status, error) {

            // Error alert
            createNotification('Error', xhr.responseText, 'error');
        }
    });
}

/**
 * Set add modal.
 */
function setAddModal() {    
    
    var $modal = $("#add-edit-model-modal");
    
    // Reset modal
    $modal.find('form')[0].reset();
    $('#add-edit-model-form').formValidation('resetForm', true);
    
    // Title
    $modal.find("#modal-title").html("Add Machine-learning model");
    
    // Name
    var $name = $modal.find("#model-name");
    $name.prop('disabled', false);
    
    // Files (enable)
    $modal.find("#model-file").prop('disabled', false);
    $modal.find("#model-configuration-file").prop('disabled', false);
    $modal.find("#model-properties-file").prop('disabled', false);
    
    // Dictionaries
    var $availableDictionaries = $modal.find("#available-dictionaries");
    var $selectedDictionaries = $modal.find("#selected-dictionaries");
    
    $availableDictionaries.children('button').each( function () {
        $(this).show();
    });
    
    $selectedDictionaries.children('button').each( function () {
        $(this).hide();
    });
    
    // Change add or edit event flag
    addEditModalFlag = true;
    
    // Enable save button
    var $saveButton = $("#model-save-button");
    $saveButton.attr("disabled", false);
}

/**
 * Set edit modal.
 * @param model model
 */
function setEditModal(model) {    
    
    var $modal = $("#add-edit-model-modal");
    
    // Reset modal
    $modal.find('form')[0].reset();
    $('#add-edit-model-form').formValidation('resetForm', true);
    
    // Title
    $modal.find("#modal-title").html("Edit Machine-learning model");
    
    // Name
    var $name = $modal.find("#model-name");
    $name.val(model.name);
    $name.prop('disabled', true);
    
    // Files (disable)
    $modal.find("#model-file").prop('disabled', true);
    $modal.find("#model-configuration-file").prop('disabled', true);
    $modal.find("#model-properties-file").prop('disabled', true);
    
    
    // Dictionaries
    var $availableDictionaries = $modal.find("#available-dictionaries");
    
    $availableDictionaries.children('button').each( function () {
        var dictionaryName = $(this).contents().get(2).nodeValue.trim();
        var nameWithoutSpaces = dictionaryName.replace(/\s/g, '');
        var $availableButton = $(this);
        var $selectedButton = $modal.find("#selected-dictionary-"+nameWithoutSpaces);
       
        if ($.inArray(dictionaryName, model.dictionaries) > -1) {
            $availableButton.hide();
            $selectedButton.show();
        } else {
            $selectedButton.hide();
            $availableButton.show();            
        }
    });
    
    // Change add or edit event flag
    addEditModalFlag = false;
    modelId = model.id;
    
    // Enable save button
    var $saveButton = $("#service-save-button");
    $saveButton.attr("disabled", false);
}

/**
 * Add Model button click event.
 */
$("#add-model-button").on('click', function() {
    
    // Set add modal
    setAddModal();
    
    // Show modal
    $('#add-edit-model-modal').modal('show');
});

/**
 * Sets validation of form fields.
 */
function setFormValidation() {
    $('#add-edit-model-form').formValidation({
        framework: 'bootstrap',
        excluded: [':disabled'],
        icon: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
        fields: {
            'model-name': {
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
            'model-file': {
                validators: {
                    notEmpty: {
                        message: 'The model file is required'
                    }
                }
            },
            'model-configuration-file': {
                validators: {
                    notEmpty: {
                        message: 'The model configuration file is required'
                    }
                }
            },
            'model-properties-file': {
                validators: {
                    notEmpty: {
                        message: 'The model properties file is required'
                    }
                }
            }
        },
        onSuccess: function(e) {
            e.preventDefault();
            
            if (addEditModalFlag) {
                addModelEvent();
            } else {
                editModelEvent(modelId);
            }
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
