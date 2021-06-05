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
 * {@link Permission} - Simple container for a permission.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Permission {

    public final int entity;

    public final int fuid;

    public final int fp;

    public final int orp;

    public final int owp;

    public final int odp;

    public final boolean admin;

    /**
     * Initializes a new {@link Permission}
     *
     * @param entity The entity ID
     * @param fuid The folder ID
     * @param fp The folder permission
     * @param orp The object-read permission
     * @param owp The object-write permission
     * @param odp The object-delete permission
     * @param admin <code>true</code> if admin; otherwise <code>false</code>
     */
    public Permission(final int entity, final int fuid, final int fp, final int orp, final int owp, final int odp, final boolean admin) {
        super();
        this.entity = entity;
        this.fuid = fuid;
        this.admin = admin;
        this.fp = fp;
        this.odp = odp;
        this.orp = orp;
        this.owp = owp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("Entity=").append(entity).append(", Folder=").append(fuid).append('\n');
        sb.append("fp=").append(fp).append(", orp=").append(orp).append(", owp=").append(owp).append(", odp=").append(odp).append(
            ", admin=").append(admin).append('\n');
        return sb.toString();
    }

}
