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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.infostore;

import java.util.HashMap;
import java.util.Map;
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
            return new EntityInfo(identifier, displayName, null, user.getGivenName(), user.getSurname(), email1, user.getId(), null, Type.GUEST);
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
