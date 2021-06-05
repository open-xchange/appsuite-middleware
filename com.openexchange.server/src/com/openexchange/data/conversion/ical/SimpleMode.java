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

package com.openexchange.data.conversion.ical;

/**
 * {@link SimpleMode}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SimpleMode implements Mode {

    private final ZoneInfo zoneInfo;
    private final String method;

    /**
     * Initializes a new {@link SimpleMode}.
     *
     * @param zoneInfo The time zone information to use
     * @param method The method to use, or <code>null</code> for no special method
     */
    public SimpleMode(ZoneInfo zoneInfo, String method) {
        super();
        this.zoneInfo = zoneInfo;
        this.method = method;
    }

    /**
     * Initializes a new {@link SimpleMode} using the default "PUBLISH".
     *
     * @param zoneInfo The time zone information to use
     */
    public SimpleMode(ZoneInfo zoneInfo) {
        this(zoneInfo, "PUBLISH");
    }

    @Override
    public ZoneInfo getZoneInfo() {
        return zoneInfo;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "SimpleMode [zoneInfo=" + zoneInfo + ", method=" + method + "]";
    }

}
