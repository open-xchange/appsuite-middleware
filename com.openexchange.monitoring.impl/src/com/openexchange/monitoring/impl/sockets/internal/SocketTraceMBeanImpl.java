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

package com.openexchange.monitoring.impl.sockets.internal;

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
import com.openexchange.monitoring.impl.sockets.TracingSocketMonitor;
import com.openexchange.monitoring.impl.sockets.TracingSocketMonitor.SocketTrace;
import com.openexchange.monitoring.sockets.SocketTraceMBean;
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
        if (Strings.isNotEmpty(ports)) {
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
            if ((null == filterPorts || Arrays.binarySearch(filterPorts, socket.getPort()) >= 0) && (null == hostList || (!hostList.isEmpty() && hostList.contains(socket.getInetAddress())))) {
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
        if (Strings.isNotEmpty(ports)) {
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
            if ((null == filterPorts || Arrays.binarySearch(filterPorts, socket.getPort()) >= 0) && (null == hostList || (!hostList.isEmpty() && hostList.contains(socket.getInetAddress())))) {
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
