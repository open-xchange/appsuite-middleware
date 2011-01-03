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
package com.openexchange.exceptions;

import java.util.List;

import junit.framework.TestCase;

import com.openexchange.exceptions.impl.ComponentRegistryImpl;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ComponentRegistryTest extends TestCase {
    private ComponentRegistry registry;
    private Component componentA;
    private Component componentB;
    private String applicationIdA;
    private String applicationIdB;
    private DummyExceptions exceptionsA;
    private DummyExceptions exceptionsB;

    @Override
    public void setUp() {
        this.registry = new ComponentRegistryImpl();
        this.componentA = new StringComponent("CMA");
        this.componentB = new StringComponent("CMB");
        this.applicationIdA = "com.openexchange.A";
        this.applicationIdB = "com.openexchange.B";

        this.exceptionsA = new DummyExceptions();
        this.exceptionsB = new DummyExceptions();
    }

    public void testShouldDisallowRegistrationForComponentWithTwoDifferentApplicationIDs() throws ComponentAlreadyRegisteredException {

        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        try {
            registry.registerComponent(componentA, applicationIdB, exceptionsB);
            fail("Could register same component with two different application ids");
        } catch (final ComponentAlreadyRegisteredException x) {
            // Passes :)
        }
    }

    public void testShouldAllowRegistrationForOneComponentWithSameApplicationIDsTwice() {
        try {
            registry.registerComponent(componentA, applicationIdA, exceptionsA);
            registry.registerComponent(componentA, applicationIdA, exceptionsA);
        } catch (final ComponentAlreadyRegisteredException e) {
            fail(e.getMessage());
        }

    }

    public void testShouldAllowRegistrationOfMultipleComponentsWithSameApplicationID() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        registry.registerComponent(componentB, applicationIdA, exceptionsB);
    }

    public void testShouldSetApplicationIdAndComponentInExceptions() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        assertEquals(componentA, exceptionsA.getComponent());
        assertEquals(applicationIdA, exceptionsA.getApplicationId());
    }

    public void testShouldLookUpExceptionsForComponent() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);

        final Exceptions exceptions = registry.getExceptionsForComponent(componentA);
        assertEquals(exceptionsA, exceptions);
    }

    public void testShouldLookUpExceptionsForApplicationId() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        registry.registerComponent(componentB, applicationIdA, exceptionsB);

        final List<Exceptions<?>> exceptions = registry.getExceptionsForApplication(applicationIdA);
        assertEquals(2, exceptions.size());
        assertEquals(exceptionsA, exceptions.get(0));
        assertEquals(exceptionsB, exceptions.get(1));
    }

    public void testShouldListComponents() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        registry.registerComponent(componentB, applicationIdB, exceptionsB);

        final List<Component> components = registry.getComponents();
        assertEquals(2,components.size());
        assertTrue(components.remove(componentA));
        assertTrue(components.remove(componentB));
        assertEquals(0, components.size());
    }

    public void testShouldListApplicationIDs() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        registry.registerComponent(componentB, applicationIdB, exceptionsB);

        final List<String> applicationIds = registry.getApplicationIds();
        assertEquals(2, applicationIds.size());
        assertTrue(applicationIds.remove(applicationIdA));
        assertTrue(applicationIds.remove(applicationIdB));
        assertTrue(applicationIds.isEmpty());
    }

    public void testShouldListExceptions() throws ComponentAlreadyRegisteredException {
        registry.registerComponent(componentA, applicationIdA, exceptionsA);
        registry.registerComponent(componentB, applicationIdB, exceptionsB);

        final List<Exceptions<?>> exceptions = registry.getExceptions();
        assertEquals(2, exceptions.size());
        assertTrue(exceptions.remove(exceptionsA));
        assertTrue(exceptions.remove(exceptionsB));
        assertTrue(exceptions.isEmpty());
    }


    private static final class DummyExceptions extends Exceptions<AbstractOXException> {

        @Override
        protected void knownExceptions() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected AbstractOXException createException(final ErrorMessage message, final Throwable cause, final Object... args) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

    }

}
