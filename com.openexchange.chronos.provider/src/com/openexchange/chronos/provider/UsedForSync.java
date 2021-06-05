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

package com.openexchange.chronos.provider;

/**
 * {@link UsedForSync}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class UsedForSync {

    /**
     * The default {@link UsedForSync} value
     */
    public static final UsedForSync DEFAULT = new UsedForSync(true, false);
    /**
     * The {@link UsedForSync} value in case it is deactivated
     */
    public static final UsedForSync DEACTIVATED = new UsedForSync(false, true);

    /**
     * The {@link UsedForSync} value for always activated
     */
    public static final UsedForSync FORCED_ACTIVE = new UsedForSync(true, true);

    private final boolean value;
    private final boolean isProtected;
    
    /**
     * Initializes a new {@link UsedForSync}.
     * 
     * @param isUsedForSync Whether the folder is used for sync or not
     * @param isProtected Whether this field is protected or not
     */
    public UsedForSync(boolean isUsedForSync, boolean isProtected) {
        super();
        this.value = isUsedForSync;
        this.isProtected = isProtected;
    }

    /**
     * Whether the folder is used for sync or not
     * 
     * @return <code>true</code> if the folder is used for sync, <code>false</code> otherwise
     */
    public boolean isUsedForSync() {
        return value;
    }
    
    /**
     * Whether this value is protected
     *
     * @return <code>true</code> if this value is protected, <code>false</code> otherwise
     */
    public boolean isProtected() {
        return isProtected;
    }
    
    /**
     * Creates an unprotected {@link UsedForSync} object with the given value
     *
     * @param isUsedForSync Whether the folder is used for sync or not
     * @return The unprotected {@link UsedForSync} value
     */
    public static UsedForSync of(boolean isUsedForSync) {
        return new UsedForSync(isUsedForSync, false);
    }
    
}
