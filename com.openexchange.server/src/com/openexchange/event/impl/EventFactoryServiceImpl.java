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
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.RemoteEvent;
import com.openexchange.session.Session;

/**
 * {@link EventFactoryServiceImpl} - Implementation of {@link EventFactoryService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EventFactoryServiceImpl implements EventFactoryService {

    /**
     * Initializes a new {@link EventFactoryServiceImpl}.
     */
    public EventFactoryServiceImpl() {
        super();
    }

    @Override
    public CommonEvent newCommonEvent(final int contextId, final int userId, Map<Integer, Set<Integer>> affectedUsers, final int action, final int module, final Object actionObj, final Object oldObj, final Object sourceFolder, final Object destinationFolder, final Session session) {
        return new CommonEventImpl(contextId, userId, affectedUsers, action, module, actionObj, oldObj, sourceFolder, destinationFolder, session);
    }

    @Override
    public RemoteEvent newRemoteEvent(final int folderId, final int userId, final int contextId, final int action, final int module, final long timestamp) {
        return new RemoteEventImpl(folderId, userId, contextId, action, module, timestamp);
    }

}
