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

import java.io.InputStream;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;

/**
 * {@link ContentAwareComposedMailMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContentAwareComposedMailMessage extends ComposedMailMessage implements ContentAware {

    private static final long serialVersionUID = 7469781321067672927L;

    private final MimeMessage content;

    /**
     * Initializes a new {@link ContentAwareComposedMailMessage}.
     *
     * @param content The content object
     * @param session The session
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    public ContentAwareComposedMailMessage(MimeMessage content, Session session, int contextId) throws OXException {
        this(content, session, ContextStorage.getStorageContext(contextId));
    }

    /**
     * Initializes a new {@link ContentAwareComposedMailMessage}.
     *
     * @param content The content object
     * @param session The session
     * @param ctx The context
     */
    public ContentAwareComposedMailMessage(MimeMessage content, Session session, Context ctx) {
        super(session, ctx);
        this.content = content;
    }

    /**
     * Initializes a new {@link ContentAwareComposedMailMessage}. Use this
     * constructor for administrative mails that should not be send in the name of a
     * certain user.
     *
     * @param content The content object
     * @param contextId The context identifier
     * @throws OXException If initialization fails
     */
    public ContentAwareComposedMailMessage(MimeMessage content, int contextId) throws OXException {
        super(null, ContextStorage.getStorageContext(contextId));
        this.content = content;
    }

    private static InternetAddress[] convert(Address[] a) {
        if (null == a) {
            return null;
        }
        final int length = a.length;
        final InternetAddress[] ret = new InternetAddress[length];
        for (int i = 0; i < length; i++) {
            ret[i] = (InternetAddress) a[i];
        }
        return ret;
    }

    /**
     * Throws <code>UnsupportedOperationException</code>.
     */
    @Override
    public void setMailSettings(UserSettingMail mailSettings) {
        // Not applicable
        throw new UnsupportedOperationException("ContentAwareComposedMailMessage.setMailSettings()");
    }

    /**
     * Returns <code>null</code>.
     */
    @Override
    public UserSettingMail getMailSettings() {
        // Not applicable
        return null;
    }

    @Override
    public boolean containsFrom() {
        try {
            Address[] from = content.getFrom();
            return from != null && from.length > 0;
        } catch (MessagingException e) {
            return false;
        }
    }

    @Override
    public InternetAddress[] getFrom() {
        try {
            return convert(content.getFrom());
        } catch (MessagingException e) {
            return null;
        }
    }

    @Override
    public String getFirstHeader(String name) {
        try {
            return content.getHeader(name, null);
        } catch (MessagingException e) {
            return null;
        }
    }

    @Override
    public Object getContent() throws OXException {
        return content;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBodyPart(TextBodyMailPart mailPart) {
        // Nope
    }

    @Override
    public TextBodyMailPart getBodyPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MailPart removeEnclosedPart(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEnclosedPart(MailPart part) {
        // Nope
    }

    @Override
    public String getMailId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMailId(String id) {
        // Nope
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadContent() throws OXException {
        // Nope
    }

    @Override
    public void prepareForCaching() {
        // Nothing to do
    }

    @Override
    public void cleanUp() {
        // Nothing to clean
    }

}
