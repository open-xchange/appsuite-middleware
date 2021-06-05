/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.user.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;
import com.openexchange.user.json.dto.UserContact;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link ListAction} - Maps the action to a <tt>list</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class ListAction extends AbstractUserAction {

    /**
     * The <tt>list</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_LIST;

    /**
     * Initializes a new {@link ListAction}.
     */
    public ListAction(ServiceLookup services) {
        super(services);
    }

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
            final ContactService contactService = services.getService(ContactService.class);
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
                            userService = services.getService(UserService.class);
                        }
                        final User user = getUserByContact(session, userService, contact);
                        if (null != user) {
                            internalUserId = user.getId();
                            contact.setInternalUserId(internalUserId);
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
        censor(session, users);
        final List<UserContact> userContacts = new ArrayList<UserContact>(users.length);
        for (final User user : users) {
            final Contact contact = contacts.get(user.getId());
            if (null != contact) {
                userContacts.add(new UserContact(contact, user));
                final Date contactLastModified = contact.getLastModified();
                if (null != contactLastModified && ((null == lastModified) || (contactLastModified.after(lastModified)))) {
                    lastModified = contactLastModified;
                }
            } else {
                userContacts.add(new UserContact(user));
            }
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(userContacts, lastModified, "usercontact").addWarnings(warnings);
    }

    private User getUserByContact(final ServerSession session, UserService userService, final Contact contact) throws OXException {
        final String email1 = contact.getEmail1();
        User user = com.openexchange.java.Strings.isEmpty(email1) ? null : userService.searchUser(email1, session.getContext());
        if (null == user) {
            final User[] usrs = userService.searchUserByName(contact.getDisplayName(), session.getContext(), UserService.SEARCH_DISPLAY_NAME);
            if (null != usrs && usrs.length > 0) {
                user = usrs[0];
            }
        }
        return user;
    }

    private int[] parseUserIDs(final AJAXRequestData request, final int fallbackUserID) throws OXException {
        Object data = request.getData();
        if (null == data) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        if (!(data instanceof JSONArray)) {
            throw AjaxExceptionCodes.INVALID_REQUEST_BODY.create("JSONArray", data.getClass().getSimpleName());
        }

        JSONArray jsonArray = (JSONArray) request.getData();
        if (null == jsonArray) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("data");
        }
        final int length = jsonArray.length();
        final int[] userIDs = new int[length];
        try {
            for (int i = 0; i < length; i++) {
                userIDs[i] = jsonArray.isNull(i) ? fallbackUserID : jsonArray.getInt(i);
            }
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
        return userIDs;
    }

    private User[] getUsers(final ServerSession session, final int[] userIDs, final List<OXException> warnings) throws OXException {
        final UserService userService = services.getService(UserService.class);
        try {
            return userService.getUser(session.getContext(), userIDs);
        } catch (OXException e) {
            if (!UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                throw e;
            }
            final Context context = session.getContext();
            {
                final Object[] excArgs = e.getLogArgs();
                if (excArgs != null && excArgs.length >= 2) {
                    try {
                        userService.invalidateUser(context, ((Integer) excArgs[0]).intValue());
                    } catch (Exception ignore) {
                        // Ignore
                    }
                } else {
                    for (final int userId : userIDs) {
                        try {
                            userService.invalidateUser(context, userId);
                        } catch (Exception ignore) {
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
                } catch (OXException ue) {
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
}
