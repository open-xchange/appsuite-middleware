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
import javax.mail.internet.MimeMessage;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeCleanUp;

/**
 * {@link MimeMailMessage} - A subclass of {@link MailMessage} to support MIME messages (as per RFC822).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeMailMessage extends MailMessage implements MimeRawSource, MimeCleanUp {

    private static final long serialVersionUID = 4593386724062676753L;

    private MimeMailPart mailPart;

    private String id;

    private int unreadMessages;

    private char separator;

    private int seqnum;

    /**
     * Constructor - Constructs an empty mail message
     */
    public MimeMailMessage() {
        super();
        mailPart = new MimeMailPart();
    }

    /**
     * Constructor - Only applies specified message, but does not set any attributes
     *
     * @throws OXException If setting message as content fails
     */
    public MimeMailMessage(final MimeMessage msg) throws OXException {
        super();
        // TODO: this.mailPart = MIMEMessageConverter.convertPart(msg);
        mailPart = new MimeMailPart(msg);
    }

    @Override
    public Part getPart() {
        return mailPart.getPart();
    }

    /**
     * Gets the separator.
     *
     * @return The separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * Sets the separator.
     *
     * @param separator The separator to set
     */
    public void setSeparator(final char separator) {
        this.separator = separator;
    }

    /**
     * Gets the sequence number.
     *
     * @return The sequence number
     */
    public int getSeqnum() {
        return seqnum;
    }

    /**
     * Sets the sequence number.
     *
     * @param seqnum The sequence number to set
     */
    public void setSeqnum(final int seqnum) {
        this.seqnum = seqnum;
    }

    /**
     * Sets this mail message's content
     * <p>
     * Through providing a <code>null</code> reference the body is cleared from this mail.
     *
     * @param msg The MIME message or <code>null</code> to clear any body references
     * @throws OXException If setting message as content fails
     */
    public void setContent(final MimeMessage msg) throws OXException {
        // TODO: this.mailPart = msg == null ? new MIMEMailPart(null) : MIMEMessageConverter.convertPart(msg);
        mailPart = msg == null ? new MimeMailPart((Part) null) : new MimeMailPart(msg);
    }

    /**
     * Gets the {@link MimeMessage MIME message}.
     *
     * @return The {@link MimeMessage MIME message} or <code>null</code>
     */
    public MimeMessage getMimeMessage() {
        return (MimeMessage) mailPart.getPart();
    }

    @Override
    public void cleanUp() {
        final MimeMessage mimeMessage = getMimeMessage();
        if (mimeMessage instanceof MimeCleanUp) {
            try {
                ((MimeCleanUp) mimeMessage).cleanUp();
            } catch (final Exception e) {
                LoggerFactory.getLogger(MimeMailMessage.class).warn("Couldn't clean-up MIME resource.", e);
            }
        }
    }

    @Override
    public String getMailId() {
        return id;
    }

    @Override
    public void setMailId(final String id) {
        this.id = id;
    }

    @Override
    public Object getContent() throws OXException {
        return mailPart.getContent();
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return mailPart.getDataHandler();
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return mailPart.getEnclosedCount();
    }

    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return mailPart.getEnclosedMailPart(index);
    }

    @Override
    public InputStream getRawInputStream() throws OXException {
        return mailPart.getRawInputStream();
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return mailPart.getInputStream();
    }

    @Override
    public void writeTo(final OutputStream out) throws OXException {
        mailPart.writeTo(out);
    }

    @Override
    public void loadContent() throws OXException {
        mailPart.loadContent();
    }

    @Override
    public void prepareForCaching() {
        mailPart.prepareForCaching();
    }

    @Override
    public void setUnreadMessages(final int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    @Override
    public int getUnreadMessages() {
        return unreadMessages;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append("MimeMailMessage [");
        {
            final String id = getMailId();
            if (id != null) {
                builder.append("id=").append(id).append(", ");
            }
        }
        {
            final String folder = getFolder();
            if (folder != null) {
                builder.append("folder=").append(folder);
            }
        }
        builder.append(']');
        return builder.toString();
    }

}
