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

package com.openexchange.oauth.impl.osgi;

import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;

/**
 * {@link OSGiContextService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiContextService extends AbstractOSGiDelegateService<ContextService> implements ContextService {

    /**
     * Initializes a new {@link OSGiContextService}.
     */
    public OSGiContextService() {
        super(ContextService.class);
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        getService().setAttribute(name, value, contextId);
    }

    @Override
    public List<Integer> getAllContextIds() throws OXException {
        return getService().getAllContextIds();
    }

    @Override
    public List<Integer> getDistinctContextsPerSchema() throws OXException {
        return getService().getDistinctContextsPerSchema();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return getService().getSchemaAssociations();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException {
        return getService().getSchemaAssociationsFor(contextIds);
    }

    @Override
    public boolean exists(int contextId) throws OXException {
        return getService().exists(contextId);
    }

    @Override
    public Context getContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        return getService().getContext(contextId, updateBehavior);
    }

    @Override
    public int getContextId(final String loginContextInfo) throws OXException {
        return getService().getContextId(loginContextInfo);
    }

    @Override
    public void invalidateContext(final int contextId) throws OXException {
        getService().invalidateContext(contextId);
    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        try {
            getService().invalidateContexts(contextIDs);
        } catch (OXException e) {
            throw e;
        }
    }

    @Override
    public void invalidateLoginInfo(final String loginContextInfo) throws OXException {
        getService().invalidateLoginInfo(loginContextInfo);
    }

    @Override
    public Context loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        return getService().loadContext(contextId, updateBehavior);
    }

}
