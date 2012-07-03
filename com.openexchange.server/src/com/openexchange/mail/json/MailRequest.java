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

package com.openexchange.mail.json;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailRequest {

    /**
     * Constant for not-found number.
     */
    public static final int NOT_FOUND = -9999;

    private final ServerSession session;

    private final AJAXRequestData requestData;

    /**
     * Initializes a new {@link MailRequest}.
     *
     * @param session The session
     * @param request The request
     */
    public MailRequest(final AJAXRequestData request, final ServerSession session) {
        super();
        this.requestData = request;
        this.session = session;
    }

    private static final Set<String> ALIASES_MAX = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("max", "maximum")));

    /**
     * Gets the '<code>max</code>' parameter.
     * 
     * @return The '<code>max</code>' parameter
     * @throws OXException If <code>max</code> is not a number
     */
    public long getMax() throws OXException {
        String s = null;
        for (final Iterator<String> it = ALIASES_MAX.iterator(); (null == s) && it.hasNext();) {
            s = requestData.getParameter(it.next());
        }
        if (null == s) {
            return -1L;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("max", s);
        }
    }

    public String checkParameter(final String name) throws OXException {
        return requestData.checkParameter(name);
    }

    public String getParameter(final String name) {
        return requestData.getParameter(name);
    }

    /**
     * Gets optional <code>boolean</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>boolean</code>
     */
    public boolean optBool(final String name) {
        return AJAXRequestDataTools.parseBoolParameter(requestData.getParameter(name));
    }

    /**
     * Gets optional <code>boolean</code> parameter.
     *
     * @param name The parameter name
     * @param def The default value to return if such a parameter is absent
     * @return The <code>boolean</code>
     */
    public boolean optBool(final String name, final boolean def) {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            return def;
        }
        return AJAXRequestDataTools.parseBoolParameter(parameter);
    }

    /**
     * Gets optional <code>int</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>int</code>
     * @throws OXException If parameter is an invalid number value
     */
    public int optInt(final String name) throws OXException {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            return NOT_FOUND;
        }
        try {
            return Integer.parseInt(parameter.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    /**
     * Split pattern for CSV.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Checks for presence of comma-separated <code>int</code> list.
     *
     * @param name The parameter name
     * @return The <code>int</code> array
     * @throws OXException If an error occurs
     */
    public int[] checkIntArray(final String name) throws OXException {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        if (name.equals(AJAXServlet.PARAMETER_COLUMNS)) {
            if (parameter.equals("all")) {
                return AbstractMailAction.COLUMNS_ALL_ALIAS;
            }
            if (parameter.equals("list")) {
                return AbstractMailAction.COLUMNS_LIST_ALIAS;
            }
        }
        final String[] sa = SPLIT.split(parameter, 0);
        final int[] ret = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            try {
                ret[i] = Integer.parseInt(sa[i].trim());
            } catch (final NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
            }
        }
        return ret;
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     *
     * @param name The parameter name
     * @return The <code>String</code> array
     * @throws OXException If parameter is absdent
     */
    public String[] checkStringArray(final String name) throws OXException {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     *
     * @param name The parameter name
     * @return The <code>String</code> array
     */
    public String[] optStringArray(final String name) {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            return null;
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Gets the request.
     *
     * @return The request
     */
    public AJAXRequestData getRequest() {
        return requestData;
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }
}
