package com.openexchange.upsell.multiple.api;

import java.util.Map;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;


public interface UpsellURLService {
	
	

	/**
	 * Map contains all parameters which are available in users session and already contain the needed values.
	 * @param parameters
	 * @return
	 * @throws URLGeneratorException
	 */
    public String generateUrl(Map<UpsellURLParametersMap, String> parameters,Session sessionObj, User user, User ctxadmin, Context ctx) throws URLGeneratorException;
}
