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

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.user.json.dto.UserContact;

/**
 * {@link GetAction} - Maps the action to a <tt>get</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class GetAction extends AbstractUserAction {

    /**
     * The <tt>get</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_GET;

    /**
     * Initializes a new {@link GetAction}.
     */
    public GetAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final int[] columns = parseOptionalIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        final ContactField[] contactFields;
        if (null == columns || 0 == columns.length) {
            contactFields = ContactMapper.getInstance().getAllFields();
        } else {
            contactFields = ContactMapper.getInstance().getFields(columns);
        }
        final String idParam = request.getParameter("id");
        int userId;
        if (null == idParam) {
            userId = session.getUserId();
        } else {
            userId = checkIntParameter("id", request);
        }
        /*
         * Obtain user from user service
         */
        final UserService userService = services.getService(UserService.class);
        final User user = userService.getUser(userId, session.getContext());
        /*
         * Obtain user's contact
         */
        Contact contact = null;
        if (user.isGuest()) {
            ContactUserStorage contactUserStorage = services.getService(ContactUserStorage.class);
            contact = contactUserStorage.getGuestContact(session.getContextId(), userId, contactFields);
        } else {
            ContactService contactService = services.getService(ContactService.class);
            contact = contactService.getUser(session, userId, contactFields);
        }
        if (contact.getInternalUserId() != user.getId() || user.getContactId() != contact.getObjectID()) {
//            throw UserC
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(new UserContact(contact, censor(session, user)), contact.getLastModified(), "usercontact");
    }

}
