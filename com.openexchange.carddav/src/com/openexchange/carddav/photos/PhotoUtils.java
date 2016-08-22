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

package com.openexchange.carddav.photos;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.http.client.utils.URIBuilder;
import com.google.common.io.BaseEncoding;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link PhotoUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class PhotoUtils {

    /**
     * Builds the full URI for a specific contact image.
     *
     * @param hostData The host data to use for generating the link
     * @param contact The contact to build the image URI for
     * @return The image URI, or <code>null</code> if the contact does not contain an image
     */
    public static URI buildURI(HostData hostData, Contact contact) throws WebdavProtocolException {
        if (0 >= contact.getNumberOfImages()) {
            return null;
        }
        try {
            return new URI(new URIBuilder()
                .setScheme(hostData.isSecure() ? "https" : "http")
                .setHost(hostData.getHost())
                .setPath(new StringBuilder("/photos/").append(encodeName(contact)).append('/')
                    .append("image1.").append(MimeType2ExtMap.getFileExtension(contact.getImageContentType(), "jpg")).toString())
            .toString());
        } catch (URISyntaxException e) {
            throw DAVProtocol.protocolException(new WebdavPath("/photos"), e);
        }
    }

    /**
     * Decodes the targeted contact from the given URI.
     *
     * @param uri The URI to decode
     * @return The decoded contact (with the object- and folder-id properties set)
     */
    public static Contact decodeURI(URI uri) throws IllegalArgumentException {
        String path = uri.getPath();
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
        int index = path.indexOf("photos/");
        if (-1 == index) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
        path = path.substring(7);
        index = path.indexOf('/');
        if (-1 != index) {
            path = path.substring(0, index);
        }
        return PhotoUtils.decodeName(path);
    }

    public static String encodeName(Contact contact) {
        long lastModified = null != contact.getImageLastModified() ? contact.getImageLastModified().getTime() : 0L;
        String name = contact.getParentFolderID() + "-" + contact.getObjectID() + "-" +  lastModified;
        return BaseEncoding.base64Url().omitPadding().encode(name.getBytes(Charsets.UTF_8));
    }

    public static Contact decodeName(String name) throws IllegalArgumentException {
        String decodedName = new String(BaseEncoding.base64Url().omitPadding().decode(name), Charsets.UTF_8);
        String[] splitted = Strings.splitByDelimNotInQuotes(decodedName, '-');
        if (null == splitted || 3 != splitted.length) {
            throw new IllegalArgumentException(name);
        }
        int folderId, contactId;
        long lastModified;
        try {
            folderId = Integer.parseInt(splitted[0]);
            contactId = Integer.parseInt(splitted[1]);
            lastModified = Long.parseLong(splitted[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name, e);
        }
        Contact contact = new Contact();
        contact.setParentFolderID(folderId);
        contact.setObjectID(contactId);
        contact.setImageLastModified(new Date(lastModified));
        return contact;
    }

}
