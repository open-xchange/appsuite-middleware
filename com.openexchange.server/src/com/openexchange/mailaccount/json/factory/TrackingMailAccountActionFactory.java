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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailaccount.json.factory;

import java.util.Collection;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentPriorityQueue;
import com.openexchange.mailaccount.json.DefaultMailAccountActionProvider;
import com.openexchange.mailaccount.json.MailAccountActionProvider;
import com.openexchange.osgi.util.RankedService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link TrackingMailAccountActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class TrackingMailAccountActionFactory implements ServiceTrackerCustomizer<MailAccountActionProvider, MailAccountActionProvider>, AJAXActionServiceFactory {

    private final BundleContext context;
    private final DefaultMailAccountActionProvider defaultProvider;
    private final ConcurrentPriorityQueue<RankedService<MailAccountActionProvider>> trackedProviders;

    /**
     * Initializes a new {@link TrackingMailAccountActionFactory}.
     */
    public TrackingMailAccountActionFactory(DefaultMailAccountActionProvider defaultProvider, BundleContext context) {
        super();;
        this.context = context;
        this.defaultProvider = defaultProvider;
        ConcurrentPriorityQueue<RankedService<MailAccountActionProvider>> trackedProviders = new ConcurrentPriorityQueue<RankedService<MailAccountActionProvider>>();
        trackedProviders.offer(new RankedService<MailAccountActionProvider>(defaultProvider, 0));
        this.trackedProviders = trackedProviders;
    }

    @Override
    public Collection<?> getSupportedServices() {
        return getActiveProvider().getActions().values();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        if (null == action) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create("null");
        }

        AJAXActionService actionService = getActiveProvider().getActions().get(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        return actionService;
    }

    private MailAccountActionProvider getActiveProvider() {
        RankedService<MailAccountActionProvider> activeProvider = trackedProviders.peek();
        return null == activeProvider ? defaultProvider : activeProvider.service;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    @Override
    public synchronized MailAccountActionProvider addingService(ServiceReference<MailAccountActionProvider> reference) {
        MailAccountActionProvider provider = context.getService(reference);
        trackedProviders.offer(new RankedService<MailAccountActionProvider>(provider, RankedService.getRanking(reference)));
        return provider;
    }

    @Override
    public void modifiedService(ServiceReference<MailAccountActionProvider> reference, MailAccountActionProvider provider) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<MailAccountActionProvider> reference, MailAccountActionProvider provider) {
        trackedProviders.remove(new RankedService<MailAccountActionProvider>(provider, RankedService.getRanking(reference)));
        context.ungetService(reference);
    }

}
