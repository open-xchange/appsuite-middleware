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

package com.openexchange.share.notification.impl;

import java.util.Date;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.ShareNotificationService.Transport;

/**
 * A default implementation of {@link ShareCreatedNotification} that contains all
 * necessary data as fields. Plain setters can be used to initialize an instance.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultLinkCreatedNotification<T> extends AbstractNotification<T> implements LinkCreatedNotification<T> {

    private Session session;
    private ShareTarget target;
    private String message;
    private int targetUserID;
    private String shareUrl;
    private Date expiryDate;
    private String password;

    /**
     * Initializes a new {@link DefaultLinkCreatedNotification}.
     */
    public DefaultLinkCreatedNotification(Transport transport) {
        super(transport, NotificationType.LINK_CREATED);
    }

    public DefaultLinkCreatedNotification(Transport transport, NotificationType type) {
        super(transport, type);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public ShareTarget getShareTarget() {
        return target;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getTargetUserID() {
        return targetUserID;
    }

    @Override
    public String getShareUrl() {
        return shareUrl;
    }

    @Override
    public Date getExpiryDate() {
        return expiryDate;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setTarget(ShareTarget target) {
        this.target = target;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTargetUserID(int targetUserID) {
        this.targetUserID = targetUserID;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
