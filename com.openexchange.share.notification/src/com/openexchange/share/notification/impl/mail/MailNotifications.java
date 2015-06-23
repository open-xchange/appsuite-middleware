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

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.impl.AbstractNotificationBuilder;
import com.openexchange.share.notification.impl.DefaultPasswordResetConfirmNotification;
import com.openexchange.share.notification.impl.DefaultShareCreatedNotification;
import com.openexchange.share.notification.impl.NotificationType;
import com.openexchange.share.notification.impl.PasswordResetConfirmNotification;
import com.openexchange.share.notification.impl.ShareCreatedNotification;

/**
 * {@link MailNotifications}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailNotifications {

    /**
     * Creates a new builder for {@link ShareCreatedNotification}s.
     *
     * @return The builder instance.
     */
    public static ShareCreatedBuilder shareCreated() {
        return new ShareCreatedBuilder();
    }

    public static PasswordResetConfirmBuilder passwordConfirm() {
        return new PasswordResetConfirmBuilder();
    }

    public static class PasswordResetConfirmBuilder extends AbstractNotificationBuilder<PasswordResetConfirmBuilder, PasswordResetConfirmNotification<InternetAddress>, InternetAddress> {

        private String shareToken;
        private String confirmToken;
        private String accountName;

        protected PasswordResetConfirmBuilder() {
            super(NotificationType.CONFIRM_PASSWORD_RESET);
        }

        public PasswordResetConfirmBuilder setShareToken(String shareToken) {
            this.shareToken = shareToken;
            return this;
        }

        public PasswordResetConfirmBuilder setConfirmToken(String confirmToken) {
            this.confirmToken = confirmToken;
            return this;
        }

        public PasswordResetConfirmBuilder setAccountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        @Override
        protected PasswordResetConfirmNotification<InternetAddress> doBuild() {
            checkGreaterZero(guestID, "guestID");
            checkNotNull(accountName, "accountName");
            checkNotNull(shareToken, "shareToken");
            checkNotNull(confirmToken, "confirmToken");

            DefaultPasswordResetConfirmNotification<InternetAddress> notification = new DefaultPasswordResetConfirmNotification<>(Transport.MAIL);
            notification.apply(this);
            notification.setShareToken(shareToken);
            notification.setConfirmToken(confirmToken);
            notification.setAccountName(accountName);
            notification.setGuestID(guestID);

            return notification;
        }
    }

    public static class ShareCreatedBuilder extends AbstractNotificationBuilder<ShareCreatedBuilder, ShareCreatedNotification<InternetAddress>, InternetAddress> {

        private Session session;

        private String message;

        private final List<ShareTarget> targets = new ArrayList<ShareTarget>();

        private boolean initialShare;

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
         * Sets the share targets to inform the recipient about
         *
         * @param targets The targets
         */
        public ShareCreatedBuilder setTargets(List<ShareTarget> targets) {
            this.targets.clear();
            this.targets.addAll(targets);
            return this;
        }

        /**
         * Adds a target to the list of targets to inform the recipient about
         *
         * @param target The target
         */
        public ShareCreatedBuilder addTarget(ShareTarget target) {
            targets.add(target);
            return this;
        }

        /**
         * Sets the custom message to be contained in the notification
         *
         * @param message The message
         */
        public ShareCreatedBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set whether this notification is about the first share targeting a new guest user.
         *
         * @param initialShare the flag
         */
        public ShareCreatedBuilder setIntitialShare(boolean initialShare) {
            this.initialShare = initialShare;
            return this;
        }

        @Override
        protected ShareCreatedNotification<InternetAddress> doBuild() {
            checkNotNull(session, "session");
            checkNotEmpty(targets, "targets");

            DefaultShareCreatedNotification<InternetAddress> notification = new DefaultShareCreatedNotification<InternetAddress>(Transport.MAIL);
            notification.apply(this);
            notification.setSession(session);
            notification.setTargetUserID(guestID);
            notification.setTargets(targets);
            notification.setMessage(message);
            notification.setInitialShare(initialShare);
            return notification;
        }

    }

}
