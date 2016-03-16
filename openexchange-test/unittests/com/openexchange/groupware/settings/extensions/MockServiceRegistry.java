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
package com.openexchange.groupware.settings.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class MockServiceRegistry implements ServicePublisher {

    private final Map<Class, List> services = new HashMap<Class, List>();
    private final Map<Class, List> added = new HashMap<Class, List>();
    private final Map<Class, List> removed = new HashMap<Class, List>();


    @Override
    public void publishService(final Class clazz,final Object service) {
        if(!clazz.isAssignableFrom(service.getClass())) {
            throw new IllegalArgumentException("The service is not a "+clazz);
        }
        getAllServices(clazz).add(service);
        getAdded(clazz).add(service);
    }

    @Override
    public void removeService(final Class clazz, final Object service) {
        getAllServices(clazz).remove(service);
        getRemoved(clazz).add(service);
    }

    @Override
    public void removeAllServices() {
        for(final Class clazz : services.keySet()) {
            final List serviceList = services.get(clazz);
            for(final Object service : serviceList) {
                removeService(clazz, service);
            }
        }
    }

    public List getAllServices(final Class clazz) {
        List retval = services.get(clazz);
        if(retval == null) {
            retval = new ArrayList();
            services.put(clazz, retval);
        }
        return retval;
    }

    public List getAdded(final Class clazz) {
        List retval = added.get(clazz);
        if(retval == null) {
            retval = new ArrayList();
            added.put(clazz, retval);
        }
        return retval;
    }

    public List getRemoved(final Class clazz) {
        List retval = removed.get(clazz);
        if(retval == null) {
            retval = new ArrayList();
            removed.put(clazz, retval);
        }
        return retval;
    }

    public void clearHistory() {
        removed.clear();
        added.clear();
    }
}
