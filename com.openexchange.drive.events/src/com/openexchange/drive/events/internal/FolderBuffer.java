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

package com.openexchange.drive.events.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.session.Session;

/**
 * {@link FolderBuffer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FolderBuffer {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(FolderBuffer.class);

    private final int consolidationTime;
    private final int maxDelayTime ;
    private final int defaultDelayTime;
    private final int contextID;

    private Set<String> folderIDs;
    private long lastEventTime;
    private long firstEventTime;

    /**
     * Initializes a new {@link FolderBuffer}.
     *
     * @param contextID The context ID
     * @param consolidationTime The consolidation time after which the buffer is considered to be ready to publish if no further
     *                          folders were added
     * @param maxDelayTime The maximum time after which the buffer is considered to be ready to publish, independently of the
     *                     consolidation interval
     * @param defaultDelayTime The (minimum) default delay time to wait after the first folder was added before being ready to publish
     */
    public FolderBuffer(int contextID, int consolidationTime, int maxDelayTime, int defaultDelayTime) {
        super();
        this.contextID = contextID;
        this.consolidationTime = consolidationTime;
        this.maxDelayTime = maxDelayTime;
        this.defaultDelayTime = defaultDelayTime;
    }

    /**
     * Gets a value indicating whether this buffer is ready to publish or not, based on the configured delay- and consolidation times.
     *
     * @return <code>true</code> if the event is due, <code>false</code>, otherwise
     */
    public synchronized boolean isReady() {
        if (null == folderIDs) {
            return false; // no event added yet
        }
        long now = System.currentTimeMillis();
        long timeSinceFirstEvent = now - firstEventTime;
        if (LOG.isTraceEnabled()) {
            LOG.trace("isDue(): now=" + now + ", firstEventTime=" + firstEventTime + ", lastEventTime=" + lastEventTime +
                ", timeSinceFirstEvent=" + timeSinceFirstEvent + ", timeSinceLastEvent=" + (now - lastEventTime));
        }
        if (timeSinceFirstEvent > maxDelayTime) {
            return true; // max delay time exceeded
        }
        if (timeSinceFirstEvent > defaultDelayTime && now - lastEventTime > consolidationTime) {
            return true; // consolidation time since last event passed, and default delay time exceeded
        }
        return false;
    }

    /**
     * Gets the context ID.
     *
     * @return The context ID
     */
    public int getContexctID() {
        return this.contextID;
    }

    public synchronized void add(Session session, String folderID, List<String> folderPath) {
        if (session.getContextId() != this.contextID) {
            throw new IllegalArgumentException("session not in this context");
        }
        /*
         * prepare access
         */
        lastEventTime = System.currentTimeMillis();
        if (null == folderIDs) {
            firstEventTime = lastEventTime;
            folderIDs = new HashSet<String>();
        }
        /*
         * add folder and all parent folders, resolve to root if not already known
         */
        if (folderIDs.add(folderID)) {
            folderIDs.addAll(null != folderPath ? folderPath : resolveToRoot(folderID, session));
        }
    }

    public void add(Session session, String folderID) {
        add(session, folderID, null);
    }

    public Set<String> getFolderIDs() {
        return folderIDs;
    }

    private static List<String> resolveToRoot(String folderID, Session session) {
        List<String> folderIDs = new ArrayList<String>();
        try {
            IDBasedFolderAccess folderAccess = DriveEventServiceLookup.getService(IDBasedFolderAccessFactory.class).createAccess(session);
            FileStorageFolder[] path2DefaultFolder = folderAccess.getPath2DefaultFolder(folderID);
            for (FileStorageFolder folder : path2DefaultFolder) {
                folderIDs.add(folder.getId());
            }
        } catch (OXException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error resolving path to rootfolder from event", e);
            }
        }
        return folderIDs;
    }

}
