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

package com.openexchange.inet.proxy.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.inet.proxy.InetProxyInformation;

/**
 * {@link CustomProxySelector}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomProxySelector extends ProxySelector {

    /**
     * Inner class representing a Proxy and a few extra data
     */
    static final class InnerProxy {

        Proxy proxy;
        SocketAddress addr;
        // How many times did we fail to reach this proxy?
        int failedCount = 0;

        InnerProxy(final InetSocketAddress a) {
            addr = a;
            proxy = new Proxy(Proxy.Type.HTTP, a);
        }

        SocketAddress address() {
            return addr;
        }

        Proxy toProxy() {
            return proxy;
        }

        int failed() {
            return ++failedCount;
        }
    }

    /** Keep a reference on the previous default. */
    private final ProxySelector defsel;

    /** A list of proxies, indexed by their address. */
    private final Map<SocketAddress, InnerProxy> proxies;

    /** The proxy information */
    private final InetProxyInformation proxyInformation;

    /**
     * Initializes a new {@link CustomProxySelector}.
     * 
     * @param def The default selector; see {@link ProxySelector#getDefault()}
     * @param proxyInformation The Inet proxy information
     */
    public CustomProxySelector(final ProxySelector def, final InetProxyInformation proxyInformation) {
        super();
        this.proxyInformation = proxyInformation;
        // Save the previous default
        defsel = def;
        // Populate the HashMap (list of proxies)
        final Map<SocketAddress, InnerProxy> proxies = new HashMap<SocketAddress, InnerProxy>(1);
        final InnerProxy i = new InnerProxy(new InetSocketAddress(proxyInformation.getHost(), proxyInformation.getPort()));
        proxies.put(i.address(), i);
        this.proxies = proxies;
    }

    /*
     * This is the method that the handlers will call. Returns a list of proxies.
     */
    @Override
    public java.util.List<Proxy> select(final URI uri) {
        // Let's stick to the specs.
        if (uri == null) {
            throw new IllegalArgumentException("URI can't be null.");
        }
        /*
         * If it's a known URL scheme, then we use our own list.
         */
        final String protocol = uri.getScheme();
        //if ("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol)) {
        if (null != protocol) {
            final List<Proxy> l = new ArrayList<Proxy>(proxies.size());
            for (final InnerProxy p : proxies.values()) {
                l.add(p.toProxy());
            }
            return l;
        }
        /*
         * Unknown scheme (could be SOCKS or FTP) defer to the default selector.
         */
        if (defsel != null) {
            return defsel.select(uri);
        }
        return Collections.<Proxy> singletonList(Proxy.NO_PROXY);
    }

    /*
     * Method called by the handlers when it failed to connect to one of the proxies returned by select().
     */
    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe) {
        // Let's stick to the specs again.
        if (uri == null || sa == null || ioe == null) {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
        /*
         * Let's lookup for the proxy
         */
        final InnerProxy p = proxies.get(sa);
        if (p == null) {
            /*
             * Not one of ours, let's delegate to the default.
             */
            if (defsel != null) {
                defsel.connectFailed(uri, sa, ioe);
            }
        } else {
            /*
             * It's one of ours, if it failed more than 3 times let's remove it from the list.
             */
            if (p.failed() >= 3) {
                proxies.remove(sa);
            }
        }
    }

}
