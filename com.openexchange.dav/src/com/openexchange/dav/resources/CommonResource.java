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

package com.openexchange.dav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.AttachmentUtils;
import com.openexchange.dav.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link CommonResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CommonResource<T extends CommonObject> extends DAVObjectResource<T> {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonResource.class);

    protected final FolderCollection<T> parent;

    /**
     * Initializes a new {@link CommonResource}.
     *
     * @param parent The parent folder collection
     * @param object An existing groupware object represented by this resource, or <code>null</code> if a placeholder resource should be created
     * @param url The resource url
     */
    protected CommonResource(FolderCollection<T> parent, T object, WebdavPath url) {
        super(parent, object, url);
        this.parent = parent;
    }

    protected abstract void deserialize(InputStream inputStream) throws OXException, IOException;

    @Override
    protected Date getCreationDate(T object) {
        return null != object ? object.getCreationDate() : null;
    }

    @Override
    protected Date getLastModified(T object) {
        return null != object ? object.getLastModified() : null;
    }

    @Override
    protected String getId(T object) {
        return object == null ? null : Integer.toString(object.getObjectID());
    }

    @Override
    protected int getId(FolderCollection<T> collection) throws OXException {
        return null != collection ? Tools.parse(collection.getFolder().getID()) : 0;
    }

    @Override
    public void putBody(InputStream body, boolean guessSize) throws WebdavProtocolException {
        try {
            deserialize(body);
        } catch (IOException e) {
            throw protocolException(getUrl(), e, HttpServletResponse.SC_BAD_REQUEST);
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    /**
     * Applies managed attachment properties to the supplied groupware object by injecting the metadata of all current attachments.
     *
     * @param object The groupware object to apply managed attachment properties for
     */
    protected void applyAttachments(CommonObject object) throws OXException {
        if (0 < object.getNumberOfAttachments()) {
            int moduleId = AttachmentUtils.getModuleId(parent.getFolder().getContentType());
            TimedResult<AttachmentMetadata> attachments = Attachments.getInstance().getAttachments(factory.getSession(),
                object.getParentFolderID(), object.getObjectID(), moduleId, factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            SearchIterator<AttachmentMetadata> searchIterator = null;
            HostData hostData = getHostData();
            ConfigViewFactory configViewFactory = factory.requireService(ConfigViewFactory.class);
            List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
            try {
                searchIterator = attachments.results();
                while (searchIterator.hasNext()) {
                    AttachmentMetadata metadata = AttachmentUtils.newAttachmentMetadata(searchIterator.next());
                    metadata.setFolderId(object.getParentFolderID());
                    URI uri = AttachmentUtils.buildURI(hostData, metadata, configViewFactory);
                    managedAttachments.add(new AbstractMap.SimpleEntry<URI, AttachmentMetadata>(uri, metadata));
                }
            } catch (URISyntaxException e) {
                throw WebdavProtocolException.generalError(e, getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                SearchIterators.close(searchIterator);
            }
            object.setProperty("com.openexchange.data.conversion.ical.attach.managedAttachments", managedAttachments);
        }
    }

    /**
     * Handles incoming attachment operations by evaluating the extended properties
     * <code>com.openexchange.data.conversion.ical.attach.managedAttachments</code>,
     * <code>com.openexchange.data.conversion.ical.attach.linkedAttachments</code> and
     * <code>com.openexchange.data.conversion.ical.attach.binaryAttachments</code>.
     *
     * @param originalObject The original groupware object being updated, or <code>null</code> if there is none
     * @param updatedObject The updated groupware object, possibly holding attachment-related extended properties
     * @return A new last modification date for the groupware object, or <code>null</code> if not applicable
     */
    protected Date handleAttachments(T originalObject, T updatedObject) throws OXException {
        /*
         * check for presence of parsed managed attachments, also considering the URIs of additionally linked attachments
         */
        List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
        List<Entry<URI, AttachmentMetadata>> parsedManagedAttachments = updatedObject.getProperty("com.openexchange.data.conversion.ical.attach.managedAttachments");
        updatedObject.removeProperty("com.openexchange.data.conversion.ical.attach.managedAttachments");
        if (null != parsedManagedAttachments) {
            managedAttachments.addAll(parsedManagedAttachments);
        }
        List<String> linkedAttachments = updatedObject.getProperty("com.openexchange.data.conversion.ical.attach.linkedAttachments");
        updatedObject.removeProperty("com.openexchange.data.conversion.ical.attach.linkedAttachments");
        ConfigViewFactory configViewFactory = factory.getServiceSafe(ConfigViewFactory.class);
        List<Entry<URI, AttachmentMetadata>> extractedManagedAttachments = extractManagedAttachments(linkedAttachments, configViewFactory);
        if (null != extractedManagedAttachments) {
            managedAttachments.addAll(extractedManagedAttachments);
        }
        List<IFileHolder> binaryAttachments = updatedObject.getProperty("com.openexchange.data.conversion.ical.attach.binaryAttachments");
        updatedObject.removeProperty("com.openexchange.data.conversion.ical.attach.binaryAttachments");
        /*
         * get original attachments to apply differences
         */
        List<AttachmentMetadata> originalAttachments;
        if (null != originalObject && 0 < originalObject.getNumberOfAttachments()) {
            TimedResult<AttachmentMetadata> attachments = Attachments.getInstance().getAttachments(factory.getSession(),
                originalObject.getParentFolderID(), originalObject.getObjectID(), AttachmentUtils.getModuleId(parent.getFolder().getContentType()),
                factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            originalAttachments = SearchIterators.asList(attachments.results());
        } else {
            originalAttachments = Collections.emptyList();
        }
        List<AttachmentMetadata> newAttachments = new ArrayList<AttachmentMetadata>();
        List<Integer> deletedAttachments = new ArrayList<Integer>();
        if (null == managedAttachments || 0 == managedAttachments.size()) {
            /*
             * no attachments indicated anymore, remove any previous attachments
             */
            for (AttachmentMetadata originalAttachment : originalAttachments) {
                deletedAttachments.add(Integer.valueOf(originalAttachment.getId()));
            }
        } else {
            /*
             * check for any removed (i.e. no longer referenced) or new managed attachments
             */
            List<AttachmentMetadata> referencedAttachments = new ArrayList<AttachmentMetadata>(managedAttachments.size());
            for (Entry<URI, AttachmentMetadata> managedAttachment : managedAttachments) {
                try {
                    AttachmentMetadata referencedAttachment = AttachmentUtils.decodeURI(managedAttachment.getKey(), configViewFactory);
                    if (originalAttachments.contains(referencedAttachment)) {
                        referencedAttachments.add(referencedAttachment);
                    } else {
                        newAttachments.add(referencedAttachment);
                    }
                } catch (IllegalArgumentException e) {
                    throw protocolException(getUrl(), e, HttpServletResponse.SC_FORBIDDEN);
                }
            }
            for (AttachmentMetadata originalAttachment : originalAttachments) {
                if (false == referencedAttachments.contains(originalAttachment)) {
                    deletedAttachments.add(Integer.valueOf(originalAttachment.getId()));
                }
            }
        }
        if (0 == newAttachments.size() && 0 == deletedAttachments.size() && (null == binaryAttachments || 0 == binaryAttachments.size())) {
            return null; // nothing to do
        }
        AttachmentBase attachments = Attachments.getInstance();
        Date timestamp = null;
        try {
            attachments.startTransaction();
            /*
             * copy new managed attachments (skipping implicit copies of managed attachments from the recurrence master)
             */
            for (AttachmentMetadata attachment : newAttachments) {
                AttachmentMetadata originalMetadata = attachments.getAttachment(
                    factory.getSession(), attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), attachment.getId(),
                    factory.getContext(), factory.getUser(), factory.getUserConfiguration());
                if (null != object && object.getObjectID() == originalMetadata.getAttachedId()) {
                    LOG.debug("Skipping copy of already existing master attachment {} for exception of object {}.", I(attachment.getId()), I(object.getObjectID()));
                } else {
                    copyAttachment(attachments, originalMetadata, updatedObject);
                    timestamp = updatedObject.getLastModified();
                }
            }
            /*
             * delete no longer referenced attachments
             */
            if (0 < deletedAttachments.size()) {
                int moduleId = AttachmentUtils.getModuleId(parent.getFolder().getContentType());
                long newTimestamp = attachments.detachFromObject(originalObject.getParentFolderID(), originalObject.getObjectID(), moduleId, Autoboxing.I2i(deletedAttachments),
                    factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
                timestamp = new Date(newTimestamp);
            }
            /*
             * consider any binary attachments as new
             */
            if (null != binaryAttachments && 0 < binaryAttachments.size()) {
                for (IFileHolder fileHolder : binaryAttachments) {
                    InputStream inputStream = null;
                    try {
                        inputStream = fileHolder.getStream();
                        addAttachment(attachments, inputStream, updatedObject, fileHolder.getContentType(), fileHolder.getName(), fileHolder.getLength());
                        timestamp = updatedObject.getLastModified();
                    } finally {
                        Streams.close(inputStream, fileHolder);
                    }
                }
            }
            attachments.commit();
        } catch (OXException e) {
            attachments.rollback();
            throw e;
        } finally {
            attachments.finish();
        }
        return timestamp;
    }

    /**
     * Extracts any targeted managed attachments from the supplied list of linked attachment URLs.
     *
     * @param linkedAttachments The attachment URLs to extract the managed attachments from
     * @param configViewFactory The configuration view
     * @return The managed attachments, or an empty list if none could be extracted
     */
    private static List<Entry<URI, AttachmentMetadata>> extractManagedAttachments(List<String> linkedAttachments, ConfigViewFactory configViewFactory) {
        List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
        if (null != linkedAttachments && 0 < linkedAttachments.size()) {
            for (String linkedAttachment : linkedAttachments) {
                try {
                    URI uri = new URI(linkedAttachment);
                    AttachmentMetadata metadata = AttachmentUtils.decodeURI(uri, configViewFactory);
                    managedAttachments.add(new AbstractMap.SimpleEntry<URI, AttachmentMetadata>(uri, metadata));
                } catch (IllegalArgumentException | URISyntaxException e) {
                    LOG.debug("Skipping invalid managed attachment: {}", linkedAttachment, e);
                    continue;
                }
            }
        }
        return managedAttachments;
    }

}
