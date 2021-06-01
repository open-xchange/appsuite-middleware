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

package com.openexchange.report;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Constants {

    public static final String REPORTING_DOMAIN = "com.openexchange.reporting";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Constants.class);

    public static final ObjectName REPORTING_NAME = initReportingName();
    public static final ObjectName LOGIN_COUNTER_NAME = initOxtenderMonitorName();

    /**
     * Prevent instantiation.
     */
    private Constants() {
        super();
    }

    private static final ObjectName initReportingName() {
        return getObjectName("Reporting");
    }

    private static final ObjectName initOxtenderMonitorName() {
        return getObjectName("Login Counter");
    }

    private static ObjectName getObjectName(String name) {
        ObjectName retval = null;
        try {
            retval = new ObjectName(REPORTING_DOMAIN, "name", name);
        } catch (MalformedObjectNameException | NullPointerException e) {
            LOG.error("", e);
        }
        return retval;
    }
}
