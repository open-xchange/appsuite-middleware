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

package com.openexchange.chronos.itip;

import java.util.Locale;

/**
 * {@link ITipMethod}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public enum ITipMethod {

    NO_METHOD(""),
    REQUEST("request"),
    REPLY("reply"),
    CANCEL("cancel"),
    COUNTER("counter"),
    DECLINECOUNTER("declinecounter"),
    REFRESH("refresh"),
    ADD("add"),
    PUBLISH("publish");

    private final String keyword;

    private ITipMethod(final String keyword) {
        this.keyword = keyword;
    }

    /**
     * Gets the keyword.
     *
     * @return The keyword
     */
    public String getKeyword() {
        return this.keyword;
    }

    /**
     * Gets the method parameter read to append to "Content-Type" header: <code>"method=" + &lt;keyword&gt;</code>
     *
     * @return The method parameter; <code>"method=" + &lt;keyword&gt;</code>
     */
    public String getMethod() {
        return "method=" + keyword.toUpperCase(Locale.US);
    }

    public static ITipMethod get(String keyword) {
    	if (keyword == null) {
    		return NO_METHOD;
    	}
        keyword = keyword.trim().toLowerCase(Locale.US);

        if (keyword.equals(REQUEST.getKeyword())) {
            return REQUEST;
        }
        if (keyword.equals(REPLY.getKeyword())) {
            return REPLY;
        }
        if (keyword.equals(CANCEL.getKeyword())) {
            return CANCEL;
        }
        if (keyword.equals(COUNTER.getKeyword())) {
            return COUNTER;
        }
        if (keyword.equals(DECLINECOUNTER.getKeyword())) {
            return DECLINECOUNTER;
        }
        if (keyword.equals(REFRESH.getKeyword())) {
            return REFRESH;
        }
        if (keyword.equals(ADD.getKeyword())) {
            return ADD;
        }
        if (keyword.equals(PUBLISH.getKeyword())) {
            return PUBLISH;
        }

        return NO_METHOD;
    }


}
