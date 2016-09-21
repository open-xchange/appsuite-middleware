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
        if (pOperation.equals(MEMORYCACHECOUNT) && pName.getCanonicalName().equals(CACHINGOBJECTNAME)) {
            return true;
        }
        return false;
    }
}
