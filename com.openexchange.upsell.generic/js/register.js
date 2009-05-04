/**
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License, Version 2 as published
* by the Free Software Foundation.
*
* Copyright (C) 2004-2007 Open-Xchange, Inc.
* Mail: info@open-xchange.com 
* 
* @author: Holger Achtziger
* @author: Benjamin Otterbach
*
*/

//Upsell layer
register("Feature_Not_Available", showUpsellLayer);

function showUpsellLayer(feature, win) {
 	var html = language_path = config.modules["com.openexchange.upsell.generic"].html;
	if (html) {
		var upsellMessage = config.modules["com.openexchange.upsell.generic"].html[configGetKey("language")];
		if (upsellMessage) {
			// setting corewindow to default if win is not defined
			win = win || corewindow;

			/* 
			 * Define HTML content which will be gathered from the bundle configfile
			 */
			var myDiv = newnode("div",{ textAlign: "left", padding: "1px" }, 0, [], win.document);
			myDiv.innerHTML = upsellMessage;
			 
			// calling the newAlert function to open the dialog at the given window
			win.newAlert("Advice " + feature, null, AlertPopup.close, myDiv);
		}
	}
};

/**
* upsell function in the portal pannel: syncronization for outlook and mobility
*/
if ((!(config.modules.infostore.module))&&(config.modules.portal.module)) {
	var syncupsell = MenuNodes.createSmallButtonContext("syncronisation", "Syncronisation");
	MenuNodes.createSmallButton(syncupsell,"buttonol", "Outlook", "", "", showUpsellLayer);
	MenuNodes.createSmallButton(syncupsell,"buttonsy", "Mobility", "", "", showUpsellLayer);

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