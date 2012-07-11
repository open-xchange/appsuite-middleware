/**
 * UI for managing Black/White Lists.
 */

var innode = new ox.Configuration.InnerNode("configuration/com.openexchange.custom.parallels", _("Antispam Settings"));

var blacklist_node = new ox.Configuration.LeafNode("configuration/com.openexchange.custom.parallels/blacklist", _("Blacklist"));
var whitelist_node = new ox.Configuration.LeafNode("configuration/com.openexchange.custom.parallels/whitelist", _("Whitelist"));

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
	        (new JSON).get(AjaxRoot + "/parallels/openapi?module="+list_type+"&data="+address+"&action=add&session="+session, null, handleAddData);
        		function handleAddData(data) {
		            cont([address]);
        		}
		  }

	    	};
	
		list.onDelete = function(entries,cont) {
			var naa = new Join(cont);
			for (var a = 0; a<entries.length;a++){
				var address = entries[a];
	        		 (new JSON).get(AjaxRoot + "/parallels/openapi?module="+list_type+"&data="+address+"&action=delete&session="+session, null, naa.add() );
			}
	    	};
	
	
		// get initial list and fill up
		the_page.load = function (cont){
			ox.JSON.get(AjaxRoot + "/parallels/openapi?module="+list_type+"&action=get&session=" +session,handleListData);
			function handleListData(data) {		    
			    cont(data.data);  
			}
		}

	    	the_page.addWidget(list,"items");
	}




}


function fetchListData(list_type) {
        (new JSON).get(AjaxRoot + "/parallels/openapi?module="+list_type+"&action=get&session=" +session, null, handleListData);
        function handleListData(data) {
            return data.data.items;	    
            
        }
}







/**
 *
 * This is the javascript file used for all the UI plugins which are needed in the POA integration
 *
 *
 * Here we will handle our custom logout/direct link to the different branded login guis
 */
 
 function getServername() {
          try {
              var xmlhttp = null;
              try {
                  xmlhttp = new XMLHttpRequest();
              } catch (e) {
                  try {
                      xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
                  } catch (e) {
                      xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
                  }
              }
		     // ask server for data we need
              xmlhttp.open("GET", "/ajax/parallels/info?session="+session, false);             
             
              xmlhttp.send("");
              var error;
              if (xmlhttp.status == 200) {
                  var reply = Function("return " + xmlhttp.responseText)();
                  if (!reply.error) {			
					return reply.data.branding_url ; // this is the branded url for this context
                  } else error = reply.error;
              } else {
              	error = xmlhttp.statusText;
              }
          } catch (e) {
              error = e.message;
          }          
          
      }

var redirect_url = getServername();

// now overwrite all the URLs that are used by the UI when redirecting
logout_location = "[protocol]://"+redirect_url;
sessionExpired_location = "[protocol]://"+redirect_url;
directLink_location = "[protocol]://"+redirect_url+"#m=[module]&f=[folder]&i=[object_id]";

// disable the theme selector by setting it to display none
$ALL("tabConfiguration1").getElementsByTagName("table")[0].getElementsByTagName("tr")[4].style.display="none";

var sender_address_hiding_activated = ox.api.config.get("ui.parallels.sender_address_hiding_activated");
if (sender_address_hiding_activated) {
	var tbody = $ALL("contentMailConfigCompose").getElementsByTagName("table")[0].getElementsByTagName("tbody")[0];
	var alltrs = tbody.childNodes;
	var mycount = 0;
	 for (var i=0;mycount<11;i++) {
        	 if(alltrs[i].nodeType==1){
	           mycount++;
        	 }
	 }
	tbody.childNodes[i-1].style.display="none";
}