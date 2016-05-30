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


/**
 * {@link MailCategoriesConstants}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesConstants {

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
