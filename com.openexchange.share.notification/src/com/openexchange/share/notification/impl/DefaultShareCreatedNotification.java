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
