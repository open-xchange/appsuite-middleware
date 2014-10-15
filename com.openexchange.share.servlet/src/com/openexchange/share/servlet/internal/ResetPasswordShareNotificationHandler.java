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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.share.servlet.internal;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ContentAwareComposedMailMessage;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.notification.ShareNotification;
import com.openexchange.share.notification.ShareNotificationHandler;


/**
 * {@link ResetPasswordShareNotificationHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ResetPasswordShareNotificationHandler implements ShareNotificationHandler {

    /**
     * Initializes a new {@link ResetPasswordShareNotificationHandler}.
     */
    public ResetPasswordShareNotificationHandler() {
        super();
    }

    @Override
    public <T extends ShareNotification<?>> void notify(T notification, Session session) throws OXException {
        ResetPasswordShareNotification resetPasswordNotification = (ResetPasswordShareNotification) notification;

        MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
        try {
            mimeMessage.addFrom(new Address[] { resetPasswordNotification.getSender() });
            mimeMessage.addRecipient(RecipientType.TO, resetPasswordNotification.getTransportInfo());
            mimeMessage.addHeader("X-Open-Xchange-Share", resetPasswordNotification.getUrl());
            mimeMessage.setSubject(resetPasswordNotification.getTitle(), "UTF-8");

            mimeMessage.setText(resetPasswordNotification.getMessage(), "UTF-8", "plain");
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        MailTransport transport = MailTransport.getInstance(session);
        try {
            transport.sendMailMessage(new ContentAwareComposedMailMessage(mimeMessage, session, session.getContextId()), ComposeType.NEW);
        } finally {
            transport.close();
        }
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public <T extends ShareNotification<?>> boolean handles(T notification) {
        return (notification instanceof ResetPasswordShareNotification);
    }

}
