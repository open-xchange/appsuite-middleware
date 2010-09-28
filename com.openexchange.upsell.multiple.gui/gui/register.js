// ########################################## Upsell #####################################################
/*
possible event trigger

modules/calendar/freebusy
modules/calendar/team
modules/calendar/mini_calender
modules/calendar/new/add_participants
modules/calendar/new/remove_participants
modules/calendar/new/add_attachment
modules/calendar/new/delete_attachment
modules/contacts/new/add_attachment
modules/contacts/new/delete_attachment
modules/mail/save_to_infostore
modules/infostore/send_as_attachment
modules/infostore/send_as_link
modules/infostore/mail/save_to_infostore
modules/tasks/new/add_participants
modules/tasks/new/remove_participants
modules/tasks/new/add_attachment
modules/tasks/new/delete_attachment
configuration/mail/accounts/new
modules/folders/users

modules/infostore
modules/calender
modules/contacts
modules/mail
modules/portal
modules/tasks
modules/configuration

modules/outlook (set in this plugin)
modules/mobility (set in this plugin)

*/

var upsellPluginPath = "plugins/com.openexchange.upsell.multiple.gui/";
var upsellTemplate = upsellPluginPath+"upsell_txt.html";

// was hat er angeklickt? 
var feature_cl = "";

function loadUpsellFile(page, win, feature) {
	
var xmlhttp = JSON.prototype.getXmlHttp();
function callback() {
	if (xmlhttp.readyState != 4) return;
	xmlhttp.onreadystatechange = emptyFunction; // fixes IE memory leak
	
	var s = xmlhttp.responseText;
				
	var myDiv = newnode("div",{ verticalAlign: "middle", textAlign: "left", padding: "1px" }, 0, [], win.document);
	var winTitle = "Hinweis zu Mail Professional";
       
	var produktName = "Mail Professional";
   
	if (feature.match(/infostore/g)){
		winTitle = "Hinweis zum Infostore - Mail Professional";
	} else if (feature.match(/calender/g)){
		winTitle = "Hinweis zum Teamkalender - Mail Professional";
	} else if (feature === "modules/mobility"){	
		winTitle = "Hinweis zu Mobile E-Mail - Mail Push / Mail Professional";
		produktName = " Mail Push oder Mail Professional";
	} else if (feature === "modules/outlook"){
		winTitle = "Hinweis zu Mail Professional";		
	}
	feature_cl = feature;
	myDiv.innerHTML = s.replace(/_PRODUKT_/g, produktName);
			
	function openVersatelUrl(){
		generateId();
	}
	win.newConfirm(winTitle,null,258,openVersatelUrl,null,null,null,null,null,null,null,null,myDiv);

}
xmlhttp.onreadystatechange = callback;
xmlhttp.open("GET",page, true);
xmlhttp.send("");
}

//Upsell layer
register("Feature_Not_Available", showUpsellLayer);

function generateId(){
	ox.JSON.get("/ajax/upsell/multiple?session="+parent.session+"&action=get_method", openUrl);
}

function openUrl(reply){


	if(reply.data.upsell_method==="email"){
		// trigger email via servlet and show popup/resultpage
		ox.JSON.get("/ajax/upsell/multiple?session="+parent.session+"&action=send_upsell_email&feature_clicked="+feature_cl, openMailSuccess);
	}

	if(reply.data.upsell_method==="static"){
        	// trigger redirect and follow URL from response 
		getredirect();
	}
};

function openMailSuccess(reply){
	ox.JSON.get("/ajax/upsell/multiple?session="+parent.session+"&action=change_context_permissions&upsell_plan=groupware_premium", emptyfunction);
	alert("Bestellung erfolgreich an Ihren Dienstleister weitergeleitet");
}

function emptyfunction(reply){

}

function getredirect(){
	ox.JSON.get("/ajax/upsell/multiple?session="+parent.session+"&action=get_static_redirect_url&feature_clicked="+feature_cl, openRedirUrl);
}

function openRedirUrl(reply){
	window.open(reply.data.upsell_static_redirect_url, '_blank');
}




function showUpsellLayer(feature, win) {

win = win || corewindow;
loadUpsellFile(upsellTemplate, win, feature);
	
};

/**
* upsell function in the portal pannel: syncronization for outlook and mobility
*/
if ((!(config.modules.infostore.module))&&(config.modules.portal.module)) {
var syncupsell = MenuNodes.createSmallButtonContext("syncronisation", "NEU! Synchronisierung");
MenuNodes.createSmallButton(syncupsell,"buttonol", "Windows Outlook", getFullImgSrc("img/mail/email_priolow.gif"), "", function(){
	showUpsellLayer("modules/outlook");
});

MenuNodes.createSmallButton(syncupsell,"buttonsy", "Iphone/Windows Mobile", getFullImgSrc("img/mail/email_priolow.gif"), "", function(){
	showUpsellLayer("modules/mobility");
});

/* The pannel object gets the id 42 and gets displayed in the fixed area
 * possible areas are FIXED and DYNAMIC the id controls the order in the areas
 */
addMenuNode(syncupsell.node, MenuNodes.FIXED, 42);

//Following makes the new pannel options dynamic active/inactive
changeDisplay("portal", "syncronisation");
//show the upsell area in the pannel on the first login
showNode("syncronisation");
};


/**
*disable the tabs in detail views
*/
if (!(config.modules.infostore.module)) {
//calendar
$("panelAppointmentDetail3").onclick=function() {
	corewindow.triggerEvent('Feature_Not_Available',
	'modules/calendar/detail/attachment_tab', window); 
	return false; 
}; 
$("panelAppointmentDetail2").onclick=function() {
        corewindow.triggerEvent('Feature_Not_Available',
        'modules/calendar/detail/participants_tab', window);
  		return false;
}; 
//contatcs
$("taskpanel2").onclick=function() {
        corewindow.triggerEvent('Feature_Not_Available',
        'modules/contacts/detail/participants_tab', window);
        return false;
}; 
$("taskpanel4").onclick=function() {
        corewindow.triggerEvent('Feature_Not_Available',
        'modules/contacts/detail/participants_tab', window);
        return false;
}; 
}





