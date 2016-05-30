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

package com.openexchange.drive.events.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEvent;

/**
 * {@link DriveEventImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventImpl implements DriveEvent {

    private final int contextID;
    private final Set<String> folderIDs;
    private final boolean remote;
    private final String pushTokenReference;

    /**
     * Initializes a new {@link DriveEventImpl}.
     *
     * @param contextID the context ID
     * @param folderIDs The affected folder IDs
     * @param actions The client actions to execute
     * @param remote <code>true</code> it this event is 'remote', <code>false</code>, otherwise
     * @param pushTokenReference The push token of the device causing the event, or <code>null</code> if not applicable
     */
    public DriveEventImpl(int contextID, Set<String> folderIDs, boolean remote, String pushTokenReference) {
        super();
        this.contextID = contextID;
        this.folderIDs = folderIDs;
        this.remote = remote;
        this.pushTokenReference = pushTokenReference;
    }

    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public Set<String> getFolderIDs() {
        return folderIDs;
    }

    @Override
    public List<DriveAction<? extends DriveVersion>> getActions(List<String> rootFolderIDs) {
        List<DriveAction<? extends DriveVersion>> actions = new ArrayList<DriveAction<? extends DriveVersion>>(rootFolderIDs.size());
        for (String rootFolderID : rootFolderIDs) {
            if (folderIDs.contains(rootFolderID)) {
                actions.add(new SyncDirectoriesAction(rootFolderID));
            }
        }
        return actions;
    }

    @Override
    public boolean isRemote() {
        return remote;
    }

    @Override
    public String getPushTokenReference() {
        return pushTokenReference;
    }

    @Override
    public String toString() {
        return "DriveEvent [remote=" + remote + ", contextID=" + contextID + ", folderIDs=" + folderIDs + ", pushToken=" + pushTokenReference + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextID;
        result = prime * result + ((folderIDs == null) ? 0 : folderIDs.hashCode());
        result = prime * result + ((pushTokenReference == null) ? 0 : pushTokenReference.hashCode());
        result = prime * result + (remote ? 1231 : 1237);
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
        if (!(obj instanceof DriveEventImpl)) {
            return false;
        }
        DriveEventImpl other = (DriveEventImpl) obj;
        if (contextID != other.contextID) {
            return false;
        }
        if (folderIDs == null) {
            if (other.folderIDs != null) {
                return false;
            }
        } else if (!folderIDs.equals(other.folderIDs)) {
            return false;
        }
        if (pushTokenReference == null) {
            if (other.pushTokenReference != null) {
                return false;
            }
        } else if (!pushTokenReference.equals(other.pushTokenReference)) {
            return false;
        }
        if (remote != other.remote) {
            return false;
        }
        return true;
    }

}
