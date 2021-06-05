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

package com.openexchange.subscribe.google.parser.consumers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.util.ServiceException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.io.IOUtils;

/**
 * {@link PhotoConsumer} - Parses the birthday of the contact
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PhotoConsumer implements BiConsumer<ContactEntry, Contact> {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PhotoConsumer.class);
    }

    private final ContactsService googleContactsService;

    /**
     * Initialises a new {@link PhotoConsumer}.
     */
    public PhotoConsumer(ContactsService googleContactsService) {
        super();
        this.googleContactsService = googleContactsService;
    }

    @Override
    public void accept(ContactEntry t, Contact u) {
        Link photoLink = t.getContactPhotoLink();
        if (photoLink == null) {
            return;
        }
        GDataRequest request = null;
        InputStream resultStream = null;
        ByteArrayOutputStream out = null;
        try {
            request = googleContactsService.createLinkQueryRequest(photoLink);
            request.execute();
            resultStream = request.getResponseStream();
            out = new ByteArrayOutputStream();
            int read = 0;
            byte[] buffer = new byte[4096];
            while ((read = resultStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            u.setImage1(out.toByteArray());
            u.setImageContentType(photoLink.getType());
        } catch (IOException | ServiceException e) {
            LoggerHolder.LOG.debug("Error fetching contact's image from '{}'", photoLink.getHref(), e);
        } finally {
            if (request != null) {
                request.end();
            }
            IOUtils.closeQuietly(resultStream);
            IOUtils.closeQuietly(out);
        }
    }
}
