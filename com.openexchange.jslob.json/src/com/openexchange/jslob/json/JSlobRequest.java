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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.jslob.json;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JSlobRequest} - A JSlob request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSlobRequest {

    private final AJAXRequestData requestData;

    private final ServerSession session;

    /**
     * Initializes a new {@link JSlobRequest}.
     *
     * @param requestData The AJAX request data
     * @param session The session
     */
    public JSlobRequest(final AJAXRequestData requestData, final ServerSession session) {
        super();
        this.requestData = requestData;
        this.session = session;
    }

    public int getContextId() {
        return session.getContextId();
    }

    public int getUserId() {
        return session.getUserId();
    }

    /**
     * Checks for presence of specified parameter.
     *
     * @param name The parameter name
     * @return <code>true</code> if such a parameter exists; otherwise <code>false</code> if absent
     */
    public boolean containsParameter(final String name) {
        return requestData.containsParameter(name);
    }

    /**
     * Gets the value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The value mapped to given parameter name
     * @throws NullPointerException If name is <code>null</code>
     * @throws OXException If no such parameter exists
     */
    public String checkParameter(final String name) throws OXException {
        return requestData.checkParameter(name);
    }

    /**
     * Tries to get a parameter value as parsed as a certain type
     *
     * @param name The parameter name
     * @param coerceTo The type the parameter should be interpreted as
     * @param optional TODO
     * @return The coerced value
     * @throws OXException if coercion fails
     */
    public <T> T getParameter(final String name, final Class<T> coerceTo, boolean optional) throws OXException {
        return requestData.getParameter(name, coerceTo, optional);
    }
    
    /**
     * Gets the parsed <code>int</code> value of denoted parameter.
     *
     * @param name The parameter name
     * @return The parsed <code>int</code> value
     * @throws OXException If parameter is missing or not a number.
     */
    public int getIntParameter(final String name) throws OXException {
        return requestData.getParameter(name, int.class).intValue();
    }

    /**
     * Gets the associated session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }

    /**
     * Gets the AJAX request data
     *
     * @return The AJAX request data
     */
    public AJAXRequestData getRequestData() {
        return requestData;
    }

}
