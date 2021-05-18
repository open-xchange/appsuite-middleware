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
