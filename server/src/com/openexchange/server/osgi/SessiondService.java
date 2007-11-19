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

package com.openexchange.server.osgi;

import com.openexchange.sessiond.impl.SessiondConnectorInterface;
import java.util.concurrent.atomic.AtomicInteger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


/**
 * @author <a href="mailto:sebas@open-xchange.org">Marcus Klein</a>
 */
public class SessiondService implements ServiceTrackerCustomizer {
    
    private static SessiondConnectorInterface sessiondConnectorInterface = null;
    
    private static final AtomicInteger active = new AtomicInteger();
    
    private BundleContext context;
    
    public SessiondService(final BundleContext context) {
        this.context = context;
    }
    
    public Object addingService(ServiceReference serviceReference) {
		final Object addedService = context.getService(serviceReference);
		if (addedService instanceof SessiondConnectorInterface) {
			sessiondConnectorInterface = (SessiondConnectorInterface)addedService;
		}
        return addedService;
    }

    public void modifiedService(ServiceReference serviceReference, Object object) {

    }

    public void removedService(ServiceReference serviceReference, Object object) {
		if (object instanceof SessiondConnectorInterface) {
            if (active.get() > 0) {
                System.out.println("active services still exists");
            }
			sessiondConnectorInterface = null;
		}
        context.ungetService(serviceReference);
        
    }

    public static SessiondConnectorInterface getService() throws Exception{
        if (sessiondConnectorInterface != null) {
            active.incrementAndGet();
            return sessiondConnectorInterface;
        } 
        throw new Exception("service is null");
    }
    
    public static void releaseService() {
        active.decrementAndGet();
    }
}
