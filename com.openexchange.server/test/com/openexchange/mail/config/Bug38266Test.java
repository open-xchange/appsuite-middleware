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

package com.openexchange.mail.config;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.java.Strings;
import junit.framework.TestCase;

/**
 * {@link Bug38266Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class Bug38266Test extends TestCase {

    /**
     * Initializes a new {@link Bug38266Test}.
     */
    public Bug38266Test() {
        super();
    }

    public void testForBug38266() {
        List<IPRange> ranges = new LinkedList<IPRange>();
        for (String range : Strings.splitByComma("10.30.73.4,10.30.77.0/24,10.30.73.0/24")) {
            if (null == range) {
                System.err.println("Invalid IP range value: 'null'");
            } else {
                try {
                    IPRange parsedRange = IPRange.parseRange(range.trim());
                    if (null == parsedRange) {
                        System.err.println("Invalid IP range value: "+ range);
                    } else {
                        ranges.add(parsedRange);
                    }
                } catch (Exception e) {
                    System.err.println("Invalid IP range value: "+ range);
                    e.printStackTrace(System.err);
                }
            }

        }

        assertTrue("Unexpected number of IP ranges", ranges.size() > 0);
        assertTrue("Should be white-listed, but is not", IPRange.isWhitelistedFromRateLimit("10.30.73.4", ranges));
    }

}
