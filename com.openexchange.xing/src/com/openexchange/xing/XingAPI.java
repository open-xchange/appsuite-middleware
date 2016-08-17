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

package com.openexchange.xing;

import static com.openexchange.java.Strings.asciiLowerCase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.activation.FileTypeMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.java.ImageTypeDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.xing.RESTUtility.Method;
import com.openexchange.xing.exception.XingApiException;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingIOException;
import com.openexchange.xing.exception.XingInvalidFieldException;
import com.openexchange.xing.exception.XingLeadAlreadyExistsException;
import com.openexchange.xing.exception.XingServerException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.session.Session;

/**
 * {@link XingAPI} - The XING API.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class XingAPI<S extends Session> {

    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 10;

    private static final int MAX_WITH_LATEST_MESSAGES = 100;
    private static final int DEFAULT_WITH_LATEST_MESSAGES = 0;

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
            final JSONObject responseInformation = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/me",
                VERSION,
                session);
            return new User(responseInformation.getJSONArray("users").getJSONObject(0));
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
            final JSONObject responseInformation = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId,
                VERSION,
                session);
            return new User(responseInformation.getJSONArray("users").getJSONObject(0));
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Looks up a list of users by their E-Mail addresses.
     *
     * @param emailAddresses The E-Mail addresses to look-up
     * @return The associated user identifiers; may be empty, if no users were found.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public List<String> findByEmails(final List<String> emailAddresses) throws XingException {
        if (emailAddresses == null || emailAddresses.isEmpty()) {
            return Collections.emptyList();
        }

        String addressParam = prepareMailAddresses(emailAddresses);
        if (Strings.isEmpty(addressParam)) {
            return Collections.emptyList();
        }

        assertAuthenticated();
        List<String> userIds = new LinkedList<String>();
        try {

            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(Arrays.asList(
                "emails", addressParam));
            // Fire request
            final JSONObject responseInformation = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/find_by_emails",
                VERSION,
                params.toArray(new String[0]),
                session);
            final JSONArray jItems = responseInformation.getJSONObject("results").optJSONArray("items");
            if (null == jItems) {
                return null;
            }
            final int length = jItems.length();
            if (length <= 0) {
                return null;
            }

            for (int i = 0; i < jItems.length(); i++) {
                JSONObject jUser = jItems.getJSONObject(i).optJSONObject("user");
                if (jUser != null) {
                    userIds.add(jUser.getString("id"));
                }
            }
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }

        return userIds;
    }

    private static String prepareMailAddresses(final List<String> emailAddresses) {
        if (emailAddresses.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String address : emailAddresses) {
            sb.append(address).append(',');
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }


    /**
     * Looks up a user by specified E-Mail address.
     *
     * @param emailAddress The E-Mail address to look-up
     * @return The associated user identifier or <code>null</code> if there is no such user
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public String findByEmail(final String emailAddress) throws XingException {
        if (Strings.isEmpty(emailAddress)) {
            return null;
        }
        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(Arrays.asList(
                "emails", emailAddress));
            // Fire request
            final JSONObject responseInformation = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/find_by_emails",
                VERSION,
                params.toArray(new String[0]),
                session);
            final JSONArray jItems = responseInformation.getJSONObject("results").optJSONArray("items");
            if (null == jItems) {
                return null;
            }
            final int length = jItems.length();
            if (length <= 0) {
                return null;
            }
            JSONObject jUser = jItems.getJSONObject(0).optJSONObject("user");
            if (jUser == null) {
                return null;
            }

            return jUser.getString("id");
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Looks up a user and returns all attributes from XING.
     *
     * @param emailAddresses The E-Mail addressed to look-up
     * @return The associated user attributes
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> findByEmailsGetXingAttributes(final List<String> emailAddresses) throws XingException {
    	if (emailAddresses == null || emailAddresses.isEmpty()) {
            return null;
        }

        String addressParam = prepareMailAddresses(emailAddresses);
        if (Strings.isEmpty(addressParam)) {
            return null;
        }

        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(Arrays.asList(
                "emails", addressParam));
            // Fire request
            final JSONObject responseInformation = (JSONObject) RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/find_by_emails",
                VERSION,
                params.toArray(new String[0]),
                session);

            return responseInformation.asMap();
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the shortest contact path between a user and any other XING user. The path only
     * contains the IDs of the users.
     *
     * @param fromId The XING user id
     * @param toId The other XING user id
     * @return The contact path or <code>null</code> if no path exists.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Path getShortestPath(final String fromId, final String toId) throws XingException {
        return getShortestPath(fromId, toId, null);
    }

    /**
     * Gets the shortest contact path between a user and any other XING user.
     *
     * @param fromId The XING user id
     * @param toId The other XING user id
     * @param userFields List of user attributes to return. If <code>null</code> or empty, only the ID will be returned.
     * @return The contact path or <code>null</code> if no path exists.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Path getShortestPath(final String fromId, final String toId, Collection<UserField> userFields) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(4);
            params.add("all_paths");
            params.add("false");
            if (userFields != null && !userFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(userFields, new Stringer<UserField>() {
                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + fromId + "/network/" + toId + "/paths",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();

            final JSONArray jPaths = responseInformation.getJSONObject("contact_paths").optJSONArray("paths");
            if (null == jPaths || jPaths.length() <= 0) {
                return null;
            }

            final JSONObject jPath = jPaths.getJSONObject(0);
            final JSONArray jUsers = jPath.getJSONArray("users");
            final int l = jUsers.length();
            final List<User> inBetween = new LinkedList<User>();
            User from = null;
            User to = null;
            for (int i = 0; i < l; i++) {
                final User user = new User(jUsers.getJSONObject(i));
                if (i == 0) {
                    from = user;
                } else if (i == (l - 1)) {
                    to = user;
                } else {
                    inBetween.add(user);
                }
            }
            return new Path(from, to, inBetween);
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    private static final Set<UserField> SUPPORTED_SORT_FIELDS = EnumSet.of(UserField.ID, UserField.LAST_NAME);

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
    public Contacts getContactsFrom(final String userId, final int limit, final int offset, final UserField orderBy, final Collection<UserField> userFields) throws XingException {
        if (limit < 0 || limit > MAX_LIMIT) {
            throw new XingException("Invalid limit: " + limit + ". Must be zero OR less than or equal to " + MAX_LIMIT);
        }
        if (offset < 0) {
            throw new XingException("Invalid offset: " + offset + ". Must be greater than or equal to zero.");
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
            final boolean serverSort = (null == orderBy || SUPPORTED_SORT_FIELDS.contains(orderBy));
            if (serverSort) {
                if (null != orderBy) {
                    params.add("order_by");
                    params.add(orderBy.getFieldName());
                }
            }
            // Add user fields
            if (null != userFields && !userFields.isEmpty()) {
                params.add("user_fields");
                final Iterator<UserField> iter = userFields.iterator();
                final StringBuilder fields = new StringBuilder(userFields.size() << 4);
                fields.append(iter.next().getFieldName());
                while (iter.hasNext()) {
                    fields.append(',').append(iter.next().getFieldName());
                }
                params.add(fields.toString());
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId + "/contacts",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();

            if (serverSort) {
                return new Contacts(responseInformation.getJSONObject("contacts"));
            }
            // Manually sort contacts
            final Contacts contacts = new Contacts(responseInformation.getJSONObject("contacts"));
            Collections.sort(contacts.getUsers(), (null == orderBy ? UserField.ID : orderBy).getComparator(false));
            return contacts;
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
    public Contacts getContactsFrom(final String userId, final UserField orderBy, final Collection<UserField> userFields) throws XingException {
        assertAuthenticated();
        try {
            final List<User> users;
            final int maxLimit = MAX_LIMIT;
            final int total;
            int offset = 0;
            // Request first chunk to determine total number of contacts
            {
                final Contacts contacts = getContactsFrom(userId, maxLimit, offset, null, userFields);
                final List<User> chunk = contacts.getUsers();
                final int chunkSize = chunk.size();
                if (chunkSize < maxLimit) {
                    // Obtained less than requested; no more contacts available then
                    return contacts;
                }
                total = contacts.getTotal();
                users = new ArrayList<User>(total);
                users.addAll(chunk);
                offset += chunkSize;
            }
            // Request remaining chunks
            while (offset < total) {
                final int remain = total - offset;
                final List<User> chunk = getContactsFrom(userId, remain > maxLimit ? maxLimit : remain, offset, null, userFields).getUsers();
                users.addAll(chunk);
                offset += chunk.size();
            }
            // Sort users
            Collections.sort(users, (null == orderBy ? UserField.ID : orderBy).getComparator(false));
            return new Contacts(total, users);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Returns the list of contacts who are direct contacts of both the given and the current user.
     *
     * @param userId The user to get the shared contacts from.
     * @param limit The number of contacts to be returned. Must be zero or a positive number. Default: <code>10</code>, Maximum:
     *            <code>100</code>. If its value is equal to zero, default limit is passed to request
     * @param offset The offset. Must be zero or a positive number. Default: <code>0</code>
     * @param orderBy Determines the ascending order of the returned list. Currently only supports <code>"last_name"</code>. Defaults to
     *            <code>"id"</code>
     * @param userFields List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * @return The contacts shared with the given user.
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Contacts getSharedContactsWith(final String userId, final int limit, final int offset, final UserField orderBy, final Collection<UserField> userFields) throws XingException {
        if (limit < 0 || limit > MAX_LIMIT) {
            throw new XingException("Invalid limit: " + limit + ". Must be zero OR less than or equal to " + MAX_LIMIT);
        }
        if (offset < 0) {
            throw new XingException("Invalid offset: " + offset + ". Must be greater than or equal to zero.");
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
            final boolean serverSort = (null == orderBy || SUPPORTED_SORT_FIELDS.contains(orderBy));
            if (serverSort) {
                if (null != orderBy) {
                    params.add("order_by");
                    params.add(orderBy.getFieldName());
                }
            }
            // Add user fields
            if (null != userFields && !userFields.isEmpty()) {
                params.add("user_fields");
                final Iterator<UserField> iter = userFields.iterator();
                final StringBuilder fields = new StringBuilder(userFields.size() << 4);
                fields.append(iter.next().getFieldName());
                while (iter.hasNext()) {
                    fields.append(',').append(iter.next().getFieldName());
                }
                params.add(fields.toString());
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId + "/contacts/shared",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();

            if (serverSort) {
                return new Contacts(responseInformation.getJSONObject("shared_contacts"));
            }
            // Manually sort contacts
            final Contacts contacts = new Contacts(responseInformation.getJSONObject("shared_contacts"));
            Collections.sort(contacts.getUsers(), (null == orderBy ? UserField.ID : orderBy).getComparator(false));
            return contacts;
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the conversations for specified user.
     *
     * @param userId The user identifier
     * @param limit The number of conversations to be returned. Must be zero or a positive number. Default: <code>10</code>, Maximum:
     *            <code>100</code>. If its value is equal to zero, default limit is passed to request
     * @param offset The offset. Must be zero or a positive number. Default: <code>0</code>
     * @param userFields List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * @param withLatestMessages The number of latest messages to be returned. Must be zero or a positive number. Default: <code>0</code>,
     *            Maximum: <code>100</code>. If its value is equal to zero, default limit is passed to request
     * @return The user's conversations
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Conversations getConversationsFrom(final String userId, final int limit, final int offset, final Collection<UserField> userFields, final int withLatestMessages) throws XingException {
        if (limit < 0 || limit > MAX_LIMIT) {
            throw new XingException("Invalid limit: " + limit + ". Must be zero OR less than or equal to " + MAX_LIMIT);
        }
        if (offset < 0) {
            throw new XingException("Invalid offset: " + offset + ". Must be greater than or equal to zero.");
        }
        if (withLatestMessages < 0 || withLatestMessages > MAX_WITH_LATEST_MESSAGES) {
            throw new XingException("Invalid withLatestMessages: " + withLatestMessages + ". Must be zero OR less than or equal to " + MAX_WITH_LATEST_MESSAGES);
        }
        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(Arrays.asList(
                "limit",
                Integer.toString(limit == 0 ? DEFAULT_LIMIT : limit),
                "offset",
                Integer.toString(offset),
                "with_latest_messages",
                Integer.toString(withLatestMessages == 0 ? DEFAULT_WITH_LATEST_MESSAGES : withLatestMessages)));
            // Add user fields
            if (null != userFields && !userFields.isEmpty()) {
                params.add("user_fields");
                final Iterator<UserField> iter = userFields.iterator();
                final StringBuilder fields = new StringBuilder(userFields.size() << 4);
                fields.append(iter.next().getFieldName());
                while (iter.hasNext()) {
                    fields.append(',').append(iter.next().getFieldName());
                }
                params.add(fields.toString());
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId + "/conversations",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();
            return new Conversations(responseInformation.getJSONObject("conversations"));
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets all of the requested user's conversations.
     *
     * @param userId The user identifier
     * @param userFields List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * @param withLatestMessages The number of latest messages to be returned. Must be zero or a positive number. Default: <code>0</code>,
     *            Maximum: <code>100</code>. If its value is equal to zero, default limit is passed to request
     * @return The user's conversations
     * @return The user's conversations
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Conversations getConversationsFrom(final String userId, final Collection<UserField> userFields, final int withLatestMessages) throws XingException {
        assertAuthenticated();
        try {
            final List<Conversation> items = new LinkedList<Conversation>();
            final int maxLimit = MAX_LIMIT;
            final int total;
            int offset = 0;
            // Request first chunk to determine total number of conversations
            {
                final Conversations conversations = getConversationsFrom(userId, maxLimit, offset, userFields, withLatestMessages);
                final List<Conversation> chunk = conversations.getItems();
                final int chunkSize = chunk.size();
                if (chunkSize < maxLimit) {
                    // Obtained less than requested; no more conversations available then
                    return conversations;
                }
                total = conversations.getTotal();
                items.addAll(chunk);
                offset += chunkSize;
            }
            // Request remaining chunks
            while (offset < total) {
                final int remain = total - offset;
                final List<Conversation> chunk = getConversationsFrom(
                    userId,
                    remain > maxLimit ? maxLimit : remain,
                    offset,
                    userFields,
                    withLatestMessages).getItems();
                items.addAll(chunk);
                offset += chunk.size();
            }
            return new Conversations(total, items);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the denoted conversation for specified user.
     *
     * @param id The conversation identifier
     * @param userId The user identifier
     * @param userFields List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * @param withLatestMessages The number of latest messages to be returned. Must be zero or a positive number. Default: <code>0</code>,
     *            Maximum: <code>100</code>. If its value is equal to zero, default limit is passed to request
     * @return The conversation
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Conversation getConversationFrom(final String id, final String userId, final Collection<UserField> userFields, final int withLatestMessages) throws XingException {
        if (withLatestMessages < 0 || withLatestMessages > MAX_WITH_LATEST_MESSAGES) {
            throw new XingException("Invalid withLatestMessages: " + withLatestMessages + ". Must be zero OR less than or equal to " + MAX_WITH_LATEST_MESSAGES);
        }
        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(Arrays.asList(
                "with_latest_messages",
                Integer.toString(withLatestMessages == 0 ? DEFAULT_WITH_LATEST_MESSAGES : withLatestMessages)));
            // Add user fields
            if (null != userFields && !userFields.isEmpty()) {
                params.add("user_fields");
                final Iterator<UserField> iter = userFields.iterator();
                final StringBuilder fields = new StringBuilder(userFields.size() << 4);
                fields.append(iter.next().getFieldName());
                while (iter.hasNext()) {
                    fields.append(',').append(iter.next().getFieldName());
                }
                params.add(fields.toString());
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId + "/conversations/" + id,
                VERSION,
                params.toArray(new String[0]),
                session).toObject();
            return new Conversation(responseInformation.getJSONObject("conversation"));
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Initiates a contact request between the current user (<code>userId</code>) and the specified user (<code>recipientUserId</code>).
     *
     * @param userId The identifier of the user
     * @param recipientUserId The identifier of the recipient
     * @param optMessage The optional message
     * @throws XingException If contact request fails
     */
    public void initiateContactRequest(final String recipientUserId, final String optMessage) throws XingException {
        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(4);

            if (!Strings.isEmpty(optMessage)) {
                params.add("message");
                params.add(optMessage);
            }

            RESTUtility.streamRequest(
                Method.POST,
                session.getAPIServer(),
                "/users/" + recipientUserId + "/contact_requests",
                VERSION,
                params.toArray(new String[0]),
                session,
                Arrays.asList(XingServerException._201_CREATED));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Send invitations via email to contacts who do not have a XING profile.
     * <p>
     * The user is allowed to invite 2000 people per week.
     *
     * @param userId The identifier of the user that attempts to invite others
     * @param addresses A list of one or more E-Mail addresses. NOTE: The current user's email address will be filtered out.
     * @param optMessage The message that is sent together with the invitation. The maximum length of this message is 150 characters for BASIC users and 600 characters for PREMIUM users. Defaults to the XING standard text for invitations.
     * @param optUserFields A list of user attributes to be returned. If this parameter is not used, only the ID will be returned. For a list of available profile user attributes, please refer to the get user details call.
     * @return A invitation response
     * @throws XingException If invitation attempt fails
     */
    public InvitationStats invite(final List<String> addresses, final String optMessage, final Collection<UserField> optUserFields) throws XingException {
        if (null == addresses || addresses.isEmpty()) {
            throw new XingException("Invalid addresses");
        }
        assertAuthenticated();
        try {
            // Add parameters limit & offset
            final List<String> params = new ArrayList<String>(6);

            params.add("to_emails");
            params.add(collectionToCsv(addresses));

            if (!Strings.isEmpty(optMessage)) {
                params.add("message");
                params.add(optMessage);
            }

            if (null != optUserFields && !optUserFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(optUserFields, new Stringer<UserField>() {

                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.POST,
                session.getAPIServer(),
                "/users/invite",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();
            return new InvitationStats(responseInformation.getJSONObject("invitation_stats"));
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the user's network feed; a stream of activities recently performed by the user's network.
     *
     * @param userId The ID of the user whose contacts' activities are to be returned
     * @param optAggregate If set to <code>true</code> (default) similar activities may be combined into one. Set this to <code>false</code> if you don't want any aggregation at all.
     * @param optSince Only returns activities that are newer than the specified time stamp. <b>Can't be combined with until!</b>
     * @param optUntil Only returns activities that are older than the specified time stamp. <b>Can't be combined with since!</b>
     * @param optUserFields The list of user attributes to be returned in nested user objects. If this parameter is not used, only the ID will be returned.
     * @return A generic map representing return network feed data
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> getNetworkFeed(final String userId, final Boolean optAggregate, final Date optSince, final Date optUntil, final Collection<UserField> optUserFields) throws XingException {
        assertAuthenticated();
        try {
            // Add parameters
            final List<String> params = new ArrayList<String>(6);

            if (null != optAggregate) {
                params.add("aggregate");
                params.add(optAggregate.toString());
            }

            if (null != optSince) {
                params.add("since");
                synchronized (ISO6801) {
                    params.add(ISO6801.format(optSince));
                }
            }

            if (null != optUntil) {
                params.add("until");
                synchronized (ISO6801) {
                    params.add(ISO6801.format(optUntil));
                }
            }

            if (null != optUserFields && !optUserFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(optUserFields, new Stringer<UserField>() {

                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + userId + "/network_feed",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();

            return responseInformation.asMap();

        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Revokes a contact request between the current user (<code>userId</code>) and the specified user (<code>recipientUserId</code>).
     *
     * @param userId The identifier of the user
     * @param recipientUserId The identifier of the recipient
     * @throws XingException If contact request fails
     */
    public void revokeContactRequest(final String senderId, final String recipientUserId) throws XingException {
        assertAuthenticated();
        try {

            RESTUtility.streamRequest(
                Method.DELETE,
                session.getAPIServer(),
                "/users/" + recipientUserId + "/contact_requests/" + senderId,
                VERSION,
                null,
                session,
                Arrays.asList(XingServerException._204_NO_CONTENT));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }
    /**
     * Gets the user feed; a stream of activities recently performed by the user.
     *
     * @param xingUserId The ID of the user whose contacts' activities are to be returned
     * @param optSince Only returns activities that are newer than the specified time stamp. <b>Can't be combined with until!</b>
     * @param optUntil Only returns activities that are older than the specified time stamp. <b>Can't be combined with since!</b>
     * @param optUserFields The list of user attributes to be returned in nested user objects. If this parameter is not used, only the ID
     *            will be returned.
     * @return A generic map representing return network feed data only activities of the user will be shown
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> getUserFeed(String xingUserId, Date optSince, Date optUntil, Collection<UserField> optUserFields) throws XingException {
        assertAuthenticated();
        try {
            // Add parameters
            final List<String> params = new ArrayList<String>(6);

            if (null != optSince) {
                params.add("since");
                synchronized (ISO6801) {
                    params.add(ISO6801.format(optSince));
                }
            }

            if (null != optUntil) {
                params.add("until");
                synchronized (ISO6801) {
                    params.add(ISO6801.format(optUntil));
                }
            }

            if (null != optUserFields && !optUserFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(optUserFields, new Stringer<UserField>() {

                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject responseInformation = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/users/" + xingUserId + "/feed",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();

            return responseInformation.asMap();

        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Creates a comment for a certain network activity.
     *
     * @param activityId the activity id
     * @param text comment
     * @return A map representing the outcome of the operation
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     */
    public void commentActivity(final String activityId, final String text) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(1);
            params.add("text");
            params.add(text);

            RESTUtility.streamRequest(
                Method.POST,
                session.getAPIServer(),
                "/activities/" + activityId + "/comments",
                VERSION,
                params.toArray(new String[0]),
                session,
                Arrays.asList(XingServerException._201_CREATED));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Retrieves a list with comments for the specified activity.
     *
     * @param activityId the id of the activity
     * @param optLimit restricts the number of comments to be returned. (Optional, Default 10)
     * @param optOffset the offset (Optional, Default 0)
     * @param optUserFields a Collection with all user fields to be returned (Optional, defaults to ID only)
     * @return a map representation of the activity's comments.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> getComments(final String activityId, final int optLimit, final int optOffset, final Collection<UserField> optUserFields) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(3);
            if (optLimit > 0) {
                params.add("limit");
                params.add(Integer.toString(optLimit));
            }

            if (optOffset > 0) {
                params.add("offset");
                params.add(Integer.toString(optOffset));
            }

            if (optUserFields != null && !optUserFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(optUserFields, new Stringer<UserField>() {
                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject response = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/activities/" + activityId + "/comments",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();
            return response.asMap();
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Deletes the comment specified under the provided commentId for the activity specified under the provided activityId
     *
     * @param activityId
     * @param commentId
     * @throws XingException
     */
    public void deleteComment(final String activityId, final String commentId) throws XingException {
        assertAuthenticated();
        try {
            RESTUtility.streamRequest(
                Method.DELETE,
                session.getAPIServer(),
                "/activities/" + activityId + "/comments/" + commentId,
                VERSION,
                null,
                session,
                Arrays.asList(XingServerException._204_NO_CONTENT));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Change the status message of the user
     *
     * @param userId The userId
     * @param message The status message
     * @return The hardcoded string <code>Status update has been posted<code> on success
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public void changeStatusMessage(final String userId, final String message) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(4);
            params.add("message");
            params.add(message);

            RESTUtility.streamRequest(
                Method.POST,
                session.getAPIServer(),
                "/users/" + userId + "/status_message",
                VERSION,
                params.toArray(new String[0]),
                session,
                Arrays.asList(XingServerException._201_CREATED));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Like a certain network activity
     *
     * @param The id of the activity
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     */
    public void likeActivity(final String activityId) throws XingException {
        assertAuthenticated();
        try {

            RESTUtility.streamRequest(
                Method.PUT,
                session.getAPIServer(),
                "/activities/" + activityId + "/like",
                VERSION,
                null,
                session,
                Arrays.asList(XingServerException._204_NO_CONTENT));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Unlike a certain network activity
     *
     * @param The id of the activity
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     */
    public void unlikeActivity(final String activityId) throws XingException {
        assertAuthenticated();
        try {

            RESTUtility.streamRequest(
                Method.DELETE,
                session.getAPIServer(),
                "/activities/" + activityId + "/like",
                VERSION,
                null,
                session,
                Arrays.asList(XingServerException._204_NO_CONTENT));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Retrieves a list of users who liked the specified activity.
     *
     * @param activityId the id of the activity
     * @param optLimit restricts the number of comments to be returned. (Optional, Default 10)
     * @param optOffset the offset (Optional, Default 0)
     * @param optUserFields a Collection with all user fields to be returned (Optional, defaults to ID only)
     * @return a map representation of the activity's comments.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> getLikes(final String activityId, final int optLimit, final int optOffset, final Collection<UserField> optUserFields) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(3);
            if (optLimit > 0) {
                params.add("limit");
                params.add(Integer.toString(optLimit));
            }

            if (optOffset > 0) {
                params.add("offset");
                params.add(Integer.toString(optOffset));
            }

            if (optUserFields != null && !optUserFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(optUserFields, new Stringer<UserField>() {
                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject response = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/activities/" + activityId + "/likes",
                VERSION,
                params.toArray(new String[0]),
                session).toObject();
            return response.asMap();
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Shows an activity
     *
     * @param activityId The id of the activity
     * @param optUserFields The list of user attributes to be returned in nested user objects. If this parameter is not used, only the ID
     *            will be returned.
     * @return A map representing the outcome of the operation
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> showActivity(final String activityId, Collection<UserField> optUserFields) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(4);

            if (null != optUserFields && !optUserFields.isEmpty()) {
                params.add("user_fields");
                params.add(collectionToCsv(optUserFields, new Stringer<UserField>() {

                    @Override
                    public String getString(final UserField element) {
                        return element.getFieldName();
                    }
                }));
            }

            final JSONObject response = RESTUtility.request(
                Method.GET,
                session.getAPIServer(),
                "/activities/" + activityId,
                VERSION,
                params.toArray(new String[0]),
                session).toObject();
            return response.asMap();
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Shares an activity
     *
     * @param activityId The id of the activity
     * @param optTextMessage An optional text message - up to 140 characters
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public void shareActivity(final String activityId, final String optTextMessage) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(4);

            params.add("id");
            params.add(activityId);

            if (optTextMessage != null) {
                params.add("text");
                params.add(optTextMessage);
            }

            RESTUtility.streamRequest(
                Method.POST,
                session.getAPIServer(),
                "/activities/" + activityId + "/share",
                VERSION,
                params.toArray(new String[0]),
                session,
                Arrays.asList(XingServerException._204_NO_CONTENT));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Deletes an activity
     *
     * @param activityId The id of the activity
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public void deleteActivity(final String activityId) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(4);

            params.add("id");
            params.add(activityId);

            RESTUtility.streamRequest(
                Method.DELETE,
                session.getAPIServer(),
                "/activities/" + activityId,
                VERSION,
                params.toArray(new String[0]),
                session,
                Arrays.asList(XingServerException._204_NO_CONTENT));
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Shares a link in network activity
     *
     * @param uri
     * @return A map reprecenting the outcome of the operation
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     */
    public Map<String, Object> shareLink(final String uri) throws XingException {
        assertAuthenticated();
        try {
            final List<String> params = new ArrayList<String>(1);
            params.add("uri");
            params.add(uri);

            final JSONObject response = RESTUtility.request(
                Method.POST,
                session.getAPIServer(),
                "/users/me/share/link",
                VERSION,
                params.toArray(new String[0]),
                session,
                Arrays.asList(XingServerException._201_CREATED)).toObject();
            return response.asMap();
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Performs a sign-up request for a lead.
     *
     * @param leadDescription The lead description
     * @return A map representation of created lead
     * @throws XingUnlinkedException If you have not set an access token pair on the session, or if the user has revoked access.
     * @throws XingServerException If the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException If any network-related error occurs.
     * @throws XingException For any other unknown errors. This is also a superclass of all other XING exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public Map<String, Object> signUpLead(final LeadDescription leadDescription) throws XingException {
        final String url = RESTUtility.buildURL(session.getWebServer(), -1, "/signup-api/v1/leads", null);

        String email = null;
        try {
            final JSONObject jLeadDesc = new JSONObject(10);
            {
                final String tmp = leadDescription.getEmail();
                if (!Strings.isEmpty(tmp)) {
                    email = tmp;
                    jLeadDesc.put("email", tmp);
                }
            }
            {
                final String tmp = leadDescription.getFirstName();
                if (!Strings.isEmpty(tmp)) {
                    jLeadDesc.put("first_name", tmp);
                }
            }
            {
                final String tmp = leadDescription.getLastName();
                if (!Strings.isEmpty(tmp)) {
                    jLeadDesc.put("last_name", tmp);
                }
            }
            {
                final Language tmp = leadDescription.getLanguage();
                if (null != tmp) {
                    jLeadDesc.put("language", tmp.getLangId());
                }
            }
            {
                jLeadDesc.put("tandc_check", leadDescription.isTandcCheck());
            }

            jLeadDesc.put("reg_consumer_key", session.getConsumerPair().key);
            jLeadDesc.put("reg_consumer_secret", session.getConsumerPair().secret);

            final JSONObject jResponse = RESTUtility.basicRequest(
                Method.POST,
                url,
                jLeadDesc,
                session,
                Arrays.asList(XingServerException._201_CREATED)).toObject();
            return jResponse.asMap();
        } catch (final XingApiException e) {
            if ("INVALID".equals(e.getErrorName())) {
                String desc = null;
                @SuppressWarnings("unchecked") Map<String, Object> errorProps = (Map<String, Object>) e.getProperties().get("errors");
                if (null != errorProps) {
                    StringBuilder descBuilder = new StringBuilder();
                    boolean first = true;
                    for (Map.Entry<String, Object> entry : errorProps.entrySet()) {
                        if (first) {
                            first = false;
                        } else {
                            descBuilder.append(", ");
                        }

                        String fieldName = entry.getKey();
                        descBuilder.append(fieldName).append(" ");

                        Object reason = entry.getValue();
                        if (reason instanceof List) {
                            @SuppressWarnings("unchecked") List<Object> reasons = (List<Object>) reason;
                            if (1 == reasons.size()) {
                                String sCause = reasons.get(0).toString();
                                if ("email".equals(fieldName) && "EXISTS_AS_LEAD".equals(sCause)) {
                                    throw new XingLeadAlreadyExistsException(email, e);
                                }

                                descBuilder.append(asciiLowerCase(sCause));
                            } else {
                                descBuilder.append(asciiLowerCase(reasons.toString()));
                            }
                        } else {
                            descBuilder.append(asciiLowerCase(reason.toString()));
                        }
                    }
                    desc = descBuilder.toString();
                }

                if (null != desc) {
                    throw new XingInvalidFieldException(desc, e);
                }
            }

            throw e;
        } catch (final JSONException e) {
            throw new XingException(e);
        } catch (final RuntimeException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the photo denoted by given URL.
     *
     * @param url The photo URL
     * @return The loaded photo or <code>null</code>
     * @throws XingException If loading photo fails
     */
    public IFileHolder getPhoto(final String url) throws XingException {
        if (null == url) {
            return null;
        }

        return loadImageFromURL(url);
    }

    /**
     * Gets the associated session.
     */
    public S getSession() {
        return session;
    }

    // -------------------------------------------------------------------------------------------------------- //

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection.
     *
     * @param url The URI parameter's value
     * @return The appropriate file holder
     * @throws XingException If converting image's data fails
     */
    private static IFileHolder loadImageFromURL(final String url) throws XingException {
        try {
            return loadImageFromURL(new URL(url));
        } catch (final MalformedURLException e) {
            throw new XingException("Problem loading photo from URL: " + url, e);
        }
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection.
     *
     * @param url The image URL
     * @return The appropriate file holder
     * @throws XingException If converting image's data fails
     */
    private static IFileHolder loadImageFromURL(final URL url) throws XingException {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final InputStream in = urlCon.getInputStream();
            try {
                final ByteArrayOutputStream buffer = Streams.newByteArrayOutputStream(in.available());
                transfer(in, buffer);
                bytes = buffer.toByteArray();
            } finally {
                Streams.close(in);
            }
        } catch (final SocketTimeoutException e) {
            throw new XingException("Timeout while loading photo from URL: " + url, e);
        } catch (final IOException e) {
            throw new XingException("I/O problem loading photo from URL: " + url, e);
        }
        if (null != bytes) {
            final ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(bytes);
            if (mimeType == null) {
                mimeType = ImageTypeDetector.getMimeType(bytes);
                if ("application/octet-stream".equals(mimeType)) {
                    mimeType = getMimeType(url.toString());
                }
            }
            if (isValidImage(bytes)) {
                // Mime type should be of image type. Otherwise web server send some error page instead of 404 error code.
                if (null == mimeType) {
                    mimeType = "image/jpeg";
                }
                fileHolder.setContentType(mimeType);
            }
            return fileHolder;
        }
        return null;
    }

    private static void transfer(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
        out.flush();
    }

    private static final FileTypeMap DEFAULT_FILE_TYPE_MAP = FileTypeMap.getDefaultFileTypeMap();

    private static String getMimeType(final String filename) {
        return DEFAULT_FILE_TYPE_MAP.getContentType(filename);
    }

    private static boolean isValidImage(final byte[] data) {
        java.awt.image.BufferedImage bimg = null;
        try {
            bimg = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(data));
        } catch (final Exception e) {
            return false;
        }
        return (bimg != null);
    }

    private static interface Stringer<E> {

        String getString(E element);
    }

    private static final Stringer<Object> DEFAULT_STRINGER = new Stringer<Object>() {

        @Override
        public String getString(final Object e) {
            return null == e ? "null" : e.toString();
        }
    };

    private static <E> String collectionToCsv(final Collection<E> col) {
        return collectionToCsv(col, null);
    }

    private static <E> String collectionToCsv(final Collection<E> col, final Stringer<E> stringer) {
        if (null == col || col.isEmpty()) {
            return null;
        }
        final Stringer<E> str = (Stringer<E>) (null == stringer ? DEFAULT_STRINGER : stringer);
        final Iterator<E> iter = col.iterator();
        final StringBuilder sb = new StringBuilder(col.size() << 4);
        sb.append(str.getString(iter.next()));
        while (iter.hasNext()) {
            sb.append(',').append(str.getString(iter.next()));
        }
        return sb.toString();
    }

    private static final DateFormat ISO6801;

    static {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO6801 = df;
    }


}
