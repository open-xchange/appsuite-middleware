package com.openexchange.upsell.multiple.api;

public enum UpsellURLParametersMap {

	// parameters from UI/USER/SESSION
	MAP_ATTR_USER ( "_USER_"),
	MAP_ATTR_PWD ( "_PWD_" ),
	MAP_ATTR_MAIL ( "_MAIL_") ,
	MAP_ATTR_LOGIN ( "_LOGIN_"),
	MAP_ATTR_IMAP_LOGIN ( "_IMAPLOGIN_"),
	MAP_ATTR_CID ( "_CID_"),
	MAP_ATTR_USERID ( "_USERID_"),
	MAP_ATTR_CLICKED_FEATURE ( "_CLICKED_FEATURE_"),
	MAP_ATTR_UPSELL_PLAN ( "_UPSELL_PLAN_"),
	MAP_ATTR_LANGUAGE ( "_LANG_"),
	MAP_ATTR_PURCHASE_TYPE ( "_PURCHASE_TYPE_"),
	MAP_ATTR_INVITE ( "_INVITE_");

	public String propertyName;

	private UpsellURLParametersMap(String propertyName) {
		this.propertyName = propertyName;
	}

}
