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

package com.openexchange.monitoring;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * {@link MonitorUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MonitorUtility {

    /**
     * Initializes a new {@link MonitorUtility}
     */
    private MonitorUtility() {
        super();
    }

    /**
     * Creates an array of <code>java.lang.String</code> with a length set to <code>2</code> which contains the domain and name of given
     * class name
     * <p>
     * Example:<br>
     * If class name is <code>my.path.to.class.ClassName</code>, then its domain is <code>my.path.to.class</code> and its name is
     * <code>ClassName</code>.
     *
     * @param className The class name
     * @param defaultDomain Whether to use default domain or not
     * @return An array which contains the domain and name of given class name
     */
    public static final String[] getDomainAndName(final String className, final boolean defaultDomain) {
        final int pos = className.lastIndexOf('.');
        if (pos == -1 || defaultDomain) {
            return new String[] { MonitorMBean.DEFAULT_DOMAIN, pos == -1 ? className : className.substring(pos + 1) };
        }
        return new String[] { className.substring(0, pos), className.substring(pos + 1) };
    }

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name.
     *
     * @param className The class name to use as object name
     * @param defaultDomain <code>true</code> to use the default domain {@link MonitorMBean#DEFAULT_DOMAIN}; otherwise the canonical class
     *            path extracted from <code>className</code> argument
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     * @throws NullPointerException If instantiation of {@link ObjectName} fails due to a <code>null</code> constructor argument
     * @see #getDomainAndName(String, boolean)
     */
    public static final ObjectName getObjectName(final String className, final boolean defaultDomain) throws MalformedObjectNameException, NullPointerException {
        final int pos = className.lastIndexOf('.');
        if (pos == -1 || defaultDomain) {
            return new ObjectName(MonitorMBean.DEFAULT_DOMAIN, "name", pos == -1 ? className : className.substring(pos + 1));
        }
        return new ObjectName(className.substring(0, pos), "name", className.substring(pos + 1));
    }
}
