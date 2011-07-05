/**
 * UI for managing Black/White Lists.
 */


var blacklist_node = new ox.Configuration.LeafNode("configuration/mail/blacklist", _("Blacklist"));
var whitelist_node = new ox.Configuration.LeafNode("configuration/mail/whitelist", _("Whitelist"));

var blacklist_page = new ox.Configuration.Page(blacklist_node, _("Your current blacklisted addresses"),false);
blacklist_page.init = initListPage(blacklist_page,"blacklist",_("Email Addresses / Domains"),_("This list has no content"),_("List"));

var whitelist_page = new ox.Configuration.Page(whitelist_node, _("Your current whitelisted addresses"),false);
whitelist_page.init = initListPage(whitelist_page,"whitelist",_("Email Addresses / Domains"),_("This list has no content"),_("List"));

function initListPage(the_page,list_type,add_remove_text,empty_list_text,list_title){
	var list;
	the_page.viewModified = ox.Configuration.IFrame.prototype.viewModified; // workaround for bug in configuration.js
	return function(){
		list = new ox.Configuration.EditableList(add_remove_text, "16em", list_title);
		list.emptyText = empty_list_text;
				
		var addContinuation = emptyFunction;
		list.add = function(cont) {
	        newInput(_("Add new address"), "", _("Address: "), "", AlertPopup.OKCANCEL,addAdressToList,null,null,null,"input");
		
		  function addAdressToList() {
		   var address = $("create_window_text_field").value;
	        (new JSON).get(AjaxRoot + "/openapi?module="+list_type+"&data="+address+"&action=add&session="+session, null, handleAddData);
        		function handleAddData(data) {
		            cont([address]);
        		}
		  }

	    	};
	
		list.onDelete = function(entries,cont) {
			var naa = new Join(cont);
			for (var a = 0; a<entries.length;a++){
				var address = entries[a];
	        		 (new JSON).get(AjaxRoot + "/openapi?module="+list_type+"&data="+address+"&action=delete&session="+session, null, naa.add() );
			}
	    	};
	
	
		// get initial list and fill up
		the_page.load = function (cont){
			ox.JSON.get(AjaxRoot + "/openapi?module="+list_type+"&action=get&session=" +session,handleListData);
			function handleListData(data) {		    
			    cont(data.data);  
			}
		}

	    	the_page.addWidget(list,"items");
	}




}


function fetchListData(list_type) {
        (new JSON).get(AjaxRoot + "/openapi?module="+list_type+"&action=get&session=" +session, null, handleListData);
        function handleListData(data) {
            return data.data.items;	    
            
        }
}
