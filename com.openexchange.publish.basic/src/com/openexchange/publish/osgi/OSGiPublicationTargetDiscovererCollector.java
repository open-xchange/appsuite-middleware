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

package com.openexchange.publish.osgi;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.tools.CompositePublicationTargetDiscoveryService;


/**
 * {@link OSGiPublicationTargetDiscovererCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OSGiPublicationTargetDiscovererCollector implements PublicationTargetDiscoveryService, ServiceTrackerCustomizer {

    private final BundleContext context;
    private final ServiceTracker tracker;

    private final Set<PublicationTargetDiscoveryService> blacklist = new HashSet<PublicationTargetDiscoveryService>();

    private final CompositePublicationTargetDiscoveryService delegate = new CompositePublicationTargetDiscoveryService();

    private final List<ServiceReference> references = new LinkedList<ServiceReference>();
    private boolean grabbedAll = false;


    public OSGiPublicationTargetDiscovererCollector(BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker(context, PublicationTargetDiscoveryService.class.getName(), this);
        tracker.open();
        blacklist.add(this);
    }

    public void close() {
        tracker.close();
        delegate.clear();
        for(ServiceReference reference : references) {
            context.ungetService(reference);
        }
    }

    private void grabAll() {
        grabbedAll = true;
        try {
            ServiceReference[] refs = context.getAllServiceReferences(PublicationTargetDiscoveryService.class.getName(), null);
            for(ServiceReference ref : refs) {
                add(ref);
            }
        } catch (InvalidSyntaxException e) {
            // IGNORE
        }
    }

    @Override
    public Object addingService(ServiceReference reference) {
        return add(reference);
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {

    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        delegate.removeDiscoveryService((PublicationTargetDiscoveryService) service);
        context.ungetService(reference);
    }

    private PublicationTargetDiscoveryService add(ServiceReference ref) {
        PublicationTargetDiscoveryService discovery = (PublicationTargetDiscoveryService) context.getService(ref);
        if(blacklist.contains(discovery)) {
            context.ungetService(ref);
            return discovery;
        }
        delegate.addDiscoveryService(discovery);
        return discovery;
    }

    @Override
    public PublicationTarget getTarget(Context context, int publicationId) throws OXException {
        if(!grabbedAll) {
            grabAll();
        }
        return delegate.getTarget(context, publicationId);
    }

    @Override
    public PublicationTarget getTarget(String id) throws OXException {
        if(!grabbedAll) {
            grabAll();
        }
        return delegate.getTarget(id);
    }

    @Override
    public Collection<PublicationTarget> getTargetsForEntityType(String module) throws OXException {
        if(!grabbedAll) {
            grabAll();
        }
        return delegate.getTargetsForEntityType(module);
    }

    @Override
    public boolean knows(String id) throws OXException {
        if(!grabbedAll) {
            grabAll();
        }
        return delegate.knows(id);
    }

    @Override
    public List<PublicationTarget> listTargets() throws OXException {
        if(!grabbedAll) {
            grabAll();
        }
        return delegate.listTargets();
    }

    public void ignore(PublicationTargetDiscoveryService discovery) {
        blacklist.add(discovery);
    }

}
