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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.ContactPictureUtil;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.FinderResult;
import com.openexchange.exception.OXException;
import com.openexchange.functions.OXFunction;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;

/**
 * {@link AbstractContactFinder} - Abstract class for all {@link ContactService} related searches for contact pictures.
 * <p>
 * <b>CAUTION</b>: This class uses a continuous integer to keep track of its children. Classes that wan't to inherit from this class should use {@link #childCount}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class AbstractContactFinder implements ContactPictureFinder {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractContactFinder.class);

    protected final static ContactField[] IMAGE_FIELD = new ContactField[] { ContactField.OBJECT_ID, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE1_URL,
        ContactField.IMAGE_LAST_MODIFIED };

    protected final ContactService contactService;

    protected static AtomicInteger childCount = new AtomicInteger(0);

    private final int child;

    /**
     * Initializes a new {@link AbstractContactFinder}.
     * 
     * @param contactService The {@link ContactService}
     * @param child The ranking of the children
     * 
     */
    public AbstractContactFinder(ContactService contactService, int child) {
        super();
        this.contactService = contactService;
        this.child = child;
    }

    @Override
    public FinderResult getPicture(ContactPictureRequestData cprd) {
        FinderResult result = new FinderResult(cprd);
        try {
            Contact contact = getContact().apply(cprd);
            if (null != contact.getImage1()) {
                result.setPicture(ContactPictureUtil.fromContact(contact, cprd.onlyETag()));
            } else {
                result.modify().setEmails(contact.getEmail1(), contact.getEmail2(), contact.getEmail3());
            }
        } catch (OXException e) {
            handleException().accept(cprd, e);
        }
        return result;
    }

    /**
     * Get the function to provide the Contact in this context
     * 
     * @return The {@link OXFunction}
     */
    abstract OXFunction<ContactPictureRequestData, Contact> getContact();

    /**
     * Get the function to handle {@link OXException} when applying {@link #getContact()}
     * 
     * @return The {@link BiConsumer} to handle {@link OXException}
     */
    abstract BiConsumer<ContactPictureRequestData, OXException> handleException();

    @Override
    public boolean isRunnable(ContactPictureRequestData cprd) {
        return null != contactService;
    }

    @Override
    public int getRanking() {
        return 20 + child;
    }
}
