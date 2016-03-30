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

package com.openexchange.user.json.actions;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.user.json.UserContact;

/**
 * {@link GetAction} - Maps the action to a <tt>get</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.GET, name = "get", description = "Get a user.", parameters = {
		@Parameter(name = "session", description = "A session ID previously obtained from the login module."),
		@Parameter(name = "id", optional = true, description = "Object ID of the requested user. Since v6.18.1, this parameter is optional: the default is the currently logged in user.")
}, responseDescription = "Response with timestamp: An object containing all data of the requested user. The fields of the object are listed in Common object data, Detailed contact data and Detailed user data.")
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
