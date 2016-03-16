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

package com.openexchange.database.migration.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.openexchange.database.migration.DBMigrationMonitorService;

/**
 * Monitors the currently running migration tasks within this JVM. This for instance helps to hinder the server to shut down in case the
 * migration is running on this machine.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationMonitor implements DBMigrationMonitorService {

    private final ConcurrentMap<String, Thread> statesBySchema = new ConcurrentHashMap<String, Thread>();

    // prevent instantiation
    private DBMigrationMonitor() {
    }

    /**
     * SingletonHolder is loaded on the first execution of DBMigrationMonitor.getInstance() or the first access to SingletonHolder.INSTANCE,
     * not before.
     */
    private static class SingletonHolder {
        private static final DBMigrationMonitor INSTANCE = new DBMigrationMonitor();
    }

    public static DBMigrationMonitor getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Adds a new liquibase changelog file name desired to execute to this monitor. This indicates that one or more update tasks for this
     * schema have been scheduled and are going to be executed by the same thread that performs this call.
     *
     * @param String - string with the liquibase changelog file name to execute
     * @return Whether the state was added or not. If the same thread already added a state, <code>false</code> is returned and the state is
     *         not added.
     */
    public boolean addFile(String fileName) {
        return statesBySchema.putIfAbsent(fileName, Thread.currentThread()) == null;
    }

    /**
     * Removes the given executed liquibase changelog filename if it has been added by this thread.
     *
     * @param String - string with the liquibase changelog filename to remove
     * @return Whether a state has been removed or not (i.e. wasn't added before).
     */
    public boolean removeFile(String fileName) {
        return statesBySchema.remove(fileName, Thread.currentThread());
    }

    /**
     * Returns a list of {@link String}s with the currently scheduled files. Every item indicates that one or more changelog files have been
     * scheduled and are going to be executed or are currently running.
     *
     * @return A list of states
     */
    @Override
    public Collection<String> getScheduledFiles() {
        return new ArrayList<String>(statesBySchema.keySet());
    }
}
