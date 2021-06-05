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

package com.openexchange.mail.config;

import static org.junit.Assert.assertTrue;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.openexchange.java.Strings;

/**
 * {@link Bug38266Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class Bug38266Test {
    /**
     * Initializes a new {@link Bug38266Test}.
     */
    public Bug38266Test() {
        super();
    }

         @Test
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
