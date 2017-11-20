var suggestion_langs = ['it','en','el','sv'];

$(function () {
    requestToken();
});

function init() {
    $.ajax({
        url: "/classes",
        success: function (data) {
            $("#classname").typeahead({
                source: data
            });
        },
        error: function (a) {
            var error = a.responseJSON.error;
            var exception = a.responseJSON.exception;
            alert(error + ": " + exception);
        }
    });
}


function requestToken() {
    init();
    /*$.post("https://sso.sparkworks.net/aa/oauth/token",
        "client_id=gaia-prato&client_secret=27d7ecb0-4563-4815-95c8-98f55899b852&scope=read&grant_type=password&username=gaia-prato&password=cmRxm2",
        function (resp) {
            var token = resp.access_token;
            setAuth(token);
            init();
        })*/
}
function setAuth(auth) {
    $.ajaxSetup({
        beforeSend: function (xhr) {
            xhr.setRequestHeader("Accept", "application/json");
            xhr.setRequestHeader("Authorization", "Bearer " + auth);
        }
    });
    console.info("Token set: " + auth)
}

function queryFields(classname) {
    $.ajax({
        url: "/rules/" + classname + "/default?force=true",
        success: function (data) {
            createForm(data);
        },
        error: function (a) {
            var error = a.responseJSON.error;
            var exception = a.responseJSON.exception;
            alert(error + ": " + exception);
        }
    });
}


function createForm(data) {
    var f_template = $('#fieldtemplate').html();
    //var s_template = $('#suggestiontemplate').html();
    var s_template = $('#suggestiontemplate2').html();
    fields = data.fields;
    suggestions = data.suggestion;
    for (f in fields) {
        var view_f = {key: f, value: fields[f].value, description: fields[f].description, required: fields[f].required};
        var html = Mustache.to_html(f_template, view_f);
        $("#fieldscontainer").append(html);
    }
    for (var l in suggestion_langs){
        var lang = suggestion_langs[l];
        if(suggestions==null)
            var view_s = {key: lang, value: ""};
        else
            var view_s = {key: lang, value: suggestions[lang]};
        var html = Mustache.to_html(s_template, view_s);
        $("#suggestioncontainer").append(html);
    }
}

$(function bind() {
    $("#queryform").submit(function (o) {
        o.preventDefault();
        $("#fieldscontainer").empty();
        $("#suggestioncontainer").empty();
        queryFields($("#classname").val());
    });
    $("#bodyform").submit(function (o) {
        o.preventDefault();
        editRule($("#classname").val(), createBody());
    })
});

function createBody() {
    var body = {};
    var fields = {};
    var suggestion = {};
    var children = $("#fieldscontainer").children();
    for (var i = 0; i < children.length; i++) {
        fields = (getFieldContent(children[i],fields));
    }
    var children = $("#suggestioncontainer").find("[data-lang]");
    for (var i = 0; i < children.length; i++) {
        var result = getSuggestionContent(children[i]);
        suggestion[result["lang"]] = result["value"];
    }
    body['fields'] = fields;
    body['suggestion'] = suggestion;
    return body;
}

function getFieldContent(field,fields) {
    var value, description, required;
    var f = $(field).find("[data-value]");
    if (f.length > 0) {
        f = f[0];
        if($(f).val()=="" || f == undefined)
            return fields;
        value = Number($(f).val());
        if (isNaN(value))
            value = $(f).val();
    }
    var f = $(field).find("[data-description]");
    if (f.length > 0) {
        f = f[0];
        description = $(f).val();
    }
    var f = $(field).find("[data-required]");
    if (f.length > 0) {
        f = f[0];
        required = Boolean($(f)[0].checked);
    }
    var key = $(field).data("field");
    fields[key] = {"value": value, "description": description, "required": required};
    return fields;
}

function getSuggestionContent(suggestion) {
    var result = {};
    result['lang'] = $(suggestion).data("lang");
    result['value'] = $(suggestion).val();
    return result;
}

function editRule(classname, body){
    $.ajax({
        url: "/rules/" + classname + "/default",
        type: 'PUT',
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        data: JSON.stringify(body),
        success: function(result) {
            alert("Done");
        },
        error: function (a) {
            var error = a.responseJSON.error;
            var exception = a.responseJSON.exception;
            alert(error + ": " + exception);
        }
    });
}