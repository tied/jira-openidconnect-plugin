(function ($) {

    var url = AJS.contextPath() + "/rest/oauth/admin/1.0/";

    $(document).ready(function () {
        // request the config information from the server
        $.ajax({
            url: url,
            dataType: "json"
        }).done(function (config) { // when the configuration is returned...
            // ...populate the form.
            // todo: check why classes not working
            $("#domain").val(config.domain);
            $("#client-id").val(config.clientId);
            $("#client-secret").val(config.clientSecret);
        });

        AJS.$("#admin").submit(function(e) {
            e.preventDefault();
            updateConfig();
        });
    });

    function updateConfig() {
        AJS.$.ajax({
            url: AJS.contextPath() + "/rest/oauth/admin/1.0/",
            type: "PUT",
            contentType: "application/json",
            data: '{ "domain": "' + AJS.$("#domain").attr("value").trim()
                + '", "clientId": "' + AJS.$("#client-id").attr("value").trim()
                + '", "clientSecret": "' + AJS.$("#client-secret").attr("value").trim()  + '" }',
            processData: false
        });
    }

})(AJS.$ || jQuery);