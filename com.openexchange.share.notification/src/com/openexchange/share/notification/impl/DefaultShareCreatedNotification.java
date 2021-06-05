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

import java.util.List;
import com.openexchange.group.Group;
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
public class DefaultShareCreatedNotification<T> extends AbstractNotification<T> implements ShareCreatedNotification<T> {

    private Session session;
    private List<ShareTarget> targets;
    private String message;
    private int targetUserID;
    private String shareUrl;
    private Group group;

    /**
     * Initializes a new {@link DefaultShareCreatedNotification}.
     */
    public DefaultShareCreatedNotification(Transport transport) {
        super(transport, NotificationType.SHARE_CREATED);
    }

    public DefaultShareCreatedNotification(Transport transport, NotificationType type) {
        super(transport, type);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public List<ShareTarget> getShareTargets() {
        return targets;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public List<ShareTarget> getTargets() {
        return targets;
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
    public Group getTargetGroup() {
        return group;
    }

    public void setTargets(List<ShareTarget> targets) {
        this.targets = targets;
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

    public void setTargetGroup(Group group) {
        this.group = group;
    }

}
