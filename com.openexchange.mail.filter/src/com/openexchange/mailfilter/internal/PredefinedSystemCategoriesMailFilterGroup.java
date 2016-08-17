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

package com.openexchange.mailfilter.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;

/**
 * {@link PredefinedSystemCategoriesMailFilterGroup}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class PredefinedSystemCategoriesMailFilterGroup implements MailFilterGroup {

    private static final String SYSTEM_CATEGORY_FLAG = "syscategory";

    private static final Comparator<Rule> RULE_COMPARATOR = new Comparator<Rule>() {

        @Override
        public int compare(Rule o1, Rule o2) {
            if (o1 == null) {
                return o2 == null ? 0 : -1;
            } else if (o2 == null) {
                return 1;
            }
            int uniqueid1 = o1.getRuleComment().getUniqueid();
            int uniqueid2 = o2.getRuleComment().getUniqueid();
            return (uniqueid1 < uniqueid2) ? -1 : ((uniqueid1 == uniqueid2) ? 0 : 1);
        }
    };

    /**
     * Initializes a new {@link PredefinedSystemCategoriesMailFilterGroup}.
     */
    public PredefinedSystemCategoriesMailFilterGroup() {
        super();
    }

    @Override
    public List<Rule> getOrderedRules(List<Rule> rules) throws OXException {
        Iterator<Rule> iterator = rules.iterator();
        List<Rule> result = new ArrayList<Rule>(rules.size());
        while (iterator.hasNext()) {
            Rule rule = iterator.next();
            List<String> flags = rule.getRuleComment().getFlags();
            if (flags != null && flags.contains(SYSTEM_CATEGORY_FLAG)) {
                result.add(rule);
                iterator.remove();
            }
        }
        Collections.sort(result, RULE_COMPARATOR);
        return result;
    }

}
