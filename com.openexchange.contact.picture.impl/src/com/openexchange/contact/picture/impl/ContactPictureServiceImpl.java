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

package com.openexchange.contact.picture.impl;

import static com.openexchange.contact.picture.ContactPicture.FALLBACK_PICTURE;
import java.util.Iterator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.UnmodifiableContactPictureRequestData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.FinderResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;

/**
 * {@link ContactPictureServiceImpl}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactPictureServiceImpl extends RankingAwareNearRegistryServiceTracker<ContactPictureFinder> implements ContactPictureService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContactPictureServiceImpl.class);

    /**
     * Initializes a new {@link ContactPictureServiceImpl}.
     * 
     * @param context The {@link BundleContext}
     */
    public ContactPictureServiceImpl(BundleContext context) {
        super(context, ContactPictureFinder.class);
    }

    @Override
    public ContactPicture getPicture(ContactPictureRequestData contactData) {
        // Ask each finder if it contains the picture
        try {
            UnmodifiableContactPictureRequestData original = new UnmodifiableContactPictureRequestData(contactData);
            ContactPictureRequestData modified = contactData;
            FinderResult result = new FinderResult(original, modified);
            for (Iterator<ContactPictureFinder> iterator = iterator(); iterator.hasNext();) {
                ContactPictureFinder next = iterator.next();

                // Try to get contact picture
                if (next.isRunnable(original)) {
                    result = next.getPicture(result);
                    if (null != result.getContactPicture()) {
                        return result.getContactPicture();
                    }
                }
            }
        } catch (OXException e) {
            LOGGER.warn("Unable to get contact picture. Using fallback instead.", e);
        }
        return FALLBACK_PICTURE;
    }
}
