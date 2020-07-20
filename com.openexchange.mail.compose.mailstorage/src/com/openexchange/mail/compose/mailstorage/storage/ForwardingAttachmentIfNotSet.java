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

package com.openexchange.mail.compose.mailstorage.storage;

import java.io.Closeable;
import java.io.InputStream;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorageReference;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.DataProvider;
import com.openexchange.mail.compose.SharedAttachmentReference;


/**
 * {@link ForwardingAttachmentIfNotSet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ForwardingAttachmentIfNotSet implements Attachment {

    /**
     * Gets the <code>ForwardingAttachmentIfNotSet</code> instance for given attachment
     *
     * @param attachment The attachment
     * @return The <code>ForwardingAttachmentIfNotSet</code> instance
     */
    public static ForwardingAttachmentIfNotSet valueFor(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return attachment instanceof ForwardingAttachmentIfNotSet ? (ForwardingAttachmentIfNotSet) attachment : new ForwardingAttachmentIfNotSet(attachment);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Attachment delegate;

    private DataProvider dataProvider;
    private UUID id;
    private UUID compositionSpaceId;
    private AttachmentStorageReference storageReference;
    private String name;
    private Long size;
    private String mimeType;
    private ContentId contentId;
    private ContentDisposition disposition;
    private AttachmentOrigin origin;
    private SharedAttachmentReference sharedAttachmentReference;

    private boolean b_dataProvider;
    private boolean b_id;
    private boolean b_compositionSpaceId;
    private boolean b_storageReference;
    private boolean b_name;
    private boolean b_size;
    private boolean b_mimeType;
    private boolean b_contentId;
    private boolean b_disposition;
    private boolean b_origin;

    /**
     * Initializes a new {@link ForwardingAttachmentIfNotSet}.
     *
     * @param delegate The attachment to delegate to if not set
     */
    private ForwardingAttachmentIfNotSet(Attachment delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public UUID getId() {
        return b_id ? id : delegate.getId();
    }

    /**
     * Sets the attachment identifier.
     *
     * @param id The attachment identifier
     */
    public void setId(UUID id) {
        this.id = id;
        b_id = true;
    }

    @Override
    public UUID getCompositionSpaceId() {
        return b_compositionSpaceId ? compositionSpaceId : delegate.getCompositionSpaceId();
    }

    /**
     * Sets the composition space identifier.
     *
     * @param compositionSpaceId The composition space identifier
     */
    public void setCompositionSpaceId(UUID compositionSpaceId) {
        this.compositionSpaceId = compositionSpaceId;
        b_compositionSpaceId = true;
    }

    @Override
    public AttachmentStorageReference getStorageReference() {
        return b_storageReference ? storageReference : delegate.getStorageReference();
    }

    /**
     * Sets the storage reference.
     *
     * @param storageReference The storage reference
     */
    public void setStorageReference(AttachmentStorageReference storageReference) {
        this.storageReference = storageReference;
        b_storageReference = true;
    }

    @Override
    public void close() {
        if (dataProvider instanceof Closeable) {
            try {
                ((Closeable) dataProvider).close();
            } catch (Exception e) {
                // Ignore
            }
        }
        delegate.close();
    }

    @Override
    public InputStream getData() throws OXException {
        return b_dataProvider ? dataProvider.getData() : delegate.getData();
    }

    /**
     * Sets the data provider.
     *
     * @param dataProvider The data provider
     */
    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        b_dataProvider = true;
    }

    @Override
    public String getName() {
        return b_name ? name : delegate.getName();
    }

    /**
     * Sets the name.
     *
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
        b_name = true;
    }

    @Override
    public long getSize() {
        return b_size ? size.longValue() : delegate.getSize();
    }

    /**
     * Sets the size in bytes.
     *
     * @param size The size
     */
    public void setSize(long size) {
        this.size = Long.valueOf(size);
        b_size = true;
    }

    @Override
    public String getMimeType() {
        return b_mimeType ? mimeType : delegate.getMimeType();
    }

    /**
     * Sets the MIME type.
     *
     * @param mimeType The MIME type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        b_mimeType = true;
    }

    @Override
    public String getContentId() {
        return b_contentId ? (contentId == null ? null : contentId.getContentId()) : delegate.getContentId();
    }

    @Override
    public ContentId getContentIdAsObject() {
        return b_contentId ? contentId : delegate.getContentIdAsObject();
    }

    /**
     * Sets the content identifier.
     *
     * @param contentId The content identifier
     */
    public void setContentId(String contentId) {
        this.contentId = ContentId.valueOf(contentId);
        b_contentId = true;
    }

    /**
     * Sets the content identifier.
     *
     * @param contentId The content identifier
     */
    public void setContentIdAsObject(ContentId contentId) {
        this.contentId = contentId;
        b_contentId = true;
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return b_disposition ? disposition : delegate.getContentDisposition();
    }

    /**
     * Sets the disposition.
     *
     * @param disposition The disposition
     */
    public void setDisposition(ContentDisposition disposition) {
        this.disposition = disposition;
        b_disposition = true;
    }

    @Override
    public AttachmentOrigin getOrigin() {
        return b_origin ? origin : delegate.getOrigin();
    }

    /**
     * Sets the origin
     *
     * @param origin The origin
     */
    public void setOrigin(AttachmentOrigin origin) {
        this.origin = origin;
        b_origin = true;
    }

    /**
     * Gets the shared attachment reference.
     *
     * @return The shared attachment reference or <code>null</code>
     */
    public SharedAttachmentReference getSharedAttachmentReference() {
        return sharedAttachmentReference;
    }

    /**
     * Sets the shared attachment reference.
     *
     * @param sharedAttachmentReference The shared attachment reference or <code>null</code>
     */
    public void setSharedAttachmentReference(SharedAttachmentReference sharedAttachmentReference) {
        this.sharedAttachmentReference = sharedAttachmentReference;
    }

}
