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
    protected ComposedMailMessage(Session session, Context ctx) {
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
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
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
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
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
    public void addRecipient(InternetAddress recipient) {
        recipients.add(recipient);
    }

    /**
     * Adds dedicated recipients to this composed mail.
     *
     * @param recipients The recipients to add
     */
    public void addRecipients(InternetAddress[] recipients) {
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
    public void setFiller(MimeMessageFiller filler) {
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
                MailPart enclosedMailPart = getEnclosedMailPart(i);
                if (enclosedMailPart instanceof ComposedMailPart) {
                    ComposedMailPart composedMailPart = (ComposedMailPart) enclosedMailPart;

                    if (composedMailPart.getType() != null) {
                        switch (composedMailPart.getType()) {
                            case REFERENCE:
                                if (composedMailPart instanceof ReferencedMailPart) {
                                    ((ReferencedMailPart) (composedMailPart)).close();
                                }
                                break;
                            case DATA:
                                if (composedMailPart instanceof DataMailPart) {
                                    ((DataMailPart) (composedMailPart)).close();
                                }
                                break;
                            case FILE:
                                if (composedMailPart instanceof UploadFileMailPart) {
                                    final File f = ((UploadFileMailPart) (composedMailPart)).getUploadFile();
                                    if (f.exists() && !f.delete()) {
                                        LOG.warn("Temporary store file '{}' could not be deleted.", f.getName());
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Override
    public int getUnreadMessages() {
        throw new UnsupportedOperationException("ComposedMailMessage.getUnreadMessages() not supported");
    }

    @Override
    public void setUnreadMessages(int unreadMessages) {
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
    public abstract MailPart getEnclosedMailPart(int index) throws OXException;

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
