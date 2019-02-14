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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import java.io.InputStream;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.compose.AttachmentStorageReference;

/**
 * {@link DefaultAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class DefaultAttachment implements RandomAccessAttachment {

    /**
     * Creates a new instance of <code>DefaultAttachment</code> carrying given attachment and composition space identifier.
     *
     * @param attachmentId The attachment identifier
     * @param compositionSpaceId The composition space identifier
     * @return The <code>DefaultAttachment</code> instance
     */
    public static DefaultAttachment createWithId(UUID attachmentId, UUID compositionSpaceId) {
        return new DefaultAttachment(null, attachmentId, compositionSpaceId, null, null, -1, null, null, null, null);
    }

    /**
     * Creates a new builder for an instance of <code>DefaultAttachment</code>
     *
     * @param id The attachment identifier
     * @return The new builder
     */
    public static Builder builder(UUID id) {
        return new Builder(id);
    }

    /** The builder for an instance of <code>DefaultAttachment</code> */
    public static class Builder {

        private DataProvider dataProvider;
        private final UUID id;
        private UUID compositionSpaceId;
        private AttachmentStorageReference storageReference;
        private String name;
        private long size;
        private String mimeType;
        private String contentId;
        private ContentDisposition disposition;
        private AttachmentOrigin origin;

        /**
         * Initializes a new {@link DefaultAttachment.Builder}.
         */
        Builder(UUID id) {
            super();
            this.id = id;
            size = -1;
        }

        public Builder withDataProvider(DataProvider dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }

        public Builder withCompositionSpaceId(UUID compositionSpaceId) {
            this.compositionSpaceId = compositionSpaceId;
            return this;
        }

        public Builder withStorageReference(AttachmentStorageReference storageReference) {
            this.storageReference = storageReference;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSize(long size) {
            this.size = size;
            return this;
        }

        public Builder withMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder withContentId(String contentId) {
            this.contentId = contentId;
            return this;
        }

        public Builder withDisposition(ContentDisposition disposition) {
            this.disposition = disposition;
            return this;
        }

        public Builder withContentDisposition(ContentDisposition disposition) {
            this.disposition = disposition;
            return this;
        }

        public Builder withOrigin(AttachmentOrigin origin) {
            this.origin = origin;
            return this;
        }

        public DefaultAttachment build() {
            return new DefaultAttachment(dataProvider, id, compositionSpaceId, storageReference, name, size, mimeType, contentId, disposition, origin);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private final DataProvider dataProvider;
    private final UUID id;
    private final UUID compositionSpaceId;
    private final AttachmentStorageReference storageReference;
    private final String name;
    private final long size;
    private final String mimeType;
    private final String contentId;
    private final ContentDisposition disposition;
    private final AttachmentOrigin origin;

    DefaultAttachment(DataProvider dataProvider, UUID id, UUID compositionSpaceId, AttachmentStorageReference storageReference, String name, long size, String mimeType, String contentId, ContentDisposition disposition, AttachmentOrigin origin) {
        super();
        this.dataProvider = dataProvider;
        this.id = id;
        this.compositionSpaceId = compositionSpaceId;
        this.storageReference = storageReference;
        this.name = name;
        this.size = size;
        this.mimeType = mimeType;
        this.contentId = contentId;
        this.disposition = disposition;
        this.origin = origin;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getCompositionSpaceId() {
        return compositionSpaceId;
    }

    @Override
    public AttachmentStorageReference getStorageReference() {
        return storageReference;
    }

    @Override
    public boolean supportsRandomAccess() {
        return dataProvider instanceof SeekingDataProvider;
    }

    @Override
    public InputStream getData() throws OXException {
        return dataProvider.getData();
    }

    @Override
    public InputStream getData(long offset, long length) throws OXException {
        try {
            SeekingDataProvider seekingDataProvider = (SeekingDataProvider) dataProvider;
            return seekingDataProvider.getData(offset, length);
        } catch (ClassCastException e) {
            throw MailExceptionCode.UNSUPPORTED_OPERATION.create(e, new Object[0]);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getContentId() {
        return contentId;
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return disposition;
    }

    @Override
    public AttachmentOrigin getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("[");
        if (dataProvider != null) {
            builder2.append("dataProvider=").append(dataProvider).append(", ");
        }
        if (id != null) {
            builder2.append("id=").append(id).append(", ");
        }
        if (compositionSpaceId != null) {
            builder2.append("compositionSpaceId=").append(compositionSpaceId).append(", ");
        }
        if (storageReference != null) {
            builder2.append("storageReference=").append(storageReference).append(", ");
        }
        if (name != null) {
            builder2.append("name=").append(name).append(", ");
        }
        builder2.append("size=").append(size).append(", ");
        if (mimeType != null) {
            builder2.append("mimeType=").append(mimeType).append(", ");
        }
        if (contentId != null) {
            builder2.append("contentId=").append(contentId).append(", ");
        }
        if (disposition != null) {
            builder2.append("disposition=").append(disposition).append(", ");
        }
        if (origin != null) {
            builder2.append("origin=").append(origin);
        }
        builder2.append("]");
        return builder2.toString();
    }

}
