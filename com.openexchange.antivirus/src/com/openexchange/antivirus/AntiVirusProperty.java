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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.antivirus;

import com.openexchange.config.lean.Property;

/**
 * {@link AntiVirusProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum AntiVirusProperty implements Property {
    /**
     * Defines whether the Anti-Virus capability is enabled
     * Default: false
     */
    enabled(false),
    /**
     * Defines the address of the server.
     * Default: localhost
     */
    server("localhost"),
    /**
     * Defines the port at which the C-ICAP server is running
     * Default: 1344
     */
    port(1344),
    /**
     * Defines the anti-virus service's name
     * Default: avscan
     */
    service("avscan"),
    /**
     * Dictates the operation mode of the service. In 'streaming' mode
     * the data stream that will reach the end-point-client after will
     * be coming from the ICAP/AV server. In 'double-fetch' mode the
     * data stream will have to be fetched from the storage twice (one
     * for scanning and one for delivering to the end-point-client).
     * The streaming mode is still at an experimental phase.
     * 
     * Default: double-fetch
     */
    mode("double-fetch"),
    /**
     * Defines the maximum file size (in MB) that is acceptable for the underlying
     * Anti-Virus service to scan. Files larger than that size will NOT be scanned
     * and an appropriate warning will be displayed to the user.
     * 
     * Default: 100
     */
    maxFileSize(100);

    private final Object defaultValue;
    private static final String PREFIX = "com.openexchange.antivirus.";

    /**
     * Initialises a new {@link AntiVirusProperty}.
     */
    private AntiVirusProperty(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.lean.Property#getFQPropertyName()
     */
    @Override
    public String getFQPropertyName() {
        return PREFIX + name();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.config.lean.Property#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
