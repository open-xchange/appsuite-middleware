/**
 * UI for managing Black/White Lists.
 */



function MyList() {
    ox.Configuration.EditableList.apply(this, arguments);
}

MyList.prototype = extend(ox.Configuration.EditableList, {
      
        addContent: function(node_id) {
            var selection = new Selection();
            this.grid = new LiveGrid([{
                text: addTranslated(this.label),
                index: 1,
                clear: LiveGrid.makeClear(""),
                set: LiveGrid.defaultSet
            }], selection);
            this.grid.emptylivegridtext = this.emptyText;
            this.head = newnode("div");
            this.body = newnode("div",
                { height: this.height, position: "relative" });
            this.head.appendChild(this.grid.getHeader());
            this.grid.getTable(this.body);
            this.applyEnabled();
      
            this.node = this.parent.addRow(newnode("div", 0, 0, 
                [this.head, this.body]));
            var id = "ox.Configuration.EditableList."
                     + ox.Configuration.EditableList.id++;
            var menu = MenuNodes.createSmallButtonContext(id, this.section);
            var Self = this; 
            MenuNodes.createSmallButton(menu, id + ".add", _("Add"),
                getFullImgSrc("img/menu/add_signature.gif"), getFullImgSrc("img/menu/add_signature.gif"),
                function() {
                    if (Self.add) Self.add(function(values) {
                        if (!values.length) return;
                        var oldlen = Self.values.length;
                        Self.values = Self.values.concat(values);
                        var data = new Array(values.length);
                        for (var i = 0; i < values.length; i++)
                            data[i] = [i + oldlen, Self.getText(values[i])];
                        Self.storage.append(data);
                        Self.grid.focus = oldlen;
                        Self.grid.showFocus();
                    });   
                });   
            MenuNodes.createSmallButton(menu, id + ".remove", _("Remove"),
                getFullImgSrc("img/menu/remove_signature.gif"), getFullImgSrc("img/menu/remove_signature.gif"),
                del); 
            this.grid.events.register("Deleted", del); 
           function del() {
                var indices = Self.grid.selection.getSelected();
                var deleted = {};
                var values = new Array(indices.length);
                for (var i = 0; i < indices.length; i++) {
                    deleted[indices[i]] = true;
                    values[i] = Self.values[indices[i]];
                }
                if (Self.onDelete) {
                    Self.onDelete(values, cont, dontDelete);
                } else {
                    cont();
                }
                function cont() {
                    for (var d = 0; d < Self.values.length && !deleted[d]; d++);
                    for (var s = d + 1; s < Self.values.length; s++) {
                        if (!deleted[s]) Self.values[d++] = Self.values[s];
                    }
                    Self.values.length = d;
                    Self.set(Self.values);
                }
                function dontDelete(index) { delete deleted[indices[index]]; }
            }
            addMenuNode(menu.node, MenuNodes.FIXED,
                        ox.Configuration.EditableList.id);
            changeDisplay(node_id, id);
            selection.events.register("Selected", function(count) {
                menuselectedfolders = [];
                triggerEvent("OX_SELECTED_ITEMS_CHANGED", count);
                triggerEvent("OX_SELECTION_CHANGED", count);
            });
            //menuarrows[node_id] = {};
            register("OX_SELECTED_ITEMS_CHANGED", function() {
                menuglobalzaehler = 0;
                //menuarrows[node_id][id] = [];
                menu_display_contents(node_id, id, true, id + ".add");
                menu_display_contents(node_id, id, selection.count > 0,
                    id + ".remove");
            });
            ox.UI.Widget.prototype.addContent.apply(this, arguments);
        }
});




var blacklist_node = new ox.Configuration.LeafNode("configuration/mail/blacklist", "Blacklist");
var whitelist_node = new ox.Configuration.LeafNode("configuration/mail/whitelist", "Whitelist");

var blacklist_page = new ox.Configuration.Page(blacklist_node, _("Your current blacklisted addresses"),false);
blacklist_page.init = initListPage(blacklist_page,"blacklist",_("Email Addresses / Domains"),_("This list has no content"),_("List"));

var whitelist_page = new ox.Configuration.Page(whitelist_node, _("Your current whitelisted addresses"),false);
whitelist_page.init = initListPage(whitelist_page,"whitelist",_("Email Addresses / Domains"),_("This list has no content"),_("List"));

function initListPage(the_page,list_type,add_remove_text,empty_list_text,list_title){
	var list;
	the_page.viewModified = ox.Configuration.IFrame.prototype.viewModified; // workaround for bug in configuration.js
	return function(){
		list = new MyList(add_remove_text, "16em", list_title);
		list.emptyText = empty_list_text;
				
		var addContinuation = emptyFunction;
		list.add = function(cont) {
	        newInput(_("Add new address"), "", _("Address: "), "", AlertPopup.OKCANCEL,addAdressToList,null,null,null,"input");
		
		  function addAdressToList() {
		   var address = $("create_window_text_field").value;
	        (new JSON).get(AjaxRoot + "/blackwhitelist?module="+list_type+"&data="+address+"&action=add&session="+session, null, handleAddData);
        		function handleAddData(data) {
		            cont([address]);
        		}
		  }

	    	};
	
		list.onDelete = function(entries,cont) {
			var naa = new Join(cont);
			for (var a = 0; a<entries.length;a++){
				var address = entries[a];
	        		 (new JSON).get(AjaxRoot + "/blackwhitelist?module="+list_type+"&data="+address+"&action=delete&session="+session, null, naa.add() );
			}
	    	};
	
	
		// get initial list and fill up
		the_page.load = function (cont){
			ox.JSON.get(AjaxRoot + "/blackwhitelist?module="+list_type+"&action=get&session=" +session,handleListData);
			function handleListData(data) {		    
			    cont(data.data);  
			}
		}

	    	the_page.addWidget(list,"items");
	}




}


function fetchListData(list_type) {
        (new JSON).get(AjaxRoot + "/blackwhitelist?module="+list_type+"&action=get&session=" +session, null, handleListData);
        function handleListData(data) {
            return data.data.items;	    
            
        }
}
