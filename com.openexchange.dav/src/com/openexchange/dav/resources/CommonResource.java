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

package com.openexchange.dav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
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
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.attachments.AttachmentUtils;
import com.openexchange.dav.internal.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.stream.CountingInputStream;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link CommonResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class CommonResource<T extends CommonObject> extends DAVResource {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonResource.class);

    protected final CommonFolderCollection<T> parent;
    protected T object;
    protected boolean exists;
    protected int parentFolderID;

    /**
     * Initializes a new {@link CommonResource}.
     *
     * @param parent The parent folder collection
     * @param object An existing groupware object represented by this resource, or <code>null</code> if a placeholder resource should be created
     * @param url The resource url
     */
    protected CommonResource(CommonFolderCollection<T> parent, T object, WebdavPath url) throws OXException {
        super(parent.getFactory(), url);
        this.parent = parent;
        this.object = object;
        this.exists = null != object;
        this.parentFolderID = Tools.parse(parent.getFolder().getID());
    }

    protected abstract String getFileExtension();

    protected abstract void deserialize(InputStream inputStream) throws OXException, IOException;

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return null != object ? object.getCreationDate() : null;
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        if (false == exists() || null == object || null == object.getLastModified()) {
            return "";
        }
        return "http://www.open-xchange.com/etags/" + object.getObjectID() + '-' + object.getLastModified().getTime();
    }

    @Override
    public boolean exists() throws WebdavProtocolException {
        return exists;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null != object ? object.getLastModified() : null;
    }

    @Override
    public boolean hasBody() throws WebdavProtocolException {
        return true;
    }

    @Override
    public String getDisplayName() throws WebdavProtocolException {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) throws WebdavProtocolException {
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
     * Adds an attachment to the underlying groupware object.
     *
     * @param inputStream The binary attachment data
     * @param contentType The attachment's content type
     * @param fileName The target filename
     * @param size The attachment size
     * @param recurrenceIDs The targeted recurrence ids, or <code>null</code> if not applicable or to apply to the master instance only
     * @return Metadata of the managed attachments
     */
    public AttachmentMetadata[] addAttachment(InputStream inputStream, String contentType, String fileName, long size, String[] recurrenceIDs) throws OXException {
        List<T> targetObjects = getTargetedObjects(recurrenceIDs);
        if (null == targetObjects || 0 == targetObjects.size()) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        AttachmentMetadata[] attachmentMetadata = new AttachmentMetadata[targetObjects.size()];
        AttachmentBase attachments = Attachments.getInstance();
        try {
            attachments.startTransaction();
            /*
             * create first attachment
             */
            AttachmentMetadata metadata = addAttachment(attachments, inputStream, targetObjects.get(0), contentType, fileName, size);
            attachmentMetadata[0] = metadata;
            /*
             * copy attachment to further targets
             */
            for (int i = 1; i < targetObjects.size(); i++) {
                AttachmentMetadata furtherMetadata = copyAttachment(attachments, metadata, targetObjects.get(i));
                attachmentMetadata[i] = furtherMetadata;
            }
            attachments.commit();
        } catch (OXException e) {
            attachments.rollback();
            throw e;
        } finally {
            attachments.finish();
        }
        return attachmentMetadata;
    }

    /**
     * Replaces an existing attachment with an updated one.
     *
     * @param attachmentId The identifier of the attachment to update
     * @param inputStream The binary attachment data
     * @param contentType The attachment's content type
     * @param fileName The target filename
     * @param size The attachment size
     * @return Metadata of the (new) updated attachment
     */
    public AttachmentMetadata updateAttachment(int attachmentId, InputStream inputStream, String contentType, String fileName, long size) throws OXException {
        AttachmentBase attachments = Attachments.getInstance();
        try {
            attachments.startTransaction();
            /*
             * store new attachment & remove previous attachment
             */
            AttachmentMetadata metadata = addAttachment(attachments, inputStream, object, contentType, fileName, size);
            attachments.detachFromObject(object.getParentFolderID(), object.getObjectID(), AttachmentUtils.getModuleId(parent.getFolder().getContentType()),
                new int[] { attachmentId }, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            attachments.commit();
            return metadata;
        } catch (OXException e) {
            attachments.rollback();
            throw e;
        } finally {
            attachments.finish();
        }
    }

    /**
     * Removes an attachment from the underlying groupware object.
     *
     * @param attachmentId The identifier of the attachment to remove
     * @param recurrenceIDs The targeted recurrence ids, or <code>null</code> if not applicable or to apply to the master instance only
     */
    public void removeAttachment(int attachmentId, String[] recurrenceIDs) throws OXException {
        List<T> targetObjects = getTargetedObjects(recurrenceIDs);
        if (null == targetObjects || 0 == targetObjects.size()) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        AttachmentBase attachments = Attachments.getInstance();
        try {
            attachments.startTransaction();
            /*
             * detach attachment
             */
            attachments.detachFromObject(object.getParentFolderID(), object.getObjectID(), AttachmentUtils.getModuleId(parent.getFolder().getContentType()),
                new int[] { attachmentId }, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            attachments.commit();
        } catch (OXException e) {
            attachments.rollback();
            throw e;
        } finally {
            attachments.finish();
        }
    }

    /**
     * Extracts a list of targeted objects based on the recurrence IDs as supplied by the client in the attachment action. <p/>
     * This default implementation always sticks to the underlying groupware object of the resource.
     *
     * @param recurrenceIDs The recurrence IDs as supplied by the client
     * @return The targeted objects
     */
    protected List<T> getTargetedObjects(String[] recurrenceIDs) throws OXException {
        return Collections.singletonList(object);
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
            List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
            try {
                searchIterator = attachments.results();
                while (searchIterator.hasNext()) {
                    AttachmentMetadata metadata = AttachmentUtils.newAttachmentMetadata(searchIterator.next());
                    metadata.setFolderId(object.getParentFolderID());
                    URI uri = AttachmentUtils.buildURI(hostData, metadata);
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
        List<Entry<URI, AttachmentMetadata>> extractedManagedAttachments = extractManagedAttachments(linkedAttachments);
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
                    AttachmentMetadata referencedAttachment = AttachmentUtils.decodeURI(managedAttachment.getKey());
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
                    LOG.debug("Skipping copy of already existing master attachment {} for exception of object {}.", attachment.getId(), object.getObjectID());
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

    protected String extractResourceName() {
        return Tools.extractResourceName(url, getFileExtension());
    }

    /**
     * Copies an existing attachment to another target object.
     *
     * @param attachments The attachment service instance to use for the operation
     * @param originalMetadata The metadata of the attachment to copy
     * @param targetObject The target groupware object for adding the attachment
     * @return The copied attachment metadata; the last modification timestamp of the target object is updated accordingly
     */
    private AttachmentMetadata copyAttachment(AttachmentBase attachments, AttachmentMetadata originalMetadata, T targetObject) throws OXException {
        AttachmentMetadata metadata = AttachmentUtils.newAttachmentMetadata(originalMetadata);
        metadata.setId(0);
        metadata.setAttachedId(targetObject.getObjectID());
        metadata.setFolderId(targetObject.getParentFolderID());
        InputStream inputStream = null;
        try {
            inputStream = attachments.getAttachedFile(factory.getSession(), originalMetadata.getFolderId(), originalMetadata.getAttachedId(),
                originalMetadata.getModuleId(), originalMetadata.getId(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            long newTimestamp = attachments.attachToObject(metadata, inputStream, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            targetObject.setLastModified(new Date(newTimestamp));
            return metadata;
        } finally {
            Streams.close(inputStream);
        }
    }

    /**
     * Adds a new attachment to a groupware object.
     *
     * @param attachments The attachment service instance to use for the operation
     * @param inputStream The attachment data to store
     * @param targetObject The target groupware object for adding the attachment
     * @param contentType The content type of the attachment
     * @param fileName The filename of the attachment
     * @param size The indicated size in bytes of the attachment
     * @return The added attachment's metadata; the last modification timestamp of the target object is updated accordingly
     */
    private AttachmentMetadata addAttachment(AttachmentBase attachments, InputStream inputStream, T targetObject, String contentType, String fileName, long size) throws OXException {
        long maxSize = AttachmentConfig.getMaxUploadSize();
        if (0 < maxSize) {
            if (maxSize < size) {
                throw new PreconditionException(DAVProtocol.CAL_NS.getURI(), "max-attachment-size", getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
            inputStream = new CountingInputStream(inputStream, maxSize);
        }
        AttachmentMetadata metadata = AttachmentUtils.newAttachmentMetadata();
        metadata.setAttachedId(targetObject.getObjectID());
        metadata.setFileMIMEType(contentType);
        metadata.setFilename(fileName);
        metadata.setModuleId(AttachmentUtils.getModuleId(parent.getFolder().getContentType()));
        metadata.setFilesize(size);
        metadata.setFolderId(targetObject.getParentFolderID());
        long newTimestamp = attachments.attachToObject(metadata, inputStream, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
        targetObject.setLastModified(new Date(newTimestamp));
        return metadata;
    }

    /**
     * Extracts any targeted managed attachments from the supplied list of linked attachment URLs.
     *
     * @param linkedAttachments The attachment URLs to extract the managed attachments from
     * @return The managed attachments, or an empty list if none could be extracted
     */
    private static List<Entry<URI, AttachmentMetadata>> extractManagedAttachments(List<String> linkedAttachments) {
        List<Entry<URI, AttachmentMetadata>> managedAttachments = new ArrayList<Entry<URI, AttachmentMetadata>>();
        if (null != linkedAttachments && 0 < linkedAttachments.size()) {
            for (String linkedAttachment : linkedAttachments) {
                try {
                    URI uri = new URI(linkedAttachment);
                    AttachmentMetadata metadata = AttachmentUtils.decodeURI(uri);
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
