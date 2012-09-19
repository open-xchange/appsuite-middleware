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

package com.openexchange.user.json.actions;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.ContactInterfaceFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.UserContact;
import com.openexchange.user.json.services.ServiceRegistry;
import com.openexchange.user.json.writer.UserWriter;

/**
 * {@link ListAction} - Maps the action to a <tt>list</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "list", description = "Get a list of users.", parameters = { 
		@Parameter(name = "session", description = "A session ID previously obtained from the login module."),
		@Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for users are defined in Common object data, Detailed contact data and Detailed user data.")
}, requestBody = "An array of numbers. Each number is the ID of requested user. Since v6.18.1, a null value in the array is interpreted as the currently logged in user.",
responseDescription = "Response with timestamp: An array with user data. Each array element describes one user and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public final class ListAction extends AbstractUserAction {

    /**
     * The <tt>list</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_LIST;

    /**
     * Initializes a new {@link ListAction}.
     */
    public ListAction() {
        super();
    }

    private static final Set<String> EXPECTED_NAMES =
        Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            AJAXServlet.PARAMETER_COLUMNS,
            AJAXServlet.PARAMETER_TIMEZONE,
            AJAXServlet.PARAMETER_SESSION,
            AJAXServlet.PARAMETER_ACTION)));

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final int[] userIDs = parseUserIDs(request, session.getUserId());
        if (0 == userIDs.length) {
            return new AJAXRequestResult(new JSONArray());
        }
        final int[] columnIDs = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        /*
         * Get users/contacts
         */
        final TIntObjectMap<Contact> contacts;
        {
            final ContactService contactService = ServiceRegistry.getInstance().getService(ContactService.class, true);
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = contactService.getUsers(session, userIDs, ContactMapper.getInstance().getFields(columnIDs, ContactField.LAST_MODIFIED, ContactField.INTERNAL_USERID, ContactField.EMAIL1, ContactField.DISPLAY_NAME));
                UserService userService = null;
                contacts = new TIntObjectHashMap<Contact>();
                while (searchIterator.hasNext()) {
                    final Contact contact = searchIterator.next();
                    int internalUserId = contact.getInternalUserId();
                    if (internalUserId <= 0) {
                        if (null == userService) {
                            userService = ServiceRegistry.getInstance().getService(UserService.class, true);
                        }
                        final User user = getUserByContact(session, userService, contact);
                        if (null != user) {
                            internalUserId = user.getId();
                        }
                    }
                    contacts.put(internalUserId, contact);
                }
            } finally {
                if (null != searchIterator) {
                    searchIterator.close();
                }
            }
        }
        /*
         * Map user to contact information
         */
        Date lastModified = null;
        final List<OXException> warnings = new LinkedList<OXException>();
        final User[] users = getUsers(session, userIDs, warnings);
        final List<UserContact> userContacts = new ArrayList<UserContact>(users.length);
        for (final User user : users) {
            final Contact contact = contacts.get(user.getId());
            if (null != contact) {
                userContacts.add(new UserContact(contact, user));
                final Date contactLastModified = contact.getLastModified();
                if (null != contactLastModified && ((null == lastModified) || (contactLastModified.after(lastModified)))) {
                    lastModified = contactLastModified;
                }
            }
		}
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(userContacts, lastModified, "usercontact").addWarnings(warnings);
    }

    private User getUserByContact(final ServerSession session, UserService userService, final Contact contact) throws OXException {
        final String email1 = contact.getEmail1();
        User user = isEmpty(email1) ? null : userService.searchUser(email1, session.getContext());
        if (null == user) {
            final User[] usrs = userService.searchUserByName(contact.getDisplayName(), session.getContext(), UserService.SEARCH_DISPLAY_NAME);
            if (null != usrs && usrs.length > 0) {
                user = usrs[0];
            }
        }
        return user;
    }

    private int[] parseUserIDs(final AJAXRequestData request, final int fallbackUserID) throws OXException {
        final JSONArray jsonArray = (JSONArray) request.getData();
        if (null == jsonArray) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("data");
        }
        final int length = jsonArray.length();
        final int[] userIDs = new int[length];
        try {
            for (int i = 0; i < length; i++) {
                userIDs[i] = jsonArray.isNull(i) ? fallbackUserID : jsonArray.getInt(i);
            }
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        return userIDs;
    }

	private User[] getUsers(final ServerSession session, final int[] userIDs, final List<OXException> warnings) throws OXException {
        final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
		try {
		    return userService.getUser(session.getContext(), userIDs);
		} catch (final OXException e) {
		    if (!UserExceptionCode.USER_NOT_FOUND.equals(e)) {
		        throw e;
		    }
		    final Context context = session.getContext();
		    {
		        final Object[] excArgs = e.getLogArgs();
		        if (excArgs != null && excArgs.length >= 2) {
		            try {
		                userService.invalidateUser(context, ((Integer) excArgs[0]).intValue());
		            } catch (final Exception ignore) {
		                // Ignore
		            }
		        } else {
		            for (final int userId : userIDs) {
		                try {
		                    userService.invalidateUser(context, userId);
		                } catch (final Exception ignore) {
		                    // Ignore
		                }
		            }
		        }
		    }
		    // Load one-by-one
		    final int length = userIDs.length;
		    final List<User> list = new ArrayList<User>(length);
		    for (int i = 0; i < length; i++) {
		        try {
		            list.add(userService.getUser(userIDs[i], context));
		        } catch (final OXException ue) {
		            if (!UserExceptionCode.USER_NOT_FOUND.equals(ue)) {
		                throw ue;
		            }
		            warnings.add(ue.setCategory(Category.CATEGORY_WARNING));
		        }
		    }
		    if (list.isEmpty()) {
                // None loaded
		        throw e;
            }
		    return list.toArray(new User[list.size()]);
		}
	}

    public AJAXRequestResult performOLD(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            final int[] userIdArray;
            {
                final JSONArray jsonArray = getJsonArrayFromData(request.getData());
                if (null == jsonArray) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create( "data");
                }
                final int len = jsonArray.length();
                userIdArray = new int[len];
                for (int a = 0; a < len; a++) {
                    if (jsonArray.isNull(a)) {
                        userIdArray[a] = session.getUserId();
                    } else {
                        userIdArray[a] = jsonArray.getInt(a);
                    }
                }
            }
            final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
            final String timeZoneId = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            /*
             * Get remaining parameters
             */
            final Map<String, List<String>> attributeParameters = getAttributeParameters(EXPECTED_NAMES, request);
            /*
             * Get services
             */
            final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
            final ContactInterface contactInterface =
                ServiceRegistry.getInstance().getService(ContactInterfaceFactory.class, true).create(
                    Constants.USER_ADDRESS_BOOK_FOLDER_ID,
                    session);
            /*
             * Get users/contacts
             */
            User[] users;
            try {
                users = userService.getUser(session.getContext(), userIdArray);
            } catch (final OXException e) {
                if (!UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                    throw e;
                }
                final Context context = session.getContext();
                {
                    final Object[] excArgs = e.getLogArgs();
                    if (excArgs != null && excArgs.length >= 2) {
                        try {
                            userService.invalidateUser(context, ((Integer) excArgs[0]).intValue());
                        } catch (final Exception ignore) {
                            // Ignore
                        }
                    } else {
                        for (final int userId : userIdArray) {
                            try {
                                userService.invalidateUser(context, userId);
                            } catch (final Exception ignore) {
                                // Ignore
                            }
                        }
                    }
                }
                // Load one-by-one
                final int length = userIdArray.length;
                final List<User> list = new ArrayList<User>(length);
                for (int i = 0; i < length; i++) {
                    try {
                        list.add(userService.getUser(userIdArray[i], context));
                    } catch (final OXException ue) {
                        if (!UserExceptionCode.USER_NOT_FOUND.equals(ue)) {
                            throw ue;
                        }
                    }
                }
                users = list.toArray(new User[list.size()]);
            }
            final Contact[] contacts = contactInterface.getUsersById(userIdArray, false);
            /*
             * Determine max. last-modified time stamp
             */
            Date lastModified = contacts[0].getLastModified();
            for (int i = 1; i < contacts.length; i++) {
                final Date lm = contacts[i].getLastModified();
                if (lastModified.before(lm)) {
                    lastModified = lm;
                }
            }
            /*
             * Write users as JSON arrays to JSON array
             */
            censor(session, contacts);
            censor(session, users);
            final JSONArray jsonArray = UserWriter.writeMultiple2Array(columns, attributeParameters, users, contacts, timeZoneId);
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(jsonArray, lastModified);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    private JSONArray getJsonArrayFromData(final Object data) {
        if (null == data) {
            return new JSONArray();
        }
        if (data instanceof JSONArray) {
            return (JSONArray) data;
        }
        final String sData = data.toString().trim();
        if (isEmpty(sData)) {
            return new JSONArray();
        }
        if ('[' == sData.charAt(0)) {
            try {
                return new JSONArray(sData);
            } catch (final JSONException e) {
                // Ignore
            }
        }
        final JSONArray ret = new JSONArray();
        ret.put(sData);
        return ret;
    }

    private boolean isEmpty(final String string) {
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
