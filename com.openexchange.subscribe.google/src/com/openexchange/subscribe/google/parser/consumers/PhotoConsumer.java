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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.util.ServiceException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.google.GoogleContactsSubscribeService;

/**
 * {@link PhotoConsumer} - Parses the birthday of the contact
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PhotoConsumer implements BiConsumer<ContactEntry, Contact> {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleContactsSubscribeService.class);

    private final ContactsService googleContactsService;

    /**
     * Initialises a new {@link PhotoConsumer}.
     */
    public PhotoConsumer(ContactsService googleContactsService) {
        super();
        this.googleContactsService = googleContactsService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
     */
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
            LOG.debug("Error fetching contact's image from '{}'", photoLink.getHref());
        } finally {
            if (request != null) {
                request.end();
            }
            IOUtils.closeQuietly(resultStream);
            IOUtils.closeQuietly(out);
        }
    }
}
