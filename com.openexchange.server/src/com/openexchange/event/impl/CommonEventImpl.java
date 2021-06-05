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

package com.openexchange.event.impl;

import java.util.Map;
import java.util.Set;
import com.openexchange.event.CommonEvent;
import com.openexchange.session.Session;

/**
 * {@link CommonEventImpl} - Implementation of {@link CommonEvent}.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public final class CommonEventImpl implements CommonEvent {

    private final int contextId;

    private final int userId;

    private final Map<Integer, Set<Integer>> affectedUsersWithFolder;

    private final int module;

    private final Object actionObj;

    private final Object oldObj;

    private final Object sourceFolder;

    private final Object destinationFolder;

    private final int action;

    private final Session session;

    public CommonEventImpl(final int contextId, int userId, Map<Integer, Set<Integer>> affectedUsersWithFolder, final int action, final int module, final Object actionObj, final Object oldObj, final Object sourceFolder, final Object destinationFolder, final Session session) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.affectedUsersWithFolder = affectedUsersWithFolder;
        this.action = action;
        this.module = module;
        this.actionObj = actionObj;
        this.oldObj = oldObj;
        this.sourceFolder = sourceFolder;
        this.destinationFolder = destinationFolder;
        this.session = session;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getModule() {
        return module;
    }

    @Override
    public Object getActionObj() {
        return actionObj;
    }

    @Override
    public Object getOldObj() {
        return oldObj;
    }

    @Override
    public Object getSourceFolder() {
        return sourceFolder;
    }

    @Override
    public Object getDestinationFolder() {
        return destinationFolder;
    }

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Map<Integer, Set<Integer>> getAffectedUsersWithFolder() {
        return affectedUsersWithFolder;
    }
}
