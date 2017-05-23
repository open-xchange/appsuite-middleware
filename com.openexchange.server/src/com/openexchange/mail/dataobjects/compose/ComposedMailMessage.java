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

package com.openexchange.mail.dataobjects.compose;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposedMailPart.ComposedPartType;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;

/**
 * {@link ComposedMailMessage} - Subclass of {@link MailMessage} designed for composing a mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class ComposedMailMessage extends MailMessage {

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ComposedMailMessage.class);

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -6179506566418364076L;

    private final Session session;
    private final Context ctx;
    private ComposeType sendType;
    private transient MimeMessageFiller filler;
    private final Set<InternetAddress> recipients;
    private UserSettingMail mailSettings;
    private boolean appendToSentFolder;
    private boolean transportToRecipients;
    private SecuritySettings securitySettings;

    /**
     * Default constructor
     */
    protected ComposedMailMessage(final Session session, final Context ctx) {
        super();
        this.session = session;
        this.ctx = ctx;
        recipients = new LinkedHashSet<InternetAddress>();
        appendToSentFolder = true;
        transportToRecipients = true;
    }

    /**
     * Sets the mail settings
     *
     * @param mailSettings The mail settings to set
     */
    public void setMailSettings(UserSettingMail mailSettings) {
        this.mailSettings = mailSettings;
    }

    /**
     * Gets the optional mail settings
     *
     * @return The mail settings or <code>null</code>
     */
    public UserSettingMail getMailSettings() {
        return mailSettings;
    }

    /**
     * Sets the security settings
     *
     * @param securitySettings The security settings to set
     */
    public void setSecuritySettings(SecuritySettings securitySettings) {
        this.securitySettings = securitySettings;
    }

    /**
     * Gets the security settings
     *
     * @return The security settings or <code>null</code>
     */
    public SecuritySettings getSecuritySettings() {
        return securitySettings;
    }

    /**
     * Checks if this composed message is supposed to be appended to standard sent folder.
     *
     * @return <code>true</code> if it should be appended to standard sent folder; otherwise <code>false</code>
     */
    public boolean isAppendToSentFolder() {
        return appendToSentFolder;
    }

    /**
     * Sets if this composed message is supposed to be appended to standard sent folder.
     *
     * @param appendToSentFolder <code>true</code> if it should be appended to standard sent folder; otherwise <code>false</code>
     */
    public void setAppendToSentFolder(boolean appendToSentFolder) {
        this.appendToSentFolder = appendToSentFolder;
    }

    /**
     * Checks if this composed message is supposed to be actually transported.
     *
     * @return <code>true</code> if it should be transported; otherwise <code>false</code>
     */
    public boolean isTransportToRecipients() {
        return transportToRecipients;
    }

    /**
     * Sets if this composed message is supposed to be actually transported.
     *
     * @param transportToRecipients <code>true</code> if it should be transported; otherwise <code>false</code>
     */
    public void setTransportToRecipients(boolean transportToRecipients) {
        this.transportToRecipients = transportToRecipients;
    }

    /**
     * Sets the send type.
     *
     * @param sendType The send type
     */
    public void setSendType(ComposeType sendType) {
        this.sendType = sendType;
    }

    /**
     * Gets the send type.
     *
     * @return The send type
     */
    public ComposeType getSendType() {
        return sendType;
    }

    /**
     * The readObject method is responsible for reading from the stream and restoring the classes fields. It may call in.defaultReadObject
     * to invoke the default mechanism for restoring the object's non-static and non-transient fields. The
     * {@link ObjectInputStream#defaultReadObject()} method uses information in the stream to assign the fields of the object saved in the
     * stream with the correspondingly named fields in the current object. This handles the case when the class has evolved to add new
     * fields. The method does not need to concern itself with the state belonging to its super classes or subclasses. State is saved by
     * writing the individual fields to the ObjectOutputStream using the writeObject method or by using the methods for primitive data types
     * supported by {@link DataOutput}.
     *
     * @param in The object input stream
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a casting fails
     */
    private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException(ComposedMailMessage.class.getName());
    }

    /**
     * The writeObject method is responsible for writing the state of the object for its particular class so that the corresponding
     * readObject method can restore it. The default mechanism for saving the Object's fields can be invoked by calling
     * {@link ObjectOutputStream#defaultWriteObject()}. The method does not need to concern itself with the state belonging to its super
     * classes or subclasses. State is saved by writing the individual fields to the ObjectOutputStream using the writeObject method or by
     * using the methods for primitive data types supported by {@link DataOutput}.
     *
     * @param out The object output stream
     * @throws IOException If an I/O error occurs
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
        throw new NotSerializableException(ComposedMailMessage.class.getName());
    }

    /**
     * Checks if this composed mail has dedicated recipients.
     *
     * @return <code>true</code> if this composed mail has dedicated recipients; otherwise <code>false</code>
     */
    public boolean hasRecipients() {
        return !recipients.isEmpty();
    }

    /**
     * Gets the composed mail's dedicated recipients.
     *
     * @return The dedicated recipients
     */
    public InternetAddress[] getRecipients() {
        return recipients.toArray(new QuotedInternetAddress[recipients.size()]);
    }

    /**
     * Adds a dedicated recipient to this composed mail.
     *
     * @param recipient The recipient to add
     */
    public void addRecipient(final InternetAddress recipient) {
        recipients.add(recipient);
    }

    /**
     * Adds dedicated recipients to this composed mail.
     *
     * @param recipients The recipients to add
     */
    public void addRecipients(final InternetAddress[] recipients) {
        this.recipients.addAll(Arrays.asList(recipients));
    }

    /**
     * Gets the session
     *
     * @return the session, possibly <code>null</code>!
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the context
     *
     * @return the context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * Sets the mail filler
     *
     * @param filler The mail filler
     */
    public void setFiller(final MimeMessageFiller filler) {
        this.filler = filler;
    }

    /**
     * Cleans-up this composed mail's referenced uploaded files and frees temporary stored files.
     */
    public void cleanUp() {
        if (null != filler) {
            filler.deleteReferencedUploadFiles();
        }
        try {
            final int count = getEnclosedCount();
            for (int i = 0; i < count; i++) {
                if (getEnclosedMailPart(i) instanceof ComposedMailPart) {
                    final ComposedMailPart composedMailPart = (ComposedMailPart) getEnclosedMailPart(i);
                    if (ComposedPartType.REFERENCE.equals(composedMailPart.getType())) {
                        ((ReferencedMailPart) (composedMailPart)).close();
                    } else if (ComposedPartType.DATA.equals(composedMailPart.getType())) {
                        ((DataMailPart) (composedMailPart)).close();
                    } else if (ComposedPartType.FILE.equals(composedMailPart.getType())) {
                        final File f = ((UploadFileMailPart) (composedMailPart)).getUploadFile();
                        if (f.exists() && !f.delete()) {
                            LOG.warn("Temporary store file '{}' could not be deleted.", f.getName());
                        }
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
        }
    }

    @Override
    public int getUnreadMessages() {
        throw new UnsupportedOperationException("ComposedMailMessage.getUnreadMessages() not supported");
    }

    @Override
    public void setUnreadMessages(final int unreadMessages) {
        throw new UnsupportedOperationException("ComposedMailMessage.setUnreadMessages() not supported");
    }

    /**
     * Gets the number of enclosed mail parts.
     * <p>
     * <b>Note</b>: The returned number does not include the text body part applied with {@link #setBodyPart(TextBodyMailPart)}. To check
     * for contained parts:
     *
     * <pre>
     * composedMail.getEnclosedCount() &gt; 0
     * </pre>
     *
     * @see #NO_ENCLOSED_PARTS
     * @return The number of enclosed mail parts or {@link #NO_ENCLOSED_PARTS} if not applicable
     */
    @Override
    public abstract int getEnclosedCount() throws OXException;

    /**
     * Gets this composed mail's part located at given index.
     * <p>
     * <b>Note</b>: This method does not include the text body part applied with {@link #setBodyPart(TextBodyMailPart)}.
     *
     * @param index The index of desired mail part or <code>null</code> if not applicable
     * @return The mail part
     */
    @Override
    public abstract MailPart getEnclosedMailPart(final int index) throws OXException;

    /**
     * Sets this composed message's body part.
     * <p>
     * The body part's content is supposed to be HTML content which is ought to be converted to appropriate MIME type on transport.
     *
     * @param mailPart The body part
     */
    public abstract void setBodyPart(TextBodyMailPart mailPart);

    /**
     * Gets this composed message's body part.
     * <p>
     * The body part's content is supposed to be HTML content which is ought to be converted to appropriate MIME type on transport.
     *
     * @return The body part
     */
    public abstract TextBodyMailPart getBodyPart();

    /**
     * Removes the enclosed part at the specified position. Shifts any subsequent parts to the left (subtracts one from their indices).
     * Returns the part that was removed.
     *
     * @param index The index position
     * @return The removed part
     */
    public abstract MailPart removeEnclosedPart(int index);

    /**
     * Adds an instance of {@link MailPart} to enclosed parts
     *
     * @param part The instance of {@link MailPart} to add
     */
    public abstract void addEnclosedPart(MailPart part);

}
