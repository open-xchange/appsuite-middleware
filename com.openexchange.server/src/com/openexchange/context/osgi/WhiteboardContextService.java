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

package com.openexchange.context.osgi;

import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;


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
    public void setAttribute(String name, String value, int contextId) throws OXException {
        getDelegate().setAttribute(name, value, contextId);
    }

    @Override
    public Context getContext(int contextId) throws OXException {
        return getDelegate().getContext(contextId);
    }

    @Override
    public Context loadContext(int contextId) throws OXException {
        return getDelegate().loadContext(contextId);
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
        if(delegate != null) {
            return delegate;
        }
        ServiceReference<ContextService> serviceReference = context.getServiceReference(ContextService.class);
        if(serviceReference == null) {
            return null;
        }
        return context.getService(serviceReference);
    }



}
