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

package com.openexchange.groupware.infostore;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.EntityInfo.Type;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.ContactUtil;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link EntityInfoLoader}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
@NotThreadSafe
public class EntityInfoLoader {

    private final Map<String, EntityInfo> knownUsers;

    public EntityInfoLoader() {
        super();
        this.knownUsers = new HashMap<String, EntityInfo>();
    }

    public EntityInfo load(int userId, Session session) throws OXException {
        if (null == session) {
            return null;
        }
        String identifier = String.valueOf(userId);
        if (knownUsers.containsKey(identifier)) {
            return knownUsers.get(identifier);
        }
        UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }
        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class);
        if (null == contactService) {
            throw ServiceExceptionCode.absentService(ContactService.class);
        }
        GroupService groupService = ServerServiceRegistry.getInstance().getService(GroupService.class);
        if (null == groupService) {
            throw ServiceExceptionCode.absentService(GroupService.class);
        }
        User user = userService.getUser(userId, session.getContextId());
        String displayName = user.getDisplayName();
        String email1 = user.getMail();
        if (user.isGuest()) {
            EntityInfo.Type type = user.isAnonymousGuest() ? Type.ANONYMOUS : Type.GUEST;
            return new EntityInfo(identifier, displayName, null, user.getGivenName(), user.getSurname(), email1, user.getId(), null, type);
        }
        Contact contact;
        Type type;
        try {
            contact = contactService.getUser(session, userId);
            type = Type.USER;
        } catch (OXException e) {
            if (ContactExceptionCodes.CONTACT_NOT_FOUND.equals(e)) {
                ContactSearchObject cso = new ContactSearchObject();
                cso.setEmail1(email1);
                SearchIterator<Contact> it = contactService.searchContacts(session, cso);
                if (it.hasNext()) {
                    contact = it.next();
                    type = Type.GUEST;
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
        String title = contact.getTitle();
        String firstName = contact.getGivenName();
        String lastName = contact.getSurName();
        String imageUrl = null;
        if (0 < contact.getNumberOfImages()) {
            imageUrl = ContactUtil.generateImageUrl(session, contact);
        }
        EntityInfo entityInfo = new EntityInfo(identifier, displayName, title, firstName, lastName, email1, userId, imageUrl, type);
        knownUsers.put(identifier, entityInfo);
        return entityInfo;
    }

}
