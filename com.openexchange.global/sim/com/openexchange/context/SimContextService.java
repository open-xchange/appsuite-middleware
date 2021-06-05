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

package com.openexchange.context;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;


/**
 * {@link SimContextService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimContextService implements ContextService{
    @Override
    public List<Integer> getAllContextIds() throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public List<Integer> getDistinctContextsPerSchema() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return null;
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException {
        return null;
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        // Nothing
    }

    @Override
    public Context getContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public Context loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public int getContextId(String loginContextInfo) throws OXException {
        // Nothing to do
        return 0;
    }

    @Override
    public void invalidateContext(int contextId) throws OXException {
        // Nothing to do

    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        // Nothing to do
    }

    @Override
    public void invalidateLoginInfo(String loginContextInfo) throws OXException {
        // Nothing to do

    }
}
