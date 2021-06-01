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

package com.openexchange.mailfilter.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
    public List<Rule> getOrderedRules(List<Rule> rules) {
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
