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

package com.openexchange.osgi.osgi;

import com.openexchange.management.ManagementService;
import com.openexchange.osgi.DeferredActivator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.console.ServiceStateLookup;
import com.openexchange.osgi.console.osgi.ConsoleActivator;

/**
 * {@link OsgiActivator} - Activator for OSGi-Bundle
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class OsgiActivator extends HousekeepingActivator {

    private volatile ConsoleActivator consoleActivator;

    /**
     * Initializes a new {@link OsgiActivator}.
     */
    public OsgiActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OsgiActivator.class);
        logger.info("starting bundle: com.openexchange.osgi");
        try {
            registerService(ServiceStateLookup.class, DeferredActivator.getLookup());
            track(ManagementService.class, new ManagementRegisterer(context));
            openTrackers();
            final ConsoleActivator consoleActivator = new ConsoleActivator();
            consoleActivator.start(context);
            this.consoleActivator = consoleActivator;
        } catch (final Exception e) {
            logger.error("OsgiActivator: start", e);
            throw e;
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OsgiActivator.class);
        logger.info("stopping bundle: com.openexchange.osgi");
        try {
            final ConsoleActivator consoleActivator = this.consoleActivator;
            if (null != consoleActivator) {
                consoleActivator.stop(context);
                this.consoleActivator = null;
            }
            cleanUp();
        } catch (final Exception e) {
            logger.error("OsgiActivator: stop", e);
            throw e;
        }
    }

}
