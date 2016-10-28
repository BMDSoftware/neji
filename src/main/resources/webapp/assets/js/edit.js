$(document).ready(function() {

    $("#checkAll1").click(function(){
        $("input[name='dict']").attr('checked','checked');
    });

    $("#checkNone1").click(function(){
        $("input[name='dict']").removeAttr('checked');
    });

    $("#checkAll2").click(function(){
        $("input[name='model']").attr('checked','checked');
    });

    $("#checkNone2").click(function(){
        $("input[name='model']").removeAttr('checked');
    });

    $('#parserLevel').change(function() {
        $('#parserLevelSelected').val($(this).find(":selected").val());
    });

});
