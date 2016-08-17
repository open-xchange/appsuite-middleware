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
     * Add a subrule to this rule.
     *
     * @param subRule The subrule
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
