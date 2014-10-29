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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.impl.ConnectionHelper;

/**
 * {@link GuestCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class GuestCleaner {

    private static final Logger LOG = LoggerFactory.getLogger(GuestCleaner.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link GuestCleaner}.
     *
     * @param services A service lookup reference
     */
    public GuestCleaner(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Asynchronously cleans obsolete shares and corresponding guest user remnants for a context.
     *
     * @param contextID The context ID
     */
    public void scheduleContextCleanup(int contextID) throws OXException {
        ContextCleanupTask cleanupTask = new ContextCleanupTask(1, services, contextID);
        //TODO
    }

    /**
     * Asynchronously cleans obsolete shares and corresponding guest user remnants for specific guest users in a context.
     *
     * @param contextID The context ID
     * @param guestIDs The identifiers of the guest users to consider for cleanup
     */
    public void scheduleGuestCleanup(int contextID, int[] guestIDs) throws OXException {
        for (int guestID : guestIDs) {
            GuestCleanupTask cleanupTask = new GuestCleanupTask(1, services, contextID, guestID);
            //TODO
        }
    }

    /**
     * Synchronously cleans obsolete shares and corresponding guest user remnants for a context.
     *
     * @param connectionHelper A (started) connection helper
     * @param contextID The context ID
     */
    public void cleanupContext(ConnectionHelper connectionHelper, int contextID) throws OXException {
        /*
         * execute context- and resulting guest cleanup tasks in current thread
         */
        try {
            List<GuestCleanupTask> guestCleanupTasks = new ContextCleanupTask(0, services, connectionHelper, contextID).call();
            for (GuestCleanupTask guestCleanupTask : guestCleanupTasks) {
                guestCleanupTask.call();
            }
        } catch (Exception e) {
            if (OXException.class.isInstance(e)) {
                throw (OXException) e;
            }
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup");
        }
    }

    /**
     * Synchronously cleans obsolete shares and corresponding guest user remnants for specific guest users in a context.
     *
     * @param connectionHelper A (started) connection helper
     * @param contextID The context ID
     * @param guestIDs The identifiers of the guest users to consider for cleanup
     */
    public void cleanupGuests(ConnectionHelper connectionHelper, int contextID, int[] guestIDs) throws OXException {
        /*
         * execute guest cleanup tasks in current thread
         */
        try {
            for (int guestID : guestIDs) {
                new GuestCleanupTask(0, services, connectionHelper, contextID, guestID).call();
            }
        } catch (Exception e) {
            if (OXException.class.isInstance(e)) {
                throw (OXException) e;
            }
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup");
        }
    }

}

