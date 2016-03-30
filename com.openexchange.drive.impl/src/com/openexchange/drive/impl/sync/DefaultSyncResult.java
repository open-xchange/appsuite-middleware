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

package com.openexchange.drive.impl.sync;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.SyncResult;
import com.openexchange.drive.impl.actions.AbstractAction;


/**
 * {@link DefaultSyncResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DefaultSyncResult<T extends DriveVersion> implements SyncResult<T> {

    private final List<DriveAction<T>> actionsForClient;
    private final String diagnosticsLog;

    /**
     * Initializes a new {@link DefaultSyncResult}.
     *
     * @param actionsForClient The resulting actions for the client
     * @param diagnosticsLog The diagnostics log
     */
    public DefaultSyncResult(List<AbstractAction<T>> actionsForClient, String diagnosticsLog) {
        super();
        this.actionsForClient = new ArrayList<DriveAction<T>>(actionsForClient.size());
        for (DriveAction<? extends T> driveAction : actionsForClient) {
            this.actionsForClient.add((DriveAction<T>) driveAction);
        }
        this.diagnosticsLog = diagnosticsLog;
    }

    /**
     * Gets the resulting actions for the client.
     *
     * @return The resulting actions for the client
     */
    @Override
    public List<DriveAction<T>> getActionsForClient() {
        return actionsForClient;
    }

    /**
     * Gets the diagnostics log.
     *
     * @return The diagnostics log, or <code>null</code> if not available
     */
    @Override
    public String getDiagnostics() {
        return diagnosticsLog;
    }

    @Override
    public String toString() {
        StringBuilder StringBuilder = new StringBuilder();
        if (null != actionsForClient) {
            StringBuilder.append("Actions for client:\n");
            for (DriveAction<T> action : actionsForClient) {
                StringBuilder.append("  ").append(action).append('\n');
            }
        }
        return StringBuilder.toString();
    }

}
