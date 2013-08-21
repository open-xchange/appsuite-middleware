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
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.actions.AbstractAction;
import com.openexchange.java.StringAllocator;


/**
 * {@link IntermediateSyncResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class IntermediateSyncResult<T extends DriveVersion> {

    private final List<AbstractAction<T>> actionsForServer;
    private final List<AbstractAction<T>> actionsForClient;

    public IntermediateSyncResult(List<AbstractAction<T>> actionsForServer, List<AbstractAction<T>> actionsForClient) {
        super();
        this.actionsForClient = actionsForClient;
        this.actionsForServer = actionsForServer;
    }

    public IntermediateSyncResult() {
        this(new ArrayList<AbstractAction<T>>(), new ArrayList<AbstractAction<T>>());
    }

    public void addActionForClient(AbstractAction<T> action) {
        actionsForClient.add(action);
    }

    public void addActionForServer(AbstractAction<T> action) {
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
    public List<AbstractAction<T>> getActionsForServer() {
        return actionsForServer;
    }

    /**
     * Gets the actionsForClient
     *
     * @return The actionsForClient
     */
    public List<AbstractAction<T>> getActionsForClient() {
        return actionsForClient;
    }

    @Override
    public String toString() {
        StringAllocator stringAllocator = new StringAllocator();
        if (null != actionsForServer) {
            stringAllocator.append("Actions for server:\n");
            for (AbstractAction<T> action : actionsForServer) {
                stringAllocator.append("  ").append(action).append('\n');
            }
        }
        if (null != actionsForClient) {
            stringAllocator.append("Actions for client:\n");
            for (AbstractAction<T> action : actionsForClient) {
                stringAllocator.append("  ").append(action).append('\n');
            }
        }
        return stringAllocator.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionsForClient == null) ? 0 : actionsForClient.hashCode());
        result = prime * result + ((actionsForServer == null) ? 0 : actionsForServer.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IntermediateSyncResult)) {
            return false;
        }
        IntermediateSyncResult other = (IntermediateSyncResult) obj;
        if (actionsForClient == null) {
            if (other.actionsForClient != null) {
                return false;
            }
        } else if (!actionsForClient.equals(other.actionsForClient)) {
            return false;
        }
        if (actionsForServer == null) {
            if (other.actionsForServer != null) {
                return false;
            }
        } else if (!actionsForServer.equals(other.actionsForServer)) {
            return false;
        }
        return true;
    }

}
