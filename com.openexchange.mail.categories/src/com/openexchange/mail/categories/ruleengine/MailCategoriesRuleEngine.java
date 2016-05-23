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
     * Removes all rules which does not belong to any category.
     * 
     * @param flags The category flags
     * @param session The user session
     * @throws OXException if cleanUp fails
     */
    public void cleanUp(List<String> flags, Session session) throws OXException;

}
