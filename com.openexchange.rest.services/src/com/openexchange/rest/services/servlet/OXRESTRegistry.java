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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rest.services.servlet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.ServiceReference;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.rest.services.OXRESTMatch;
import com.openexchange.rest.services.OXRESTRoute;
import com.openexchange.rest.services.internal.OXRESTServiceFactory;
import com.openexchange.rest.services.internal.OXRESTServiceWrapper;


/**
 * {@link OXRESTRegistry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXRESTRegistry implements SimpleRegistryListener<OXRESTServiceFactory>{

    private ConcurrentHashMap<String, List<OXRESTServiceFactory>> factories = new ConcurrentHashMap<String, List<OXRESTServiceFactory>>();
    
    @Override
    public void added(ServiceReference<OXRESTServiceFactory> ref, OXRESTServiceFactory service) {
        String root = service.getRoot();
        List<OXRESTServiceFactory> list = factories.get(root);
        
        if (list == null) {
            List<OXRESTServiceFactory> meantime = factories.putIfAbsent(root, list = new CopyOnWriteArrayList<OXRESTServiceFactory>());
            if (meantime != null) {
                list = meantime;
            }
        }
        list.add(service);
    }

    @Override
    public void removed(ServiceReference<OXRESTServiceFactory> ref, OXRESTServiceFactory service) {
        String root = service.getRoot();
        List<OXRESTServiceFactory> list = factories.get(root);
        if (list == null) {
            return;
        }
        list.remove(service);
    }

    public OXRESTServiceWrapper retrieve(String method, String path) {
        for(String root: factories.keySet()) {
            if (path.startsWith(root)) {
                String subpath = path.substring(root.length());
                if (!subpath.startsWith("/")) {
                    subpath = "/" + subpath;
                }
                
                List<OXRESTServiceFactory> list = factories.get(root);
                for (OXRESTServiceFactory factory : list) {
                    List<OXRESTRoute> routes = factory.getRoutes();
                    for (OXRESTRoute route: routes) {
                        OXRESTMatch match = route.match(method, subpath);
                        if (match != null) {
                            return factory.newWrapper(match);
                        }
                    }
                }
            }
        }
        return null;
    }

}
