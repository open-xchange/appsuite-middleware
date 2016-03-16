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

package com.openexchange.share.impl.cleanup;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.user.UserService;

/**
 * {@link ContextCleanupTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ContextCleanupTask extends AbstractTask<List<GuestCleanupTask>> {

    private static final Logger LOG = LoggerFactory.getLogger(ContextCleanupTask.class);

    protected final ServiceLookup services;
    protected final int contextID;
    protected final long guestExpiry;

    /**
     * Initializes a new {@link ContextCleanupTask}.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestExpiry the timespan (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public ContextCleanupTask(ServiceLookup services, int contextID, long guestExpiry) {
        super();
        this.services = services;
        this.contextID = contextID;
        this.guestExpiry = guestExpiry;
    }

    @Override
    public List<GuestCleanupTask> call() throws Exception {
        try {
            return cleanContext();
        } catch (OXException e) {
            if ("CTX-0002".equals(e.getErrorCode())) {
                LOG.debug("Context {} no longer found, cancelling cleanup.", contextID, e);
                return Collections.emptyList();
            }
            throw e;
        }
    }

    private List<GuestCleanupTask> cleanContext() throws OXException {
        /*
         * Check if context needs update
         */
        Updater updater = Updater.getInstance();
        UpdateStatus status = updater.getStatus(contextID);
        if (status.needsBackgroundUpdates() || status.needsBlockingUpdates() || status.backgroundUpdatesRunning() || status.blockingUpdatesRunning()) {
            LOG.info("Context {} needs update, skipping cleanup task.", I(contextID));
            return Collections.emptyList();
        }

        /*
         * gather guest users in context
         */
        int[] guestIDs = services.getService(UserService.class).listAllUser(contextID, true, true);
        if (null == guestIDs || 0 == guestIDs.length) {
            LOG.debug("No guest users found in context {}, skipping cleanup task.", I(contextID));
            return Collections.emptyList();
        }
        LOG.debug("Found {} guest users in context {}, preparing corresponding cleanup tasks.", I(guestIDs.length), I(contextID));
        List<GuestCleanupTask> cleanupTasks = new ArrayList<GuestCleanupTask>(guestIDs.length);
        for (int guestID : guestIDs) {
            cleanupTasks.add(new GuestCleanupTask(services, contextID, guestID, guestExpiry));
        }
        return cleanupTasks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextID;
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
        if (!(obj instanceof ContextCleanupTask)) {
            return false;
        }
        ContextCleanupTask other = (ContextCleanupTask) obj;
        if (contextID != other.contextID) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ContextCleanupTask [contextID=" + contextID + "]";
    }

}
