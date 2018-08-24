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

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.UnmodifiableContactPictureRequestData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.impl.ContactPictureUtil;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractContactFinder} - Abstract class for all {@link ContactService} related searches for contact pictures.
 * <p>
 * <b>CAUTION</b>: This class uses a continuous integer to keep track of its children.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class AbstractContactFinder implements ContactPictureFinder {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractContactFinder.class);

    protected final ContactService contactService;

    private final static AtomicInteger childCount = new AtomicInteger(0);

    private final int child;

    private UserPermissionService userPermissionService;

    /**
     * Initializes a new {@link AbstractContactFinder}.
     * 
     * @param userPermissionService The {@link UserPermissionService}
     * @param contactService The {@link ContactService}
     */
    public AbstractContactFinder(UserPermissionService userPermissionService, ContactService contactService) {
        super();
        this.userPermissionService = userPermissionService;
        this.contactService = contactService;
        this.child = childCount.incrementAndGet();
    }

    /**
     * Get the contact
     * 
     * @param data The data to get the contact from
     * @return The {@link Contact}
     * @throws OXException If contact can't be found
     */
    abstract Contact getContact(ContactPictureRequestData data) throws OXException;

    /**
     * Modifies the {@link ContactPictureRequestData} in the result
     * 
     * @param data The {@link ContactPictureRequestData}
     * @param contact To get the data from
     */
    abstract void modfiyResult(ContactPictureRequestData data, Contact contact);

    /**
     * Personalized error logging
     * 
     * @param data The data to log
     * @param exception The original exception to log
     */
    abstract void handleException(ContactPictureRequestData data, OXException exception);

    @Override
    public ContactPicture getPicture(UnmodifiableContactPictureRequestData original, ContactPictureRequestData modified) throws OXException {
        try {
            Contact contact = getContact(modified);
            if (null != contact.getImage1() && ContactPictureUtil.checkImage(contact.getImage1(), modified)) {
                return ContactPictureUtil.fromContact(contact, modified.onlyETag());
            } else {
                modfiyResult(modified, contact);
            }
        } catch (OXException e) {
            handleException(modified, e);
        }
        return null;
    }

    @Override
    public boolean isApplicable(ContactPictureRequestData cprd) {
        if (null != userPermissionService && null != contactService) {
            try {
                return userPermissionService.getUserPermissionBits(cprd.getSession().getUserId(), cprd.getContextId().intValue()).hasContact();
            } catch (OXException e) {
                LOGGER.warn("Unable to get user permissions. Therefore can't allow to access contacts.", e);
            }
        }
        return false;
    }

    @Override
    public int getRanking() {
        return 20 + child;
    }
}
