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

package com.openexchange.dav.attachments;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import com.google.common.io.BaseEncoding;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link AttachmentUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AttachmentUtils {

    private static final AttachmentMetadataFactory FACTORY = new AttachmentMetadataFactory();

    /**
     * Creates a new attachment metadata instance, copying over the properties of the supplied reference.
     *
     * @param originalMetadata The original metadata to apply
     * @return The new attachment metadata instance
     */
    public static AttachmentMetadata newAttachmentMetadata(AttachmentMetadata originalMetadata) {
        return FACTORY.newAttachmentMetadata(originalMetadata);
    }

    /**
     * Creates a new attachment metadata instance.
     *
     * @return The new attachment metadata instance
     */
    public static AttachmentMetadata newAttachmentMetadata() {
        return FACTORY.newAttachmentMetadata();
    }

    /**
     * Extracts the target filename based on the <code>Content-Disposition</code> header found in the supplied WebDAV request, falling back
     * to a generic default value if none could be extracted.
     *
     * @param request The WebDAV request to parse the filename for
     * @return The parsed filename, falling back to <code>attachment</code> if none could be extracted.
     */
    public static String parseFileName(WebdavRequest request) throws WebdavProtocolException {
        try {
            ContentDisposition contentDisposition = new ContentDisposition(request.getHeader("Content-Disposition"));
            String filename = contentDisposition.getFilenameParameter();
            return null != filename ? filename : "attachment";
        } catch (OXException e) {
            throw WebdavProtocolException.generalError(e, request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Gets the groupware module type identifier for a specific content type, i.e. one of the module constants defined at {@link Types}
     * (beware, this is <b>not</b> the same as returned by {@link ContentType#getModule()}.
     *
     * @return The module identifier, or <code>-1</code> if unknown
     */
    public static int getModuleId(ContentType contentType) {
        if (null != contentType) {
            if (CalendarContentType.getInstance().equals(contentType)) {
                return com.openexchange.groupware.Types.APPOINTMENT;
            }
            if (TaskContentType.getInstance().equals(contentType)) {
                return com.openexchange.groupware.Types.TASK;
            }
            if (ContactContentType.getInstance().equals(contentType)) {
                return com.openexchange.groupware.Types.CONTACT;
            }
        }
        return -1;
    }

    /**
     * Builds the full URI for a specific attachment.
     *
     * @param hostData The host data to use for generating the link
     * @param metadata The attachment metadata to build the URI for
     * @return The URI
     */
    public static URI buildURI(HostData hostData, AttachmentMetadata metadata) throws URISyntaxException {
        return new URI(new URIBuilder()
            .setScheme(hostData.isSecure() ? "https" : "http")
            .setHost(hostData.getHost())
            .setPath(new StringBuilder("/attachments/").append(encodeName(metadata)).append('/').append(metadata.getFilename()).toString())
        .toString());
    }

    /**
     * Decodes attachment metadata from the given URI.
     *
     * @param uri The URI to decode
     * @return The decoded attachment metadata
     */
    public static AttachmentMetadata decodeURI(URI uri) throws IllegalArgumentException {
        String path = uri.getPath();
        if (Strings.isEmpty(path)) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
        int index = path.indexOf("attachments/");
        if (-1 == index) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
        path = path.substring(13);
        index = path.indexOf('/');
        if (-1 != index) {
            path = path.substring(0, index);
        }
        return AttachmentUtils.decodeName(path);
    }

    public static String encodeName(AttachmentMetadata metadata) {
        String name = metadata.getModuleId() + "-" + metadata.getFolderId() + "-" + metadata.getAttachedId() + "-" + metadata.getId();
        return BaseEncoding.base64Url().omitPadding().encode(name.getBytes(Charsets.UTF_8));
    }

    public static AttachmentMetadata decodeName(String name) throws IllegalArgumentException {
        String decodedName = new String(BaseEncoding.base64Url().omitPadding().decode(name), Charsets.UTF_8);
        String[] splitted = Strings.splitByDelimNotInQuotes(decodedName, '-');
        if (null == splitted || 4 != splitted.length) {
            throw new IllegalArgumentException(name);
        }
        int moduleId, folderId, attachedId, id;
        try {
            moduleId = Integer.parseInt(splitted[0]);
            folderId = Integer.parseInt(splitted[1]);
            attachedId = Integer.parseInt(splitted[2]);
            id = Integer.parseInt(splitted[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name, e);
        }
        AttachmentMetadata metdata = newAttachmentMetadata();
        metdata.setModuleId(moduleId);
        metdata.setFolderId(folderId);
        metdata.setAttachedId(attachedId);
        metdata.setId(id);
        return metdata;
    }

    public static WebdavProtocolException protocolException(OXException e, WebdavPath url) {
        if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            return WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_FORBIDDEN);
        }
        if ("ATT-0405".equals(e.getErrorCode())) {
            return WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_NOT_FOUND);
        }
        return WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

}
