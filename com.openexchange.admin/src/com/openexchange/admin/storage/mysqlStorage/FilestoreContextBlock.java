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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.Map;

/**
 * Combines the context file store information for a specific database server and a certain file store. Collects across all schemas of that
 * database server the file store usage for the given file store.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
class FilestoreContextBlock {

    private static final class IntPair {

        private final int i1;
        private final int i2;
        private final int hash;

        IntPair(int i1, int i2) {
            super();
            this.i1 = i1;
            this.i2 = i2;

            int result = 31 * 1 + i1;
            result = 31 * result + i2;
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof IntPair)) {
                return false;
            }
            IntPair other = (IntPair) obj;
            if (i1 != other.i1) {
                return false;
            }
            if (i2 != other.i2) {
                return false;
            }
            return true;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public final int writeDBPoolID;
    public final String schema;
    public final int filestoreID;

    public final Map<Integer, FilestoreInfo> contextFilestores = new HashMap<Integer, FilestoreInfo>();
    public final Map<Integer, Map<Integer, FilestoreInfo>> userFilestores = new HashMap<Integer, Map<Integer, FilestoreInfo>>();

    public FilestoreContextBlock(int writeDBPoolID, String schema, int filestoreID) {
        super();
        this.writeDBPoolID = writeDBPoolID;
        this.schema = schema;
        this.filestoreID = filestoreID;
    }

    public boolean isEmpty() {
        return contextFilestores.isEmpty() && userFilestores.isEmpty();
    }

    public int sizeForContext() {
        return contextFilestores.size();
    }

    public void addForContext(FilestoreInfo newInfo) {
        contextFilestores.put(I(newInfo.contextID), newInfo);
    }

    public void updateForContext(int contextID, final long usage) {
        final FilestoreInfo info = contextFilestores.get(I(contextID));
        if (info != null) {
            info.usage = usage;
        }
        // The schema may contain contexts having the files stored in another file store.
    }

    public int sizeFoUser() {
        return userFilestores.size();
    }

    public void addForUser(FilestoreInfo newInfo) {
        Map<Integer, FilestoreInfo> users = userFilestores.get(I(newInfo.contextID));
        if (null == users) {
            users = new HashMap<Integer, FilestoreInfo>();
            userFilestores.put(I(newInfo.contextID), users);
        }

        users.put(I(newInfo.userID), newInfo);
    }

    public void updateForUser(int contextID, int userID, long usage) {
        Map<Integer, FilestoreInfo> users = userFilestores.get(I(contextID));
        if (null != users) {
            FilestoreInfo info = users.get(I(userID));
            if (info != null) {
                info.usage = usage;
            }
        }
    }

    @Override
    public String toString(){
        return "["+filestoreID+"] Elements: " + (sizeForContext() + sizeFoUser()) + ", writepoolID: " + writeDBPoolID;
    }
}
