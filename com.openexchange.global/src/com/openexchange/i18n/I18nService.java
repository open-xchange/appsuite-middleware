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

package com.openexchange.i18n;

import java.util.Locale;

/**
 * Service for publishing implementations to translate texts into other languages. Implementating services must carry the property named {@link #LANGUAGE} containing the string representation of the locate returned by the {@link #getLocale()} method.
 * 
 * @author <a href="mailto:ben.pahne@open-xchange">Ben Pahne</a>
 */
public interface I18nService {

    static final String LANGUAGE = "language";

    String getLocalized(String key);    

    boolean hasKey(String key);

    boolean hasKey(String context, String key);

    /**
     * Localizes the given key.
     * This method is also a marker for xgettext to generate the translation templates.
     * For wrapping this with additional functionality, you must just keep the method signature identical and the call must contain the strings rather than variables.
     * 
     * @param key
     * @return
     */
    String getL10NLocalized(String key);

    /**
     * Localizes the given key in the given context.
     * This method is also a marker for xgettext to generate the translation templates.
     * If you want something translated with a context you should use this method to avoid ambiguous keys which might have different translations depending on the context.
     * For wrapping this with additional functionality, you must just keep the method signature identical and the call must contain the strings rather than variables.
     * 
     * For simple localization without context you can just use the com.openexchange.i18n.LocalizableStrings marker Interface.
     * 
     * @param messageContext The context
     * @param key
     * @return
     */
    String getL10NContextLocalized(String messageContext, String key);

    /**
     * Localizes the given key in the given context depending on the number.
     * This method is also a marker for xgettext to generate the translation templates.
     * If you want something translated with a context you should use this method to avoid ambiguous keys which might have different translations depending on the context.
     * The plural form describes the number used for the translation. 0 means singular, everything >0 is a plural form. There should only be one english plural form, but other languages might have additional forms.
     * If a plural form is not available this defaults to the highest available form.
     * For wrapping this with additional functionality, you must just keep the method signature identical and the call must contain the strings rather than variables.
     * 
     * For simple localization without context you can just use the com.openexchange.i18n.LocalizableStrings marker Interface.
     * 
     * @param messageContext The context
     * @param key The english singular String
     * @param keyPlural The english plural String
     * @param plural The number form. 0 for singular, >0 for a plural form.
     * @return
     */
    String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural);

    Locale getLocale();

}
