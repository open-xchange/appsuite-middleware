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

package com.openexchange.carddav.photos;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.ContactService;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.mixins.AddressbookHomeSet;
import com.openexchange.dav.mixins.CalendarHomeSet;
import com.openexchange.dav.mixins.CurrentUserPrincipal;
import com.openexchange.dav.mixins.PrincipalCollectionSet;
import com.openexchange.dav.resources.DAVResource;
import com.openexchange.dav.resources.DAVRootCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

/**
 * {@link RootPhotoCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class RootPhotoCollection extends DAVRootCollection {

    private static final ContactField[] IMAGE_FIELDS = {
        ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.NUMBER_OF_IMAGES,
        ContactField.IMAGE1, ContactField.IMAGE1_CONTENT_TYPE, ContactField.IMAGE_LAST_MODIFIED
    };

    /**
     * Initializes a new {@link RootPhotoCollection}.
     *
     * @param factory The factory
     */
    public RootPhotoCollection(PhotoFactory factory) {
        super(factory, "Photos");
        ConfigViewFactory configViewFactory = factory.getService(ConfigViewFactory.class);
        includeProperties(new CurrentUserPrincipal(factory), new PrincipalCollectionSet(configViewFactory), new CalendarHomeSet(configViewFactory), new AddressbookHomeSet(configViewFactory));
    }

    @Override
    public List<WebdavResource> getChildren() throws WebdavProtocolException {
        return Collections.emptyList();
    }

    @Override
    public DAVResource getChild(String name) throws WebdavProtocolException {
        /*
         * decode name
         */
        Contact contact;
        try {
            contact = PhotoUtils.decodeName(name);
        } catch (IllegalArgumentException e) {
            throw protocolException(getUrl(), e, HttpServletResponse.SC_NOT_FOUND);
        }
        /*
         * re-load image data from storage, verify timestamp & create corresponding photo resource
         */
        DAVFactory factory = getFactory();
        try {
            Contact loadedContact = factory.requireService(ContactService.class).getContact(
                factory.getSession(), String.valueOf(contact.getParentFolderID()), String.valueOf(contact.getObjectID()), IMAGE_FIELDS);
            if (null != contact.getImageLastModified() && false == contact.getImageLastModified().equals(loadedContact.getImageLastModified())) {
                throw protocolException(getUrl(), HttpServletResponse.SC_NOT_FOUND);
            }
            return new PhotoResource(factory, loadedContact, constructPathForChildResource(name));
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

}
