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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.StringAllocator;
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

    private static final int MAX_LIMIT = 100;

    private static final int DEFAULT_LIMIT = 10;

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
     * Throws a {@link XingUnlinkedException} if the session in this instance is not linked.
     */
    protected void assertAuthenticated() throws XingUnlinkedException {
        if (!session.isLinked()) {
            throw new XingUnlinkedException();
        }
    }

    /**
     * Returns the {@link User} associated with the current {@link Session}.
     * 
     * @return the current session's {@link User}.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public User userInfo() throws XingException {
        assertAuthenticated();
        try {
            final JSONObject accountInfo = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/me",
                VERSION,
                session);
            return new User(accountInfo.getJSONArray("users").getJSONObject(0));
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Returns the {@link User} associated with given user identifier.
     * 
     * @param userId The user identifier
     * @return The specified user.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public User userInfo(final String userId) throws XingException {
        assertAuthenticated();
        try {
            final JSONObject accountInfo = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId,
                VERSION,
                session);
            return new User(accountInfo.getJSONArray("users").getJSONObject(0));
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the requested user's contacts.
     * 
     * @param userId The user identifier
     * @param limit The number of contacts to be returned. Must be zero or a positive number. Default: <code>10</code>, Maximum:
     *            <code>100</code>. If its value is equal to zero, default limit is passed to request 
     * @param offset The offset. Must be zero or a positive number. Default: <code>0</code>
     * @param orderBy Determines the ascending order of the returned list. Currently only supports <code>"last_name"</code>. Defaults to
     *            <code>"id"</code>
     * @param userFields List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * @return The user's contacts
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public List<User> getContactsFrom(final String userId, final int limit, final int offset, final OrderBy orderBy, final Collection<UserField> userFields) throws XingException {
        if (limit < 0 || limit > 100) {
            throw new XingException("Invalid limit: " + limit + ". Must be zero OR less than or equal to 100.");
        }
        if (offset < 0) {
            throw new XingException("Invalid offset: " + offset + ". Must be greater than zero.");
        }
        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(Arrays.asList(
                "limit",
                Integer.toString(limit == 0 ? DEFAULT_LIMIT : limit),
                "offset",
                Integer.toString(offset)));
            // Add order-by
            if (null != orderBy) {
                params.add("orderBy");
                params.add(orderBy.getFieldName());
            }
            // Add user fields
            if (null != userFields && !userFields.isEmpty()) {
                params.add("user_fields");
                final Iterator<UserField> iter = userFields.iterator();
                final StringAllocator fields = new StringAllocator(userFields.size() << 4);
                fields.append(iter.next().getFieldName());
                while (iter.hasNext()) {
                    fields.append(',').append(iter.next().getFieldName());
                }
                params.add(fields.toString());
            }

            final JSONObject usersJsonObject = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId + "/contacts",
                VERSION,
                params.toArray(new String[0]),
                session);

            final JSONArray users = usersJsonObject.getJSONArray("users");
            final int length = users.length();
            final List<User> retval = new ArrayList<User>(length);
            for (int i = 0; i < length; i++) {
                retval.add(new User(users.getJSONObject(i)));
            }
            return retval;
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets all of the requested user's contacts.
     * 
     * @param userId The user identifier
     * @param orderBy Determines the ascending order of the returned list. Currently only supports <code>"last_name"</code>. Defaults to
     *            <code>"id"</code>
     * @param userFields List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * @return The user's contacts
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public List<User> getContactsFrom(final String userId, final OrderBy orderBy, final Collection<UserField> userFields) throws XingException {
        assertAuthenticated();
        try {
            final List<User> retval = new LinkedList<User>();
            final int limit = MAX_LIMIT;
            int offset = 0;
            while (offset >= 0) {
                final List<User> contacts = getContactsFrom(userId, offset, limit, orderBy, userFields);
                retval.addAll(contacts);
                if (contacts.size() < limit) {
                    // Obtained less than requested; no more contacts available then
                    offset = -1;
                } else {
                    offset += limit;
                }
            }
            return retval;
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

}
