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
import java.util.Locale;
import com.openexchange.exception.OXException;
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
     * @param locale The user's locale
     * @param onlyEnabled Whether only enabled or forced configurations should be returned
     * @param includeGeneral Whether the general category should be included or not
     * @return A list of category configurations
     * @throws OXException
     */
    List<MailCategoryConfig> getAllCategories(Session session, Locale locale, boolean onlyEnabled, boolean includeGeneral) throws OXException;

    /**
     * Retrieves all category flags for the given user
     *
     * @param session The user session
     * @param onlyEnabled Whether only enabled or forced configurations should be returned
     * @param onlyUserCategories Whether only user categories should be returned
     * @return String array of category flags
     * @throws OXException
     */
    String[] getAllFlags(Session session, boolean onlyEnabled, boolean onlyUserCategories) throws OXException;

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
     * Updates the given configurations
     *
     * @param configs A list of MailCategoryConfig's
     * @param session The user session
     * @param locale The users locale
     * @throws OXException
     */
    void updateConfigurations(List<MailCategoryConfig> configs, Session session, Locale locale) throws OXException;

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
     * Trains the given category with the given mail addresses
     *
     * @param category The category identifier
     * @param addresses The email addresses
     * @param createRule Whether to create a rule or not
     * @param reorganize Whether to re-organize existing messages
     * @param session The user session
     * @throws OXException if a category with this identifier does not exist
     */
    void trainCategory(String category, List<String> addresses, boolean createRule, ReorganizeParameter reorganize, Session session) throws OXException;

    /**
     * Enables or disables the mail categories feature for the given user
     *
     * @param session The user session
     * @param enable A flag indicating if the feature should be enabled or disabled
     * @throws OXException
     */
    void enable(Session session, boolean enable) throws OXException;

    /**
     * Returns true if the mail categories feature is enabled for the given user
     *
     * @param session The user session
     * @return true if enabled, otherwise false
     * @throws OXException
     */
    boolean isEnabled(Session session) throws OXException;

    /**
     * Returns true if the mail categories feature is forced for the given user
     *
     * @param session The user session
     * @return true if forced, otherwise false
     * @throws OXException
     */
    boolean isForced(Session session) throws OXException;

    /**
     * Remove all old category flags from the given emails and add the new one.
     * 
     * @param session The user session
     * @param mails The mails to flag
     * @param category The category identifier
     * @throws OXException
     */
    void addMails(Session session, List<MailObjectParameter> mails, String category) throws OXException;

    /**
     * Retrieves the init status, which can be 'notyetstarted', 'running' or 'finished';
     * 
     * @param session The user session
     * @return The status of the init process
     * @throws OXException
     */
    String getInitStatus(Session session) throws OXException;

}
