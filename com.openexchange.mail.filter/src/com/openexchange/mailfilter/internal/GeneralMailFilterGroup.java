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

import java.util.LinkedList;
import java.util.List;
import com.openexchange.jsieve.commands.Rule;

/**
 * {@link GeneralMailFilterGroup} is the general / fallback MailFilterGroup which removes all Rules and orders them according to the given uid array.
 *  Rules which don't match any uid in the uid array are appended at the end of the list.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class GeneralMailFilterGroup implements MailFilterGroup {

    private final int[] userOrder;

    /**
     * Initializes a new {@link MailFilterGroup}.
     */
    public GeneralMailFilterGroup(int uids[]) {
        super();
        this.userOrder = uids;
    }

    @Override
    public List<Rule> getOrderedRules(List<Rule> rules) {
        List<Rule> result = new LinkedList<Rule>(rules);
        rules.clear();
        for (int i = 0; i < userOrder.length; i++) {
            int uniqueid = userOrder[i];
            RuleAndPosition rightRule = getRightRuleForUniqueId(result, uniqueid);
            if (rightRule == null) {
                // skip unknown rules
                continue;
            }
            int position = rightRule.position;
            result.remove(position);
            result.add(i, rightRule.rule);
        }
        return result;
    }


    /**
     * Search within the given List of Rules for the one matching the specified UID
     */
    private RuleAndPosition getRightRuleForUniqueId(List<Rule> clientrules, int uniqueid) {
        for (int i = 0; i < clientrules.size(); i++) {
            Rule rule = clientrules.get(i);
            if (uniqueid == rule.getUniqueId()) {
                return new RuleAndPosition(rule, i);
            }
        }
        return null;
    }

    private static final class RuleAndPosition {

        final int position;
        final Rule rule;

        RuleAndPosition(Rule rule, int position) {
            super();
            this.rule = rule;
            this.position = position;
        }

    }
}
