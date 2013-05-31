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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.sync;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.java.StringAllocator;


/**
 * {@link SyncResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncResult<T extends DriveVersion> {

    private final List<DriveAction<T>> actionsForServer;
    private final List<DriveAction<T>> actionsForClient;

    public SyncResult(List<DriveAction<T>> actionsForServer, List<DriveAction<T>> actionsForClient) {
        super();
        this.actionsForClient = actionsForClient;
        this.actionsForServer = actionsForServer;
    }

    public SyncResult() {
        this(new ArrayList<DriveAction<T>>(), new ArrayList<DriveAction<T>>());
    }

    public void addActionForClient(DriveAction<T> action) {
        actionsForClient.add(action);
    }

    public void addActionForServer(DriveAction<T> action) {
        actionsForServer.add(action);
    }

    public boolean isEmpty() {
        return (null == actionsForServer || 0 == actionsForServer.size()) && (null == actionsForClient || 0 == actionsForClient.size());
    }

    /**
     * Gets the actionsForServer
     *
     * @return The actionsForServer
     */
    public List<DriveAction<T>> getActionsForServer() {
        return actionsForServer;
    }

    /**
     * Gets the actionsForClient
     *
     * @return The actionsForClient
     */
    public List<DriveAction<T>> getActionsForClient() {
        return actionsForClient;
    }

    @Override
    public String toString() {
        StringAllocator stringAllocator = new StringAllocator();
        if (null != actionsForServer) {
            stringAllocator.append("Actions for server:\n");
            for (DriveAction<T> action : actionsForServer) {
                stringAllocator.append("  ").append(action).append('\n');
            }
        }
        if (null != actionsForClient) {
            stringAllocator.append("Actions for client:\n");
            for (DriveAction<T> action : actionsForClient) {
                stringAllocator.append("  ").append(action).append('\n');
            }
        }
        return stringAllocator.toString();
    }
}
