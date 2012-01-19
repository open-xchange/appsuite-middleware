var innode = new ox.Configuration.InnerNode("configuration/com.openexchange.custom.spamexperts", _("Antispam Options"));

// antispam panel page
var spamexperts_user_panel = new ox.Configuration.LeafNode("configuration/com.openexchange.custom.spamexperts/user_panel", _("Management"));
var keb_iframe_email_alias = new ox.Configuration.IFrame( spamexperts_user_panel, _("Antispam"),"newInfoItemHidden.html"); 
keb_iframe_email_alias.enter = getKEBSessionAndRedirect(keb_iframe_email_alias);

//create a new menu Object for the panel and define the unique id
var mymenue = MenuNodes.createSmallButtonContext("user_panel", _("Antispam Options"));

// assign img to this new button object
MenuNodes.createSmallButton(mymenue,"buttonx_keb", _("Antispam Options"), "themes/default/img/menu/edit.gif", "themes/default/img/menu/edit_d.gif", clickx);
addMenuNode(mymenue.node, MenuNodes.FIXED, 39);

//Following makes the new panel button active
changeDisplay("portal", "user_panel");
changeDisplay("configuration", "user_panel");
showNode("user_panel");

function clickx() {
        spamexperts_user_panel.click();
}

/**
 * 
 * Function to retrieve valid panel Session which is generated via OX Plugin and then redirect
 * 
 * @return
 */
function getKEBSessionAndRedirect(source_iframe){
	return function getKEBSessionAndRedirect(){
		ox.JSON.get("/ajax/spamexperts/panel?session="+parent.session+"&action=generate_panel_session", spamexperts_response_success_handler(source_iframe),spamexperts_response_error_handler(source_iframe));
	};
}



/**
 * Redirect to spampanel with valid session and target page
 * @param reply
 * @return
 */
function spamexperts_response_success_handler(source_iframe){
	return function keb_response_success_handler(reply){		
		source_iframe.content.src =  reply.data.panel_web_ui_url+""+reply.data.panel_session;
	};
}

/**
 * If error occured, show an error message to the users
 * @param reply
 * @param status
 * @return
 */
function spamexperts_response_error_handler(source_iframe){
	return function keb_response_error_handler(reply,status){
		source_iframe.content.src =  "plugins/com.openexchange.custom.spamexperts/error.html";
	};
}
