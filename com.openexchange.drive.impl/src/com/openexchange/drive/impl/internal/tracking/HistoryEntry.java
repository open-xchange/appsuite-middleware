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

package com.openexchange.drive.impl.internal.tracking;

import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;

/**
 * {@link HistoryEntry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class HistoryEntry {

    private final IntermediateSyncResult<? extends DriveVersion> syncResult;
    private final int hashCode;
    private final String path;

    /**
     * Initializes a new {@link HistoryEntry}.
     *
     * @param syncResult The sync result
     * @param path The path where this sync result was resulting from, or <code>null</code> if not relevant
     */
    public HistoryEntry(IntermediateSyncResult<? extends DriveVersion> syncResult, String path) {
        this(syncResult, calculateHash(path, syncResult), path);
    }

    private HistoryEntry(IntermediateSyncResult<? extends DriveVersion> syncResult, int hashCode, String path) {
        super();
        this.syncResult = syncResult;
        this.path = path;
        this.hashCode = hashCode;
    }

    /**
     * Gets path where this sync result was resulting from.
     *
     * @return The path, or <code>null</code> if not relevant
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets a compact view of the history entry, i.e. an entry that does not contain the complete sync result, but preserves the
     * hash code for comparisons.
     *
     * @return A new, compact {@link HistoryEntry} instance
     */
    public HistoryEntry compact() {
        return new HistoryEntry(null, hashCode, path);
    }

    /**
     * Gets the syncResult
     *
     * @return The syncResult
     */
    public IntermediateSyncResult<? extends DriveVersion> getSyncResult() {
        return syncResult;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HistoryEntry)) {
            return false;
        }
        HistoryEntry other = (HistoryEntry) obj;
        return hashCode == other.hashCode;
    }

    @Override
    public String toString() {
        if (null == syncResult) {
            return String.valueOf(hashCode);
        } else {
            StringBuilder StringBuilder = new StringBuilder();
            StringBuilder.append(hashCode).append(" (").append(syncResult.toString().replace('\n', ' ').trim());
            return StringBuilder.append(')').toString();
        }
    }

    private static int calculateHash(String path, IntermediateSyncResult<? extends DriveVersion> syncResult) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((syncResult == null) ? 0 : syncResult.hashCode());
        return result;
    }

}
