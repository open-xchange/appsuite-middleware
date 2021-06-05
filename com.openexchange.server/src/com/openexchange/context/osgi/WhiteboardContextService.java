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

package com.openexchange.context.osgi;

import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;


/**
 * {@link WhiteboardContextService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class WhiteboardContextService implements ServiceTrackerCustomizer<ContextService, ContextService>, ContextService {

    private final BundleContext context;
    private final ServiceTracker<ContextService, ContextService> tracker;
    private ContextService delegate;

    public WhiteboardContextService(BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker<ContextService, ContextService>(context, ContextService.class, this);
        tracker.open();
    }

    public void close() {
        tracker.close();
    }

    @Override
    public ContextService addingService(ServiceReference<ContextService> reference) {
        delegate = context.getService(reference);
        return delegate;
    }

    @Override
    public void modifiedService(ServiceReference<ContextService> reference, ContextService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<ContextService> reference, ContextService service) {
        context.ungetService(reference);
        delegate = null;
    }

    @Override
    public List<Integer> getAllContextIds() throws OXException {
        return getDelegate().getAllContextIds();
    }

    @Override
    public List<Integer> getDistinctContextsPerSchema() throws OXException {
        return getDelegate().getDistinctContextsPerSchema();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return getDelegate().getSchemaAssociations();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException {
        return getDelegate().getSchemaAssociationsFor(contextIds);
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        getDelegate().setAttribute(name, value, contextId);
    }

    @Override
    public boolean exists(int contextId) throws OXException {
        return getDelegate().exists(contextId);
    }

    @Override
    public Context getContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        return getDelegate().getContext(contextId, updateBehavior);
    }

    @Override
    public Context loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        return getDelegate().loadContext(contextId, updateBehavior);
    }

    @Override
    public int getContextId(String loginContextInfo) throws OXException {
        return getDelegate().getContextId(loginContextInfo);
    }

    @Override
    public void invalidateContext(int contextId) throws OXException {
        getDelegate().invalidateContext(contextId);
    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        getDelegate().invalidateContexts(contextIDs);
    }

    @Override
    public void invalidateLoginInfo(String loginContextInfo) throws OXException {
        getDelegate().invalidateLoginInfo(loginContextInfo);
    }

    private ContextService getDelegate() {
        if (delegate != null) {
            return delegate;
        }
        ServiceReference<ContextService> serviceReference = context.getServiceReference(ContextService.class);
        if (serviceReference == null) {
            return null;
        }
        return context.getService(serviceReference);
    }



}
