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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.groupware.update.UpdateTask;

/**
 * {@link DynamicList} - Registry for {@link UpdateTask update tasks}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DynamicList implements UpdateTaskList<UpdateTask> {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DynamicList.class));

    private static final DynamicList SINGLETON = new DynamicList();

    /**
     * Gets the singleton instance of {@link DynamicList}.
     *
     * @return The singleton instance
     */
    public static DynamicList getInstance() {
        return SINGLETON;
    }

    /*-
     * -------------------------------- Member section --------------------------------
     */

    private final ConcurrentMap<Class<? extends UpdateTask>, UpdateTask> taskList = new ConcurrentHashMap<Class<? extends UpdateTask>, UpdateTask>();

    /**
     * Initializes a new {@link DynamicList}.
     */
    private DynamicList() {
        super();
    }

    public boolean addUpdateTask(final UpdateTask updateTask) {
        final boolean added = (null == taskList.putIfAbsent(updateTask.getClass(), updateTask));
        if (added) {
            UpdateTaskCollection.getInstance().dirtyVersion();
        } else {
            LOG.error("Update task \"" + updateTask.getClass().getName() + "\" is already registered.");
        }
        return added;
    }

    /**
     * Removes specified update task from this registry.
     *
     * @param updateTask The update task
     */
    public void removeUpdateTask(final UpdateTask updateTask) {
        final UpdateTask removed = taskList.remove(updateTask.getClass());
        if (null == removed) {
            LOG.error("Update task \"" + updateTask.getClass().getName() + "\" is unknown and could not be deregistered.");
        } else {
            UpdateTaskCollection.getInstance().dirtyVersion();
        }
    }

    @Override
    public List<UpdateTask> getTaskList() {
        final List<UpdateTask> retval = new ArrayList<UpdateTask>(taskList.size());
        retval.addAll(taskList.values());
        return retval;
    }
}
