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

import java.sql.Connection;
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.api2.ContactInterfaceFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.Utility;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.mapping.UserMapper;
import com.openexchange.user.json.parser.ParsedUser;
import com.openexchange.user.json.parser.UserParser;
import com.openexchange.user.json.services.ServiceRegistry;

/**
 * {@link UpdateAction} - Maps the action to an <tt>update</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update a user.", parameters = { 
		@Parameter(name = "session", description = "A session ID previously obtained from the login module."),
		@Parameter(name = "id", description = "Object ID of the updated user."),
		@Parameter(name = "timestamp", type = Type.NUMBER, description = "Timestamp of the updated user. If the user was modified after the specified timestamp, then the update must fail.")
}, requestBody = "User object as described in Common object data, Detailed contact data and Detailed user data. Only modified fields are present. Note: \"timezone\" and \"locale\" are the only fields from Detailed user data which are allowed to be updated.", 
responseDescription = "Response with timestamp: An empty object.")
public final class UpdateAction extends AbstractUserAction {

    /**
     * The <tt>update</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_UPDATE;

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction() {
        super();
    }

    private static ContactField[] CONTACT_FIELDS = {
        ContactField.DISTRIBUTIONLIST, ContactField.LINKS, ContactField.CATEGORIES, ContactField.COLOR_LABEL, ContactField.PRIVATE_FLAG,
        ContactField.NUMBER_OF_ATTACHMENTS, ContactField.FOLDER_ID, ContactField.OBJECT_ID, ContactField.INTERNAL_USERID,
        ContactField.CREATED_BY, ContactField.CREATION_DATE, ContactField.MODIFIED_BY, ContactField.LAST_MODIFIED, ContactField.STATE_HOME,
        ContactField.COMPANY, ContactField.CELLULAR_TELEPHONE1, ContactField.STREET_HOME, ContactField.STREET_BUSINESS, ContactField.TELEPHONE_HOME1,
        ContactField.STATE_BUSINESS, ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.CITY_HOME, ContactField.MIDDLE_NAME,
        ContactField.BIRTHDAY, ContactField.FAX_BUSINESS, ContactField.GIVEN_NAME, ContactField.POSTAL_CODE_HOME, ContactField.POSTAL_CODE_BUSINESS,
        ContactField.TELEPHONE_BUSINESS1, ContactField.CITY_BUSINESS };

    private static UserField[] USER_FIELDS = { UserField.ID, UserField.LOCALE, UserField.TIME_ZONE };

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
        final Date clientLastModified = new Date(checkLongParameter(AJAXServlet.PARAMETER_TIMESTAMP, request));
        /*
         * Get user service to get contact ID
         */
        final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
        final User storageUser = userService.getUser(id, session.getContext());
        final int contactId = storageUser.getContactId();
        /*
         * Parse user & contact data
         */
        String timeZoneID = request.getParameter("timezone");
        if (null == timeZoneID) {
            timeZoneID = session.getUser().getTimeZone();
        }
        final JSONObject jData = (JSONObject) request.getData();
        Contact parsedUserContact;
        User parsedUser;
		try {
			parsedUserContact = ContactMapper.getInstance().deserialize(jData, CONTACT_FIELDS, timeZoneID);
	        parsedUserContact.setObjectID(contactId);
			jData.put(UserField.ID.getName(), id);
			parsedUser = UserMapper.getInstance().deserialize(jData, USER_FIELDS, timeZoneID);
		} catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
		}
        /*
         * Update contact
         */
        final ContactService contactService = ServiceRegistry.getInstance().getService(ContactService.class, true);
        if (parsedUserContact.containsDisplayName()) {
            final String displayName = parsedUserContact.getDisplayName();
            if (null != displayName) {
                if (isEmpty(displayName)) {
                    parsedUserContact.removeDisplayName();
                } else {
                    // Remove display name if equal to storage version to avoid update conflict
                    final Contact storageContact = contactService.getUser(session, id);
                    if (displayName.equals(storageContact.getDisplayName())) {
                        parsedUserContact.removeDisplayName();
                    }
                }
            }
        }
        contactService.updateUser(session, Integer.toString(Constants.USER_ADDRESS_BOOK_FOLDER_ID), Integer.toString(contactId),  parsedUserContact, clientLastModified);
        /*
         * Update user if necessary, too
         */
        final String parsedTimeZone = parsedUser.getTimeZone();
        final Locale parsedLocale = parsedUser.getLocale();
        if ((null != parsedTimeZone) || (null != parsedLocale)) {
            if (null == parsedTimeZone) {
            	UserMapper.getInstance().get(UserField.TIME_ZONE).copy(storageUser, parsedUser);
            }
            if (null == parsedLocale) {
            	UserMapper.getInstance().get(UserField.LOCALE).copy(storageUser, parsedUser);
            }
            userService.updateUser(parsedUser, session.getContext());
        }
        /*
         * Check what has been updated
         */
        if (parsedUserContact.containsDisplayName() && null != parsedUserContact.getDisplayName()) {
            // Update folder name if display-name was changed
            final DatabaseService service = com.openexchange.user.json.services.ServiceRegistry.getInstance().getService(DatabaseService.class);
            if (null != service) {
                final int contextId = session.getContextId();
                Connection con = null;
                boolean rollback = false;
                try {
                    con = service.getWritable(contextId);
                    con.setAutoCommit(false);
                    rollback = true;
                    final int[] changedfields = new int[] { Contact.DISPLAY_NAME };
                    OXFolderAdminHelper.propagateUserModification(id, changedfields, System.currentTimeMillis(), con, con, contextId);
                    con.commit();
                    rollback = false;
                } catch (final Exception ignore) {
                    // Ignore
                } finally {
                    if (null != con) {
                        if (rollback) {
                            Databases.rollback(con);
                        }
                        Databases.autocommit(con);
                        service.backWritable(contextId, con);
                    }
                }
            }
        }
        /*
         * Return contact last-modified from server
         */
        return new AJAXRequestResult(new JSONObject(), parsedUserContact.getLastModified());
    }

    public AJAXRequestResult performOLD(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
        final Date clientLastModified = new Date(checkLongParameter(AJAXServlet.PARAMETER_TIMESTAMP, request));
        /*
         * Get user service to get contact ID
         */
        final UserService userService = ServiceRegistry.getInstance().getService(UserService.class, true);
        final User storageUser = userService.getUser(id, session.getContext());
        final int contactId = storageUser.getContactId();
        /*
         * Parse user contact
         */
        final JSONObject jData = (JSONObject) request.getData();
        final Contact parsedUserContact = UserParser.parseUserContact(jData, Utility.getTimeZone(session.getUser().getTimeZone()));
        parsedUserContact.setObjectID(contactId);
        /*
         * Perform update
         */
        final ContactInterface contactInterface =
            ServiceRegistry.getInstance().getService(ContactInterfaceFactory.class, true).create(
                Constants.USER_ADDRESS_BOOK_FOLDER_ID,
                session);
        contactInterface.updateUserContact(parsedUserContact, clientLastModified);
        /*
         * Update user, too
         */
        final ParsedUser parsedUser = UserParser.parseUserData(jData, id);
        final String parsedTimeZone = parsedUser.getTimeZone();
        final Locale parsedLocale = parsedUser.getLocale();
        if ((null != parsedTimeZone) || (null != parsedLocale)) {
            if (null == parsedTimeZone) {
                parsedUser.setTimeZone(storageUser.getTimeZone());
            }
            if (null == parsedLocale) {
                parsedUser.setLocale(storageUser.getLocale());
            }
            userService.updateUser(parsedUser, session.getContext());
        }
        /*
         * Get last-modified from server
         */
        final Date lastModified = contactInterface.getUserById(id, false).getLastModified();
        return new AJAXRequestResult(new JSONObject(), lastModified);
    }

    private static boolean isEmpty(final String string) {
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
