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

package com.openexchange.share.impl.groupware;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.openexchange.share.groupware.TargetPermission;
import com.openexchange.share.groupware.TargetProxy;


/**
 * {@link AbstractTargetProxy}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractTargetProxy implements TargetProxy {

    private boolean modified = false;
    private boolean touched = false;

    @Override
    public boolean wasModified() {
        return modified;
    }

    protected void setModified() {
        modified = true;
    }

    @Override
    public void touch() {
        touched = true;
    }

    @Override
    public boolean wasTouched() {
        return touched;
    }

    protected static interface PermissionConverter<T> {

        int getEntity(T permission);

        boolean isGroup(T permission);

        int getBits(T permission);

        boolean isSystem(T permission);

        T convert(TargetPermission permission);

        TargetPermission convert(T permission);

    }

    protected static <T> List<T> removePermissions(List<T> origPermissions, List<TargetPermission> toRemove, PermissionConverter<T> converter) {
        if (origPermissions == null || origPermissions.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> newPermissions = new ArrayList<T>(origPermissions);
        Iterator<T> it = newPermissions.iterator();
        while (it.hasNext()) {
            T permission = it.next();
            for (TargetPermission removable : toRemove) {
                if (converter.isGroup(permission) == removable.isGroup() && converter.getEntity(permission) == removable.getEntity()) {
                    it.remove();
                    break;
                }
            }
        }

        return newPermissions;
    }

    protected static <T> List<T> mergePermissions(List<T> origPermissions, List<TargetPermission> permissions, PermissionConverter<T> converter) {
        if (origPermissions == null) {
            origPermissions = Collections.emptyList();
        }

        List<T> newPermissions = new ArrayList<T>(origPermissions.size() + permissions.size());
        if (origPermissions.isEmpty()) {
            for (TargetPermission permission : permissions) {
                newPermissions.add(converter.convert(permission));
            }
        } else {
            ListMultimap<Integer, T> permissionsByUser = ArrayListMultimap.create();
            ListMultimap<Integer, T> permissionsByGroup = ArrayListMultimap.create();
            for (T permission : origPermissions) {
                if (converter.isGroup(permission)) {
                    permissionsByGroup.put(converter.getEntity(permission), permission);
                } else {
                    permissionsByUser.put(converter.getEntity(permission), permission);
                }
            }

            /*
             * Keep potentially modified permissions
             */
            for (TargetPermission permission : permissions) {
                List<T> removed;
                if (permission.isGroup()) {
                    removed = permissionsByGroup.removeAll(permission.getEntity());
                } else {
                    removed = permissionsByUser.removeAll(permission.getEntity());
                }

                newPermissions.add(converter.convert(permission));
                for (T r : removed) {
                    if (converter.isSystem(r)) {
                        newPermissions.add(r);
                    }
                }
            }

            /*
             * Add new permissions
             */
            for (T permission : permissionsByUser.values()) {
                newPermissions.add(permission);
            }

            for (T permission : permissionsByGroup.values()) {
                newPermissions.add(permission);
            }
        }

        return newPermissions;
    }

}
