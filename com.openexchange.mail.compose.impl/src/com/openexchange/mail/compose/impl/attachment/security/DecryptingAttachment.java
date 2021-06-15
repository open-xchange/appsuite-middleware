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

package com.openexchange.mail.compose.impl.attachment.security;

import java.io.InputStream;
import java.security.Key;
import java.util.UUID;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorageReference;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.ContentId;
import com.openexchange.mail.compose.CryptoUtility;

/**
 * {@link DecryptingAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class DecryptingAttachment implements Attachment { // Do not implement RandomAccessAttachment. Trying to decrypt chunks might fail.

    private final Attachment attachment;
    private final Key key;
    private final String decryptedName;
    private final CryptoService cryptoService;

    /**
     * Initializes a new {@link DecryptingAttachment}.
     *
     * @throws OXException If attachment's file name cannot be decrypted
     */
    public DecryptingAttachment(Attachment attachment, Key key, CryptoService cryptoService) throws OXException {
        super();
        this.cryptoService = cryptoService;
        try {
            this.attachment = attachment;
            this.key = key;
            this.decryptedName = CryptoUtility.decrypt(attachment.getName(), key, cryptoService);
        } catch (OXException e) {
            if (CryptoErrorMessage.BadPassword.equals(e)) {
                throw CompositionSpaceErrorCode.MISSING_KEY.create(e, UUIDs.getUnformattedString(attachment.getCompositionSpaceId()));
            }
            throw e;
        }
    }

    @Override
    public UUID getId() {
        return attachment.getId();
    }

    @Override
    public UUID getCompositionSpaceId() {
        return attachment.getCompositionSpaceId();
    }

    @Override
    public AttachmentStorageReference getStorageReference() {
        return attachment.getStorageReference();
    }

    @Override
    public void close() {
        attachment.close();
    }

    @Override
    public InputStream getData() throws OXException {
        InputStream data = attachment.getData();
        try {
            InputStream retval = CryptoUtility.decryptingStreamFor(data, key, cryptoService);
            data = null; // Null'ify to prevent premature closing
            return retval;
        } finally {
            Streams.close(data);
        }
    }

    @Override
    public String getName() {
        return decryptedName;
    }

    @Override
    public long getSize() {
        return attachment.getSize();
    }

    @Override
    public String getMimeType() {
        return attachment.getMimeType();
    }

    @Override
    public String getContentId() {
        return attachment.getContentId();
    }

    @Override
    public ContentId getContentIdAsObject() {
        return attachment.getContentIdAsObject();
    }

    @Override
    public ContentDisposition getContentDisposition() {
        return attachment.getContentDisposition();
    }

    @Override
    public AttachmentOrigin getOrigin() {
        return attachment.getOrigin();
    }

}
