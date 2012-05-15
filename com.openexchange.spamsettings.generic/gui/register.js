/**
 * 
 * All content on this website (including text, images, source
 * code and any other original works), unless otherwise noted,
 * is licensed under a Creative Commons License.
 * 
 * http://creativecommons.org/licenses/by-nc-sa/2.5/
 * 
 * Copyright (C) Open-Xchange Inc., 2011
 * Mail: info@open-xchange.com 
 * 
 * @author Wolfgang Rosenauer <wolfgang.rosenauer@open-xchange.com>
 * 
 */

var widgets = ox.Configuration.DynamicForm.prototype.widgets;

if (!widgets.radiobuttons) {
    widgets.radiobuttons = function(def) {
        var rb = new ox.UI.RadioButtons();
        var vals = def.options.values;
        var values = [];
        for (var i = 0; i < vals.length; i++) values.push(noI18n(vals[i]));
        rb.setEntries(def.options.keys, values);
        return rb;
    };
}

var controllers = ox.Configuration.DynamicForm.prototype.controllers;

var node = new ox.Configuration.LeafNode("configuration/mail/spam",_("Spam settings"));
var page = new ox.Configuration.Page(node, _("Change spam settings"));

var value = {};
var formWidgets = new Array();

page.init = function() {
	var intro = new ox.UI.Text("");
	page.addWidget(intro);
};

page.load = function(cont) {
	ox.JSON.get(AjaxRoot + "/spamsettings?action=get&session=" + session,
		function(reply) {
			value = reply.data.value;
			createForm(reply.data.formDescription);
			cont(reply.data.value);
	});
};

page.save = function(data, cont) {
    ox.JSON.put(AjaxRoot + "/spamsettings?action=update&session=" + session,
        data,
        function() {
            value = data;
            ox.Configuration.info(_("Your settings have been saved."));
            cont();
        });
};

function createForm(formDescription) {
	// remove dynamic widgets before readding them
	for (var j = 0; j < formWidgets.length; j++) { 
		page.deleteWidget(formWidgets[j]); 
	}
    for (var i = 0; i < formDescription.length; i++) {
        var def = formDescription[i];
        var widget = widgets[def.widget](def);
        var controller = controllers[def.widget];
        formWidgets.push(widget);
        page.addWidget(widget, controller ? controller(def.name)
                                          : def.name);
        if ("defaultValue" in def) {
            widget.default_value = def.defaultValue;
            widget.set(def.defaultValue);
        }
    }
}
