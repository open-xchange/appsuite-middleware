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

package com.openexchange.heapdump;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import com.openexchange.java.Strings;


/**
 * {@link HeapDumper} - Command-line tool to obtain a heap dump.
 * <p>
 * Aligned to <a href="https://blogs.oracle.com/sundararajan/entry/programmatically_dumping_heap_from_java">this article</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HeapDumper {

    /** This is the name of the HotSpot Diagnostic MBean */
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";

    /** field to store the HotSpot diagnostic MBean */
    private static volatile com.sun.management.HotSpotDiagnosticMXBean hotspotMBean;

    /**
     * Call this method from your application whenever you want to dump the heap snapshot into a file.
     *
     * @param fileName The name of the heap dump file
     * @param live The flag that tells whether to dump only the live objects
     */
    static void dumpHeap(String fileName, boolean live) {
        // Initialize HotSpot diagnostic MBean
        initHotspotMBean();
        try {
            hotspotMBean.dumpHeap(fileName, live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * Initializes the HotSpot diagnostic MBean field
     */
    private static void initHotspotMBean() {
        if (hotspotMBean == null) {
            synchronized (HeapDumper.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    /**
     * Gets the HotSpot diagnostic MBean from the platform MBean server
     *
     * @return The HotSpot diagnostic MBean
     */
    private static com.sun.management.HotSpotDiagnosticMXBean getHotspotMBean() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Class<com.sun.management.HotSpotDiagnosticMXBean> mxbeanInterface = com.sun.management.HotSpotDiagnosticMXBean.class;
            return ManagementFactory.newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, mxbeanInterface);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    public static void main(String[] args) {
        // Default heap dump file name
        String fileName = "heap.bin";

        // By default dump only the live objects
        boolean live = true;

        // Simple command line options
        switch (args.length) {
        case 2:
            live = Boolean.parseBoolean(args[1]);
            //$FALL-THROUGH$
        case 1:
            fileName = args[0];
        }

        if (Strings.isEmpty(fileName)) {
            fileName = "heap.bin";
        }

        // Dump the heap
        dumpHeap(fileName, live);
    }

}
