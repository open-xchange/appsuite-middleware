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

package com.openexchange.contact.picture.impl.finder;

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.ContactIDUtil;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.PictureResult;
import com.openexchange.contact.picture.impl.ContactPictureUtil;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link AbstractContactFinder} - Abstract class for all {@link ContactService} related searches for contact pictures.
 * <p>
 * <b>CAUTION</b>: This class uses a continuous, decrementing integer to keep track of its children.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class AbstractContactFinder implements ContactPictureFinder {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractContactFinder.class);

    protected final IDBasedContactsAccessFactory idBasedContactsAccessFactory;

    private final static AtomicInteger childCount = new AtomicInteger(20);

    private final int child;

    // ---------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AbstractContactFinder}.
     *
     * @param idBasedContactsAccessFactory The {@link IDBasedContactsAccessFactory}
     */
    protected AbstractContactFinder(IDBasedContactsAccessFactory idBasedContactsAccessFactory) {
        super();
        this.idBasedContactsAccessFactory = idBasedContactsAccessFactory;
        this.child = childCount.decrementAndGet();
    }

    /**
     * Creates a {@link ContactID} for the given {@link PicuteSearchData}
     *
     * @param data The data to create the {@link ContactID} for
     * @return The {@link ContactID} for the given data
     */
    protected ContactID createContactID(PictureSearchData data) {
        return ContactIDUtil.createContactID(data.getFolderId(), data.getContactId());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Get the contact
     *
     * @param session The {@link Session}
     * @param data The data to get the contact from
     * @return The {@link Contact}
     * @throws OXException If contact can't be found
     */
    abstract Contact getContact(Session session, PictureSearchData data, ContactField... fields) throws OXException;

    /**
     * Modifies the {@link PictureSearchData} in the result
     *
     * @param contact To get the data from
     * @return The modified {@link PictureSearchData}
     */
    abstract PictureSearchData modfiyResult(Contact contact);

    /**
     * Personalized error logging
     *
     * @param data The data to log
     * @param exception The original exception to log
     */
    abstract void handleException(PictureSearchData data, OXException exception);

    // ---------------------------------------------------------------------------------------------

    @Override
    public PictureResult getPicture(Session session, PictureSearchData data) throws OXException {
        Contact contact = getContact0(session, data, SearchFields.PICTURE);
        if (null == contact) {
            return new PictureResult(data);
        } else if (0 == contact.getNumberOfImages() || false == ContactPictureUtil.hasValidImage(I(session.getContextId()), contact, data)) {
            return new PictureResult(modfiyResult(contact));
        }
        return new PictureResult(ContactPictureUtil.fromContact(contact));
    }

    @Override
    public PictureResult getETag(Session session, PictureSearchData data) {
        Contact contact = getContact0(session, data, SearchFields.ETAG);
        if (null == contact) {
            return new PictureResult(data);
        } else if (0 == contact.getNumberOfImages()) {
            return new PictureResult(modfiyResult(contact));
        }
        return new PictureResult(ContactPictureUtil.fromContact(contact));
    }

    @Override
    public PictureResult getLastModified(Session session, PictureSearchData data) {
        Contact contact = getContact0(session, data, SearchFields.LAST_MODIFIED);
        if (null == contact) {
            return new PictureResult(data);
        } else if (0 == contact.getNumberOfImages()) {
            return new PictureResult(modfiyResult(contact));
        }
        return new PictureResult(ContactPictureUtil.fromContact(contact));
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public int getRanking() {
        return 500 + child;
    }

    // ---------------------------------------------------------------------------------------------

    private Contact getContact0(Session session, PictureSearchData data, SearchFields fields) {
        if (Strings.isNotEmpty(data.getAccountId())) {
            return null;
        }

        Contact contact = null;
        try {
            contact = getContact(session, data, fields.getContactFields());
        } catch (OXException e) {
            handleException(data, e);
        }
        return contact;
    }

}
