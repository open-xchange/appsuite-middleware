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

package com.openexchange.i18n.internal;

import com.google.common.collect.ImmutableMap;

/**
 * {@link Locale2LanguageMapping}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Locale2LanguageMapping {

    private static final ImmutableMap<String, String> mapping;
    static {
        //@formatter:off
        mapping = new ImmutableMap.Builder<String, String>().
            put("sq-AL", "sq-AL").
            put("ar-DZ", "en-US").
            put("es-AR", "es-MX").
            put("en-AU", "en-GB").
            put("de-AT", "de-DE").
            put("az-Latn-AZ", "az-Latn-AZ").
            put("ar-BH", "en-US").
            put("bn-BD", "en-GB").
            put("be-BY", "en-US").
            put("fr-BE", "fr-FR").
            put("nl-BE", "nl-BE").
            put("en-BZ", "en-GB").
            put("es-VE", "es-MX").
            put("es-BO", "es-MX").
            put("hr-BA", "hr-HR").
            put("sr-Latn-BA", "sr-Latn-RS").
            put("pt-BR", "pt-BR").
            put("ms-BN", "ms-MY").
            put("bg-BG", "en-US").
            put("km-KH", "en-US").
            put("fr-CM", "fr-FR").
            put("en-CA", "en-US").
            put("fr-CA", "fr-CA").
            put("es-CL", "es-MX").
            put("zh-CN", "zh-CN").
            put("es-CO", "es-MX").
            put("es-CR", "es-MX").
            put("fr-CI", "fr-FR").
            put("hr-HR", "hr-HR").
            put("cs-CZ", "cs-CZ").
            put("da-DK", "da-DK").
            put("es-DO", "es-MX").
            put("es-EC", "es-MX").
            put("ar-EG", "en-US").
            put("es-SV", "es-MX").
            put("et-EE", "et-EE").
            put("am-ET", "am-ET").
            put("fi-FI", "fi-FI").
            put("sv-FI", "sv-SE").
            put("fr-FR", "fr-FR").
            put("de-DE", "de-DE").
            put("el-GR", "en-US").
            put("es-GT", "es-MX").
            put("fr-HT", "fr-FR").
            put("es-HN", "es-MX").
            put("zh-HK", "en-GB").
            put("en-HK", "en-GB").
            put("hu-HU", "hu-HU").
            put("is-IS", "is-IS").
            put("en-IN", "en-IN").
            put("hi-IN", "en-IN").
            put("id-ID", "id-ID").
            put("fa-IR", "en-US").
            put("ar-IQ", "en-US").
            put("en-IE", "en-GB").
            put("he-IL", "en-US").
            put("it-IT", "it-IT").
            put("en-JM", "en-GB").
            put("ja-JP", "ja-JP").
            put("ar-JO", "en-US").
            put("kk-KZ", "en-US").
            put("sw-KE", "sw-KE").
            put("ko-KR", "en-US").
            put("ar-KW", "en-US").
            put("lo-LA", "en-US").
            put("lv-LV", "lv-LV").
            put("ar-LB", "en-US").
            put("de-LI", "de-DE").
            put("lt-LT", "lt-LT").
            put("fr-LU", "fr-FR").
            put("de-LU", "de-DE").
            put("zh-MO", "en-GB").
            put("mk-MK", "en-US").
            put("en-MY", "en-GB").
            put("ms-MY", "ms-MY").
            put("es-MX", "es-MX").
            put("ro-MD", "ro-RO").
            put("fr-MC", "fr-FR").
            put("sr-Latn-ME", "sr-Latn-CS").
            put("ar-MA", "fr-FR").
            put("fr-MA", "fr-FR").
            put("nl-NL", "nl-NL").
            put("en-NZ", "en-GB").
            put("es-NI", "es-MX").
            put("ha-Latn-NG", "ha-Latn-NG").
            put("nb-NO", "nb-NO").
            put("ar-OM", "en-US").
            put("es-PA", "es-MX").
            put("es-PY", "es-MX").
            put("es-PE", "es-MX").
            put("fil-PH", "en-US").
            put("en-PH", "en-US").
            put("pl-PL", "pl-PL").
            put("pt-PT", "pt-PT").
            put("es-PR", "es-MX").
            put("ar-QA", "en-US").
            put("fr-RE", "fr-FR").
            put("ro-RO", "ro-RO").
            put("ru-RU", "en-US").
            put("ar-SA", "en-US").
            put("fr-SN", "fr-FR").
            put("sr-Latn-RS", "sr-Latn-CS").
            put("zh-SG", "en-GB").
            put("en-SG", "en-GB").
            put("sk-SK", "sk-SK").
            put("sl-SI", "sl-SI").
            put("af-ZA", "af-ZA").
            put("en-ZA", "en-GB").
            put("eu-ES", "eu-ES").
            put("ca-ES", "ca-ES").
            put("gl-ES", "gl-ES").
            put("es-ES", "es-ES").
            put("sv-SE", "sv-SE").
            put("fr-CH", "fr-CH").
            put("de-CH", "de-DE").
            put("it-CH", "it-IT").
            put("ar-SY", "en-US").
            put("zh-TW", "en-US").
            put("th-TH", "en-US").
            put("en-TT", "en-GB").
            put("ar-TN", "en-US").
            put("tr-TR", "tr-TR").
            put("uk-UA", "en-US").
            put("ar-AE", "en-US").
            put("en-GB", "en-GB").
            put("en-US", "en-US").
            put("es-US", "es-MX").
            put("es-UY", "es-MX").
            put("uz-Latn-UZ", "uz-Latn-UZ").
            put("vi-VN", "vi-VN").
            put("ar-YE", "en-US").
            put("en-ZW", "en-GB").build();
        //@formatter:on
    }

    /**
     * Retrieves the language identifier for the specified locale
     * 
     * @param locale The locale
     * @return the language identifier for the specified locale or
     *         <code>null</code> if no such locale exists.
     */
    public static String getLanguageForLocale(String locale) {
        return mapping.get(locale);
    }
}
