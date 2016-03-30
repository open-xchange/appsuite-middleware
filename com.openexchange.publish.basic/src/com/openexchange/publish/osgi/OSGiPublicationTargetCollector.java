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
import java.util.LinkedList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.tools.PublicationTargetCollector;


/**
 * {@link OSGiPublicationTargetCollector}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OSGiPublicationTargetCollector implements ServiceTrackerCustomizer, PublicationTargetDiscoveryService {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiPublicationTargetCollector.class);

    private final BundleContext context;
    private final ServiceTracker tracker;

    private final PublicationTargetCollector delegate = new PublicationTargetCollector();

    private final List<ServiceReference> references = new LinkedList<ServiceReference>();
    private boolean grabbedAll = false;

    public OSGiPublicationTargetCollector(BundleContext context) {
        this.context = context;
        this.tracker = new ServiceTracker(context, PublicationService.class.getName(), this);
        tracker.open();
    }

    public void close() {
        delegate.clear();
        for(ServiceReference reference : references) {
            context.ungetService(reference);
        }
        this.tracker.close();
        grabbedAll = false;
    }

    @Override
    public Object addingService(ServiceReference reference) {
        try {
            return add(reference);
        } catch (OXException e) {
            LOG.error("", e);
        }
        return null;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {

    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        try {
            remove(reference, service);
        } catch (OXException e) {
            LOG.error("", e);
        }
    }

    private void grabAll() throws OXException {
        grabbedAll = true;
        try {
            ServiceReference[] refs = context.getAllServiceReferences(PublicationService.class.getName(), null);
            if(refs == null) {
                return;
            }
            for(ServiceReference reference : refs) {
                add(reference);
            }

        } catch (InvalidSyntaxException e) {
            // Won't happen, we don't use filters;
        }

    }

    private void remove(ServiceReference reference, Object service) throws OXException {
        references.remove(reference);
        context.ungetService(reference);
        delegate.removePublicationService((PublicationService) service);
    }

    private PublicationService add(ServiceReference reference) throws OXException {
        references.add(reference);
        PublicationService publisher = (PublicationService) context.getService(reference);
        delegate.addPublicationService(publisher);
        return publisher;
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
    public Collection<PublicationTarget> listTargets() throws OXException {
        if(!grabbedAll) {
            grabAll();
        }
        return delegate.listTargets();
    }


}

