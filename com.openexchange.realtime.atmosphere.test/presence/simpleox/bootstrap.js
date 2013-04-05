define("simpleox/bootstrap", ["simpleox/login"], function (login) {
    window.ox = window.ox || {};
    
    options = {};
    var def = $.Deferred();
    
    function drawLoginForm() {
        var $form = $(".loginForm"),
            $name, $password;
        if (!$form) {
            return def;
        }
        $form.css('margin-top', '200px');
        
        $form.append($name = $('<input type="text">'), '<br/>', $password = $('<input type="password">'), '<br/>', '<button>Login</button>');
        $form.find("button").on("click", function () {
	        ox.userName=$name.val();
            login.login($name.val(), $password.val()).done(function (resp) {
                if (resp.session) {
                     ox.session = resp.session;
                     login.store(resp.session);
                     def.resolve(resp.session);
                    $form.remove();
                } else {
                    alert("Wrong login name or password");
                }
            });
        });
        $form.keypress(function(event) {
        	if (event.which == 13) {
        		ox.userName=$name.val();
                login.login($name.val(), $password.val()).done(function (resp) {
                    if (resp.session) {
                         ox.session = resp.session;
                         login.store(resp.session);
                         def.resolve(resp.session);
                        $form.remove();
                    } else {
                        alert("Wrong login name or password");
                    }
                });
            }
        }) 
    }
    
    hash = window.location.hash;
    if (hash) {
        hash = hash.substring(1).split('&');
        jQuery.each(hash, function (index, tuple) {
            tuple = tuple.split('=');
            options[tuple[0]] = tuple[1];
        });
    }
    
    ox.options = options;

    if (options.session) {
        // 1. Was the session passed via parameter?
        ox.session = options.session;
        login.store();
        def.resolve(resp.session);
        return def;
    } else {
        // 2. Autologin
        login.autologin().done(function (resp) {
            if (resp.session) {
                ox.session = resp.session;
                login.store(resp.session);
                def.resolve(resp.session);
                
            } else {
                // 3. login & password via URL
                if (options.name && options.password) {
                    login.login(options.name, options.password).done(function (resp) {
                        if (resp.session) {
                            ox.session = resp.session;
                            login.store(resp.session);
                            def.resolve(resp.session);
                        } else {
                            // 4. draw login form
                            drawLoginForm();
                        }
                    });
                } else {
                    drawLoginForm();
                }
            }
        });
    }
    
    
    return def;
    
});
