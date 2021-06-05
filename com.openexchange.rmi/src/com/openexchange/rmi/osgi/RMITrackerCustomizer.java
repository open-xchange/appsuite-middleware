/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.rmi.osgi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.rmi.internal.RMIUtility;

/**
 * {@link RMITrackerCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RMITrackerCustomizer implements ServiceTrackerCustomizer<Remote, Remote> {

    private final BundleContext context;
    private final Registry registry;

    /**
     * Initializes a new {@link RMITrackerCustomizer}.
     *
     * @param registry The RMI registry
     * @param context The bundle context
     */
    public RMITrackerCustomizer(Registry registry, BundleContext context) {
        super();
        this.registry = registry;
        this.context = context;
    }

    @Override
    public Remote addingService(ServiceReference<Remote> reference) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RMITrackerCustomizer.class);

        Remote r = context.getService(reference);
        if (null == r) {
            logger.warn("Remote reference is null");
            context.ungetService(reference);
            return null;
        }

        // Try to register/bind it...
        String name = RMIUtility.findRMIName(reference, r);
        try {
            Remote exportObject = UnicastRemoteObject.exportObject(r, 0);
            registry.bind(name, exportObject);
            return r;
        } catch (AccessException e) {
            logger.error("", e);
        } catch (RemoteException e) {
            logger.error("Failed to bind remote reference {} to name {}.", r.getClass().getName(), name, e);
        } catch (AlreadyBoundException e) {
            logger.error("", e);
        } catch (RuntimeException e) {
            logger.error("", e);
        }

        // Apparently failed to register, thus nothing to track...
        logger.warn("Could not register remote reference {}", r.getClass().getName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Remote> reference, Remote r) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<Remote> reference, Remote r) {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RMITrackerCustomizer.class);

        try {
            String name = RMIUtility.findRMIName(reference, r);
            registry.unbind(name);
            UnicastRemoteObject.unexportObject(r, true);
        } catch (AccessException e) {
            logger.error("", e);
        } catch (RemoteException e) {
            logger.error("", e);
        } catch (NotBoundException e) {
            logger.error("", e);
        } catch (RuntimeException e) {
            logger.error("", e);
        } finally {
            context.ungetService(reference);
        }
    }

}
