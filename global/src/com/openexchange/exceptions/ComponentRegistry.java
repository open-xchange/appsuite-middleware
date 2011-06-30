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
import com.openexchange.groupware.Component;

/**
 * The component registry makes sure that the component shorthand for messages is uniquely assigned to individual applications (bundles). It
 * keeps track of all error messages registered in the server.
 * 
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ComponentRegistry {

    /**
     * Called during bundle startup in the Activator of a given bundle to claim a component String. Upon success the given exceptions class
     * is registered in the server and initialized with the given component and applicationId. If the component has already been taken a
     * ComponentAlreadyRegisteredException is thrown.
     * 
     * @param component The component String the client wants to claim.
     * @param applicationId The application identifier of the client. Usually the bundle identifier, in all cases this should be in java
     *            package (reverse dns) notation.
     * @param exceptions The Exceptions subclass the bundle will use to create its exceptions.
     * @throws ComponentAlreadyRegisteredException Thrown when the component has already been claimed by another application.
     */
    public void registerComponent(Component component, String applicationId, Exceptions<?> exceptions) throws ComponentAlreadyRegisteredException;

    /**
     * Called during bundle shutdown to remove the registration of a component.
     * 
     * @param component The component to deregister
     */
    public void deregisterComponent(Component component);

    /**
     * Looks up the exceptions subclass responsible for the error messages of a given component.
     * 
     * @param component The component.
     * @return The Exceptions subclass registered for the given component. Returns null, if the component was not registered.
     */
    public Exceptions<?> getExceptionsForComponent(Component component);

    /**
     * Looks up the exceptions subclasses responsible for the error messages of a given application.
     * 
     * @param applicationId The application identifier. Usually the bundle identifier, in all cases this should be in java package (reverse
     *            dns) notation.
     * @return The Exceptions subclass registered for the given application. Returns null, if the application was not registered.
     */
    public List<Exceptions<?>> getExceptionsForApplication(String applicationId);

    /**
     * Returns a list of all registered components
     * 
     * @return A list of all components registered with this component registry.
     */
    public List<Component> getComponents();

    /**
     * Returns a list of all application ids.
     * 
     * @return A list of all application ids registered with this component registry.
     */
    public List<String> getApplicationIds();

    /**
     * Returns a list of all Exceptions subclasses registered with this component registry.
     * 
     * @return A list of all Exceptions subclasses registered with this component registry.
     */
    public List<Exceptions<?>> getExceptions();

}
