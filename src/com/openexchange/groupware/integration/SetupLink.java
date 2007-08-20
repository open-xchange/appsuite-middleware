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

package com.openexchange.groupware.integration;

import java.net.URL;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.configuration.SystemConfig.Property;
import com.openexchange.groupware.integration.SetupLinkException.Code;

/**
 * This interface defines the methods that will be used to generate browser
 * links that redirect the user to the setup system.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class SetupLink {

    /**
     * Implementing sub class.
     */
    private static Class< ? extends SetupLink> implementingClass;

    /**
     * Default constructor.
     */
    protected SetupLink() {
        super();
    }

    /**
     * Factory method for an instance of SetupLink.
     * @return an instance implementing the getLink() method.
     * @throws SetupLinkException if instantiation fails.
     */
    public static SetupLink getInstance() throws SetupLinkException {
        SetupLink instance = null;
        try {
            instance = implementingClass.newInstance();
            instance.initialize();
        } catch (InstantiationException e) {
            throw new SetupLinkException(Code.INSTANTIATION_FAILED, e);
        } catch (IllegalAccessException e) {
            throw new SetupLinkException(Code.INSTANTIATION_FAILED, e);
        }
        return instance;
    }

    /**
     * This method has to return the user specific link to the setup system.
     * @param values the implementation of this method can define a number of
     * objects to pass for generating a user specific link.
     * @return a ready to use link to redirect the user to the setup system.
     * @throws SetupLinkException if creating the url fails.
     */
    public abstract URL getLink(Object... values) throws SetupLinkException;

    /**
     * Initialization of the setup link class.
     * @throws SetupLinkException if initialization fails.
     */
    protected abstract void initialize() throws SetupLinkException;

    /**
     * Initializes the login info implementation.
     * @throws SetupLinkException if initialization fails.
     */
    public static void init() throws SetupLinkException {
        if (null != implementingClass) {
            return;
        }
        final String className = SystemConfig.getProperty(Property.SETUP_LINK);
        if (null == className) {
            throw new SetupLinkException(Code.MISSING_SETTING, Property
                .SETUP_LINK.getPropertyName());
        }
        try {
            implementingClass = Class.forName(className)
                .asSubclass(SetupLink.class);
        } catch (ClassNotFoundException e) {
            throw new SetupLinkException(Code.CLASS_NOT_FOUND, e, className);
        } catch (ClassCastException e) {
            throw new SetupLinkException(Code.CLASS_NOT_FOUND, e, className);
        }
    }

    protected int getContextId(final Object... values) {
        return (Integer) values[0];
    }

    protected String getUserLogin(final Object... values) {
        return (String) values[1];
    }

    protected String getPassword(final Object... values) {
        return (String) values[2];
    }
}
