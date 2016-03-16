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

package com.openexchange.loxandra.util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.tools.NodeProbe;


/**
 * {@link ClusterUtils}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ClusterUtils {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClusterUtils.class);

    /**
     * Re-Balance the cluster by assigning a new token to each node.
     *
     * @param nodes String array with ring's IP addresses
     */
    public static void rebalance(String[] nodes) {
        //BasicConfigurator.configure();
        BigInteger[] tokens = TokenUtils.calculateTokens(nodes.length);
        NodeProbe np;

        for (int i = 0; i < tokens.length; i++) {
            try {
                log.info("Moving node {} to token {}", nodes[i], tokens[i]);
                np = new NodeProbe(nodes[i], 7199);
                np.move(tokens[i].toString());
            } catch (IOException e) {
                log.error("Error connection to remote JMX agent!");
                log.error(e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void cleanup(String[] nodes) {
        BigInteger[] tokens = TokenUtils.calculateTokens(nodes.length);
        NodeProbe np;

        for (int i = 0; i < tokens.length; i++) {
            try {
                log.info("Cleaning up {}", nodes[i]);
                np = new NodeProbe(nodes[i], 7199);
                np.forceTableCleanup("OX", new String[0]);
                np.forceTableCleanup("OpsCenter", new String[0]);
            } catch (IOException e) {
                log.error("Error connection to remote JMX agent!");
                log.error(e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void flush(String[] nodes) {
        BigInteger[] tokens = TokenUtils.calculateTokens(nodes.length);
        NodeProbe np;

        for (int i = 0; i < tokens.length; i++) {
            try {
                log.info("Cleaning up {}", nodes[i]);
                np = new NodeProbe(nodes[i], 7199);
                np.forceTableFlush("OX", new String[0]);
                np.forceTableCleanup("OpsCenter", new String[0]);
            } catch (IOException e) {
                log.error("Error connection to remote JMX agent!");
                log.error(e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }


    private enum Level {ONE, QUORUM, ALL};

    public static void calculateConsistencies(int clusterSize, int replicationFactor, Level writeLevel, Level readLevel) {
        int r = realSize(replicationFactor, readLevel);
        int w = realSize(replicationFactor, writeLevel);

        System.out.println("Reads are " + ((r + w > replicationFactor) ? "consistent." : "eventually consistent."));
        System.out.println("Reading from " + ((r > 1) ? r + " nodes" : 1 + " node"));
        System.out.println("Writing to " + ((w > 1) ? w + " nodes" : 1 + " node"));

        int survival = replicationFactor - Math.max(r, w);

        System.out.println("The cluster can survive the loss of " +
                ((survival > 1) ? survival + " nodes" : survival == 1 ? "1 node" : "no nodes"));
        System.out.println("Every node holds "
                + (((float) replicationFactor / (float) clusterSize) * 100)
                + " % of data");
    }

    private static int realSize(int n, Level l) {
        switch(l) {
        case ONE:
            return 1;
        case QUORUM:
            return (int)Math.floor(n / 2 + 1);
        case ALL:
            return n;
        default:
            return 0;
        }
    }
}
