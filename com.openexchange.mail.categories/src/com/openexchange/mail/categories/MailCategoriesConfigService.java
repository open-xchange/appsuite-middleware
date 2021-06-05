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

    public static final String TOPIC_REORGANIZE = "TOPIC_MAIL_REORGANIZE";
    public static final String PROP_USER_ID = "userId";
    public static final String PROP_CONTEXT_ID = "ctxId";

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
