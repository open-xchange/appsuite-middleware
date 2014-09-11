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

package com.openexchange.groupware.update.internal;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.TimerService;

/**
 * Implementation for the updater interface.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UpdaterImpl extends Updater {

    /**
     * Default constructor.
     */
    public UpdaterImpl() {
        super();
    }

    @Override
    public UpdateStatus getStatus(final int contextId) throws OXException {
        return getStatus(getSchema(contextId));
    }

    @Override
    public UpdateStatus getStatus(final String schema, final int writePoolId) throws OXException {
        return getStatus(getSchema(writePoolId, schema));
    }

    private UpdateStatus getStatus(final SchemaUpdateState schema) {
        final SeparatedTasks tasks = UpdateTaskCollection.getInstance().getFilteredAndSeparatedTasks(schema);
        return new UpdateStatus() {
            @Override
            public boolean needsBlockingUpdates() {
                return tasks.getBlocking().size() > 0;
            }
            @Override
            public boolean needsBackgroundUpdates() {
                return tasks.getBackground().size() > 0;
            }
            @Override
            public boolean blockingUpdatesRunning() {
                return schema.isLocked();
            }
            @Override
            public boolean backgroundUpdatesRunning() {
                return schema.backgroundUpdatesRunning();
            }
        };
    }

    @Override
    public void startUpdate(final int contextId) throws OXException {
        final TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class, true);
        timerService.schedule(new UpdateProcess(contextId), 0);
    }

    private SchemaUpdateState getSchema(final int contextId) throws OXException {
        return SchemaStore.getInstance().getSchema(contextId);
    }

    private SchemaUpdateState getSchema(final int poolId, final String schemaName) throws OXException {
        return SchemaStore.getInstance().getSchema(poolId, schemaName);
    }

    @Override
    public UpdateTask[] getAvailableUpdateTasks() {
        final List<UpdateTask> retval = UpdateTaskCollection.getInstance().getListWithoutExcludes();
        return retval.toArray(new UpdateTask[retval.size()]);
    }

    @Override
    public Collection<SchemaUpdateState> getLocallyScheduledTasks() {
        return LocalUpdateTaskMonitor.getInstance().getScheduledStates();
    }

}
