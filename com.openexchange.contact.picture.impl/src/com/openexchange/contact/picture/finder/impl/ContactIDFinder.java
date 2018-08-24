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

package com.openexchange.contact.picture.finder.impl;

import java.util.function.BiConsumer;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.finder.FinderResult;
import com.openexchange.exception.OXException;
import com.openexchange.functions.OXFunction;
import com.openexchange.groupware.container.Contact;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ContactIDFinder} - Finds picture based on contact identifier
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactIDFinder extends AbstractContactFinder {

    /**
     * Initializes a new {@link ContactUserFinder}.
     * 
     * @param userPermissionService The {@link UserPermissionService}
     * @param contactService The {@link ContactService}
     */
    public ContactIDFinder(UserPermissionService userPermissionService, ContactService contactService) {
        super(userPermissionService, contactService);
    }

    @Override
    public boolean isRunnable(ContactPictureRequestData cprd) {
        return super.isRunnable(cprd) && cprd.hasFolder();
    }

    @Override
    OXFunction<ContactPictureRequestData, Contact> getContact() {
        return (ContactPictureRequestData cprd) -> {
            return contactService.getContact(cprd.getSession(), String.valueOf(cprd.getFolderId()), String.valueOf(cprd.getContactId()), IMAGE_FIELD);
        };
    }

    @Override
    BiConsumer<FinderResult, Contact> modfiyResult() {
        return (FinderResult result, Contact contact) -> {
            result.modify().setEmails(contact.getEmail1(), contact.getEmail2(), contact.getEmail3());
        };
    }

    @Override
    BiConsumer<ContactPictureRequestData, OXException> handleException() {
        return (ContactPictureRequestData cprd, OXException e) -> {
            LOGGER.debug("Unable to get contact for ID {} in folder {},", cprd.getContactId(), cprd.getFolderId(), e);
        };
    }

}
