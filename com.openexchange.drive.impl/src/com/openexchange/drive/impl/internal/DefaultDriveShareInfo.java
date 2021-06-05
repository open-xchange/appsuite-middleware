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

package com.openexchange.drive.impl.internal;

import com.openexchange.drive.DriveShareInfo;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.SubfolderAwareShareInfo;

/**
 * {@link DefaultDriveShareInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultDriveShareInfo implements DriveShareInfo, SubfolderAwareShareInfo {

    private final ShareInfo shareInfo;
    private final DriveShareTarget driveTarget;

    public DefaultDriveShareInfo(ShareInfo shareInfo, DriveShareTarget driveTarget) {
        super();
        this.shareInfo = shareInfo;
        this.driveTarget = driveTarget;
    }

    @Override
    public GuestInfo getGuest() {
        return shareInfo.getGuest();
    }

    @Override
    public String getShareURL(HostData hostData) throws OXException {
        return shareInfo.getShareURL(hostData);
    }

    @Override
    public DriveShareTarget getTarget() {
        return driveTarget;
    }

    @Override
    public ShareTarget getDestinationTarget() {
        return shareInfo.getDestinationTarget();
    }

    @Override
    public boolean isIncludeSubfolders() {
        return shareInfo instanceof SubfolderAwareShareInfo ? ((SubfolderAwareShareInfo) shareInfo).isIncludeSubfolders() : false;
    }

}
