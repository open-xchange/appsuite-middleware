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

package com.sun.mail.util;

import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link AddressSelector}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AddressSelector {

    /** The collection of shared <code>RoundRobinSelector</code> instances */
    private static volatile ConcurrentMap<String, RoundRobinSelector> selectors;

    /**
     * Gets the collection of shared <code>RoundRobinSelector</code> instances
     *
     * @return The collection
     */
    private static ConcurrentMap<String, RoundRobinSelector> selectors() {
        ConcurrentMap<String, RoundRobinSelector> tmp = selectors;
        if (null == selectors) {
            synchronized (AddressSelector.class) {
                tmp = selectors;
                if (null == selectors) {
                    tmp = new ConcurrentHashMap<>(256, 0.9F, 1);
                    selectors = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Gets the suitable address selector for specified arguments.
     *
     * @param host The host name
     * @param addresses The IP addresses to which the host name has been resolved
     * @param props The properties
     * @param prefix The prefix to use; e.g. <code>"mail.imap"</code>
     * @return The (possibly shared) address selector
     */
    static AddressSelector getSelectorFor(String host, InetAddress[] addresses, Properties props, String prefix) {
        int hash = PropUtil.getIntProperty(props, prefix + ".multiAddress.key", -1);
        if (hash >= 0) {
            return new StaticSelector(addresses, hash);
        }

        ConcurrentMap<String, RoundRobinSelector> selectors = selectors();
        RoundRobinSelector selector = selectors.get(host);
        if (null == selector) {
            RoundRobinSelector newSelector = new RoundRobinSelector(addresses);
            selector = selectors.putIfAbsent(host, newSelector);
            if (null == selector) {
                selector = newSelector;
            }
        } else if (false == selector.stillValid(addresses)) {
            RoundRobinSelector newSelector = new RoundRobinSelector(addresses);
            if (selectors.replace(host, selector, newSelector)) {
                selector = newSelector;
            } else {
                selector = selectors.get(host);
            }
        }
        return selector;
    }

    // -------------------------------------------------------------------------------

    protected AddressSelector() {
        super();
    }

    /**
     * Gets the number of addresses managed by this selector.
     *
     * @return The number of available addresses
     */
    abstract int length();

    /**
     * Gets the next address to use.
     *
     * @return The next address
     */
    abstract InetAddress nextAddress();

    // -------------------------------------------------------------------------------------

    private static final class RoundRobinSelector extends AddressSelector {

        private final AtomicInteger counter;
        private final InetAddress[] addresses;
        private final int length;

        RoundRobinSelector(InetAddress[] addresses) {
            super();
            this.addresses = addresses;
            length = addresses.length;
            counter = new AtomicInteger(length);
        }

        boolean stillValid(InetAddress[] a2) {
            InetAddress[] a = this.addresses;
            if (a==a2) {
                return true;
            }
            if (a==null || a2==null) {
                return false;
            }

            int length = a.length;
            if (a2.length != length) {
                return false;
            }

            for (int i=0; i<length; i++) {
                InetAddress addr1 = a[i];
                boolean found = false;
                for (int j = 0; !found && j < a2.length; j++) {
                    found = a2[j].equals(addr1);
                }
                if (!found) {
                    return false;
                }
            }

            return true;
        }

        @Override
        int length() {
            return length;
        }

        @Override
        InetAddress nextAddress() {
            int next = counter.incrementAndGet();
            while (next < 0) {
                int newNext = length;
                if (counter.compareAndSet(next, newNext)) {
                    next = newNext;
                } else {
                    next = counter.incrementAndGet();
                }
            }
            return addresses[next % length];
        }
    }

    private static final class StaticSelector extends AddressSelector {

        private final InetAddress address;

        StaticSelector(InetAddress[] addresses, int hash) {
            super();
            int length = addresses.length;
            address = addresses[hash % length];
        }

        @Override
        int length() {
            return 1;
        }

        @Override
        InetAddress nextAddress() {
            return address;
        }
    }

}
