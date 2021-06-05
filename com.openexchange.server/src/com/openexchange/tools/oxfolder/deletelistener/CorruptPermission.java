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

package com.openexchange.tools.oxfolder.deletelistener;

/**
 * {@link CorruptPermission} - Simple container for a corrupt permission entry.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CorruptPermission {

    public final int cid;

    public final int fuid;

    public final int permission_id;

    /**
     * Creates a new {@link CorruptPermission}
     *
     * @param cid The context ID
     * @param fuid The folder ID
     * @param permission_id The permission entity's ID
     */
    public CorruptPermission(final int cid, final int fuid, final int permission_id) {
        super();
        this.cid = cid;
        this.fuid = fuid;
        this.permission_id = permission_id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("cid=").append(cid).append(", fuid=").append(fuid).append(", permission_id=").append(permission_id);
        return sb.toString();
    }
}
