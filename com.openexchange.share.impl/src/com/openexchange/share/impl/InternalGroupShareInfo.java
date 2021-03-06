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

import java.util.Date;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link InternalGroupShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class InternalGroupShareInfo extends AbstractShareInfo {

    final int contextID;
    final Group group;

    /**
     * Initializes a new {@link InternalGroupShareInfo}.
     *
     * @param contextID The context identifier
     * @param group The group
     * @param srcTarget The share target from the sharing users point of view
     * @param dstTarget The share target from the recipients point of view
     * @param includeSubfolders Whether sub-folders should be included in case the target is a infostore folder
     */
    public InternalGroupShareInfo(int contextID, Group group, ShareTarget srcTarget, ShareTarget dstTarget, boolean includeSubfolders) {
        super(srcTarget, dstTarget, includeSubfolders);
        this.contextID = contextID;
        this.group = group;
    }

    @Override
    public String getShareURL(HostData hostData) throws OXException {
        return ShareLinks.generateInternal(hostData, getDestinationTarget());
    }

    @Override
    public GuestInfo getGuest() {
        /*
         * use special guest info for internal user
         */
        return new GuestInfo() {

            @Override
            public RecipientType getRecipientType() {
                return RecipientType.GROUP;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public int getGuestID() {
                return group.getIdentifier();
            }

            @Override
            public String getEmailAddress() {
                return null;
            }

            @Override
            public String getDisplayName() {
                return group.getDisplayName();
            }

            @Override
            public int getCreatedBy() {
                return 0;
            }

            @Override
            public int getContextID() {
                return contextID;
            }

            @Override
            public String getBaseToken() {
                return null;
            }

            @Override
            public AuthenticationMode getAuthentication() {
                return null;
            }

            @Override
            public ShareTarget getLinkTarget() {
                return null;
            }

            @Override
            public Date getExpiryDate() {
                return null;
            }

            @Override
            public String generateLink(HostData hostData, ShareTargetPath targetPath) throws OXException {
                return ShareLinks.generateInternal(hostData, getDestinationTarget());
            }
        };
    }

}