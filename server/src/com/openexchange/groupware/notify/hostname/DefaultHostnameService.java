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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify.hostname;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link DefaultHostnameService} - The default host name service implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultHostnameService implements HostnameService {

    private static final DefaultHostnameService INSTANCE = new DefaultHostnameService();

    /**
     * Gets the {@link DefaultHostnameService} instance.
     * 
     * @return The {@link DefaultHostnameService} instance.
     */
    public static DefaultHostnameService getInstance() {
        return INSTANCE;
    }

    private final AtomicBoolean initialized;

    private volatile boolean secure;

    private volatile String hostName;

    private volatile int port;

    /**
     * Initializes a new {@link DefaultHostnameService}.
     */
    private DefaultHostnameService() {
        super();
        initialized = new AtomicBoolean();
    }

    /**
     * Checks if this {@link DefaultHostnameService} instance has been initialized.
     * 
     * @return <code>true</code> if initialized; otherwise <code>false</code>
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Checks if caller may initialize this {@link DefaultHostnameService} instance.
     * 
     * @return <code>true</code> if caller may initialize this {@link DefaultHostnameService} instance; otherwise <code>false</code>
     */
    public boolean performInitialization() {
        return initialized.compareAndSet(false, true);
    }

    public String getHostname(final int userId, final int contextId) {
        return hostName;
    }

    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets whether a secure connection is established.
     * 
     * @param secure <code>true</code> for secure connection; otherwise <code>false</code>
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    /**
     * Sets the host name.
     * 
     * @param hostName The host name
     */
    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    /**
     * Gets the port.
     * 
     * @return The port
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     * 
     * @param port The port
     */
    public void setPort(final int port) {
        this.port = port;
    }

}
