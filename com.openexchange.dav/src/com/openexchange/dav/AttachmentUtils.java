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

package com.openexchange.dav;

import static com.openexchange.dav.DAVProtocol.CAL_NS;
import static com.openexchange.dav.DAVProtocol.SC_INSUFFICIENT_STORAGE;
import static com.openexchange.dav.DAVTools.getExternalPath;
import static com.openexchange.dav.DAVTools.removePathPrefixFromPath;
import static com.openexchange.dav.DAVTools.removePrefixFromPath;
import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import com.google.common.io.BaseEncoding;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.AttachmentMetadataFactory;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.tools.stream.CountingInputStream;
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
            if (CalendarContentType.getInstance().equals(contentType) ||
                com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance().equals(contentType)) {
                return com.openexchange.groupware.Types.APPOINTMENT;
            }
            if (TaskContentType.getInstance().equals(contentType)) {
                return com.openexchange.groupware.Types.TASK;
            }
            if (ContactsContentType.getInstance().equals(contentType)) {
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
     * @param configViewFactory The configuration view
     * @return The URI
     * @throws URISyntaxException 
     */
    public static URI buildURI(HostData hostData, AttachmentMetadata metadata, ConfigViewFactory configViewFactory) throws URISyntaxException {
        return new URI(new URIBuilder()
            .setScheme(hostData.isSecure() ? "https" : "http")
            .setHost(hostData.getHost())
            .setPath(new StringBuilder(getExternalPath(configViewFactory, "/attachments/"))
                .append(encodeName(metadata))
                .append('/')
                .append(metadata.getFilename()).toString())
        .toString());
    }

    /**
     * Decodes attachment metadata from the given URI.
     *
     * @param uri The URI to decode
     * @param configViewFactory The configuration view
     * @return The decoded attachment metadata
     * @throws IllegalArgumentException If path isn't valid
     */
    public static AttachmentMetadata decodeURI(URI uri, ConfigViewFactory configViewFactory) throws IllegalArgumentException {
        String name;
        {
            /*
             * Get URI and remove servlet prefix
             */
            String path = null == uri ? null : uri.getPath();
            if (Strings.isEmpty(path)) {
                throw new IllegalArgumentException(String.valueOf(uri));
            }
            path = removePathPrefixFromPath(configViewFactory, path);
            name = removePrefixFromPath("/attachments", path);
            if (path.equals(name)) {
                /*
                 * URI does not contain "attachments" sub-path
                 */
                throw new IllegalArgumentException(String.valueOf(uri));
            }
        }
        /*
         * Extract encoded metadata part from path
         */
        int begin = name.startsWith("/") ? 1 : 0;
        int end = name.indexOf('/', begin);
        name = name.substring(begin, -1 == end ? name.length() : end);

        if (Strings.isEmpty(name)) {
            throw new IllegalArgumentException(String.valueOf(uri));
        }
        return AttachmentUtils.decodeName(name);
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
        if ("ATT-0001".equals(e.getErrorCode())) {
            return new PreconditionException(DAV_NS.getURI(), "quota-not-exceeded", url, SC_INSUFFICIENT_STORAGE);
        }
        if (Category.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            return WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_FORBIDDEN);
        }
        if ("ATT-0405".equals(e.getErrorCode())) {
            return WebdavProtocolException.generalError(e, url, HttpServletResponse.SC_NOT_FOUND);
        }
        return DAVProtocol.protocolException(url, e);
    }

    /**
     * Copies an existing attachment to another target object.
     *
     * @param attachments The attachment service instance to use for the operation
     * @param collection The parent folder collection
     * @param originalMetadata The metadata of the attachment to copy
     * @param targetObjectID The target groupware object for adding the attachment
     * @return The copied attachment metadata
     * @throws OXException In case attachment can't be copied
     */
    public static AttachmentMetadata copyAttachment(AttachmentBase attachments, FolderCollection<?> collection, AttachmentMetadata originalMetadata, int targetObjectID) throws OXException {
        DAVFactory factory = collection.getFactory();
        AttachmentMetadata metadata = newAttachmentMetadata(originalMetadata);
        metadata.setId(0);
        metadata.setAttachedId(targetObjectID);
        InputStream inputStream = null;
        try {
            inputStream = attachments.getAttachedFile(factory.getSession(), originalMetadata.getFolderId(), originalMetadata.getAttachedId(), originalMetadata.getModuleId(), originalMetadata.getId(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            attachments.attachToObject(metadata, inputStream, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            return metadata;
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Adds a new attachment to a groupware object.
     *
     * @param attachments The attachment service instance to use for the operation
     * @param collection The parent folder collection
     * @param inputStream The attachment data to store
     * @param folderID The folder identifier
     * @param objectID The attachment identifier
     * @param contentType The content type of the attachment
     * @param fileName The filename of the attachment
     * @param size The indicated size in bytes of the attachment
     * @return The added attachment's metadata
     * @throws OXException If max upload size is surpassed or adding to attachments fails
     */
    public static AttachmentMetadata addAttachment(AttachmentBase attachments, FolderCollection<?> collection, InputStream inputStream, int folderID, int objectID, String contentType, String fileName, long size) throws OXException {
        long maxSize = AttachmentConfig.getMaxUploadSize();
        if (0 < maxSize) {
            if (maxSize < size) {
                throw new PreconditionException(CAL_NS.getURI(), "max-attachment-size", collection.getUrl(), SC_INSUFFICIENT_STORAGE);
            }
            inputStream = new CountingInputStream(inputStream, maxSize);
        }
        DAVFactory factory = collection.getFactory();
        AttachmentMetadata metadata = newAttachmentMetadata();
        metadata.setAttachedId(objectID);
        metadata.setFileMIMEType(contentType);
        metadata.setFilename(fileName);
        metadata.setModuleId(getModuleId(collection.getFolder().getContentType()));
        metadata.setFilesize(size);
        metadata.setFolderId(folderID);
        attachments.attachToObject(metadata, inputStream, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
        return metadata;
    }

}
