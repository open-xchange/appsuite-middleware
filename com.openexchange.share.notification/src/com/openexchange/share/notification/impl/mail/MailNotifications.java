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

package com.openexchange.share.notification.impl.mail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.group.Group;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.impl.AbstractNotificationBuilder;
import com.openexchange.share.notification.impl.DefaultLinkCreatedNotification;
import com.openexchange.share.notification.impl.DefaultPasswordResetConfirmNotification;
import com.openexchange.share.notification.impl.DefaultShareCreatedNotification;
import com.openexchange.share.notification.impl.LinkCreatedNotification;
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

    public static LinkCreatedBuilder linkCreated() {
        return new LinkCreatedBuilder();
    }

    public static PasswordResetConfirmBuilder passwordConfirm() {
        return new PasswordResetConfirmBuilder();
    }

    public static class PasswordResetConfirmBuilder extends AbstractNotificationBuilder<PasswordResetConfirmBuilder, PasswordResetConfirmNotification<InternetAddress>, InternetAddress> {

        private String shareUrl;
        private String pwResetUrl;

        protected PasswordResetConfirmBuilder() {
            super(NotificationType.CONFIRM_PASSWORD_RESET);
        }

        /**
         * Sets the share URL.
         *
         * @param shareUrl The URL
         */
        public PasswordResetConfirmBuilder setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
            return this;
        }

        /**
         * Sets the URL to confirm the password reset request
         *
         * @param pwResetUrl The URL
         */
        public PasswordResetConfirmBuilder setConfirmPasswordResetUrl(String pwResetUrl) {
            this.pwResetUrl = pwResetUrl;
            return this;
        }

        @Override
        protected PasswordResetConfirmNotification<InternetAddress> doBuild() {
            checkGreaterZero(userID, "userID");
            checkNotNull(shareUrl, "shareUrl");
            checkNotNull(pwResetUrl, "pwResetUrl");

            DefaultPasswordResetConfirmNotification<InternetAddress> notification = new DefaultPasswordResetConfirmNotification<>(Transport.MAIL);
            notification.apply(this);
            notification.setGuestID(userID);
            notification.setShareUrl(shareUrl);
            notification.setConfirmPasswordResetUrl(pwResetUrl);
            return notification;
        }
    }

    public static class ShareCreatedBuilder extends AbstractNotificationBuilder<ShareCreatedBuilder, ShareCreatedNotification<InternetAddress>, InternetAddress> {

        private Session session;
        private String message;
        private final List<ShareTarget> targets = new ArrayList<ShareTarget>();
        private String shareUrl;
        private Group group;

        private ShareCreatedBuilder() {
            super(NotificationType.SHARE_CREATED);
        }

        /**
         * Sets the group of the recipient user in cases where the group was added as permission entity and not
         * the user itself.
         *
         * @param group The group or <code>null</code> if the target was shared directly to the user
         */
        public ShareCreatedBuilder setTargetGroup(Group group) {
            this.group = group;
            return this;
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
         * Sets the share URL.
         *
         * @param shareUrl The URL
         */
        public ShareCreatedBuilder setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
            return this;
        }

        @Override
        protected ShareCreatedNotification<InternetAddress> doBuild() {
            checkNotNull(session, "session");
            checkNotNull(shareUrl, "shareUrl");
            checkNotEmpty(targets, "targets");

            DefaultShareCreatedNotification<InternetAddress> notification = new DefaultShareCreatedNotification<InternetAddress>(Transport.MAIL);
            notification.apply(this);
            notification.setSession(session);
            notification.setTargetUserID(userID);
            notification.setTargets(targets);
            notification.setMessage(message);
            notification.setShareUrl(shareUrl);
            notification.setTargetGroup(group);
            return notification;
        }

    }

    public static class LinkCreatedBuilder extends AbstractNotificationBuilder<LinkCreatedBuilder, LinkCreatedNotification<InternetAddress>, InternetAddress> {

        private Session session;
        private String message;
        private ShareTarget target;
        private String shareUrl;
        private Date expiryDate;
        private String password;

        private LinkCreatedBuilder() {
            super(NotificationType.LINK_CREATED);
        }

        /**
         * Sets the session
         *
         * @param session The session to set
         */
        public LinkCreatedBuilder setSession(Session session) {
            this.session = session;
            return this;
        }

        /**
         * Sets the target to inform the recipient about
         *
         * @param target The target
         */
        public LinkCreatedBuilder setTarget(ShareTarget target) {
            this.target = target;
            return this;
        }

        /**
         * Sets the custom message to be contained in the notification
         *
         * @param message The message
         */
        public LinkCreatedBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the share URL.
         *
         * @param shareUrl The URL
         */
        public LinkCreatedBuilder setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
            return this;
        }

        /**
         * Sets the expiryDate
         *
         * @param expiryDate The expiryDate to set
         */
        public LinkCreatedBuilder setExpiryDate(Date expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }


        /**
         * Sets the password
         *
         * @param password The password to set
         * @return
         */
        public LinkCreatedBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        protected LinkCreatedNotification<InternetAddress> doBuild() {
            checkNotNull(session, "session");
            checkNotNull(shareUrl, "shareUrl");
            checkNotNull(target, "target");

            DefaultLinkCreatedNotification<InternetAddress> notification = new DefaultLinkCreatedNotification<InternetAddress>(Transport.MAIL);
            notification.apply(this);
            notification.setSession(session);
            notification.setTargetUserID(userID);
            notification.setTarget(target);
            notification.setMessage(message);
            notification.setShareUrl(shareUrl);
            notification.setExpiryDate(expiryDate);
            notification.setPassword(password);
            return notification;
        }

    }

}
