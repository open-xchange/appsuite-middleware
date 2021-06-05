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

import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contacts.json.RequestTools;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.guest.GuestService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.user.json.Constants;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.mapping.UserMapper;

/**
 * {@link UpdateAction} - Maps the action to an <tt>update</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class UpdateAction extends AbstractUserAction {

    /**
     * The <tt>update</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_UPDATE;

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction(ServiceLookup services) {
        super(services);
    }

    private static UserField[] USER_FIELDS = { UserField.ID, UserField.LOCALE, UserField.TIME_ZONE };

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            /*
             * Parse parameters
             */
            long maxSize = sysconfMaxUpload();
            boolean containsImage = request.hasUploads(-1, maxSize > 0 ? maxSize : -1L);

            final int id = checkIntParameter(AJAXServlet.PARAMETER_ID, request);
            final Date clientLastModified = new Date(checkLongParameter(AJAXServlet.PARAMETER_TIMESTAMP, request));
            /*
             * Get user service to get contact ID
             */
            final UserService userService = services.getService(UserService.class);
            final User storageUser = userService.getUser(id, session.getContext());
            final int contactId = storageUser.getContactId();
            /*
             * Parse user & contact data
             */
            String timeZoneID = request.getParameter("timezone");
            if (null == timeZoneID) {
                timeZoneID = session.getUser().getTimeZone();
            }
            final JSONObject jData = containsImage ? new JSONObject(request.getUploadEvent().getFormField("json")) : (JSONObject) request.requireData();
            Contact parsedUserContact;
            User parsedUser;
            parsedUserContact = ContactMapper.getInstance().deserialize(jData, ContactMapper.getInstance().getAllFields(), timeZoneID);
            parsedUserContact.setObjectID(contactId);
            jData.put(UserField.ID.getName(), id);
            parsedUser = UserMapper.getInstance().deserialize(jData, USER_FIELDS, timeZoneID);

            if (containsImage) {
                RequestTools.setImageData(request, parsedUserContact);
            }

            /*
             * Update contact
             */
            final ContactService contactService;
            if (!storageUser.isGuest()) {
                contactService = services.getService(ContactService.class);
                if (parsedUserContact.containsDisplayName()) {
                    final String displayName = parsedUserContact.getDisplayName();
                    if (null != displayName) {
                        if (com.openexchange.java.Strings.isEmpty(displayName)) {
                            parsedUserContact.removeDisplayName();
                        } else {
                            // Remove display name if equal to storage version to avoid update conflict
                            Contact storageContact = contactService.getUser(session, id, new ContactField[] { ContactField.DISPLAY_NAME });
                            if (displayName.equals(storageContact.getDisplayName())) {
                                parsedUserContact.removeDisplayName();
                            }
                        }
                    }
                }
                contactService.updateUser(session, Integer.toString(Constants.USER_ADDRESS_BOOK_FOLDER_ID), Integer.toString(contactId), parsedUserContact, clientLastModified);
            } else {
                ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
                contactUserStorage.updateGuestContact(session, contactId, parsedUserContact, parsedUserContact.getLastModified());

                GuestService guestService = services.getService(GuestService.class);
                if ((guestService != null) && (storageUser.isGuest())) {
                    Contact updatedGuestContact = contactUserStorage.getGuestContact(session.getContextId(), id, ContactField.values());
                    guestService.updateGuestContact(updatedGuestContact, session.getContextId());
                }
            }
            /*
             * Update user, too, if necessary
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

                GuestService guestService = services.getService(GuestService.class);
                if ((guestService != null) && (storageUser.isGuest())) {
                    User updatedUser = userService.getUser(id, session.getContextId());
                    guestService.updateGuestUser(updatedUser, session.getContextId());
                }
            }
            /*
             * Return contact last-modified from server
             */
            return new AJAXRequestResult(new JSONObject(0), parsedUserContact.getLastModified());
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }

    }

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if (null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

}
