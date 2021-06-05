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
    public MimeMailMessage(MimeMessage msg) throws OXException {
        super();
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
    public void setSeparator(char separator) {
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
    public void setSeqnum(int seqnum) {
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
    public void setContent(MimeMessage msg) throws OXException {
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
            } catch (Exception e) {
                LoggerFactory.getLogger(MimeMailMessage.class).warn("Couldn't clean-up MIME resource.", e);
            }
        }
    }

    @Override
    public String getMailId() {
        return id;
    }

    @Override
    public void setMailId(String id) {
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
    public MailPart getEnclosedMailPart(int index) throws OXException {
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
    public void writeTo(OutputStream out) throws OXException {
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
    public void setUnreadMessages(int unreadMessages) {
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
