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

package com.openexchange.test.osgi;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import junit.framework.TestSuite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * {@link JUnitCollector}
 * 
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class JUnitCollector implements ServiceListener {

    private final ConcurrentMap<String, TestSuite> jUnitServices;

    private final BundleContext context;

    private volatile boolean open;

    /**
     * Initializes a new {@link JUnitCollector}.
     * 
     * @param context The bundle context
     */
    public JUnitCollector(final BundleContext context) {
        super();
        jUnitServices = new ConcurrentHashMap<String, TestSuite>();
        this.context = context;
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (!open) {
            return;
        }
        final int type = event.getType();
        if (ServiceEvent.REGISTERED == type) {
            add(event.getServiceReference());
        } else if (ServiceEvent.UNREGISTERING == type) {
            remove(event.getServiceReference());
        }
    }

    /**
     * Opens this collector.
     */
    public void open() {
//        System.out.println("Starting opening progress");
        try {
            final ServiceReference<?>[] allServiceReferences = context.getServiceReferences(TestSuite.class.getName(), null);
//            System.out.println("im still there");
            if (allServiceReferences != null && allServiceReferences.length > 0) {
                for (final ServiceReference<?> serviceReference : allServiceReferences) {
                    add(serviceReference);
                }    
            }
            

//        } catch (final InvalidSyntaxException e) {
//            // Impossible, no filter specified.
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("finished opening");
        
        open = true;
    }

    /**
     * Closes this collector.
     */
    public void close() {
        open = false;
        for (final Entry<String, TestSuite> entry : jUnitServices.entrySet()) {
            remove(entry.getKey(), entry.getValue());
        }
    }

    private void remove(final ServiceReference<?> ref) {
        final Object service = context.getService(ref);

        if (isJUnit(service)) {
            final String name = getName(ref, service);
            remove(name, service);
        }
    }

    private void add(final ServiceReference<?> ref) {
        
        final Object service = context.getService(ref);
        if (isJUnit(service)) {
            final String name = getName(ref, service);
            jUnitServices.put(name,(TestSuite) service);
            
            //vorlaeufig
            System.out.println("added " + name + " as OSGI Test" );
            runAllTests();
            System.out.println("ran all tests");
        }
    }

    private String getName(final ServiceReference<?> ref, final Object service) {
        return ((TestSuite)service).getName();
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    public void runAllTests(){
        JUnitTestExecutor executor = new JUnitTestExecutor();
        executor.runAllTests(jUnitServices.values().toArray(new TestSuite[jUnitServices.size()]));
    }
    
    private void remove(final String name, final Object service) {
        jUnitServices.remove(name);
    }

    private boolean isJUnit(final Object service) {
        return (service.getClass().getName() == TestSuite.class.getName() ? true : false);
    }
}
