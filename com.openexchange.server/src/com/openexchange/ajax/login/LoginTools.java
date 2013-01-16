/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.login;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.fields.LoginFields.AUTHID_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_IP_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * Shared methods for login operations.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class LoginTools {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(LoginTools.class);

    private LoginTools() {
        super();
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String encodeUrl(final String s, final boolean forAnchor) {
        return AJAXServlet.encodeUrl(s, forAnchor);
    }

    public static String generateRedirectURL(String uiWebPathParam, String shouldStore, String sessionId, String uiWebPath) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = addFragmentParameter(retval, PARAMETER_SESSION, sessionId);
        if (shouldStore != null) {
            retval = addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }

    public static String addFragmentParameter(String usedUIWebPath, String param, String value) {
        String retval = usedUIWebPath;
        final int fragIndex = retval.indexOf('#');

        // First get rid of the query String, so we can re-append it later
        final int questionMarkIndex = retval.indexOf('?', fragIndex);
        String query = "";
        if (questionMarkIndex > 0) {
            query = retval.substring(questionMarkIndex);
            retval = retval.substring(0, questionMarkIndex);
        }
        // Now let's see, if this url already contains a fragment
        if (retval.indexOf('#') < 0) {
            // Apparently it didn't, so we can append our own
            return retval + '#' + param + '=' + value + query;
        }
        // Alright, we already have a fragment, let's append a new parameter

        return retval + '&' + param + '=' + value + query;
    }

    public static String parseAuthId(HttpServletRequest req, boolean strict) throws OXException {
        return parseParameter(req, AUTHID_PARAM, strict, UUIDs.getUnformattedString(UUID.randomUUID()));
    }

    public static String parseClient(HttpServletRequest req, boolean strict, String defaultClient) throws OXException {
        return parseParameter(req, CLIENT_PARAM, strict, defaultClient);
    }

    public static String parseParameter(HttpServletRequest req, String paramName, boolean strict, String fallback) throws OXException {
        final String value = req.getParameter(paramName);
        if (null == value) {
            if (strict) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(paramName);
            }
            return fallback;
        }
        return value;
    }

    public static String parseParameter(HttpServletRequest req, String paramName, String fallback) {
        final String value = req.getParameter(paramName);
        if (null == value) {
            return fallback;
        }
        return value;
    }

    public static String parseParameter(HttpServletRequest req, String paramName) throws OXException {
        final String value = req.getParameter(paramName);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(paramName);
        }
        return value;
    }

    public static String parseClientIP(HttpServletRequest req) {
        return parseParameter(req, CLIENT_IP_PARAM, req.getRemoteAddr());
    }

    public static String parseUserAgent(HttpServletRequest req) {
        return parseParameter(req, LoginFields.USER_AGENT, req.getHeader(Header.USER_AGENT));
    }

    public static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
