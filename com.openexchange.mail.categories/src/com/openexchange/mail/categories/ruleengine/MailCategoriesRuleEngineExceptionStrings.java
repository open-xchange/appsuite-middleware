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


/**
 * {@link MailCategoriesRuleEngineExceptionStrings}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesRuleEngineExceptionStrings {

    /**
     * Prevent instantiation
     */
    private MailCategoriesRuleEngineExceptionStrings() {}

    // Exception while setting rule: %1$s
    public static final String UNABLE_TO_SET_RULE = "Exception while setting rule: %1$s";

    // Exception while removing rule: %1$s
    public static final String UNABLE_TO_REMOVE_RULE = "Exception while removing rule: %1$s";

    // Unable to retrieve rule.
    public static final String UNABLE_TO_RETRIEVE_RULE = "Unable to retrieve rule.";

    // The given rule is not valid.
    public static final String INVALID_RULE = "The given rule is not valid.";

}
