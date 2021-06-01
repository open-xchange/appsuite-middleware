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

import static com.openexchange.dav.DAVTools.getExternalPath;
import static com.openexchange.dav.DAVTools.removePathPrefixFromPath;
import static com.openexchange.dav.DAVTools.removePrefixFromPath;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.http.client.utils.URIBuilder;
import com.google.common.io.BaseEncoding;
import com.openexchange.config.cascade.ConfigViewFactory;
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
     * @param configViewFactory The configuration 
     * @param hostData The host data to use for generating the link
     * @param contact The contact to build the image URI for
     * @return The image URI, or <code>null</code> if the contact does not contain an image
     * @throws WebdavProtocolException If URI building fails
     */
    public static URI buildURI(ConfigViewFactory configViewFactory, HostData hostData, Contact contact) throws WebdavProtocolException {
        if (0 >= contact.getNumberOfImages()) {
            return null;
        }
        try {
            String path = new StringBuilder().append("/photos/").append(encodeName(contact)).append('/')
                .append("image1.").append(MimeType2ExtMap.getFileExtension(contact.getImageContentType(), "jpg")).toString();
            return new URI(new URIBuilder()
                .setScheme(hostData.isSecure() ? "https" : "http")
                .setHost(hostData.getHost())
                .setPath(getExternalPath(configViewFactory, path))
            .toString());
        } catch (URISyntaxException e) {
            throw DAVProtocol.protocolException(new WebdavPath("/photos"), e);
        }
    }

    /**
     * Decodes the targeted contact from the given URI.
     * 
     * @param configViewFactory The configuration 
     * @param uri The URI to decode
     * @return The decoded contact (with the object- and folder-id properties set)
     * @throws IllegalArgumentException If path isn't valid
     */
    public static Contact decodeURI(ConfigViewFactory configViewFactory, URI uri) throws IllegalArgumentException {
        String path = uri.getPath();
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
        path = removePathPrefixFromPath(configViewFactory, path);
        path = removePrefixFromPath("/photos", path);
        int index = path.indexOf('/');
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
