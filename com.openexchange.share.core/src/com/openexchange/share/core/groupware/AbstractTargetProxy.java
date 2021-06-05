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

package com.openexchange.share.core.groupware;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.openexchange.i18n.Translator;
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
                    permissionsByGroup.put(I(converter.getEntity(permission)), permission);
                } else {
                    permissionsByUser.put(I(converter.getEntity(permission)), permission);
                }
            }

            /*
             * Keep potentially modified permissions
             */
            for (TargetPermission permission : permissions) {
                List<T> removed;
                if (permission.isGroup()) {
                    removed = permissionsByGroup.removeAll(I(permission.getEntity()));
                } else {
                    removed = permissionsByUser.removeAll(I(permission.getEntity()));
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
    
   
    @Override
    public String getLocalizedTitle(Translator translator) {
        return getTitle();
    }

}
