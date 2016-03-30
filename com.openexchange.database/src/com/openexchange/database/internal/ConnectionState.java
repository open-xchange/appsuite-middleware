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

package com.openexchange.database.internal;

/**
 * {@link ConnectionState} - Tracks a connection state.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class ConnectionState {

    private boolean usedAsRead;
    private boolean usedForUpdate;
    private boolean updateCommitted;

    /**
     * Initializes a new {@link ConnectionState}.
     *
     * @param usedAsRead <code>true</code> if a read-write connection has been intentionally used for reading; otherwise <code>false</code>
     */
    public ConnectionState(boolean usedAsRead) {
        super();
        this.usedAsRead = usedAsRead;
        usedForUpdate = false;
        updateCommitted = false;
    }

    /**
     * Checks if a read-write connection has been intentionally used for reading.
     *
     * @return <code>true</code> if a read-write connection has been intentionally used for reading; otherwise <code>false</code>
     */
    public boolean isUsedAsRead() {
        return usedAsRead;
    }

    /**
     * Sets if a read-write connection has been intentionally used for reading.
     *
     * @param usedAsRead <code>true</code> if a read-write connection has been intentionally used for reading; otherwise <code>false</code>
     */
    public void setUsedAsRead(boolean usedAsRead) {
        this.usedAsRead = usedAsRead;
    }

    /**
     * Checks if associated connection has been used for a data modification operation; such as <code>INSERT</code>, <code>UPDATE</code>, <code>DELETE</code>.
     *
     * @return <code>true</code> for a data modification operation; otherwise <code>false</code>
     */
    public boolean isUsedForUpdate() {
        return usedForUpdate;
    }

    /**
     * Sets if associated connection has been used for a data modification operation; such as <code>INSERT</code>, <code>UPDATE</code>, <code>DELETE</code>.
     *
     * @param usedForUpdate <code>true</code> for a data modification operation; otherwise <code>false</code>
     */
    public void setUsedForUpdate(boolean usedForUpdate) {
        this.usedForUpdate = usedForUpdate;
    }

    /**
     * Checks if a <code>COMMIT</code> has been invoked on associated connection.
     *
     * @return <code>true</code> if a <code>COMMIT</code> has been invoked; otherwise <code>false</code>
     */
    public boolean isUpdateCommitted() {
        return updateCommitted;
    }

    /**
     * Sets if a <code>COMMIT</code> has been invoked on associated connection.
     *
     * @param updateCommitted <code>true</code> if a <code>COMMIT</code> has been invoked; otherwise <code>false</code>
     */
    public void setUpdateCommitted(boolean updateCommitted) {
        this.updateCommitted = updateCommitted;
    }

}
