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

package com.openexchange.server.impl;

/**
 *
 * {@link ComparedOCLFolderPermissions} is a helper class to calculate a diff of the ocl folder permissions on an update request.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ComparedOCLFolderPermissions extends ComparedOCLPermission<OCLPermission> {

    /**
     * Initializes a new {@link ComparedOCLFolderPermissions}.
     *
     * @param newPermissions The new permissions
     * @param originalPermissions The original permissions
     */
    public ComparedOCLFolderPermissions(OCLPermission[] newPermissions, OCLPermission[] originalPermissions) {
        super(newPermissions, originalPermissions);
        calc();
    }

    @Override
    protected boolean isSystemPermission(OCLPermission p) {
        return p.getSystem() != 0;
    }

    @Override
    protected boolean isGroupPermission(OCLPermission p) {
        return p.isGroupPermission();
    }

    @Override
    protected int getEntityId(OCLPermission p) {
        return p.getEntity();
    }

    @Override
    protected boolean areEqual(OCLPermission p1, OCLPermission p2) {
        if (p1 == null) {
            if (p2 == null) {
                return true;
            }

            return false;
        }

        if (p2 == null) {
            return false;
        }

        return p1.equals(p2);
    }
}
