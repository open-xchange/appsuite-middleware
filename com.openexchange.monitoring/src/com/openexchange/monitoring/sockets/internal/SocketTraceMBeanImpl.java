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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.monitoring.sockets.internal;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import com.openexchange.java.Strings;
import com.openexchange.management.AnnotatedStandardMBean;
import com.openexchange.monitoring.sockets.SocketTraceMBean;
import com.openexchange.monitoring.sockets.TracingSocketMonitor;
import com.openexchange.monitoring.sockets.TracingSocketMonitor.SocketTrace;
import com.openexchange.net.HostList;


/**
 * {@link SocketTraceMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SocketTraceMBeanImpl extends AnnotatedStandardMBean implements SocketTraceMBean {

    private final TracingSocketMonitor monitor;

    /**
     * Initializes a new {@link SocketTraceMBeanImpl}.
     *
     * @param monitor The monitor tracing sockets
     * @throws NotCompliantMBeanException
     */
    public SocketTraceMBeanImpl(TracingSocketMonitor monitor) throws NotCompliantMBeanException {
        super("Management Bean for traced Sockets", SocketTraceMBean.class);
        this.monitor = monitor;
    }

    @Override
    public long getAverageDuration(String hostNames, String ports) throws MBeanException {
        HostList hostList = Strings.isEmpty(hostNames) ? null : HostList.valueOf(hostNames);
        int[] filterPorts = null;
        if (!Strings.isEmpty(ports)) {
            try {
                String[] sPorts = Strings.splitByComma(ports);
                filterPorts = new int[sPorts.length];
                for (int i = 0; i < sPorts.length; i++) {
                    filterPorts[i] = Integer.parseInt(sPorts[i]);
                }
                Arrays.sort(filterPorts);
            } catch (NumberFormatException e) {
                // Ignore invalid port filter
                filterPorts = null;
            }
        }

        BigInteger avg = null;
        int numSamples = 0;
        for (SocketTrace trace : monitor.getSocketTraces()) {
            Socket socket = trace.getSocket();
            if ((null == filterPorts || Arrays.binarySearch(filterPorts, socket.getPort()) >= 0) && (null == hostList || hostList.contains(socket.getInetAddress()))) {
                long averageDuration = trace.getSamples().getAverageDuration();
                if (averageDuration > 0) {
                    BigInteger cur = BigInteger.valueOf(averageDuration);
                    avg = null == avg ? cur : avg.add(cur);
                    numSamples++;
                }
            }
        }
        return avg == null ? 0 : avg.divide(BigInteger.valueOf(numSamples)).longValue();
    }

    @Override
    public long getAverageTimeouts(String hostNames, String ports) throws MBeanException {
        HostList hostList = Strings.isEmpty(hostNames) ? null : HostList.valueOf(hostNames);
        int[] filterPorts = null;
        if (!Strings.isEmpty(ports)) {
            try {
                String[] sPorts = Strings.splitByComma(ports);
                filterPorts = new int[sPorts.length];
                for (int i = 0; i < sPorts.length; i++) {
                    filterPorts[i] = Integer.parseInt(sPorts[i]);
                }
                Arrays.sort(filterPorts);
            } catch (NumberFormatException e) {
                // Ignore invalid port filter
                filterPorts = null;
            }
        }

        BigInteger avg = null;
        int numSamples = 0;
        for (SocketTrace trace : monitor.getSocketTraces()) {
            Socket socket = trace.getSocket();
            if ((null == filterPorts || Arrays.binarySearch(filterPorts, socket.getPort()) >= 0) && (null == hostList || hostList.contains(socket.getInetAddress()))) {
                long averageDuration = trace.getSamples().getAverageTimeout();
                if (averageDuration > 0) {
                    BigInteger cur = BigInteger.valueOf(averageDuration);
                    avg = null == avg ? cur : avg.add(cur);
                    numSamples++;
                }
            }
        }
        return avg == null ? 0 : avg.divide(BigInteger.valueOf(numSamples)).longValue();
    }

    @Override
    public List<List<String>> listAverageDurations() throws MBeanException {
        List<SocketTrace> socketTraces = monitor.getSocketTraces();

        Map<InetAddress, List<SocketTrace>> traces = new HashMap<>(socketTraces.size());
        for (SocketTrace trace : socketTraces) {
            List<SocketTrace> list = traces.get(trace.getSocket().getInetAddress());
            if (null == list) {
                list = new LinkedList<>();
                traces.put(trace.getSocket().getInetAddress(), list);
            }
            list.add(trace);
        }
        socketTraces = null; // No more needed

        TreeMap<String, Long> durations = new TreeMap<>();
        for (Map.Entry<InetAddress, List<SocketTrace>> entry : traces.entrySet()) {
            BigInteger avg = null;
            int numSamples = 0;
            for (SocketTrace trace : entry.getValue()) {
                long averageDuration = trace.getSamples().getAverageDuration();
                if (averageDuration > 0) {
                    BigInteger cur = BigInteger.valueOf(averageDuration);
                    avg = null == avg ? cur : avg.add(cur);
                    numSamples++;
                }
            }
            if (null != avg) {
                durations.put(entry.getKey().toString(), Long.valueOf(avg.divide(BigInteger.valueOf(numSamples)).longValue()));
            }
        }
        traces = null; // No more needed

        int size = durations.size();
        List<List<String>> list = new ArrayList<List<String>>(size);
        for (Map.Entry<String, Long> entry : durations.entrySet()) {
            list.add(Arrays.asList(entry.getKey(), entry.getValue().toString()));
        }
        return list;
    }

}
