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

package com.openexchange.rmi.internal;

import java.lang.reflect.Field;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import org.osgi.framework.ServiceReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.rmi.exceptions.RMIExceptionCodes;

/**
 * {@link RMIUtility}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RMIUtility {

    /**
     * Initializes a new {@link RMIUtility}.
     */
    private RMIUtility() {
        super();
    }

    /**
     * Creates a new <code>java.rmi.registry.Registry</code> instance.
     *
     * @param configService The configuration service to utilize
     * @return The <code>java.rmi.registry.Registry</code> instance
     * @throws OXException If initialization fails
     */
    public static Registry createRegistry(ConfigurationService configService) throws OXException {
        try {
            int port = configService.getIntProperty("com.openexchange.rmi.port", 1099);
            String hostname = configService.getProperty("com.openexchange.rmi.host", "localhost");
            return LocateRegistry.createRegistry(port, RMISocketFactory.getDefaultSocketFactory(), new LocalServerFactory(hostname));
        } catch (RemoteException e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RMIUtility.class);
            logger.error("", e);
            throw RMIExceptionCodes.RMI_CREATE_REGISTRY_FAILED.create(e);
        }
    }

    /**
     * Looks up the appropriate name to associate with the remote reference
     *
     * @param reference The service reference for the remote reference
     * @param r The remote reference
     * @return The name to associate with the remote reference
     */
    public static String findRMIName(ServiceReference<Remote> reference, Remote r) {
        // Check for "RMIName"/"RMI_NAME" service property
        {
            Object name = reference.getProperty("RMIName");
            if (name != null) {
                return (String) name;
            }
            name = reference.getProperty("RMI_NAME");
            if (name != null) {
                return (String) name;
            }
        }

        // Look-up by Java Reflection
        try {
            Field field = r.getClass().getField("RMI_NAME");
            return (String) field.get(r);
        } catch (SecurityException e) {
            return r.getClass().getSimpleName();
        } catch (NoSuchFieldException e) {
            return r.getClass().getSimpleName();
        } catch (IllegalArgumentException e) {
            return r.getClass().getSimpleName();
        } catch (IllegalAccessException e) {
            return r.getClass().getSimpleName();
        }
    }

}
