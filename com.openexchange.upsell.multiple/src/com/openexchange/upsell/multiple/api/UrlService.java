package com.openexchange.upsell.multiple.api;

import java.util.Map;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;


public interface UrlService {
	
	// parameters from UI/USER/SESSION
	public static String MAP_ATTR_USER = "_USER_";
	public static String MAP_ATTR_PWD = "_PWD_";
	public static String MAP_ATTR_MAIL = "_MAIL_";
	public static String MAP_ATTR_LOGIN = "_LOGIN_";
	public static String MAP_ATTR_IMAP_LOGIN = "_IMAPLOGIN_";
	public static String MAP_ATTR_CID = "_CID_";
	public static String MAP_ATTR_USERID = "_USERID_";
	public static String MAP_ATTR_CLICKED_FEATURE = "_CLICKED_FEATURE_";
	public static String MAP_ATTR_UPSELL_PLAN = "_UPSELL_PLAN_";
	public static String MAP_ATTR_LANGUAGE = "_LANG_";
	public static String MAP_ATTR_PURCHASE_TYPE = "_PURCHASE_TYPE_"; // buy or trial button clicked
	public static String MAP_ATTR_INVITE = "_INVITE_";

	/**
	 * Map contains all parameters which are available in users session and already contain the needed values.
	 * @param parameters
	 * @return
	 * @throws UrlGeneratorException
	 */
    public String generateUrl(Map parameters,Session sessionObj, User user, User ctxadmin, Context ctx) throws UrlGeneratorException;
}
