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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.notification.impl.mail;

import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.impl.PasswordResetConfirmNotification;
import com.openexchange.share.notification.impl.ShareCreatedNotification;
import com.openexchange.share.notification.impl.ShareNotification;
import com.openexchange.share.notification.impl.ShareNotificationHandler;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link MailNotificationHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class MailNotificationHandler implements ShareNotificationHandler<InternetAddress> {

    private final TransportProvider transportProvider;

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailNotificationHandler}.
     * @throws OXException
     */
    public MailNotificationHandler(ServiceLookup services) throws OXException {
        super();
        this.transportProvider = TransportProviderRegistry.getTransportProvider("smtp");
        this.services = services;
    }

    @Override
    public Transport getTransport() {
        return Transport.MAIL;
    }

    @Override
    public <T extends ShareNotification<InternetAddress>> void send(T notification) throws OXException {
        try {
            switch (notification.getType()) {
                case SHARE_CREATED:
                    sendShareCreated(notification);
                    break;

                case CONFIRM_PASSWORD_RESET:
                    sendPasswordResetConfirm(notification);
                    break;

                default:
                    throw new OXException(new IllegalArgumentException("MailNotificationHandler cannot handle notifications of type " + notification.getType().toString()));
            }
        } catch (UnsupportedEncodingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void sendShareCreated(ShareNotification<InternetAddress> notification) throws UnsupportedEncodingException, OXException, MessagingException {
        ShareCreatedNotification<InternetAddress> casted = (ShareCreatedNotification<InternetAddress>) notification;
        ComposedMailMessage mail = ShareCreatedMail.init(casted, transportProvider, services).compose();
        if (ServerSessionAdapter.valueOf(casted.getSession()).getUserConfiguration().hasWebMail()) {
            sendMail(transportProvider.createNewMailTransport(casted.getSession()), mail);
        } else {
            sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID()), mail);
        }
    }

    private void sendPasswordResetConfirm(ShareNotification<InternetAddress> notification) throws UnsupportedEncodingException, OXException, MessagingException {
        PasswordResetConfirmNotification<InternetAddress> casted = (PasswordResetConfirmNotification<InternetAddress>) notification;
        ComposedMailMessage mail = ConfirmPasswordResetMail.init(casted, transportProvider, services).compose();
        sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID()), mail);
    }

    private static void sendMail(MailTransport transport, ComposedMailMessage mail) throws OXException {
        try {
            transport.sendMailMessage(mail, ComposeType.NEW);
        } finally {
            try {
                transport.close();
            } catch (OXException e) {
                // ignore
            }
        }
    }

}
