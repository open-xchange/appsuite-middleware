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

package com.openexchange.global.osgi;

import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;

/**
 * {@link ServerInitialization} - The {@link Initialization initialization} for server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ServerInitialization implements Initialization {

    private volatile String previousTTL;
    private volatile String previousNegativeTTL;

    /**
     * Initializes a new {@link ServerInitialization}.
     */
    ServerInitialization() {
        super();
    }

    @Override
    public void start() throws OXException {
        /*
         * Remember previous settings
         */
        previousTTL = java.security.Security.getProperty("networkaddress.cache.ttl");
        previousNegativeTTL = java.security.Security.getProperty("networkaddress.cache.negative.ttl");
        /*
         * The number of seconds to cache the successful lookup
         */
        java.security.Security.setProperty("networkaddress.cache.ttl", Integer.toString(3600));
        System.setProperty("sun.net.inetaddr.ttl", Integer.toString(3600));
        /*
         * The number of seconds to cache the failure for un-successful lookups
         */
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", Integer.toString(10));
    }

    @Override
    public void stop() throws OXException {
        String previousTTL = this.previousTTL;
        if (null == previousTTL) {
            java.security.Security.setProperty("networkaddress.cache.ttl", "-1");
        } else {
            /*
             * Restore previous settings
             */
            java.security.Security.setProperty("networkaddress.cache.ttl", previousTTL);
            this.previousTTL = null;
        }
        String previousNegativeTTL = this.previousNegativeTTL;
        if (null == previousNegativeTTL) {
            java.security.Security.setProperty("networkaddress.cache.negative.ttl", "10");
        } else {
            java.security.Security.setProperty("networkaddress.cache.negative.ttl", previousNegativeTTL);
            this.previousNegativeTTL = null;
        }
    }

}
