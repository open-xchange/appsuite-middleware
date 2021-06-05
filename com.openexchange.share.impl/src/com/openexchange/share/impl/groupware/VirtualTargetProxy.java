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

package com.openexchange.share.impl.groupware;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.groupware.AbstractTargetProxy;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.groupware.TargetProxyType;
import com.openexchange.share.groupware.VirtualTargetProxyType;


/**
 * {@link VirtualTargetProxy} - A {@link TargetProxy} for non groupware modules aka. third party plugins like e.g. messenger. This
 * {@link TargetProxy} only contains the minimum set of infos.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class VirtualTargetProxy extends AbstractTargetProxy {

    private final String folderId;
    private final String item;
    private final String title;
    private final ShareTarget target;
    private final ShareTargetPath targetPath;

    public VirtualTargetProxy(int module, String folderId, String item, String title) {
        super();
        this.folderId = folderId;
        this.item = item;
        this.title = title;
        target = new ShareTarget(module, folderId, item);
        targetPath = new ShareTargetPath(module, folderId, item);
    }

    /**
     * Initializes a new {@link VirtualTargetProxy}.
     *
     * @param target The target
     */
    public VirtualTargetProxy(ShareTarget target) {
        this(target.getModule(), target.getFolder(), target.getItem(), getTitle(target));
    }

    @Override
    public String getID() {
        return item;
    }

    @Override
    public String getFolderID() {
        return folderId;
    }

    @Override
    public ShareTarget getTarget() {
        return target;
    }

    @Override
    public ShareTargetPath getTargetPath() {
        return targetPath;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<TargetPermission> getPermissions() {
        return Collections.emptyList();
    }

    @Override
    public void applyPermissions(List<TargetPermission> permissions) {
        //
    }

    @Override
    public void removePermissions(List<TargetPermission> permissions) {
        //
    }

    @Override
    public TargetProxyType getProxyType() {
        return VirtualTargetProxyType.getInstance();
    }

    private static String getTitle(ShareTarget target) {
        return null != target.getItem() ? target.getItem() : target.toString();
    }

    @Override
    public boolean mayAdjust() {
        return true;
    }

    @Override
    public Date getTimestamp() {
        return new Date();
    }

}
