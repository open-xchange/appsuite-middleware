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

package com.openexchange.share.notification.mail;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.AbstractNotificationBuilder;
import com.openexchange.share.notification.DefaultPasswordResetNotification;
import com.openexchange.share.notification.DefaultShareCreatedNotification;
import com.openexchange.share.notification.PasswordResetNotification;
import com.openexchange.share.notification.ShareCreatedNotification;
import com.openexchange.share.notification.ShareNotification.NotificationType;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link MailNotifications}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailNotifications {

    public static ShareCreatedBuilder shareCreated() {
        return new ShareCreatedBuilder();
    }

    public static PasswordResetBuilder passwordReset() {
        return new PasswordResetBuilder();
    }

    public static class PasswordResetBuilder extends AbstractNotificationBuilder<PasswordResetBuilder, PasswordResetNotification<InternetAddress>, InternetAddress> {

        private String username;

        private String password;

        /**
         * Initializes a new {@link PasswordResetBuilder}.
         */
        protected PasswordResetBuilder() {
            super(NotificationType.PASSWORD_RESET);
        }

        public PasswordResetBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public PasswordResetBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public PasswordResetNotification<InternetAddress> build() throws OXException {
            checkNotNull(transportInfo);
            checkNotNull(linkProvider);
            checkNotNull(contextID);
            checkNotNull(locale);
            checkNotNull(username);
            checkNotNull(password);

            DefaultPasswordResetNotification<InternetAddress> notification = new DefaultPasswordResetNotification<InternetAddress>(Transport.MAIL);
            notification.apply(this);
            notification.setUsername(username);
            notification.setPassword(password);
            return notification;
        }

    }

    public static class ShareCreatedBuilder extends AbstractNotificationBuilder<ShareCreatedBuilder, ShareCreatedNotification<InternetAddress>, InternetAddress> {

        private Session session;

        private ShareRecipient recipient;

        private String message;

        private List<ShareTarget> targets = new ArrayList<ShareTarget>();

        private ShareCreatedBuilder() {
            super(NotificationType.SHARE_CREATED);
        }


        /**
         * Sets the session
         *
         * @param session The session to set
         */
        public ShareCreatedBuilder setSession(Session session) {
            this.session = session;
            return this;
        }


        /**
         * Sets the recipient
         *
         * @param recipient The recipient to set
         */
        public ShareCreatedBuilder setRecipient(ShareRecipient recipient) {
            this.recipient = recipient;
            return this;
        }


        /**
         * Sets the targets
         *
         * @param targets The targets to set
         */
        public ShareCreatedBuilder setTargets(List<ShareTarget> targets) {
            this.targets.clear();
            this.targets.addAll(targets);
            return this;
        }

        public ShareCreatedBuilder addTarget(ShareTarget target) {
            targets.add(target);
            return this;
        }

        /**
         * Sets the message
         *
         * @param message The message to set
         */
        public ShareCreatedBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public ShareCreatedNotification<InternetAddress> build() throws OXException {
            checkNotNull(transportInfo);
            checkNotNull(linkProvider);
            checkNotNull(contextID);
            checkNotNull(locale);
            checkNotNull(session);
            checkNotNull(recipient);
            checkState(targets.size() > 0, "At least one share target must be set!");

            DefaultShareCreatedNotification<InternetAddress> notification = new DefaultShareCreatedNotification<InternetAddress>(Transport.MAIL);
            notification.apply(this);
            notification.setSession(session);
            notification.setRecipient(recipient);
            notification.setTargets(targets);
            notification.setMessage(message);
            return notification;
        }

    }

}
