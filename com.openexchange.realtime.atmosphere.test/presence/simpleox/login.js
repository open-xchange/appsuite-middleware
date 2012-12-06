define("simpleox/login", function () {
    var options = {dataType: "text"};
    
    function parse(resp) {
        return JSON.parse(resp);
    }
    
    return {
        login: function (name, password) {
            return $.ajax("/ajax/login?action=login&name=" + name + "&password=" + password + "&client=com.openexchange.ox.gui.dhtml", options).pipe(parse);
        },
        
        store: function (session) {
            return $.ajax("/ajax/login?action=store&session=" + session, options).pipe(parse);
        },
        
        autologin: function () {
            return $.ajax("/ajax/login?action=autologin&client=com.openexchange.ox.gui.dhtml", options).pipe(parse);
        } 
    }
});