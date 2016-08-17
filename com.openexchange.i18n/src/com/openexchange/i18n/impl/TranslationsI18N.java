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

package com.openexchange.i18n.impl;

import java.util.Locale;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.parsing.Translations;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class TranslationsI18N implements I18nService {

    private final Translations translations;

    public TranslationsI18N(final Translations translations) {
        this.translations = translations;
    }

    @Override
    public String getLocalized(final String key) {
        final String translation = translations.translate(key);
        if (translation == null) {
            return key;
        }
        return translation;
    }

    @Override
    public String getL10NContextLocalized(String messageContext, String key) {
        String t = translations.translate(messageContext, key);
        if (t == null) {
            return key;
        }
        return t;
    }

    @Override
    public String getL10NPluralLocalized(String messageContext, String key, String keyPlural, int plural) {
        String t = translations.translate(messageContext, key, plural);
        if (t == null) {
            t = translations.translate(messageContext, keyPlural, plural);
        }
        if (t == null) {
            return key;
        }
        return t;
        
    }

    @Override
    public boolean hasKey(final String key) {
        return translations.getKnownStrings().contains(key);
    }

    @Override
    public boolean hasKey(String context, String key) {
        return translations.getKnownStrings(context).contains(key);
    }

    @Override
    public Locale getLocale() {
        return translations.getLocale();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append('{');
        final Locale locale = getLocale();
        if (null != locale) {
            builder.append("locale=").append(locale);
        }
        if (translations != null) {
            builder.append("translations=").append(translations);
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public String getL10NLocalized(String key) {
        return getLocalized(key);
    }

}
