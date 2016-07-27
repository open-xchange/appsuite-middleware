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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.TimerService;

/**
 * Implementation for the updater interface.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UpdaterImpl extends Updater {

    private static final UpdaterImpl INSTANCE = new UpdaterImpl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static UpdaterImpl getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Default constructor.
     */
    private UpdaterImpl() {
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
            @Override
            public Date blockingUpdatesRunningSince() {
                return schema.blockingUpdatesRunningSince();
            }
            @Override
            public Date backgroundUpdatesRunningSince() {
                return schema.backgroundUpdatesRunningSince();
            }
        };
    }

    @Override
    public void unblock(String schemaName, int poolId, int contextId) throws OXException {
        SchemaUpdateState schema = SchemaStore.getInstance().getSchema(poolId, schemaName);
        SchemaStore.getInstance().unlockSchema(schema, contextId, false);
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
    public UpdateTaskV2[] getAvailableUpdateTasks() {
        final List<UpdateTaskV2> retval = UpdateTaskCollection.getInstance().getListWithoutExcludes();
        return retval.toArray(new UpdateTaskV2[retval.size()]);
    }

    @Override
    public Collection<String> getLocallyScheduledTasks() {
        return LocalUpdateTaskMonitor.getInstance().getScheduledStates();
    }

}
