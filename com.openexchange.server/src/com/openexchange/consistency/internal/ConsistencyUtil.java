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
