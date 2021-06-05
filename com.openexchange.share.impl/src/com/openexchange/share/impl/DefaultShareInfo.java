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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.user.User;

/**
 * {@link DefaultShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultShareInfo extends AbstractShareInfo {

    private final DefaultGuestInfo guestInfo;
    private final ShareTargetPath targetPath;

    /**
     * Creates a list of extended share info objects for the supplied shares.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestUser The guest user
     * @param srcTarget The share target from the sharing users point of view
     * @param dstTarget The share target from the recipients point of view
     * @param targetPath The target path
     * @param includeSubfolders Whether sub-folders should be included in case the target is a infostore folder
     */
    public DefaultShareInfo(ServiceLookup services, int contextID, User guestUser, ShareTarget srcTarget, ShareTarget dstTarget, ShareTargetPath targetPath, boolean includeSubfolders) throws OXException {
        super(srcTarget, dstTarget, includeSubfolders);
        if (ShareTool.isAnonymousGuest(guestUser)) {
            this.guestInfo = new DefaultGuestInfo(services, contextID, guestUser, srcTarget);
        } else {
            this.guestInfo = new DefaultGuestInfo(services, contextID, guestUser, null);
        }
        this.targetPath = targetPath;
    }

    @Override
    public GuestInfo getGuest() {
        return guestInfo;
    }

    @Override
    public String getShareURL(HostData hostData) {
        return ShareLinks.generateExternal(hostData, guestInfo.getBaseToken(), targetPath);
    }

}
