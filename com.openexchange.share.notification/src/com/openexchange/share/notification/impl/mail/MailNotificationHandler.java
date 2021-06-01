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

package com.openexchange.share.notification.impl.mail;

import javax.mail.internet.InternetAddress;
import com.openexchange.authentication.application.AppPasswordUtils;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.impl.LinkCreatedNotification;
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

    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailNotificationHandler}.
     */
    public MailNotificationHandler(ServiceLookup services) {
        super();
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

                case LINK_CREATED:
                    sendLinkCreated(notification);
                    break;

                case CONFIRM_PASSWORD_RESET:
                    sendPasswordResetConfirm(notification);
                    break;

                default:
                    throw new OXException(new IllegalArgumentException("MailNotificationHandler cannot handle notifications of type " + notification.getType().toString()));
            }
        } catch (Exception e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private void sendShareCreated(ShareNotification<InternetAddress> notification) throws OXException {
        TransportProvider transportProvider = getTransportProvider();
        ShareCreatedNotification<InternetAddress> casted = (ShareCreatedNotification<InternetAddress>) notification;
        ComposedMailMessage mail = ShareCreatedMail.init(casted, services).compose();
        if (preferNoReplyAccount(casted.getSession())) {
            if (getBoolValue("com.openexchange.share.notification.usePersonalEmailAddress", false, casted.getSession())) {
                sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID(), false), mail);
            } else {
                sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID(), true), mail);
            }
        } else {
            sendMail(transportProvider.createNewMailTransport(casted.getSession()), mail);
        }
    }

    private void sendLinkCreated(ShareNotification<InternetAddress> notification) throws OXException {
        TransportProvider transportProvider = getTransportProvider();
        LinkCreatedNotification<InternetAddress> casted = (LinkCreatedNotification<InternetAddress>) notification;
        ComposedMailMessage mail = LinkCreatedMail.init(casted, services).compose();
        if (preferNoReplyAccount(casted.getSession())) {
            if (getBoolValue("com.openexchange.share.notification.usePersonalEmailAddress", false, casted.getSession())) {
                sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID(), false), mail);
            } else {
                sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID(), true), mail);
            }
        } else {
            sendMail(transportProvider.createNewMailTransport(casted.getSession()), mail);
        }
    }

    /**
     * Gets a value indicating whether to prefer the <i>no-reply</i> transport account when sending notification mails, or to stick to
     * the user's primary mail transport account instead.
     *
     * @param session The session to decide the preference for
     * @return <code>true</code> if the no-reply account should be used, <code>false</code>, otherwise
     */
    private boolean preferNoReplyAccount(Session session) throws OXException {
        /*
         * use no-reply if user has no mail module permission
         */
        if (null == session || false == ServerSessionAdapter.valueOf(session).getUserConfiguration().hasWebMail()) {
            return true;
        }
        /*
         * otherwise use no-reply if session is restricted and has no required scope
         */
        return false == AppPasswordUtils.isNotRestrictedOrHasScopes(session, "write_mail");
    }

    private void sendPasswordResetConfirm(ShareNotification<InternetAddress> notification) throws OXException {
        TransportProvider transportProvider = getTransportProvider();
        PasswordResetConfirmNotification<InternetAddress> casted = (PasswordResetConfirmNotification<InternetAddress>) notification;
        ComposedMailMessage mail = ConfirmPasswordResetMail.init(casted, services).compose();
        sendMail(transportProvider.createNewNoReplyTransport(casted.getContextID()), mail);
    }

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
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

    /**
     * Gets the value for specified <code>boolean</code> property.
     *
     * @param propertyName The name of the <code>boolean</code> property
     * @param defaultValue The default <code>boolean</code> value
     * @param session The session from requesting user
     * @return The <code>boolean</code> value or <code>defaultValue</code> (if absent)
     * @throws OXException If <code>boolean</code> value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    private boolean getBoolValue(String propertyName, boolean defaultValue, Session session) throws OXException {
        if (null == session) {
            throw new IllegalArgumentException("Session must not be null");
        }
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<Boolean> property = view.property(propertyName, boolean.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        return property.get().booleanValue();
    }

}
