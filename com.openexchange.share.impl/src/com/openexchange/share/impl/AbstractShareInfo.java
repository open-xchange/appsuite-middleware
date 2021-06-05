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

package com.openexchange.share.impl;

import com.openexchange.java.Strings;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.SubfolderAwareShareInfo;

/**
 * {@link AbstractShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public abstract class AbstractShareInfo implements SubfolderAwareShareInfo {

    private final ShareTarget srcTarget;
    private final ShareTarget dstTarget;
    private final boolean includeSubfolders;

    /**
     * Initializes a new {@link AbstractShareInfo}.
     *
     * @param srcTarget The share target from the sharing users point of view
     * @param dstTarget The share target from the recipients point of view
     * @param includeSubfolders Whether sub-folders should be included in case the target is a infostore folder
     */
    protected AbstractShareInfo(ShareTarget srcTarget, ShareTarget dstTarget, boolean includeSubfolders) {
        super();
        this.srcTarget = srcTarget;
        this.dstTarget = dstTarget;
        this.includeSubfolders = includeSubfolders;
    }

    @Override
    public ShareTarget getTarget() {
        return srcTarget;
    }

    @Override
    public ShareTarget getDestinationTarget() {
        return dstTarget;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Share");
        ShareTarget target = getTarget();
        if (null != target) {
            stringBuilder.append(" [module=").append(target.getModule()).append(", folder=").append(target.getFolder());
            if (null != target.getItem()) {
                stringBuilder.append(", item=").append(target.getItem());
            }
            stringBuilder.append(']');
        }
        GuestInfo guest = getGuest();
        if (null != guest) {
            stringBuilder.append(" [recipient=").append(guest.getRecipientType()).append(", id=").append(guest.getGuestID()).append(", context=").append(guest.getContextID());
            if (Strings.isNotEmpty(guest.getEmailAddress())) {
                stringBuilder.append(", email=").append(guest.getEmailAddress());
            }
            if (Strings.isNotEmpty(guest.getDisplayName())) {
                stringBuilder.append(", name=").append(guest.getDisplayName());
            }
            if (0 < guest.getCreatedBy()) {
                stringBuilder.append(", createdBy=").append(guest.getCreatedBy());
            }
            if (Strings.isNotEmpty(guest.getBaseToken())) {
                stringBuilder.append(", token=").append(guest.getBaseToken());
            }
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean isIncludeSubfolders() {
        return includeSubfolders;
    }

}
