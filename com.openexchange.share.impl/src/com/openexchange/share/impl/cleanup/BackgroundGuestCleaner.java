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

package com.openexchange.share.impl.cleanup;

import static com.openexchange.java.Autoboxing.L;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BackgroundGuestCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class BackgroundGuestCleaner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BackgroundGuestCleaner.class);
    private final BlockingQueue<GuestCleanupTask> cleanupTasks;
    private final AtomicBoolean active;

    /**
     * Initializes a new {@link BackgroundGuestCleaner}.
     *
     * @param cleanupTasks The queue yielding cleanup tasks for the worker
     */
    public BackgroundGuestCleaner(BlockingQueue<GuestCleanupTask> cleanupTasks) {
        super();
        this.cleanupTasks = cleanupTasks;
        this.active = new AtomicBoolean(true);
    }

    /**
     * Stops all background processing by signaling termination flag.
     */
    public void stop() {
        active.set(false);
    }

    @Override
    public void run() {
        LOG.info("Background guest cleaner starting.");
        while (active.get()) {
            try {
                GuestCleanupTask task = cleanupTasks.take();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Starting cleanup task {}.", task);
                    long start = System.currentTimeMillis();
                    task.call();
                    LOG.debug("Guest cleanup task {} finished after {}ms.", task, L((System.currentTimeMillis() - start)));
                } else {
                    task.call();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                LOG.error("Error during guest cleanup", e);
            }
        }
        LOG.info("Background guest cleaner stopped.");
    }

}
