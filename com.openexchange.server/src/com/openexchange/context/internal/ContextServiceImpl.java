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

package com.openexchange.context.internal;

import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.groupware.contexts.impl.ContextStorage;

/**
 * {@link ContextServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ContextServiceImpl implements ContextService {

    /**
     * Initializes a new {@link ContextServiceImpl}
     */
    public ContextServiceImpl() {
        super();
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        ContextStorage.getInstance().setAttribute(name, value, contextId);
    }

    @Override
    public List<Integer> getAllContextIds() throws OXException {
        return ContextStorage.getInstance().getAllContextIds();
    }

    @Override
    public List<Integer> getDistinctContextsPerSchema() throws OXException {
        return ContextStorage.getInstance().getDistinctContextsPerSchema();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return ContextStorage.getInstance().getSchemaAssociations();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException {
        return ContextStorage.getInstance().getSchemaAssociationsFor(contextIds);
    }

    @Override
    public Context getContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        return ContextStorage.getInstance().getContext(contextId, updateBehavior);
    }

    @Override
    public boolean exists(int contextId) throws OXException {
        return ContextStorage.getInstance().exists(contextId);
    }

    @Override
    public Context loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        return ContextStorage.getInstance().loadContext(contextId, updateBehavior);
    }

    @Override
    public int getContextId(final String loginContextInfo) throws OXException {
        return ContextStorage.getInstance().getContextId(loginContextInfo);
    }

    @Override
    public void invalidateContext(final int contextId) throws OXException {
        ContextStorage.getInstance().invalidateContext(contextId);
    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        ContextStorage.getInstance().invalidateContexts(contextIDs);
    }

    @Override
    public void invalidateLoginInfo(final String loginContextInfo) throws OXException {
        ContextStorage.getInstance().invalidateLoginInfo(loginContextInfo);
    }

}
