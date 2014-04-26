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

package com.openexchange.rest.services.osgiservice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.rest.services.OXRESTMatch;
import com.openexchange.rest.services.OXRESTRoute;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.annotations.DELETE;
import com.openexchange.rest.services.annotations.GET;
import com.openexchange.rest.services.annotations.LINK;
import com.openexchange.rest.services.annotations.OPTIONS;
import com.openexchange.rest.services.annotations.PATCH;
import com.openexchange.rest.services.annotations.POST;
import com.openexchange.rest.services.annotations.PUT;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.rest.services.annotations.UNLINK;
import com.openexchange.rest.services.internal.OXRESTServiceFactory;
import com.openexchange.rest.services.internal.OXRESTServiceWrapper;
import com.openexchange.server.ServiceLookup;


/**
 * The {@link IntrospectingServiceFactory} examines an OXRESTService class for annotations to offer a REST service implementation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IntrospectingServiceFactory<T> implements OXRESTServiceFactory {

    private final Class<? extends OXRESTService<T>> klass;
    private final String root;

    private final Map<OXRESTRoute, Method> methods = new HashMap<OXRESTRoute, Method>();
    private List<OXRESTRoute> routes = null;
    private final ServiceLookup services;
    private final T context;

    /**
     * Initializes a new {@link IntrospectingServiceFactory}.
     * @param klass
     * @param services
     */
    public IntrospectingServiceFactory(Class<? extends OXRESTService<T>> klass, ServiceLookup services, T context) {
        super();
        this.services = services;
        this.klass = klass;
        this.context = context;
        ROOT rootAnnotation = klass.getAnnotation(ROOT.class);
        if (rootAnnotation == null) {
            throw new IllegalArgumentException("The service class must contain a 'root' annotation");
        }

        this.root = rootAnnotation.value();


        Method[] instanceMethods = klass.getMethods();
        routes = new ArrayList<OXRESTRoute>(instanceMethods.length);
        for(Method m: instanceMethods) {
            GET getAnnotation = m.getAnnotation(GET.class);
            if (getAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("get", getAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            PUT putAnnotation = m.getAnnotation(PUT.class);
            if (putAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("put", putAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            POST postAnnotation = m.getAnnotation(POST.class);
            if (postAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("post", postAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            PATCH patchAnnotation = m.getAnnotation(PATCH.class);
            if (patchAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("patch", patchAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            LINK linkAnnotation = m.getAnnotation(LINK.class);
            if (linkAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("link", linkAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            UNLINK unlinkAnnotation = m.getAnnotation(UNLINK.class);
            if (unlinkAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("unlink", unlinkAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            OPTIONS optionsAnnotation = m.getAnnotation(OPTIONS.class);
            if (optionsAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("options", optionsAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }

            DELETE deleteAnnotation = m.getAnnotation(DELETE.class);
            if (deleteAnnotation != null) {
                OXRESTRoute oxrestRoute = new OXRESTRoute("delete", deleteAnnotation.value());
                methods.put(oxrestRoute, m);
                routes.add(oxrestRoute);
            }
        }
    }

    @Override
    public String getRoot() {
        return root;
    }

    @Override
    public List<OXRESTRoute> getRoutes() {
        return routes;
    }

    @Override
    public OXRESTServiceWrapper newWrapper(OXRESTMatch match) {
        Method method = methods.get(match.getRoute());
        if (method == null) {
            return null;
        }

        try {
            OXRESTService<T> newInstance = klass.newInstance();
            newInstance.setServices(services);
            newInstance.setContext(context);
            return new ReflectiveServiceWrapper(method, newInstance, match);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

}
