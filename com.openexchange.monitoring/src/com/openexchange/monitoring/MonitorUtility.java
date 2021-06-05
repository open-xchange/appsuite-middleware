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
