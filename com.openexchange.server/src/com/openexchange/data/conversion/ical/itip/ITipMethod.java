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

package com.openexchange.data.conversion.ical.itip;

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
