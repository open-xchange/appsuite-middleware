/**
 * 
 * All content on this website (including text, images, source
 * code and any other original works), unless otherwise noted,
 * is licensed under a Creative Commons License.
 * 
 * http://creativecommons.org/licenses/by-nc-sa/2.5/
 * 
 * Copyright (C) 2004-2012 Open-Xchange, Inc.
 * Mail: info@open-xchange.com 
 * 
 * @author Viktor Pracht <viktor.pracht@open-xchange.com>
 * 
 */

var extras = new ox.Configuration.LeafNode("configuration/extras", _("Extras"));
extras.click = openConfigCenter;

var ccwin;

/**
 * Retrieves the config center URL from the server and, if not already happened,
 * opens it in a window. 
 */
function openConfigCenter() {
    new JSON().get(AjaxRoot + "/control?session=" + session, null,
        function(cb) {
            if (!ccwin || ccwin.closed) {
                ccwin = window.open(cb.data, "ConfigurationCenter");
            }
            if (ccwin) ccwin.focus();
        });
}
