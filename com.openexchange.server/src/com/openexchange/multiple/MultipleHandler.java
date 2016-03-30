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

package com.openexchange.multiple;

import java.util.Collection;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MultipleHandler} - Handles a multiple request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @deprecated Consider using {@link AJAXActionServiceFactory} framework instead
 */
@Deprecated
public interface MultipleHandler {

    /**
     * The constant for the key to get the data element from passed {@link JSONObject JSON object} instance.
     */
    public static final String DATA = "data";

    /**
     * The constant for the key to get a subpath after the original module definition
     */
    public static final String PATH = "__path";

    /**
     * The constant for the key to get the hostname used in the access.
     */
    public static final String HOSTNAME = "__hostname";

    /**
     * The constant for the key to get the route.
     */
    public static final String ROUTE = "__route";

    /**
     * The constant for the key to get the remote address.
     */
    public static final String REMOTE_ADDRESS = "__remoteAddress";

    /**
     * Performs the multiple request identified by specified action string.
     *
     * @param action The action string denoting the request to perform
     * @param jsonObject The JSON object providing request parameters and/or body
     * @param session The session providing needed user data
     * @param secure <code>true</code> for a secure connection such as HTTPS; otherwise <code>false</code>
     * @return A {@link JSONValue} as a result of the performed request
     * @throws JSONException If a JSON error occurs
     * @throws OXException If performing the request fails
     */
    public Object performRequest(String action, JSONObject jsonObject, ServerSession session, boolean secure) throws JSONException, OXException;

    /**
     * Gets the time stamp when {@link #performRequest()} has been called.
     *
     * @return The time stamp associated with performed request or <code>null</code> if none available
     */
    public Date getTimestamp();

    /**
     * Gets the warnings.
     *
     * @return The warnings
     */
    public Collection<OXException> getWarnings();

    /**
     * Closes/frees all associated resources.
     * <p>
     * Must <b>not</b> throw any (runtime) exception!
     */
    public void close();

}
