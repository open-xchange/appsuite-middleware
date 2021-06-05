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

package com.openexchange.mail.mime.dataobjects;

import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.mail.Part;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeTypes;

/**
 * {@link NestedMessageMailPart} - Represents a mail part holding a nested message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NestedMessageMailPart extends MailPart {

    private static final long serialVersionUID = 7379170932302170388L;

    private final MailMessage mailMessage;

    /**
     * Initializes a new {@link NestedMessageMailPart}.
     *
     * @param mailMessage The nested message
     * @throws OXException If initialization fails
     */
    public NestedMessageMailPart(MailMessage mailMessage) throws OXException {
        super();
        this.mailMessage = mailMessage;
        setContentType(MimeTypes.MIME_MESSAGE_RFC822);
        setContentDisposition(Part.INLINE);
    }

    @Override
    public Object getContent() throws OXException {
        return mailMessage;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        throw new UnsupportedOperationException("NestedMessageMailPart.getDataHandler()");
    }

    @Override
    public InputStream getInputStream() throws OXException {
        throw new UnsupportedOperationException("NestedMessageMailPart.getInputStream()");
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return null;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public void writeTo(OutputStream out) throws OXException {
        throw new UnsupportedOperationException("NestedMessageMailPart.writeTo()");
    }

    @Override
    public void prepareForCaching() {
        mailMessage.prepareForCaching();
    }

    @Override
    public void loadContent() throws OXException {
        mailMessage.loadContent();
    }

}
