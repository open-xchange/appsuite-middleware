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

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.AttachmentUtils;
import com.openexchange.dav.internal.Tools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link DAVObjectResource}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DAVObjectResource<T> extends DAVResource {

    protected static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DAVObjectResource.class);

    protected final FolderCollection<T> parent;

    protected T object;
    protected boolean exists;

    /**
     * Initializes a new {@link DAVObjectResource}.
     *
     * @param parent The parent folder collection
     * @param object An existing groupware object represented by this resource, or <code>null</code> if a placeholder resource should be created
     * @param url The resource url
     */
    protected DAVObjectResource(FolderCollection<T> parent, T object, WebdavPath url) throws OXException {
        super(parent.getFactory(), url);
        this.parent = parent;
        this.object = object;
        this.exists = null != object;
    }

    /**
     * Gets the filename extension typically used for resources.
     *
     * @return The file extension, e.g. <code>.ics</code>
     */
    protected abstract String getFileExtension();

    /**
     * Gets the creation date of an object.
     *
     * @param object The object to get the creation date for
     * @return The creation date
     */
    protected abstract Date getCreationDate(T object);

    /**
     * Gets the last modification date of an object.
     *
     * @param object The object to get the last modification date for
     * @return The last modification date
     */
    protected abstract Date getLastModified(T object);

    /**
     * Gets the numerical identifier of an object, as used by the attachment service.
     *
     * @param object The object to get the identifier for
     * @return The identifier
     */
    protected abstract String getId(T object);

    /**
     * Gets the numerical folder identifier of a collection, as used by the attachment service.
     *
     * @param collection The folder collection to get the identifier for
     * @return The identifier
     */
    protected abstract int getId(FolderCollection<T> collection) throws OXException;

    @Override
    public Date getCreationDate() throws WebdavProtocolException {
        return null != object ? getCreationDate(object) : null;
    }

    @Override
    public String getETag() throws WebdavProtocolException {
        if (false == exists() || null == object || null == getLastModified(object)) {
            return "";
        }
        return "http://www.open-xchange.com/etags/" + getId(object) + '-' + getLastModified(object).getTime();
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        return null != object ? getLastModified(object) : null;
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
        //
    }

    /**
     * Adds an attachment to the underlying groupware object.
     *
     * @param inputStream The binary attachment data
     * @param contentType The attachment's content type
     * @param fileName The target filename
     * @param size The attachment size
     * @param recurrenceIDs The targeted recurrence ids, or <code>null</code> if not applicable or to apply to the master instance only
     * @return The managed identifiers of the added attachments
     */
    public int[] addAttachment(InputStream inputStream, String contentType, String fileName, long size, String[] recurrenceIDs) throws OXException {
        List<T> targetObjects = getTargetedObjects(recurrenceIDs);
        if (null == targetObjects || 0 == targetObjects.size()) {
            throw WebdavProtocolException.generalError(getUrl(), HttpServletResponse.SC_NOT_FOUND);
        }
        int[] attachmentIds = new int[targetObjects.size()];
        AttachmentBase attachments = Attachments.getInstance();
        try {
            attachments.startTransaction();
            /*
             * create first attachment
             */
            AttachmentMetadata metadata = addAttachment(attachments, inputStream, targetObjects.get(0), contentType, fileName, size);
            attachmentIds[0] = metadata.getId();
            /*
             * copy attachment to further targets
             */
            for (int i = 1; i < targetObjects.size(); i++) {
                AttachmentMetadata furtherMetadata = copyAttachment(attachments, metadata, targetObjects.get(i));
                attachmentIds[i] = furtherMetadata.getId();
            }
            attachments.commit();
        } catch (OXException e) {
            attachments.rollback();
            throw e;
        } finally {
            attachments.finish();
        }
        return attachmentIds;
    }

    /**
     * Replaces an existing attachment with an updated one.
     *
     * @param attachmentId The identifier of the attachment to update
     * @param inputStream The binary attachment data
     * @param contentType The attachment's content type
     * @param fileName The target filename
     * @param size The attachment size
     * @return The managed identifier of the updated attachment
     */
    public int updateAttachment(int attachmentId, InputStream inputStream, String contentType, String fileName, long size) throws OXException {
        AttachmentBase attachments = Attachments.getInstance();
        try {
            attachments.startTransaction();
            /*
             * store new attachment & remove previous attachment
             */
            AttachmentMetadata metadata = addAttachment(attachments, inputStream, object, contentType, fileName, size);
            attachments.detachFromObject(getId(parent), parseId(getId(object)), AttachmentUtils.getModuleId(parent.getFolder().getContentType()),
                new int[] { attachmentId }, factory.getSession(), factory.getContext(), factory.getUser(), factory.getUserConfiguration());
            attachments.commit();
            return metadata.getId();
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
            attachments.detachFromObject(getId(parent), parseId(getId(object)), AttachmentUtils.getModuleId(parent.getFolder().getContentType()),
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
     * Extracts the resource filename based on the WebDAV path and typical file extension.
     *
     * @return The extracted resource name
     */
    protected String extractResourceName() {
        return Tools.extractResourceName(url, getFileExtension());
    }

    /**
     * Copies an existing attachment to another target object.
     *
     * @param attachments The attachment service instance to use for the operation
     * @param originalMetadata The metadata of the attachment to copy
     * @param targetObject The target groupware object for adding the attachment
     * @return The copied attachment metadata
     */
    protected AttachmentMetadata copyAttachment(AttachmentBase attachments, AttachmentMetadata originalMetadata, T targetObject) throws OXException {
        return AttachmentUtils.copyAttachment(attachments, parent, originalMetadata, parseId(getId(targetObject)));
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
     * @return The added attachment's metadata
     */
    protected AttachmentMetadata addAttachment(AttachmentBase attachments, InputStream inputStream, T targetObject, String contentType, String fileName, long size) throws OXException {
        return AttachmentUtils.addAttachment(attachments, parent, inputStream, getId(parent), parseId(getId(targetObject)), contentType, fileName, size);
    }
    
    private int parseId(String id) {
        return id == null ? null : Integer.parseInt(id);
    }

}
