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
    public ContentAwareComposedMailMessage(final MimeMessage content, final Session session, final int contextId) throws OXException {
        this(content, session, ContextStorage.getStorageContext(contextId));
    }

    /**
     * Initializes a new {@link ContentAwareComposedMailMessage}.
     *
     * @param content The content object
     * @param session The session
     * @param ctx The context
     */
    public ContentAwareComposedMailMessage(final MimeMessage content, final Session session, final Context ctx) {
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
    public ContentAwareComposedMailMessage(final MimeMessage content, final int contextId) throws OXException {
        super(null, ContextStorage.getStorageContext(contextId));
        this.content = content;
    }

    private static InternetAddress[] convert(final Address[] a) {
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
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBodyPart(final TextBodyMailPart mailPart) {
        // Nope
    }

    @Override
    public TextBodyMailPart getBodyPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MailPart removeEnclosedPart(final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addEnclosedPart(final MailPart part) {
        // Nope
    }

    @Override
    public String getMailId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMailId(final String id) {
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

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#prepareForCaching()
     */
    @Override
    public void prepareForCaching() {
        // TODO Auto-generated method stub

    }

}
