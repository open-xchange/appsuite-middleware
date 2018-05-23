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

package com.openexchange.groupware.update.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.java.Strings;

/**
 * {@link DynamicSet} - Registry for {@link UpdateTask update tasks}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DynamicSet implements UpdateTaskSet<UpdateTaskV2> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DynamicSet.class);

    private static final DynamicSet SINGLETON = new DynamicSet();

    /**
     * Gets the singleton instance of {@link DynamicSet}.
     *
     * @return The singleton instance
     */
    public static DynamicSet getInstance() {
        return SINGLETON;
    }

    /*-
     * -------------------------------- Member section --------------------------------
     */

    private final ConcurrentMap<String, UpdateTaskV2> taskRegistry = new ConcurrentHashMap<String, UpdateTaskV2>();

    /**
     * Initializes a new {@link DynamicSet}.
     */
    private DynamicSet() {
        super();
    }

    /**
     * Adds the specified update task to this registry.
     *
     * @param updateTask The {@link UpdateTaskV2} task to add
     * @return <code>true</code> if the task was successfully registered; <code>false</code>
     *         if the same task was previously registered.
     */
    public boolean addUpdateTask(final UpdateTaskV2 updateTask) {
        if (null == taskRegistry.putIfAbsent(getUpdateTaskName(updateTask), updateTask)) {
            UpdateTaskCollection.getInstance().dirtyVersion();
            return true;
        }

        LOG.error("Update task \"{}\" is already registered.", updateTask.getClass().getName());
        return false;
    }

    /**
     * Returns the name of the update task. If the {@link UpdateTaskV2} is implemented
     * as a local or anonymous class, then its name is being compiled by the {@link Package}
     * information and the class's name. If the {@link Package} is not available, then
     * the name falls back to a 'orphanedUpdateTask.t[timestamp].ClassName' format.
     *
     * @param updateTask The update task's name that shall be returned
     * @return the update task's name
     */
    private String getUpdateTaskName(UpdateTaskV2 updateTask) {
        String canonicalName = updateTask.getClass().getCanonicalName();
        if (Strings.isNotEmpty(canonicalName)) {
            return canonicalName;
        }
        Package pkg = updateTask.getClass().getPackage();
        if (pkg == null) {
            return "orphanedUpdateTask.t" + System.currentTimeMillis() + "." + updateTask.getClass().getName();
        }
        return pkg.getName() + updateTask.getClass().getName();
    }

    /**
     * Removes specified update task from this registry.
     *
     * @param updateTask The update task
     */
    public void removeUpdateTask(final UpdateTaskV2 updateTask) {
        if (null == taskRegistry.remove(getUpdateTaskName(updateTask))) {
            LOG.error("Update task \"{}\" is unknown and could not be deregistered.", updateTask.getClass().getName());
        } else {
            UpdateTaskCollection.getInstance().dirtyVersion();
        }
    }

    @Override
    public Set<UpdateTaskV2> getTaskSet() {
        return new HashSet<UpdateTaskV2>(taskRegistry.values());
    }

}
