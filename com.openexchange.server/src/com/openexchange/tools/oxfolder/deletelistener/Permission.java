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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
