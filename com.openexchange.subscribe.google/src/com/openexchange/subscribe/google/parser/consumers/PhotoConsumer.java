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

import static com.openexchange.java.Autoboxing.b;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.Photo;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.io.IOUtils;

/**
 * {@link PhotoConsumer} - Parses the photo of the contact
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class PhotoConsumer implements BiConsumer<Person, Contact> {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {

        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PhotoConsumer.class);
    }

    private final PeopleService googlePeopleService;

    private final int maxImageSize;

    /**
     * Initialises a new {@link PhotoConsumer}.
     */
    public PhotoConsumer(PeopleService googlePeopleService, int maxImageSize) {
        super();
        this.googlePeopleService = googlePeopleService;
        this.maxImageSize = maxImageSize;
    }

    @Override
    public void accept(Person person, Contact contact) {
        List<Photo> photos = person.getPhotos();
        if (photos == null || photos.isEmpty()) {
            return;
        }
        Photo photo = photos.get(0);
        Boolean photoDefault = photo.getDefault();
        if (photoDefault == null || !b(photoDefault)) {
            String url = photo.getUrl();
            setPhoto(url, contact);
        }
    }

    /**
     * Fetches and sets the photo
     *
     * @param url The URL to fetch the photo from
     * @param contact The {@link Contact}
     */
    private void setPhoto(String url, Contact contact) {
        InputStream resultStream = null;
        ByteArrayOutputStream out = null;
        HttpResponse response = null;
        try {
            response = googlePeopleService.getRequestFactory().buildGetRequest(new GenericUrl(url)).execute();
            resultStream = response.getContent();
            out = new ByteArrayOutputStream();
            int read = 0;
            byte[] buffer = new byte[4096];
            while ((read = resultStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                if (out.size() > maxImageSize) {
                    LoggerHolder.LOG.debug("Max image size exceeded. Ignoring image from google people api.");
                    return; // image size exceeded. do not set an image
                }
            }
            contact.setImage1(out.toByteArray());
            contact.setImageContentType(response.getContentType());
        } catch (IOException e) {
            LoggerHolder.LOG.debug("Error fetching contact's image from '{}'", url, e);
        } finally {
            IOUtils.closeQuietly(resultStream);
            IOUtils.closeQuietly(out);
        }
    }
}
