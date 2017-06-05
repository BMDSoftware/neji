// PNotify
PNotify.prototype.options.styling = "fontawesome";

/**
 * Creates a notification.
 * @param title notification title
 * @param text notification text
 * @param type notification type
 */
function createNotification(title, text, type) {
    new PNotify({
        title: title,
        text: text,
        type: type,
        opacity: .95,
        nonblock: {
            nonblock: true
        }
    });
}

/**
 * HandleBars register helper to remove white spaces from a string.
 * @param passedString passed string
 * @return string without white spaces
 */
Handlebars.registerHelper('removeStringWhiteSpacesDots', function(passedString) {
    var stringWithoutSpaces = passedString.replace(/[\s\.]/g, '');
    return stringWithoutSpaces;
});

$(document).ready(function() { 
   
    // Set navbar for regular users / administrators
    setNavbar();
    
});

/**
 * Set navbar for regular users / administrators when page loads.
 */
function setNavbar() {    
    $.ajax({
        url: window.location.origin + "/manage/getUsername",
        type: 'GET',
        dataType: 'text',   
        success: function (username) {            
            var source = $("#navbar-container-template").html();
            var template = Handlebars.compile(source);
            var object = new Object();
            object.username = username;
            var html = template(object);
            $("#navbar-container").html(html);
            
            if (username !== undefined) {
                $("#logout-button").on('click', function(){
                    logout();
                });
            }
        },
        error: function (xhr, status, error) {
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');
        }    
    });       
}

function logout() {
    
    $.ajax({
        url: window.location.origin + "/manage/logout",
        type: 'POST',
        cache: false,
        contentType: false,
        processData: false,
        success: function() {
            window.location.href = window.location.origin;
        },
        error: function (xhr, status, error) {
            
            // Error alert
            createNotification('Error', xhr.responseText, 'error');
        }
    });
}