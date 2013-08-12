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

package com.openexchange.rmi.osgi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.rmi.RMIRegistry;

/**
 * {@link RMITrackerCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class RMITrackerCustomizer implements ServiceTrackerCustomizer<Remote, Remote> {

    private static final Log log = com.openexchange.log.Log.loggerFor(RMITrackerCustomizer.class);
    private final BundleContext context;

    public RMITrackerCustomizer(final BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public Remote addingService(final ServiceReference<Remote> reference) {
        final Remote r = context.getService(reference);
        if (r == null) {
            log.warn("Added service is null.");
        } else {
            final String name = RMIRegistry.findRMIName(reference, r);
            try {
                RMIRegistry.getRMIRegistry().bind(name, UnicastRemoteObject.exportObject(r, 0));
            } catch (final AccessException e) {
                log.error(e.getMessage(), e);
            } catch (final RemoteException e) {
                log.error(e.getMessage(), e);
            } catch (final AlreadyBoundException e) {
                log.error(e.getMessage(), e);
            } catch (final OXException e) {
                log.error(e.getMessage(), e);
            }
        }
        return r;
    }

    @Override
    public void modifiedService(final ServiceReference<Remote> reference, final Remote service) {
        //nothing to do
    }

    @Override
    public void removedService(final ServiceReference<Remote> reference, final Remote service) {
        final String name = RMIRegistry.findRMIName(reference, service);
        try {
            RMIRegistry.getRMIRegistry().unbind(name);
            UnicastRemoteObject.unexportObject(service, true);
        } catch (final AccessException e) {
            log.error(e.getMessage(), e);
        } catch (final RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (final NotBoundException e) {
            log.error(e.getMessage(), e);
        } catch (final OXException e) {
            log.error(e.getMessage(), e);
        }
    }

}
