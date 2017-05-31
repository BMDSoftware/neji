// REST webservices urls
var getServicesUrl = window.location.origin + "/services/getServices";
var getDictionariesUrl = window.location.origin + "/manage/getDictionaries";
var getModelsUrl = window.location.origin + "/manage/getModels";
var annotationBaseUrl = window.location.origin + "/annotate/";
var removeServiceUrl = window.location.origin + "/services/removeService/id=";

// Current services table index
var servicesTableIndex = 0;

// Current groups
var currentGroups = {};

// Flag that indicates what operation add or edit is active in modal
// (add: true, edit: false);
var addEditModalFlag;
var serviceId;

// Validation options
var validationOptions;

/**
 * Set page contents when page is loaded.
 */
$(document).ready(function() {  
   
   // Load services data
   loadServicesData(getServicesUrl, annotationBaseUrl);
   
   // Load add service modal data.
   loadAddServiceModal(getDictionariesUrl, getModelsUrl);
   
   // Set form validation
   setFormValidation();  
});

/**
 * Load services data into table.
 * @param getServicesUrl get services url
 * @param annotationBaseUrl annotaion base url
 */
function loadServicesData(getServicesUrl, annotationBaseUrl) {
    
    // Reset index
    servicesTableIndex = 0;
    
    $.ajax({
        url: getServicesUrl,
        type: 'GET',
        contentType: "application/json; charSet=UTF-8",
        dataType: 'json',   
        success: function (data) {        
                        
            var services  = {services: data};
                                                
            // Set services table body
            var source = $("#services-table-template").html();
            var template = Handlebars.compile(source);
            var html = template(services);
            $("#services-table-body").html(html);
            
            // Add click events to service buttons
            $.each(data, function(i, service) {
                var viewSelector = "#view-" + service.id;
                var $viewButton = $(viewSelector);
                var editSelector = "#edit-" + service.id;
                var $editButton = $(editSelector);
                var deleteSelector = "#delete-" + service.id;
                var $deleteButton = $(deleteSelector);
                
                // View hyperlink
                $viewButton.attr("href", annotationBaseUrl + service.name);
                
                // Edit button
                $editButton.on('click', function(){
                    setEditModal(service);
                    $('#add-edit-service-modal').modal('show');                    
                });
                
                // Delete button
                $deleteButton.confirmation({
                    animation: true,
                    placement: 'left',
                    title: 'Are you sure?',
                    popout: true,
                    btnOkClass: 'btn-xs btn-danger',
                    onConfirm: function() {
                        removeService(service);
                    }
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
 * Load add service modal data.
 * @param getDictionariesUrl get dictionaries url
 * @param getModelsUrl get models url
 */
function loadAddServiceModal(getDictionariesUrl, getModelsUrl) {
        
    // Dictionaries
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
                var nameWithoutSpacesDots = dictionary.name.toString().replace(/[\s\.]/g, '');
                var availableSelector = "#available-dictionary-" + nameWithoutSpacesDots;
                var selectedSelector = "#selected-dictionary-" + nameWithoutSpacesDots;
                
                $(availableSelector).on('click', function(){
                    $(this).hide();
                    $(selectedSelector).show();
                    addSemanticGroupNormalizationEntry(dictionary);
                    revalidateResources();
                });
                
                $(selectedSelector).on('click', function(){
                    $(this).hide();
                    $(availableSelector).show();
                    removeSemanticGroupNormalizationEntry(dictionary);
                    revalidateResources();
                });
            });            
        },
        error: function (xhr, status, error) {
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');            
        }    
    });
    
    // Models
    $.ajax({
        url: getModelsUrl,
        type: 'GET',
        contentType: "application/json; charSet=UTF-8",
        dataType: 'json',   
        success: function (data) {        
                        
            var models  = {models: data};
                        
            // Set available models in modal
            var source = $("#available-models-template").html();
            var template = Handlebars.compile(source);
            var html = template(models);
            $("#available-models").html(html);
            
            // Set selected models in modal
            source = $("#selected-models-template").html();
            template = Handlebars.compile(source);
            html = template(models);
            $("#selected-models").html(html);
            
            // Add click events to models buttons
            $.each(data, function(i, model) {
                var nameWithoutSpacesDots = dictionary.name.toString().replace(/[\s\.]/g, '');
                var availableSelector = "#available-model-" + nameWithoutSpacesDots;
                var selectedSelector = "#selected-model-" + nameWithoutSpacesDots;
                
                $(availableSelector).on('click', function(){
                    $(this).hide();
                    $(selectedSelector).show();
                    addSemanticGroupNormalizationEntry(model);
                    revalidateResources();
                });
                
                $(selectedSelector).on('click', function(){
                    $(this).hide();
                    $(availableSelector).show();
                    removeSemanticGroupNormalizationEntry(model);
                    revalidateResources();
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
    return ++servicesTableIndex;
});

/**
 * HandleBars register helper to build dictionaries string.
 * @param dictionaries dictionaries
 * @return dictionaries string
 */
Handlebars.registerHelper('getDictionaries', function(dictionaries) {
    if (dictionaries.length !== 1) return dictionaries.length + " dictionaries";
    return dictionaries.length + " dictionary";
});

/**
 * HandleBars register helper to build models string.
 * @param models dictionaries
 * @return models models
 */
Handlebars.registerHelper('getModels', function(dictionaries) {
    if (dictionaries.length !== 1) return dictionaries.length + " models";
    return dictionaries.length + " model";
});

/**
 * Add service event.
 */
function addServiceEvent() {
        
    // Name
    var name = $('#service-name').val().trim();
    
    // Logo
    var logoFile = $('#service-logo').get(0).files[0];
    
    // Dicitionaries
    var dictionaries = [];    
    $("#selected-dictionaries").children('button').each( function () {       
       var $dictionary = $(this);
       if ($dictionary.is(':visible')) {           
           var dictionaryName = $dictionary.contents().get(2).nodeValue.trim();
           dictionaries.push(dictionaryName);
       }       
    });
    
    // Models
    var models = [];    
    $("#selected-models").children('button').each( function () {       
       var $model = $(this);
       if ($model.is(':visible')) {           
           var modelName = $model.contents().get(2).nodeValue.trim();
           models.push(modelName);
       }       
    });
    
    // Semantic groups normalization
    var groupsNormalization = {};    
    $("#semantic-groups").children('div').each( function () {       
       var $entry = $(this);
       var group = $entry.find('span').contents().get(0).nodeValue.trim();
       var normalizedName = $entry.find('input').val().trim();
       groupsNormalization[group] = normalizedName;
    });
    
    // Parsing level
    var parsingLevel = $('#service-parsing').find(":selected").text();
    
    // No ids
    var noIds = $('#service-no-ids').is(":checked");
    
    // Abbreviations
    var abbreviations = $('#service-abbreviations').is(":checked");
    
    // Disambiguation
    var disambiguation = $('#service-disambiguation').is(":checked");
    
    // FP file
    var fpFile = $('#service-fp-file').get(0).files[0];
    
    // Build object and json representation
    var service = new Object();
    service.name = name;
    service.logo = [];
    service.dictionaries = dictionaries;
    service.models = models;
    service.parsingLevel = parsingLevel;
    service.noIds = noIds;
    service.groupsNormalization = groupsNormalization;
    service.falsePositives = "";
    service.abbreviations = abbreviations;
    service.disambiguation = disambiguation;
    var serviceJson = JSON.stringify(service);
    
    // Build form data
    var formData = new FormData();
    formData.append('service_data', serviceJson);
    formData.append('service_logo', logoFile);
    formData.append('service_fp', fpFile);
    
    // Disable save button while saving
    var $saveButton = $("#service-save-button");
    disableButton($saveButton, "Saving ...");
        
    // Add service throught a REST web service
    var addServiceUrl = window.location.origin + "/services/addService/";
    $.ajax({
        url: addServiceUrl,
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function() {
            
            // Reload services data
            loadServicesData(getServicesUrl, annotationBaseUrl);
            
            // Success alert
            createNotification('Success', "Service '" + name + "' has been added with success", 'success');            
            
            // Close add modal window
            $('#add-edit-service-modal').modal('hide');
            
            // Enable button
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

/**
 * Edit service save click event.
 * @param serviceId service id
 */
function editServiceEvent(serviceId) {
    
    // Name
    var name = $('#service-name').val().trim();
    
    // Logo
    var logoFile = $('#service-logo').get(0).files[0];
    
    // Dicitionaries
    var dictionaries = [];    
    $("#selected-dictionaries").children('button').each( function () {       
        var $dictionary = $(this);
        if ($dictionary.is(':visible')) {           
            var dictionaryName = $dictionary.contents().get(2).nodeValue.trim();
            dictionaries.push(dictionaryName);
        }       
    });
    
    // Semantic groups normalization
    var groupsNormalization = {};    
    $("#semantic-groups").children('div').each( function () {       
       var $entry = $(this);
       var group = $entry.find('span').contents().get(0).nodeValue.trim();
       var normalizedName = $entry.find('input').val().trim();
       groupsNormalization[group] = normalizedName;
    });
    
    // Models
    var models = [];    
    $("#selected-models").children('button').each( function () {       
        var $model = $(this);
        if ($model.is(':visible')) {           
            var modelName = $model.contents().get(2).nodeValue.trim();
            models.push(modelName);
        }
    });
    
    // Parsing level
    var parsingLevel = $('#service-parsing').find(":selected").text();
    
    // No ids
    var noIds = $('#service-no-ids').is(":checked");
    
    // Abbreviations
    var abbreviations = $('#service-abbreviations').is(":checked");
    
    // Disambiguation
    var disambiguation = $('#service-disambiguation').is(":checked");
    
    // FP file
    var fpFile = $('#service-fp-file').get(0).files[0];
    
    // Build object and json representation
    var service = new Object();
    service.id = serviceId;
    service.name = name;
    service.logo = [];
    service.dictionaries = dictionaries;
    service.models = models;
    service.parsingLevel = parsingLevel;
    service.noIds = noIds;
    service.groupsNormalization = groupsNormalization;
    service.falsePositives = null;
    service.abbreviations = abbreviations;
    service.disambiguation = disambiguation;
    var serviceJson = JSON.stringify(service);
    
    // Build form data
    var formData = new FormData();
    formData.append('service_data', serviceJson);
    formData.append('service_logo', logoFile);
    formData.append('service_fp', fpFile);
    
    // Disable save button while saving
    var $saveButton = $("#service-save-button");
    disableButton($saveButton, "Saving ...");
        
    // Edit service throught a REST web service
    var editServiceUrl = window.location.origin + "/services/editService/";
    $.ajax({
        url: editServiceUrl,
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        success: function() {
            
            // Reload services data
            loadServicesData(getServicesUrl, annotationBaseUrl);
            
            // Success alert
            createNotification('Success', 'Service ' + name + ' has been edited with success', 'success');
            
            // Close edit modal window
            $('#add-edit-service-modal').modal('hide');
            
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
 * Remove a service.
 * @param service service
 */
function removeService(service) {
        
    url = removeServiceUrl + service.id;
    $.ajax({
        url: url,
        type: 'POST',
        cache: false,
        contentType: false,
        processData: false,
        success: function() {        

            // Reload dictionaries data
            loadServicesData(getServicesUrl);

            // Success alert
            createNotification('Success', "Service '" + service.name + "' has been deleted with success.", 'success');                    
        },
        error: function (xhr, status, error) {

            // Error alert
            createNotification('Error', xhr.responseText, 'error');
        }
    });
}

/**
 * Set edit modal.
 * @param service service
 */
function setEditModal(service) {    
    
    var $modal = $("#add-edit-service-modal");
    
    // Reset modal
    resetModal($modal);
    
    // Title
    $modal.find("#modal-title").html("Edit Service");
    
    // Name
    var $name = $modal.find("#service-name");
    $name.val(service.name);
    $name.prop('disabled', true);
    
    // Dictionaries
    var $availableDictionaries = $modal.find("#available-dictionaries");
    
    $availableDictionaries.children('button').each( function () {
        var dictionaryName = $(this).contents().get(2).nodeValue.trim();
        var nameWithoutSpacesDots = dictionaryName.replace(/[\s\.]/g, '');
        var $availableButton = $(this);
        var $selectedButton = $modal.find("#selected-dictionary-"+nameWithoutSpacesDots);
       
        if ($.inArray(dictionaryName, service.dictionaries) > -1) {
            $availableButton.hide();
            $selectedButton.show();
        } else {
            $selectedButton.hide();
            $availableButton.show();            
        }
    });
    
    // Models
    var $availableModels = $modal.find("#available-models");
    
    $availableModels.children('button').each( function () {
        var modelName = $(this).contents().get(2).nodeValue.trim();
        var nameWithoutSpaceDots = modelName.replace(/[\s\.]/g, '');
        var $availableButton = $(this);
        var $selectedButton = $modal.find("#selected-model-"+nameWithoutSpaceDots);
       
        if ($.inArray(modelName, service.models) > -1) {
            $availableButton.hide();
            $selectedButton.show();
        } else {
            $selectedButton.hide();
            $availableButton.show();            
        }
    });
    
    // Semantic groups normalization
    currentGroups = {};
    $("#semantic-groups").empty();
    addSemanticGroupNormalizationEntries(service.groupsNormalization);
    
    // Parsing Level
    var $parsingLevel = $modal.find("#service-parsing");
    $parsingLevel.val(service.parsingLevel).change();
    
    // No ids
    var $noIds = $modal.find("#service-no-ids");
    $noIds.prop('checked', service.noIds);
    
    // Abbreviations
    var $abbreviations = $modal.find("#service-abbreviations");
    $abbreviations.prop('checked', service.abbreviations);
    
    // Disambiguation
    var $disambiguation = $modal.find("#service-disambiguation");
    $disambiguation.prop('checked', service.disambiguation);
    
    // Change add or edit event flag
    addEditModalFlag = false;
    serviceId = service.id;

    // Enable save button
    var $saveButton = $("#service-save-button");
    $saveButton.attr("disabled", false);
}

/**
 * Reset modal.
 * @param $modal modal to reset
 */ 
function resetModal($modal) {
    
    var $form = $("#add-edit-service-form");
    
    // Reset modal form
    $modal.find('form')[0].reset();
    
    // Reset validation fields
    removeSemanticGroupsNormalizationEntries();
    $form.data('formValidation').resetForm();
}

/**
 * Set add modal.
 */
function setAddModal() {    
    
    var $modal = $("#add-edit-service-modal");
    
    // Reset modal
    resetModal($modal);
    
    // Title
    $modal.find("#modal-title").html("Add Service");
    
    // Name
    var $name = $modal.find("#service-name");
    $name.prop('disabled', false);
    
    // Dictionaries
    var $availableDictionaries = $modal.find("#available-dictionaries");
    var $selectedDictionaries = $modal.find("#selected-dictionaries");
    
    $availableDictionaries.children('button').each( function () {
        $(this).show();
    });
    
    $selectedDictionaries.children('button').each( function () {
        $(this).hide();
    });
    
    // Models
    var $availableModels = $modal.find("#available-models");
    var $selectedModels = $modal.find("#selected-models");
    
    $availableModels.children('button').each( function () {
        $(this).show();
    });
    
    $selectedModels.children('button').each( function () {
        $(this).hide();
    });
    
    // Semantic groups normalization
    currentGroups = {};
    $("#semantic-groups").empty();
    
    // Change add or edit event flag
    addEditModalFlag = true;
    
    // Enable save button
    var $saveButton = $("#service-save-button");
    $saveButton.attr("disabled", false);
}

/**
 * Add Service button click event.
 */
$("#add-service-button").on('click', function() {
    
    // Set add modal
    setAddModal();
    
    // Show modal
    $('#add-edit-service-modal').modal('show');
});

/**
 * Add semantic group normalization entry.
 * @param dm dictionary or model
 */
function addSemanticGroupNormalizationEntry(dm) {
      
    // Add a new entry if it doesn't exist yet
    if (!(dm.group in currentGroups)) {
        currentGroups[dm.group] = 1;    
    
        var group = new Object();
        group.name = dm.group;
    
        var source = $("#semantic-group-entry-template").html();
        var template = Handlebars.compile(source);
        var html = template({groups: [group]});
        $("#semantic-groups").append(html);
        
        // Add field validation
        var options = {
            validators: {
                regexp: {
                    regexp: /^[a-zA-Z0-9._-\s]*$/,
                    message: 'The mapping name can only consist of alphabetical, number, underscore(_), hifen(-), dot(.) and space( ) characters'
                }
            }
        };
        $('#add-edit-service-form').formValidation('addField', 'group-entry-'+dm.group, options);
        
    } else {
        currentGroups[dm.group]++; 
    }
}

/**
 * Add semantic group normalization entries.
 * @param groups map with groups and normalized names
 */
function addSemanticGroupNormalizationEntries(groups) {       
        
        var entries = [];
        
        $.each(groups, function (groupName, normalizedName) {         
            if (!(groupName in currentGroups)) {
                currentGroups[groupName] = 1;    
    
                var group = new Object();
                group.name = groupName;
                group.norm = normalizedName;
                
                entries.push(group);
            } else {    
                currentGroups[groupName]++; 
            }
        });
    
        var source = $("#semantic-group-entry-template").html();
        var template = Handlebars.compile(source);
        var html = template({groups: entries});
        $("#semantic-groups").append(html);
}

/**
 * Remove semantic group normalization entry.
 * @param dm dictionary or model
 */
function removeSemanticGroupNormalizationEntry(dm) {
        
    // Remove entry if group isn't from any dicitionary and model
    if (currentGroups[dm.group] === 1) {
        delete currentGroups[dm.group];
        $("#group-"+dm.group).remove();
        
        // Remove field validation
        $('#add-edit-service-form').formValidation('removeField', 'group-entry-'+dm.group);
    } else {
        currentGroups[dm.group]--; 
    }
}

/**
 * Remove semantic group normalization entries.
 */
function removeSemanticGroupsNormalizationEntries() {
    $.each(currentGroups, function (groupName, x) {
        $("#group-"+groupName).remove();
        $('#add-edit-service-form').formValidation('removeField', 'group-entry-'+groupName);
    });
    
    currentGroups = {};
}    

/**
 * Sets validation of form fields.
 */
function setFormValidation() {
    
    // Create new validator for servicce resources
    FormValidation.Validator.resources = {
        /**
         * @param {FormValidation.Base} validator The validator plugin instance
         * @param {jQuery} $field The jQuery object represents the field element
         * @param {Object} options The validator options
         * @returns {Boolean}
         */
        validate: function(validator, $field, options) {
            
            var counter = 0;
            
            // Dicitionaries
            $("#selected-dictionaries").children('button').each( function () {       
               var $dictionary = $(this);
               if ($dictionary.is(':visible')) {           
                   counter++;
               }       
            });
    
            // Models
            $("#selected-models").children('button').each( function () {       
               var $model = $(this);
               if ($model.is(':visible')) {           
                   counter++;
               }       
            });
    
            // Verify if it has at least one resource
            if (counter === 0) {
                return false;
            }
            
            return true;
        }
    };
    
    // Set form validator
    validationOptions = {
        framework: 'bootstrap',
        excluded: [':disabled'],
        icon: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        },
        fields: {
            'service-name': {
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
            'selected-dictionaries': {
                validators: {
                    resources: {
                        message: 'It is required at least one dictionary or model'
                    }
                }
            },
            'selected-models': {
                validators: {
                    resources: {
                        message: 'It is required at least one dictionary or model'
                    }
                }
            }
        },
        onSuccess: function(e) {
            e.preventDefault();
            
            if (addEditModalFlag) {
                addServiceEvent();
            } else {
                editServiceEvent(serviceId);
            }
        }
    }
    $('#add-edit-service-form').formValidation(validationOptions);
}

/**
 * Revalidate service resources (dictionaries and models).
 */
function revalidateResources() {   
    $("#add-edit-service-form").formValidation('revalidateField', 'selected-dictionaries');
    $("#add-edit-service-form").formValidation('revalidateField', 'selected-models');
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