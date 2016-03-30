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
    public NestedMessageMailPart(final MailMessage mailMessage) throws OXException {
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
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return null;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public void writeTo(final OutputStream out) throws OXException {
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
