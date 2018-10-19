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

package com.openexchange.contact.picture.impl.finder;

import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link OwnContactFinder} checks if the user searches for its own contact via mail. E.g. in case the user has no access to the GAB.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class OwnContactFinder extends AbstractContactFinder {

    private final UserService userService;
    private final ContactUserStorage contactUserStorage;

    /**
     * Initializes a new {@link OwnContactFinder}.
     * 
     * @param contactService The {@link ContactService}
     * @param userService The {@link UserService}
     * @param contactUserStorage The {@link ContactUserStorage}
     */
    public OwnContactFinder(ContactService contactService, UserService userService, ContactUserStorage contactUserStorage) {
        super(contactService);
        this.userService = userService;
        this.contactUserStorage = contactUserStorage;
    }

    @Override
    Contact getContact(Session session, PictureSearchData data, ContactField... fields) throws OXException {
        User user = userService.getUser(session.getUserId(), session.getContextId());
        if (user != null && isSearched(user, data)) {
            Contact contact;
            if (user.isGuest()) {
                contact = contactUserStorage.getGuestContact(session.getContextId(), session.getUserId(), fields);
            } else {
                contact = contactService.getUser(session, session.getUserId(), fields);
            }
            return contact;
        }
        return null;
    }

    /**
     * Checks if the own contact is a valid option for the search
     * 
     * @param user The current user
     * @param data The search data
     * @return true if it is applicable, false otherwise
     */
    private boolean isSearched(User user, PictureSearchData data) {
        if (data.getUserId() != null && user.getId() == data.getUserId().intValue()) {
            return true;
        }
        for (String mail : data.getEmails()) {
            if (UserAliasUtility.isAlias(mail, user.getAliases())) {
                return true;
            }
        }

        return false;
    }

    @Override
    PictureSearchData modfiyResult(Contact contact) {
        // Do nothing
        return PictureSearchData.EMPTY_DATA;
    }

    @Override
    void handleException(PictureSearchData data, OXException exception) {
        LOGGER.debug("Unable to get own contact.", data.getUserId(), exception);
    }

}
