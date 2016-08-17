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
