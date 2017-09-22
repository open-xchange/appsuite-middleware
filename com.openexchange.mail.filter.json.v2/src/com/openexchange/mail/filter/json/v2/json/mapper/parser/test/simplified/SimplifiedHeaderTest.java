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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test.simplified;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.filter.json.v2.json.mapper.parser.exceptions.CommandParserExceptionCodes;

/**
 * {@link SimplifiedHeaderTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.4
 */
public enum SimplifiedHeaderTest {
    Subject("subject", "Subject"),
    From("from", "From"),
    To("to", "To"),
    Cc("cc", "Cc"),
    AnyRecipient("anyrecipient", "to", "cc"),
    MailingList("mailinglist", "List-Id", "X-BeenThere", "X-Mailinglist", "X-Mailing-List");

    private static final Map<String, SimplifiedHeaderTest> map;
    static {
        Map<String, SimplifiedHeaderTest> m = new HashMap<>(8);
        for (SimplifiedHeaderTest sht : SimplifiedHeaderTest.values()) {
            m.put(sht.getCommandName(), sht);
        }
        map = Collections.unmodifiableMap(m);
    }

    private String commandName;
    private List<String> headerNames;

    SimplifiedHeaderTest(String commandName, String... headerNames) {
        this.commandName = commandName;
        this.headerNames = Arrays.asList(headerNames);
    }

    public String getCommandName() {
        return commandName;
    }

    /**
     * Retrieves the {@link SimplifiedHeaderTest} by name
     *
     * @param name The name
     * @return The {@link SimplifiedHeaderTest}
     * @throws OXException if no {@link SimplifiedHeaderTest} with this name exists
     */
    public static SimplifiedHeaderTest getTestByName(String name) throws OXException {
        SimplifiedHeaderTest simplifiedHeaderTest = map.get(name);
        if (simplifiedHeaderTest == null) {
            throw CommandParserExceptionCodes.UNKOWN_SIMPLIFIED_RULE.create(name);
        }
        return simplifiedHeaderTest;
    }

    /**
     * Retrieves the header names of this {@link SimplifiedHeaderTest}
     *
     * @return A list of header names
     */
    public List<String> getHeaderNames() {
        return this.headerNames;
    }
}
