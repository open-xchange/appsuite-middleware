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
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
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
    public List<MailCategoryConfig> getAllCategories(Session session, boolean onlyEnabled) throws OXException;
    
    /**
     * Retrieves all category flags for the given user
     * 
     * @param session The user session
     * @param onlyEnabled If true only returns enabled or forced categories
     * @param onlyUserCategories If true only returns user categories
     * @return String array of category flags
     * @throws OXException
     */
    public String[] getAllFlags(Session session, boolean onlyEnabled, boolean onlyUserCategories) throws OXException;

    /**
     * Retrieves the system category configuration for the given user
     * 
     * @param session The user session
     * @param category The category identifier
     * @return The category configuration or null if no configuration exists for given category
     * @throws OXException
     */
    public MailCategoryConfig getConfigByCategory(Session session, String category) throws OXException;
    
    /**
     * Retrieves the user category configuration for the given user
     * 
     * @param session The user session
     * @param category The category identifier
     * @return The category configuration or null if no configuration exists for given category
     * @throws OXException
     */
    public MailCategoryConfig getUserConfigByCategory(Session session, String category) throws OXException;
    
    /**
     * Retrieves the category flag for the given user
     * 
     * @param session The user session
     * @param category The category identifier
     * @return The category flag or null if no configuration exists for given category
     * @throws OXException
     */
    public String getFlagByCategory(Session session, String category) throws OXException;

    /**
     * Retrieves the category configuration with the given flag for the given user
     * 
     * @param session The user session
     * @param flag The flag of the configuration
     * @return The category configuration or null if no configuration exists with given flag
     * @throws OXException
     */
    public MailCategoryConfig getConfigByFlag(Session session, String flag) throws OXException;
    
    /**
     * Activates or deactivates given categories
     * 
     * @param categories An array of category identifiers
     * @param enable Boolean flag indicating activation or deactivation
     * @param session The user session
     * @return A list of all category configurations
     * @throws OXException 
     */
    public List<MailCategoryConfig> changeConfigurations(String[] categories, boolean activate, Session session) throws OXException;
    
    /**
     * Tests if the given category is a system category or not
     * 
     * @param category The category identifier
     * @param session The user session
     * @return true if the category is a system category, false otherwise
     * @throws OXException 
     */
    public boolean isSystemCategory(String category, Session session) throws OXException;
    
    /**
     * Add a new user category 
     * 
     * @param category The category identifier
     * @param flag The category flag
     * @param name The category name
     * @param session The user session
     * @throws OXException if a category with this identifier already exists
     */
    public void addUserCategory(String category, String flag, String name, Session session) throws OXException;

}
