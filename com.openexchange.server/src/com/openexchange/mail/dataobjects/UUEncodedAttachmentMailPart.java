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

package com.openexchange.mail.dataobjects;

import java.io.InputStream;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link UUEncodedAttachmentMailPart} - Subclass of {@link MailPart} designed for designed for uuencoded mail parts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UUEncodedAttachmentMailPart extends MailPart {

    private static final long serialVersionUID = 8980473176008331679L;

    private final transient UUEncodedPart uuencPart;

    /**
     * Initializes a new {@link UUEncodedAttachmentMailPart}
     *
     * @param uuencPart The uuencoded part
     */
    public UUEncodedAttachmentMailPart(UUEncodedPart uuencPart) {
        super();
        this.uuencPart = uuencPart;
    }

    @Override
    public Object getContent() throws OXException {
        return null;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        final ContentType contentType;
        if (!containsContentType()) {
            contentType = getContentType();
        } else {
            String ct = MimeType2ExtMap.getContentType(uuencPart.getFileName());
            if ((ct == null) || (ct.length() == 0)) {
                ct = MimeTypes.MIME_APPL_OCTET;
            }
            contentType = new ContentType(ct);
        }
        return uuencPart.getDataHandler(contentType.toString());
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return null;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return uuencPart.getInputStream();
    }

    @Override
    public void loadContent() {
    }

    @Override
    public void prepareForCaching() {
    }

}
