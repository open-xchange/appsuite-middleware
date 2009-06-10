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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.subscribe.osgi;

import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceCollector;


/**
 * {@link OSGiSubscriptionSourceCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OSGiSubscriptionSourceCollector extends SubscriptionSourceCollector implements ServiceTrackerCustomizer {

    private ServiceTracker tracker;
    private BundleContext context;
    private boolean grabbedAll;

    public OSGiSubscriptionSourceCollector(BundleContext context) throws InvalidSyntaxException {
        this.context = context;
        this.tracker = new ServiceTracker(context,SubscribeService.class.getName(), this);
        tracker.open();
    }

    private void grabAll() {
        if(grabbedAll) {
            return;
        }
        try {
            ServiceReference[] serviceReferences = context.getAllServiceReferences(SubscribeService.class.getName(), null);
            if(serviceReferences != null) {
                for (ServiceReference reference : serviceReferences) {
                    addingService(reference);
                }
            }
            grabbedAll = true;
        } catch (InvalidSyntaxException x) {
            // IGNORE, we didn't specify a filter, so won't happen
        }
    }
    
    public void close() {
        this.tracker.close();
    }
    
    public Object addingService(ServiceReference reference) {
        SubscribeService subscribeService = (SubscribeService) context.getService(reference);
        addSubscribeService(subscribeService);
        return subscribeService;
    }

    public void modifiedService(ServiceReference reference, Object service) {
        // IGNORE
    }

    public void removedService(ServiceReference reference, Object service) {
        removeSubscribeService(((SubscribeService) reference).getSubscriptionSource().getId());
    }

    @Override
    public SubscriptionSource getSource(com.openexchange.groupware.contexts.Context context, int subscriptionId) throws AbstractOXException {
        grabAll();
        return super.getSource(context, subscriptionId);
    }

    @Override
    public SubscriptionSource getSource(String identifier) {
        grabAll();
        return super.getSource(identifier);
    }

    @Override
    public List<SubscriptionSource> getSources(int folderModule) {
        grabAll();
        return super.getSources(folderModule);
    }

    @Override
    public boolean knowsSource(String identifier) {
        grabAll();
        return super.knowsSource(identifier);
    }
}
