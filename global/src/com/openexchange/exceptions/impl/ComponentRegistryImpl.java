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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.exceptions.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exceptions.ComponentAlreadyRegisteredException;
import com.openexchange.exceptions.ComponentRegistry;
import com.openexchange.exceptions.Exceptions;
import com.openexchange.groupware.Component;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ComponentRegistryImpl implements ComponentRegistry {

    private final Map<String, Component> abbr2component = new HashMap<String, Component>();

    private final Map<String, String> component2app = new HashMap<String, String>();

    private final Map<String, List<String>> app2components = new HashMap<String, List<String>>();

    private final Map<String, Exceptions<?>> component2exceptions = new HashMap<String, Exceptions<?>>();

    public synchronized void registerComponent(final Component component, final String applicationId, final Exceptions<?> exceptions) throws ComponentAlreadyRegisteredException {
        final String abbreviation = component.getAbbreviation();
        final String conflictingApplicationId = component2app.get(abbreviation);
        if (null != conflictingApplicationId && !applicationId.equals(conflictingApplicationId)) {
            throw new ComponentAlreadyRegisteredException(component, conflictingApplicationId);
        }
        component2app.put(abbreviation, applicationId);

        List<String> componentsForApp = app2components.get(applicationId);
        if (componentsForApp == null) {
            componentsForApp = new ArrayList<String>();
            app2components.put(applicationId, componentsForApp);
        }
        componentsForApp.add(abbreviation);
        component2exceptions.put(abbreviation, exceptions);
        abbr2component.put(abbreviation, component);

        exceptions.setComponent(component);
        exceptions.setApplicationId(applicationId);
    }

    public synchronized void deregisterComponent(final Component component) {
        final String applicationId = component2app.get(component.getAbbreviation());
        if (applicationId == null) {
            return;
        }
        final String abbreviation = component.getAbbreviation();
        component2app.remove(abbreviation);
        final List<String> componentsForApp = app2components.get(applicationId);
        componentsForApp.remove(abbreviation);
        if (componentsForApp.isEmpty()) {
            app2components.remove(applicationId);
        }
        component2exceptions.remove(abbreviation);
        abbr2component.remove(abbreviation);
    }

    public Exceptions<?> getExceptionsForComponent(final Component component) {
        return component2exceptions.get(component.getAbbreviation());
    }

    public List<Exceptions<?>> getExceptionsForApplication(final String applicationId) {
        final List<String> componentForApp = app2components.get(applicationId);
        final List<Exceptions<?>> exceptionsForApp = new ArrayList<Exceptions<?>>();
        for (final String abbreviation : componentForApp) {
            final Exceptions<?> exceptions = component2exceptions.get(abbreviation);
            if (exceptions != null) {
                exceptionsForApp.add(exceptions);
            }
        }
        return exceptionsForApp;
    }

    public List<Component> getComponents() {
        final Set<String> abbreviations = component2app.keySet();
        final List<Component> components = new ArrayList<Component>(abbreviations.size());
        for (final String abbreviation : abbreviations) {
            components.add(abbr2component.get(abbreviation));
        }
        return components;
    }

    public List<String> getApplicationIds() {
        return new ArrayList<String>(app2components.keySet());
    }

    public List<Exceptions<?>> getExceptions() {
        return new ArrayList<Exceptions<?>>(component2exceptions.values());
    }
}
