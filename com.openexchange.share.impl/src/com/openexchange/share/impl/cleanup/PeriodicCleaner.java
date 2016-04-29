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
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.ShareExceptionCodes;

/**
 * {@link PeriodicCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class PeriodicCleaner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PeriodicCleaner.class);

    private final ServiceLookup services;
    private final long guestExpiry;
    private final AtomicBoolean active;

    /**
     * Initializes a new {@link PeriodicCleaner}.
     *
     * @param services A service lookup reference
     * @param guestExpiry the timespan (in milliseconds) after which an unused guest user can be deleted permanently
     */
    public PeriodicCleaner(final ServiceLookup services, long guestExpiry) {
        super();
        this.services = services;
        this.guestExpiry = guestExpiry;
        this.active = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {
            List<Integer> contextIDs = services.getService(ContextService.class).getAllContextIds();
            int size = contextIDs.size();
            LOG.info("Periodic share cleanup task starting, going to check {} contexts...", I(size));
            long logTimeDistance = TimeUnit.SECONDS.toMillis(10);
            long lastLogTime = start;
            for (int i = 0, k = size; k-- > 0; i++) {
                int contextID = contextIDs.get(i).intValue();
                for (int retry = 0; retry < 3; retry++) {
                    if (false == active.get()) {
                        LOG.info("Periodic share cleanup task stopping.");
                        return;
                    }
                    long now = System.currentTimeMillis();
                    if (now > lastLogTime + logTimeDistance) {
                        LOG.info("Periodic share cleanup task {}% finished ({}/{}).",
                            I(i * 100 / size), I(i), I(size));
                        lastLogTime = now;
                    }
                    try {
                        cleanupContext(contextID);
                        break;
                    } catch (OXException e) {
                        if (Category.CATEGORY_TRY_AGAIN.equals(e.getCategory()) && retry < 3) {
                            long delay = 10000 + retry * 20000;
                            LOG.debug("Error during periodic share cleanup task for context {}: {}; trying again in {}ms...",
                                I(contextID), e.getMessage(), L(delay));
                            Thread.sleep(delay);
                        } else {
                            LOG.error("Error during periodic share cleanup task for context {}: {}", I(contextID), e.getMessage(), e);
                            break;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted during periodic share cleanup task: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Error during periodic share cleanup task: {}", e.getMessage(), e);
        }
        LOG.info("Periodic share cleanup task finished after {}ms.", L(System.currentTimeMillis() - start));
    }

    /**
     * Stops all background processing by signaling termination flag.
     */
    public void stop() {
        active.set(false);
    }

    /**
     * Synchronously cleans obsolete shares and corresponding guest user remnants for a context.
     *
     * @param services A service lookup reference
     * @param contextID The context ID
     * @param guestExpiry the timespan (in milliseconds) after which an unused guest user can be deleted permanently
     */
    private void cleanupContext(int contextID) throws OXException {
        /*
         * execute context- and resulting guest cleanup tasks in current thread
         */
        try {
            List<GuestCleanupTask> guestCleanupTasks = new ContextCleanupTask(services, contextID, guestExpiry).call();
            for (GuestCleanupTask guestCleanupTask : guestCleanupTasks) {
                if (false == active.get()) {
                    return;
                }
                guestCleanupTask.call();
            }
        } catch (Exception e) {
            if (OXException.class.isInstance(e)) {
                throw (OXException) e;
            }
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, "Unexpected error during cleanup");
        }
    }

}
