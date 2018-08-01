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

package com.openexchange.database.internal;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import com.openexchange.database.ConfigurationListener;

/**
 * {@link AbstractConfigurationListener}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public abstract class AbstractConfigurationListener implements ConfigurationListener {

    protected final Management management;

    protected final Timer timer;

    /**
     * Initializes a new {@link AbstractConfigurationListener}.
     * 
     * @param management {@link Management} for MBean
     * @param timer The {@link Timer} for cleanup task
     * 
     */
    public AbstractConfigurationListener(Management management, Timer timer) {
        super();
        this.management = management;
        this.timer = timer;
    }

    /**
     * Set the new pool via given {@link Consumer} and uses given {@link Lock} to secure transient phase
     * 
     * @param poolid For the {@link Management} to add or remove
     * @param cleanUp If the {@link ConnectionPool} should be removed from {@link Management} and {@link Timer}
     * @param lock The {@link Lock} to secure the transient phase
     * @param pool The new {@link ConnectionPool} to set
     * @param setter A {@link Consumer} that sets the new {@link ConnectionPool} to the calling class
     */
    protected void setPool(int poolid, boolean cleanUp, Lock lock, ConnectionPool pool, Consumer<ConnectionPool> setter) {
        if (cleanUp) {
            // Remove old pool
            timer.removeTask(pool.getCleanerTask());
            management.removePool(poolid);
        }
        if (null != lock) {
            lock.lock();
        }
        try {
            // Set new pool to this class instance
            setter.accept(pool);
        } finally {
            if (null != lock) {
                lock.unlock();
            }
        }
        // Add new pool back
        timer.addTask(pool.getCleanerTask());
        management.addPool(poolid, pool);
    }
}
