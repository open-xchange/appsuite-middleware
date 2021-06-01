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

package com.openexchange.imap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.Protocol;

/**
 * {@link IMAPProtocol} - The IMAP protocol.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPProtocol extends Protocol {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPProtocol.class);

    private static final long serialVersionUID = 7946276250330261425L;

    private static final IMAPProtocol INSTANCE = new IMAPProtocol();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static IMAPProtocol getInstance() {
        return INSTANCE;
    }

    private volatile ConcurrentMap<InetAddress, Integer> map;

    private volatile Integer overallExternalMaxCount;

    private volatile Integer maxCount;

    /**
     * Initializes a new {@link IMAPProtocol}.
     */
    private IMAPProtocol() {
        super("imap", "imaps");
        map = null;
        overallExternalMaxCount = Integer.valueOf(-1);
    }

    /**
     * Sets the max. count.
     *
     * @param maxCount The max. count
     */
    public void setMaxCount(int maxCount) {
        this.maxCount = Integer.valueOf(maxCount);
    }

    /**
     * Sets the overall max. count for external accounts.
     *
     * @param overallMaxCount The max. count
     */
    public void setOverallExternalMaxCount(int overallMaxCount) {
        map = null;
        this.overallExternalMaxCount = Integer.valueOf(overallMaxCount);
    }

    /**
     * Initializes the max-count map.
     */
    public void initExtMaxCountMap() {
        map = new NonBlockingHashMap<InetAddress, Integer>(4);
        overallExternalMaxCount = null;
    }

    /**
     * Inserts specified max-count setting for given host if no such mapping exists.
     *
     * @param host The mail system's host name
     * @param maxCount The max-count
     * @return <code>true</code> for successful insertion; otherwise <code>false</code>
     */
    public boolean putIfAbsent(String host, int maxCount) {
        final ConcurrentMap<InetAddress, Integer> concurrentMap = map;
        if (null == concurrentMap) {
            return false;
        }
        try {
            return (null == concurrentMap.putIfAbsent(InetAddress.getByName(host), Integer.valueOf(maxCount)));
        } catch (UnknownHostException e) {
            LOG.warn("Couldn't resolve host name: {}. Assume default max-count setting instead.", host, e);
            return false;
        }
    }

    /**
     * Removes the max-count setting for specified host.
     *
     * @param host The mail system's host name
     */
    public void remove(String host) {
        final ConcurrentMap<InetAddress, Integer> concurrentMap = map;
        if (null == concurrentMap) {
            return;
        }
        try {
            concurrentMap.remove(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            LOG.warn("Couldn't remove max-count setting for: {}", host, e);
        }
    }

    @Override
    public int getMaxCount(String host, boolean primary) throws OXException {
        final Integer thisMaxCount = maxCount;
        if (primary) {
            return null == thisMaxCount ? -1 : thisMaxCount.intValue();
        }
        final Integer maxCount = overallExternalMaxCount;
        if (null != maxCount) {
            return minOf(maxCount.intValue(), thisMaxCount);
        }
        final ConcurrentMap<InetAddress, Integer> concurrentMap = map;
        if (null == concurrentMap) {
            return null == thisMaxCount ? -1 : thisMaxCount.intValue();
        }
        try {
            final Integer mc = concurrentMap.get(InetAddress.getByName(host));
            return mc == null ? (null == thisMaxCount ? -1 : thisMaxCount.intValue()) : minOf(mc.intValue(), thisMaxCount);
        } catch (UnknownHostException e) {
            LOG.warn("Couldn't resolve host name: {}. Return default max-count setting instead.", host, e);
            return (null == thisMaxCount ? -1 : thisMaxCount.intValue());
        }
    }

    private static int minOf(int maxCount, Integer thisMaxCount) {
        if (null == thisMaxCount) {
            return maxCount;
        }
        final int thisMC = thisMaxCount.intValue();
        return thisMC <= maxCount ? thisMC : maxCount;
    }

}
