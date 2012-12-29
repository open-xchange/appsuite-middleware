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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.xing;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.xing.RESTUtility.Method;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.session.Session;

/**
 * {@link XingAPI}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class XingAPI<S extends Session> {

    /**
     * The version of the API that this code uses.
     */
    public static final int VERSION = 1;

    /** The session */
    private final S session;

    /**
     * Initializes a new {@link XingAPI}.
     * 
     * @param session The associated session
     */
    public XingAPI(final S session) {
        super();
        this.session = session;
    }

    /**
     * Throws a {@link DropboxUnlinkedException} if the session in this instance is not linked.
     */
    protected void assertAuthenticated() throws XingUnlinkedException {
        if (!session.isLinked()) {
            throw new XingUnlinkedException();
        }
    }

    /**
     * Returns the {@link Account} associated with the current {@link Session}.
     * 
     * @return the current session's {@link Account}.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Account accountInfo() throws XingException {
        assertAuthenticated();
        try {
            final JSONObject accountInfo = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/v1/users/me",
                VERSION,
                null,
                session);
            return new Account(accountInfo.getJSONArray("users").getJSONObject(0));
        } catch (final JSONException e) {
            throw new XingException(e);
        }
    }

}
