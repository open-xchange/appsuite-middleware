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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesRuleEngine}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public interface MailCategoriesRuleEngine {

    /**
     * Checks if this rule engine is applicable for session-associated user.
     *
     * @param session The session providing user data
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If applicability cannot be checked
     */
    public boolean isApplicable(Session session) throws OXException;

    /**
     * Initialize the rule engine for the given user. For example creates first rules etc.
     * This method should only be called once for every user when the feature is started for the first time.
     *
     * @param session The user session
     * @param rules A list of system rules to create
     * @throws OXException
     */
    public void initRuleEngineForUser(Session session, List<MailCategoryRule> rules) throws OXException;

    /**
     * Sets the rule. If available the old rule will be overwritten.
     *
     * @param session The user session
     * @param rule The new rule
     * @param type The type of the rule
     * @throws OXException If set operation fails
     */
    public void setRule(Session session, MailCategoryRule rule, RuleType type) throws OXException;

    /**
     * Removes the rule which match the given flag
     *
     * @param session The user session
     * @param flag The mail flag
     * @throws OXException If remove attempt fails
     */
    public void removeRule(Session session, String flag) throws OXException;

    /**
     * Retrieves the current rule of the given category or null if no rule exists.
     *
     * @param session The user session
     * @param flag The mail flag
     * @return The current rule or null
     * @throws OXException If rule cannot be returned
     */
    public MailCategoryRule getRule(Session session, String flag) throws OXException;

    /**
     * Removes the value from all condition headers
     *
     * @param session The user session
     * @param value The value to remove
     * @param header The mail header name
     * @throws OXException
     */
    public void removeValueFromHeader(Session session, String value, String header) throws OXException;

    /**
     * Removes all rules which do not belong to any category.
     *
     * @param flags The category flags
     * @param session The user session
     * @throws OXException if cleanUp fails
     */
    public void cleanUp(List<String> flags, Session session) throws OXException;

    /**
     * Applies a given rule to the inbox
     *
     * @param session The user session
     * @param rule The rule to apply
     * @return false in case the imap server doesn't support the FILTER=SIEVE extension, true otherwise
     * @throws OXException
     */
    public boolean applyRule(Session session, MailCategoryRule rule) throws OXException;

}
