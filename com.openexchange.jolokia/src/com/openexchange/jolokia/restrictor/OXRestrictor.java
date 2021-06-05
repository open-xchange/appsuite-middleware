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

package com.openexchange.jolokia.restrictor;

import javax.management.ObjectName;
import org.jolokia.restrictor.AllowAllRestrictor;
import org.jolokia.util.RequestType;

/**
 * 
 * Based on {@link AllowAllRestrictor}
 * Only write access is denied by {@link OXRestrictor}.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.8.3
 */
public class OXRestrictor extends AllowAllRestrictor {

    /**
     * If a remote access is allowed to perform action or not
     */
    private boolean remoteAccessAllowed = false;

    // Munin specific
    private static final String MEMORYCACHECOUNT = "getMemoryCacheCount";
    private static final String CACHINGOBJECTNAME = "com.openexchange.caching:name=JCSCacheInformation";

    public OXRestrictor() {
        super();
    }

    /**
     * Set if remote access is allowed.
     * Initializes a new {@link OXRestrictor}.
     * 
     * @param remoteAccessAllowed <code>false</code> if only localhost should access, <code>true</code> otherwise.
     */
    public OXRestrictor(boolean remoteAccessAllowed) {
        this();
        this.remoteAccessAllowed = remoteAccessAllowed;
    }

    @Override
    public boolean isAttributeWriteAllowed(ObjectName pName, String pAttribute) {
        // No write allowed at any time
        return false;
    }

    @Override
    public boolean isRemoteAccessAllowed(String... pHostOrAddress) {
        // Remote access is only allowed if it was configured in 'jolokia.propertis'
        if (false == remoteAccessAllowed) {
            return false;
        }
        return super.isRemoteAccessAllowed(pHostOrAddress);
    }

    @Override
    public boolean isTypeAllowed(RequestType pType) {
        switch (pType) {
            // Allow
            case READ:
            case LIST:
            case VERSION:
            case SEARCH:
                return true;
            // Access denied
            case WRITE:
            case EXEC:
            case REGNOTIF: // Unsupported by Jolokia
            case REMNOTIF: // Unsupported by Jolokia
            default:
                return false;
        }
    }

    @Override
    public boolean isOperationAllowed(ObjectName pName, String pOperation) {
        // Look up munin script specific case.

        if (MEMORYCACHECOUNT.equals(pOperation) && (pName != null) && CACHINGOBJECTNAME.equals(pName.getCanonicalName())) {
            return true;
        }
        return false;
    }
}
