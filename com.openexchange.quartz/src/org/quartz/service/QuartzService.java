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

package org.quartz.service;

import org.quartz.Scheduler;
import com.openexchange.exception.OXException;

/**
 * {@link QuartzService} - An OSGi service wrapped around <a href="http://quartz-scheduler.org/">Quartz</a> scheduler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface QuartzService {

    /**
     * <p>
     * Gets the default scheduler. The number of workers are configured in quartz.properties.
     * Whether this scheduler will be started automatically is also defined there. The scheduler
     * is configured during the first call to this method.
     * </p>
     *
     * @return The default scheduler
     * @throws OXException
     */
    Scheduler getDefaultScheduler() throws OXException;
    
    /**
     * <p>Gets the scheduler instance with the given name. If it did not exist before,
     * the instance is going to be created.</p>
     * <p>Be sure that you really need your dedicated scheduler instance. Very instance occupies several 
     * threads and is therefore very expensive. If you just need to schedule a few jobs, use {@link #getDefaultScheduler()} instead.</p>
     * @param name The unique name of the scheduler instance.
     * 
     * @param start <code>true</code> if the scheduler should be started before it will be returned.
     * This does not a affect scheduler instance that was already started.
     * @param threads The number of worker threads to be configured. This takes effect if scheduler is started.
     * @return The named scheduler instance.
     * @throws OXException
     */
    Scheduler getScheduler(String name, boolean start, int threads) throws OXException;
//
//    /**
//     * Gets the local instance of the clustered scheduler identified by it's name.
//     * The name has to be cluster-wide unique. For every name a local scheduler instance
//     * is created on the requesting node. All scheduler instances within the cluster that share the same name
//     * also share the same job store. A scheduler instance can be used even if it's not started. The instance
//     * acts as a job store client then and can be used to submit jobs and triggers. It will just not execute jobs then.
//     *
//     * @param name The schedulers name.
//     * @param start <code>true</code> if the scheduler should be started before it will be returned.
//     * This does not a affect scheduler instance that was already started.
//     * @param threads The number of worker threads to be configured. This takes effect if scheduler is started.
//     * @return The clustered scheduler
//     * @throws OXException
//     */
//    Scheduler getClusteredScheduler(String name, boolean start, int threads) throws OXException;
//
//    /**
//     * Releases the ressources held by this scheduler instance. Does nothing if no scheduler exists for this name or name is <code>null</code>.
//     * If the corresponding scheduler was started, it will be stopped. Currently running jobs may finish before the scheduler shuts down.
//     *
//     * @param name The schedulers name.
//     */
//    void releaseClusteredScheduler(String name);
}
