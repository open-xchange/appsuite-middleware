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

package com.openexchange.mail.compose.impl.attachment.security;

import java.io.InputStream;
import java.security.Key;
import java.util.UUID;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.AttachmentOrigin;
import com.openexchange.mail.compose.AttachmentStorageReference;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.impl.CryptoUtility;

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
    public InputStream getData() throws OXException {
        return CryptoUtility.decryptingStreamFor(attachment.getData(), key, cryptoService);
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
    public ContentDisposition getContentDisposition() {
        return attachment.getContentDisposition();
    }

    @Override
    public AttachmentOrigin getOrigin() {
        return attachment.getOrigin();
    }

}
