package com.openexchange.messaging.sms.servlet;

import javax.servlet.http.HttpServletRequest;
import com.openexchange.tools.servlet.AjaxException;

/**
 * 
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 * 
 */
public class JSONUtility {

    /**
     * Parses specified parameter into an <code>String</code>.
     * 
     * @param request The request
     * @param parameterName The parameter name
     * @return The parsed <code>String</code> value
     * @throws AjaxException If parameter is not present or invalid in given request
     */
    protected static String checkStringParameter(final HttpServletRequest request, final String parameterName) throws AjaxException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp || 0 == tmp.length()) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, parameterName);
        }
        return tmp;
    }
	
}
