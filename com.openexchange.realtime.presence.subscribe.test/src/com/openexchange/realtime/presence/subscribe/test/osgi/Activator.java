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

package com.openexchange.realtime.presence.subscribe.test.osgi;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.presence.subscribe.test.IntegrationTest;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(CommandProvider.class.getName(), new TestCommandProvider(), null);
        PresenceSubscriptionService subscriptionService = (PresenceSubscriptionService) context.getService(context.getServiceReference(PresenceSubscriptionService.class.getName()));
        IntegrationTest.subscriptionService = subscriptionService;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

    /**
     * {@link TestCommandProvider}
     * 
     * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
     */
    public class TestCommandProvider implements CommandProvider {

        @Override
        public String getHelp() {
            return "";
        }

        public void _test(final CommandInterpreter commandInterpreter) {
            String testCase = commandInterpreter.nextArgument();
            if (testCase == null) {
                testCase = "IntegrationTest";
            }

            String clazz = "com.openexchange.realtime.presence.subscribe.test." + testCase;
            try {
                Class<?> loaded = Class.forName(clazz);
                JUnitCore jUnit = new JUnitCore();
                Result run = jUnit.run(loaded);
                if (!run.wasSuccessful()) {
                    for (Failure failure : run.getFailures()) {
                        commandInterpreter.print(failure.toString());
                    }
                }
            } catch (ClassNotFoundException e) {
                commandInterpreter.print("Could not load class " + clazz);
            }
        }

    }
}
