var ox = {
    retrieveSession: new $.Deferred()
}

$(function () {
    var hash;
    ox.options = {};
    
    hash = window.location.hash;
    if (hash) {
        hash = hash.substring(1).split('&');
        jQuery.each(hash, function (index, tuple) {
            tuple = tuple.split('=');
            ox.options[tuple[0]] = tuple[1];
        });
    }
    
   // 1 Session in URL
   if (ox.options.session) {
       ox.session = ox.options.session;
   }
   
   // 2 Autologin
   var deferred = $.ajax("/ajax/login?action=autologin&client=com.openexchange.ox.gui.dhtml").always(function (resp) {
        resp = JSON.parse(resp.responseText);
        if (resp.session) {
            ox.session = resp.session;
            ox.retrieveSession.resolve(ox.session);
        } else {
            if (ox.options.login && ox.options.password) {
                $.ajax("/ajax/login?action=login", {
                   data: {
                        name: ox.options.login,
                        password: ox.options.password
                   },
                   type: "POST"
                }).always(function (resp) {
                    resp = JSON.parse(resp.responseText);
                    if (resp.session) {
                        ox.session = resp.session;
                        ox.retrieveSession.resolve(ox.session);
                    } else {
                        $("body").append($("<h1>Could not generate a session</h1>"));
                    }
                });
            } else {
                $("body").append($("<h1>Could not generate a session</h1>"));
            }
         } 
    });
   
});
