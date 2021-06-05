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


/**
 * {@link MailCategoriesConstants}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesConstants {

    /**
     * Prevents instantiation
     */
    private MailCategoriesConstants() {}

    /**
     * General mail categories switch property name
     */
    public static final String MAIL_CATEGORIES_SWITCH = "com.openexchange.mail.categories.enabled";

    /**
     * Read only property switch which overwrites the <code>MAIL_CATEGORIES_SWITCH</code> property.
     */
    public static final String MAIL_CATEGORIES_FORCE_SWITCH = "com.openexchange.mail.categories.forced";

    /**
     * The system category identifiers property name
     */
    public static final String MAIL_CATEGORIES_IDENTIFIERS = "com.openexchange.mail.categories.identifiers";

    /**
     * The user category identifiers property name
     */
    public static final String MAIL_USER_CATEGORIES_IDENTIFIERS = "com.openexchange.mail.user.categories.identifiers";

    /**
     * The prefix for all mail categories configuration properties
     */
    public static final String MAIL_CATEGORIES_PREFIX = "com.openexchange.mail.categories.";

    /**
     * The current mail category name
     */
    public static final String MAIL_CATEGORIES_NAME = ".name";

    /**
     * The current mail category description
     */
    public static final String MAIL_CATEGORIES_DESCRIPTION = ".description";

    /**
     * The name language prefix
     */
    public static final String MAIL_CATEGORIES_NAME_LANGUAGE_PREFIX = ".name.";

    /**
     * The description language prefix
     */
    public static final String MAIL_CATEGORIES_DESCRIPTION_LANGUAGE_PREFIX = ".description.";

    /**
     * The fall-back name parameter name
     */
    public static final String MAIL_CATEGORIES_FALLBACK = ".name.fallback";

    /**
     * The flag parameter name suffix
     */
    public static final String MAIL_CATEGORIES_FLAG = ".flag";

    /**
     * The force parameter name suffix
     */
    public static final String MAIL_CATEGORIES_FORCE = ".force";

    /**
     * The active parameter name suffix
     */
    public static final String MAIL_CATEGORIES_ACTIVE = ".active";

    /**
     * The apply ox rules property name
     */
    public static final String APPLY_OX_RULES_PROPERTY = "com.openexchange.mail.categories.apply.ox.rules";

    /**
     * The general category id
     */
    public static final String GENERAL_CATEGORY_ID = "general";

}
