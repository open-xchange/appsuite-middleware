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

package com.openexchange.drive.impl.sync;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveQuota;
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
    private final DriveQuota quota;
    private final String pathToRoot;

    /**
     * 
     * Initializes a new {@link DefaultSyncResult}.
     * 
     * @param actionsForClient The resulting actions for the client
     * @param diagnosticsLog The diagnostics log
     */
    public DefaultSyncResult(List<AbstractAction<T>> actionsForClient, String diagnosticsLog) {
        this(actionsForClient, diagnosticsLog, null, null);
    }

    /**
     * Initializes a new {@link DefaultSyncResult}.
     *
     * @param actionsForClient The resulting actions for the client
     * @param diagnosticsLog The diagnostics log
     * @param quota The quota information
     * @param pathToRoot The sync'ed root folder's path to the internal root folder
     */
    public DefaultSyncResult(List<AbstractAction<T>> actionsForClient, String diagnosticsLog, DriveQuota quota, String pathToRoot) {
        super();
        this.actionsForClient = new ArrayList<DriveAction<T>>(actionsForClient.size());
        for (DriveAction<? extends T> driveAction : actionsForClient) {
            this.actionsForClient.add((DriveAction<T>) driveAction);
        }
        this.diagnosticsLog = diagnosticsLog;
        this.quota = quota;
        this.pathToRoot = pathToRoot;
    }

    @Override
    public List<DriveAction<T>> getActionsForClient() {
        return actionsForClient;
    }

    @Override
    public String getDiagnostics() {
        return diagnosticsLog;
    }

    @Override
    public DriveQuota getQuota() {
        return quota;
    }

    @Override
    public String getPathToRoot() {
        return pathToRoot;
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
