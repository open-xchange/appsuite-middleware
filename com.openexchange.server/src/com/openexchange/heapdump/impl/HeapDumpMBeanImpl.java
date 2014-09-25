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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.heapdump.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.heapdump.HeapDumpMBean;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link HeapDumpMBeanImpl}
 * <p>
 * Aligned to <a href="https://blogs.oracle.com/sundararajan/entry/programmatically_dumping_heap_from_java">this article</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HeapDumpMBeanImpl extends StandardMBean implements HeapDumpMBean {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HeapDumpMBeanImpl.class);

    /** This is the name of the HotSpot Diagnostic MBean */
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    /**
     * Initializes a new {@link HeapDumpMBeanImpl}.
     *
     * @throws NotCompliantMBeanException If initialization fails
     */
    public HeapDumpMBeanImpl() throws NotCompliantMBeanException  {
        super(HeapDumpMBean.class);
    }

    @Override
    public void dumpHeap(String fileName, boolean live) throws MBeanException {
        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        String jmxLogin = configService.getProperty("JMXLogin");
        String jmxPassword = configService.getProperty("JMXPassword");

        // Build JMX environment
        Map<String, Object> environment;
        if (Strings.isEmpty(jmxLogin) || Strings.isEmpty(jmxPassword)) {
            environment = null;
        } else {
            environment = new HashMap<String, Object>(1);
            environment.put(JMXConnectorServer.AUTHENTICATOR, new JMXAuthenticatorImpl(jmxLogin, jmxPassword));
        }

        // Invoke MBean
        JMXConnector jmxConnector = null;
        try {
            int port = configService.getIntProperty("JMXPort", 9999);
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server");
            jmxConnector = JMXConnectorFactory.connect(url, environment);

            MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();
            ObjectName name = new ObjectName(HOTSPOT_BEAN_NAME);

            String fn = fileName;
            if (Strings.isEmpty(fn)) {
                fn = "heap.bin";
            }
            mbsc.invoke(name, "dumpHeap", new Object[] { fn, Boolean.valueOf(live) }, new String[]{ String.class.getCanonicalName(), "boolean"});

            LOGGER.info("Heap snapshot successfully dumped to file {}", fileName);
        } catch (RuntimeException e) {
            LOGGER.error("Heap snapshot could not be dumped to file {}", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        } catch (Exception e) {
            LOGGER.error("Heap snapshot could not be dumped to file {}", e);
            String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        } finally {
            if (null != jmxConnector) {
                try {
                    jmxConnector.close();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------ //

    private class JMXAuthenticatorImpl implements JMXAuthenticator {

        private final String login;
        private final String password;

        /**
         * Initializes a new {@link JMXAuthenticatorImpl}.
         *
         * @param login The login
         * @param password The password
         */
        public JMXAuthenticatorImpl(final String login, final String password) {
            super();
            this.login = login;
            this.password = password;
        }

        @Override
        public Subject authenticate(final Object credentials) {
            if (!(credentials instanceof String[])) {
                if (credentials == null) {
                    throw new SecurityException("Credentials required");
                }
                throw new SecurityException("Credentials should be String[]");
            }
            final String[] creds = (String[]) credentials;
            if (creds.length != 2) {
                throw new SecurityException("Credentials should have 2 elements");
            }
            /*
             * Perform authentication
             */
            final String username = creds[0];
            final String testPassword = creds[1];
            if (login.equals(username) && password.equals(testPassword)) {
                return new Subject(true, Collections.singleton(new JMXPrincipal(username)), Collections.EMPTY_SET, Collections.EMPTY_SET);
            }
            throw new SecurityException("Invalid credentials");
        }

    }

}
