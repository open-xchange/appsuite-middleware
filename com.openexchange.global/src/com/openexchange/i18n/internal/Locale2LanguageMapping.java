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
