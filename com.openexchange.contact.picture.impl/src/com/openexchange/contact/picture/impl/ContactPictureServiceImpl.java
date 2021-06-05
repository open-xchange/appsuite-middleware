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

package com.openexchange.contact.picture.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureExceptionCodes;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.PictureResult;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.session.Session;

/**
 * {@link ContactPictureServiceImpl} is a service the retrieve a picture for a given user id, contact id or email address.
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

    // ---------------------------------------------------------------------------------------------

    @Override
    public ContactPicture getPicture(Session session, PictureSearchData searchData) {
        return get(session, searchData, (ContactPictureFinder f, Session s, PictureSearchData d) -> {
            return f.getPicture(s, d);
        });
    }

    @Override
    public String getETag(Session session, PictureSearchData searchData) {
        ContactPicture picture = get(session, searchData, (ContactPictureFinder f, Session s, PictureSearchData d) -> {
            return f.getETag(s, d);
        });
        return null == picture ? null : picture.getETag();
    }

    @Override
    public Date getLastModified(Session session, PictureSearchData searchData) {
        ContactPicture picture = get(session, searchData, (ContactPictureFinder f, Session s, PictureSearchData d) -> {
            return f.getLastModified(s, d);
        });
        return null == picture ? ContactPicture.UNMODIFIED : picture.getLastModified();
    }

    // ---------------------------------------------------------------------------------------------

    private ContactPicture get(Session session, PictureSearchData searchData, ResultWrapper f) {
        try {
            if (null == session) {
                throw ContactPictureExceptionCodes.MISSING_SESSION.create();
            }

            PictureSearchData data = searchData;
            // Ask each finder if it contains the picture
            for (Iterator<ContactPictureFinder> iterator = iterator(); iterator.hasNext();) {
                ContactPictureFinder next = iterator.next();

                // Try to get contact picture
                PictureResult pictureResult = f.apply(next, session, data);
                if (pictureResult.wasFound()) {
                    return pictureResult.getPicture();
                }
                data = mergeResult(data, pictureResult);
            }
        } catch (OXException e) {
            LOGGER.debug("Unable to get contact picture. Using fallback instead.", e);
        }
        return ContactPicture.NOT_FOUND;
    }

    private PictureSearchData mergeResult(PictureSearchData data, PictureResult pictureResult) {
        PictureSearchData modified = pictureResult.getData();

        // Use client parameters prior the data we found out
        Integer userId = data.hasUser() ? data.getUserId() : modified.getUserId();
        String accountId = data.hasAccount() ? data.getAccountId() : modified.getAccountId();
        String folderId = data.hasFolder() ? data.getFolderId() : modified.getFolderId();
        String contactId = data.hasContact() ? data.getContactId() : modified.getContactId();

        LinkedHashSet<String> set = new LinkedHashSet<>(data.getEmails());
        set.addAll(modified.getEmails());

        return new PictureSearchData(userId, accountId, folderId, contactId, set);
    }

    // ---------------------------------------------------------------------------------------------

    @FunctionalInterface
    private interface ResultWrapper {

        PictureResult apply(ContactPictureFinder finder, Session session, PictureSearchData data) throws OXException;
    }

}
