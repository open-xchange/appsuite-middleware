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

package com.openexchange.mail.categories.ruleengine;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link MailCategoryRule}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoryRule {

    private final String flag;
    private boolean hasSubRules;
    private List<MailCategoryRule> subRules;
    private boolean isAND;
    private List<String> headers;
    private List<String> values;
    private String[] flagsToRemove;

    /**
     * Initializes a new {@link MailCategoryRule} with subrules.
     *
     * @param flag The mail flag
     * @param isAND A flag indicating if the subrules are AND connected or not.
     */
    public MailCategoryRule(String flag, boolean isAND) {
        super();
        this.isAND = isAND;
        this.flag = flag;
    }

    /**
     * Initializes a new {@link MailCategoryRule} as a single rule.
     *
     * @param header The header field name
     * @param value The value of the header field.
     * @param flag The mail flag
     */
    public MailCategoryRule(List<String> headers, List<String> values, String flag) {
        super();
        this.headers = headers;
        this.values = values;
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

    public boolean hasSubRules() {
        return hasSubRules;
    }

    public List<MailCategoryRule> getSubRules() {
        return subRules;
    }

    public boolean isAND() {
        return isAND;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getValues() {
        return values;
    }

    public void addFlagsToRemove(String... flags) {
        this.flagsToRemove = flags;
    }

    public String[] getFlagsToRemove() {
        return flagsToRemove;
    }

    /**
     * Adds a sub-rule to this rule.
     *
     * @param subRule The sub-rule
     */
    public void addSubRule(MailCategoryRule subRule) {
        if (this.subRules == null) {
            subRules = new ArrayList<>();
            subRules.add(subRule);
            hasSubRules = true;
        } else {
            subRules.add(subRule);
        }

    }

}
