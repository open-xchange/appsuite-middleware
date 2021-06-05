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

package com.openexchange.consistency.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import com.openexchange.java.Strings;

/**
 * {@link ConsistencyUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
final class ConsistencyUtil {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConsistencyUtil.class);

    /**
     * Builds the asymmetric set difference of the two sets, the first one is changed.
     *
     * @param first The first set, which is effectively modified
     * @param second The second set
     * @param name1 The name for the first set to use for logging purpose
     * @param name2 The name for the second set to use for logging purpose
     * @return
     */
    static boolean diffSet(SortedSet<String> first, SortedSet<String> second, String name1, String name2) {
        if (first.isEmpty()) {
            return false;
        }
        first.removeAll(second);
        if (first.isEmpty()) {
            return false;
        }
        LOG.info("Inconsistencies found in {}, the following files aren't in {}:{}", name1, name2, Strings.getLineSeparator());
        outputSet(first);
        return true;
    }

    /**
     * Logs the specified set with log level INFO
     *
     * @param set the set to log
     */
    private static void outputSet(SortedSet<String> set) {
        StringBuilder sb = new StringBuilder();
        List<Object> args = new ArrayList<>(set.size());
        for (Iterator<String> itstr = set.iterator(); itstr.hasNext();) {
            sb.append(itstr.next()).append("{}");
            args.add(Strings.getLineSeparator());
        }
        LOG.info(sb.toString(), args.toArray(new Object[args.size()]));
    }
}
