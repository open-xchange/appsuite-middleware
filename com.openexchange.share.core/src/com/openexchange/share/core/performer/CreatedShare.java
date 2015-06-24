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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.core.performer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.RequestContext;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * Encapsulates the information about a newly created share.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class CreatedShare {

    private final ShareRecipient recipient;

    private final GuestInfo guestInfo;

    private final List<ShareInfo> shareInfos;

    public CreatedShare(ShareRecipient recipient, GuestInfo guestInfo, List<ShareInfo> shareInfos) {
        super();
        this.recipient = recipient;
        this.guestInfo = guestInfo;
        this.shareInfos = shareInfos;
    }

    /**
     * Gets the number of share targets.
     *
     * @return The number
     */
    public int size() {
        return shareInfos.size();
    }

    /**
     * Gets the guest information of the according recipient.
     *
     * @return The guest info
     */
    public GuestInfo getGuestInfo() {
        return guestInfo;
    }

    /**
     * Gets whether this share has a single or multiple targets.
     *
     * @return <code>true</code> a single target is contained. The
     * result is equivalent to <code>createdShare.size() == 1</code>.
     */
    public boolean hasSingleTarget() {
        return shareInfos.size() == 1;
    }

    /**
     * Gets the first of all share targets. If this is a single-target
     * share, that target is returned.
     *
     * @return The first target.
     */
    public ShareTarget getFirstTarget() {
        return shareInfos.get(0).getShare().getTarget();
    }

    /**
     * Gets an iterable of all contained targets.
     *
     * @return The iterable
     */
    public Iterable<ShareTarget> getTargets() {
        List<ShareTarget> targets = new ArrayList<>(shareInfos.size());
        for (ShareInfo info : shareInfos) {
            targets.add(info.getShare().getTarget());
        }
        return targets;
    }

    /**
     * Gets the share info of the first contained share. If this is a single-target
     * share, the single share info instance is returned.
     *
     * @return The first share info
     */
    public ShareInfo getFirstInfo() {
        return shareInfos.get(0);
    }

    /**
     * Gets an iterable of all contained share infos.
     *
     * @return The iterable
     */
    public Iterable<ShareInfo> getInfos() {
        return Collections.unmodifiableList(shareInfos);
    }

    /**
     * Gets the token for this share. If the only a single target is contained, the
     * absolute token addressing this target is returned. Otherwise only the guest
     * users base token is returned. If the share recipient is an internal entity
     * (i.e. a user or group), <code>null</code> is returned.
     *
     * @return The token
     */
    public String getToken() {
        if (recipient.isInternal()) {
            return null;
        }

        if (hasSingleTarget()) {
            return getFirstInfo().getToken();
        }

        return guestInfo.getBaseToken();
    }

    /**
     * Gets the URL to this share. If the recipient is a guest and this share has a single target,
     * the URL points to that target using the absolute share token. If the recipient is a guest
     * and this share has multiple targets, the URL is constructed with the guest users base token.
     * If the recipient is an internal user or group, the URL points to the first target, ignoring
     * whether this share has multiple targets or not. The latter behavior is subject to change in
     * the future.
     *
     * @param requestContext
     * @return The URL
     */
    public String getUrl(RequestContext requestContext) {
        if (recipient.isInternal()) {
            // TODO: no handling for multi-target shares yet
            return ShareLinks.generateInternal(requestContext, getFirstTarget());
        }

        return ShareLinks.generateExternal(requestContext, getToken());
    }

}
