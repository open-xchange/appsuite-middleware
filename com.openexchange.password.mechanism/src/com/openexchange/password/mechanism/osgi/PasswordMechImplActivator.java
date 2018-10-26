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

package com.openexchange.password.mechanism.osgi;

import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.impl.mech.PasswordMechRegistryImpl;

/**
 * {@link PasswordMechImplActivator}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - moved from c.o.global
 * @since v7.10.1
 */
public class PasswordMechImplActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        long before = currentTimeMillis();
        getLogger(PasswordMechImplActivator.class).info("Starting bundle {}", context.getBundle().getSymbolicName());

        Services.setServiceLookup(this);

        PasswordMechRegistryImpl passwordMechRegistryImpl = new PasswordMechRegistryImpl(getService(ConfigurationService.class));

        track(PasswordMech.class, new PasswordMechServiceTrackerCustomizer(context, passwordMechRegistryImpl));
        openTrackers();

        registerService(PasswordMechRegistry.class, passwordMechRegistryImpl, null);
        registerService(Reloadable.class, passwordMechRegistryImpl);
        long after = currentTimeMillis();
        long duration = after - before;
        if (duration > 20000) {
            getLogger(PasswordMechImplActivator.class).warn("STARTING BUNDLE {} TOOK {}ms! PLEASE MAKE SURE TO HAVE AN APPROPRIATE LEVEL OF ENTROPY ON THE SYSTEM!", context.getBundle().getSymbolicName(), duration);
        } else {
            getLogger(PasswordMechImplActivator.class).info("Starting bundle {} took {}ms", context.getBundle().getSymbolicName(), duration);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        getLogger(PasswordMechImplActivator.class).info("Stopping bundle {}", context.getBundle().getSymbolicName());

        Services.setServiceLookup(null);

        super.stopBundle();
    }
}
