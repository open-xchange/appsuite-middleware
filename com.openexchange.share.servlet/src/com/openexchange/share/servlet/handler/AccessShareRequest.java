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

package com.openexchange.share.servlet.handler;

import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.TargetProxy;

/**
 * Encapsulates data of requests targeting the share servlet.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AccessShareRequest {

    private final GuestInfo guest;

    private final ShareTargetPath targetPath;

    private final TargetProxy targetProxy;

    private final boolean invalidTarget;

    /**
     * Initializes a new {@link AccessShareRequest}.
     * @param guest
     * @param targetPath
     * @param targetProxy
     * @param invalidTarget
     */
    public AccessShareRequest(GuestInfo guest, ShareTargetPath targetPath, TargetProxy targetProxy, boolean invalidTarget) {
        super();
        this.guest = guest;
        this.targetPath = targetPath;
        this.targetProxy = targetProxy;
        this.invalidTarget = invalidTarget;
    }

    public GuestInfo getGuest() {
        return guest;
    }

    public ShareTargetPath getTargetPath() {
        return targetPath;
    }

    public ShareTarget getTarget() {
        return null != targetProxy ? targetProxy.getTarget() : null;
    }

    public TargetProxy getTargetProxy() {
        return targetProxy;
    }

    public boolean isInvalidTarget() {
        return invalidTarget;
    }

}
