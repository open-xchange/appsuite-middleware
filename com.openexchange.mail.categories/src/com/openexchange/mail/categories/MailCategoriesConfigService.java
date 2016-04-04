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

package com.openexchange.mail.categories;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.ruleengine.MailCategoryRule;
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public interface MailCategoriesConfigService {

    /**
     * Retrieves all category configurations for the given user
     *
     * @param session The user session
     * @param onlyEnabled If true only returns enabled or forced configurations
     * @return A list of category configurations
     * @throws OXException
     */
    List<MailCategoryConfig> getAllCategories(Session session, boolean onlyEnabled) throws OXException;

    /**
     * Retrieves all category flags for the given user
     *
     * @param session The user session
     * @param onlyEnabled If true only returns enabled or forced categories
     * @param onlyUserCategories If true only returns user categories
     * @return String array of category flags
     * @throws OXException
     */
    String[] getAllFlags(Session session, boolean onlyEnabled, boolean onlyUserCategories) throws OXException;

    /**
     * Retrieves the system category configuration for the given user
     *
     * @param session The user session
     * @param category The category identifier
     * @return The category configuration or null if no configuration exists for given category
     * @throws OXException
     */
    MailCategoryConfig getConfigByCategory(Session session, String category) throws OXException;

    /**
     * Retrieves the user category configuration for the given user
     *
     * @param session The user session
     * @param category The category identifier
     * @return The category configuration or null if no configuration exists for given category
     * @throws OXException
     */
    MailCategoryConfig getUserConfigByCategory(Session session, String category) throws OXException;

    /**
     * Retrieves the category flag for the given user
     *
     * @param session The user session
     * @param category The category identifier
     * @return The category flag or null if no configuration exists for given category
     * @throws OXException
     */
    String getFlagByCategory(Session session, String category) throws OXException;

    /**
     * Retrieves the category configuration with the given flag for the given user
     *
     * @param session The user session
     * @param flag The flag of the configuration
     * @return The category configuration or null if no configuration exists with given flag
     * @throws OXException
     */
    MailCategoryConfig getConfigByFlag(Session session, String flag) throws OXException;

    /**
     * Activates or deactivates given categories
     *
     * @param categories An array of category identifiers
     * @param enable Boolean flag indicating activation or deactivation
     * @param session The user session
     * @return A list of all category configurations
     * @throws OXException
     */
    List<MailCategoryConfig> changeConfigurations(String[] categories, boolean activate, Session session) throws OXException;

    /**
     * Tests if the given category is a system category or not
     *
     * @param category The category identifier
     * @param session The user session
     * @return true if the category is a system category, false otherwise
     * @throws OXException
     */
    boolean isSystemCategory(String category, Session session) throws OXException;

    /**
     * Adds a new user category.
     *
     * @param category The category identifier
     * @param flag The category flag
     * @param name The category name
     * @param rule The new rule of the category
     * @param reorganize Whether to re-organize existing messages
     * @param session The user session
     * @throws OXException if a category with this identifier already exists
     */
    void addUserCategory(String category, String flag, String name, MailCategoryRule rule, ReorganizeParameter reorganize, Session session) throws OXException;

    /**
     * Updates the user category
     *
     * @param category The category identifier
     * @param name The category name
     * @param rule The new rule of the category
     * @param reorganize Whether to re-organize existing messages
     * @param session The user session
     * @throws OXException if a category with this identifier does not exist
     */
    void updateUserCategory(String category, String name, MailCategoryRule rule, ReorganizeParameter reorganize, Session session) throws OXException;

    /**
     * Trains the given category with the given mail address
     *
     * @param category The category identifier
     * @param email The email address
     * @param reorganize Whether to re-organize existing messages
     * @param session The user session
     * @throws OXException if a category with this identifier does not exist
     */
    void trainCategory(String category, String email, ReorganizeParameter reorganize, Session session) throws OXException;

    /**
     * Removes the given user categories
     *
     * @param categories The category identifiers
     * @param session The user session
     * @return A list of result objects
     * @throws OXException if a category with this identifier does not exist
     */
    List<MailCategoriesServiceResult> removeUserCategories(String[] categories, Session session) throws OXException;

    /**
     * Returns true if the mail categories feature is enabled for the given user
     *
     * @param session The user session
     * @return true if enabled, otherwise false
     * @throws OXException
     */
    boolean isEnabled(Session session) throws OXException;

    /**
     * Returns true if the given user is allowed to create own categories
     *
     * @param session The user session
     * @return true if allowed, otherwise false
     * @throws OXException
     */
    boolean isAllowedToCreateUserCategories(Session session) throws OXException;

    /**
     * Generates a flag for the given category
     *
     * @param category The category identifier
     * @return The flag
     */
    String generateFlag(String category);

}
