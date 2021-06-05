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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.ShareLink;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.SubfolderAwareShareInfo;

/**
 * {@link DefaultShareLink}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class DefaultShareLink implements ShareLink {

    private final ShareInfo delegate;
    private final Date timestamp;
    private final boolean isNew;

    /**
     * Initializes a new {@link DefaultShareLink}.
     *
     * @param delegate The underlying share link
     * @param timestamp The target timestamp
     * @param isNew <code>true</code> if the link was just created, <code>false</code>, otherwise
     */
    public DefaultShareLink(ShareInfo delegate, Date timestamp, boolean isNew) {
        super();
        this.delegate = delegate;
        this.timestamp = timestamp;
        this.isNew = isNew;
    }

    @Override
    public ShareTarget getTarget() {
        return delegate.getTarget();
    }

    @Override
    public ShareTarget getDestinationTarget() {
        return delegate.getDestinationTarget();
    }

    @Override
    public GuestInfo getGuest() {
        return delegate.getGuest();
    }

    @Override
    public String getShareURL(HostData hostData) throws OXException {
        return delegate.getShareURL(hostData);
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public boolean isIncludeSubfolders() {
        return delegate instanceof SubfolderAwareShareInfo ? ((SubfolderAwareShareInfo) delegate).isIncludeSubfolders() : false;
    }

}
