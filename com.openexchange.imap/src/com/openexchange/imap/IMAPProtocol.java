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
    public void setMaxCount(final int maxCount) {
        this.maxCount = Integer.valueOf(maxCount);
    }

    /**
     * Sets the overall max. count for external accounts.
     *
     * @param overallMaxCount The max. count
     */
    public void setOverallExternalMaxCount(final int overallMaxCount) {
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
    public boolean putIfAbsent(final String host, final int maxCount) {
        final ConcurrentMap<InetAddress, Integer> concurrentMap = map;
        if (null == concurrentMap) {
            return false;
        }
        try {
            return (null == concurrentMap.putIfAbsent(InetAddress.getByName(host), Integer.valueOf(maxCount)));
        } catch (final UnknownHostException e) {
            LOG.warn("Couldn't resolve host name: {}. Assume default max-count setting instead.", host, e);
            return false;
        }
    }

    /**
     * Removes the max-count setting for specified host.
     *
     * @param host The mail system's host name
     */
    public void remove(final String host) {
        final ConcurrentMap<InetAddress, Integer> concurrentMap = map;
        if (null == concurrentMap) {
            return;
        }
        try {
            concurrentMap.remove(InetAddress.getByName(host));
        } catch (final UnknownHostException e) {
            LOG.warn("Couldn't remove max-count setting for: {}", host, e);
        }
    }

    @Override
    public int getMaxCount(final String host, final boolean primary) throws OXException {
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
        } catch (final UnknownHostException e) {
            LOG.warn("Couldn't resolve host name: {}. Return default max-count setting instead.", host, e);
            return (null == thisMaxCount ? -1 : thisMaxCount.intValue());
        }
    }

    private static int minOf(final int maxCount, final Integer thisMaxCount) {
        if (null == thisMaxCount) {
            return maxCount;
        }
        final int thisMC = thisMaxCount.intValue();
        return thisMC <= maxCount ? thisMC : maxCount;
    }

}
