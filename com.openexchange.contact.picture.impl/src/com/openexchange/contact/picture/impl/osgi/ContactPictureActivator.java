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

package com.openexchange.contact.picture.impl.osgi;

import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.impl.ContactPictureServiceImpl;
import com.openexchange.contact.picture.impl.finder.ContactIDFinder;
import com.openexchange.contact.picture.impl.finder.ContactMailFinder;
import com.openexchange.contact.picture.impl.finder.ContactUserFinder;
import com.openexchange.contact.picture.impl.finder.OwnContactFinder;
import com.openexchange.contact.picture.impl.finder.UserPictureFinder;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.UserService;

/**
 * {@link ContactPictureActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public final class ContactPictureActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link ContactPictureActivator}.
     *
     */
    public ContactPictureActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedContactsAccessFactory.class, UserService.class, ContactUserStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        /*
         * Add tracker for Finder
         */
        ContactPictureServiceImpl contactPictureServiceImpl = new ContactPictureServiceImpl(context);
        track(ContactPictureFinder.class, contactPictureServiceImpl);
        openTrackers();

        /*
         * Register service
         */
        registerService(ContactPictureService.class, contactPictureServiceImpl);

        /*
         * Needed services for ContactPictureFinder
         */
        IDBasedContactsAccessFactory idBasedContactsAccessFactory = getServiceSafe(IDBasedContactsAccessFactory.class);
        UserService userService = getServiceSafe(UserService.class);
        ContactUserStorage contactUserStorage = getServiceSafe(ContactUserStorage.class);

        /*
         * Register ContactPictureFinder
         */
        registerService(ContactPictureFinder.class, new UserPictureFinder(userService));
        registerService(ContactPictureFinder.class, new ContactUserFinder(idBasedContactsAccessFactory));
        registerService(ContactPictureFinder.class, new ContactIDFinder(idBasedContactsAccessFactory));
        registerService(ContactPictureFinder.class, new ContactMailFinder(idBasedContactsAccessFactory));
        registerService(ContactPictureFinder.class, new OwnContactFinder(idBasedContactsAccessFactory, userService, contactUserStorage));

    }

}
