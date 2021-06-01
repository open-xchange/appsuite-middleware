package com.openexchange.l10n;

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

import java.util.Locale;

/**
 * This enum combines collations, charsets and locales for Java and MySQL into
 * one consistent package. This way, you always know which collation to use for
 * a Java locale or the other way around. Note that the only unique attribute is
 * the mysql collation: Both locales and charsets might occur more than once.
 *
 * @author tobiasp
 *
 */
public enum SuperCollator {
	PRC_1		("PRC", Locale.PRC, "gb2312", "gb2312_chinese_ci"),
	PRC_2		("PRC", Locale.PRC, "gbk","gbk_chinese_ci"),
	CHINA_TAIWAN("TAIWAN",Locale.TAIWAN, "big5","big5_chinese_ci"),
	CHINA_2		("CHINESE", Locale.CHINESE, "gb2312","gb2312_chinese_ci"),
	CHINA_3		("CHINA", Locale.CHINA, "gb2312",	"gb2312_chinese_ci"),
	DEFAULT		("DEFAULT", Locale.getDefault(), "utf8","utf8_general_ci");

	private String collation;
	private String sqlCharset;
	private Locale locale;
	private String name;

	public String getSqlCollation() {
		return collation;
	}

	public String getSqlCharset() {
		return sqlCharset;
	}

	public Locale getJavaLocale() {
		return locale;
	}

	public String getName() {
		return name;
	}
	SuperCollator(String name, Locale local, String sqlCharset, String collation) {
		this.name = name;
		this.locale = local;
		this.sqlCharset = sqlCharset;
		this.collation = collation;
	}

	public static SuperCollator get(String something) {
		SuperCollator result = null;

		result = getBySqlCollation(something);
		if (result != null) {
            return result;
        }

		result = getBySqlCharset(something);
		if (result != null) {
            return result;
        }

		result = getByJavaLocale(something);
		if (result != null) {
            return result;
        }

		result = getByName(something);
		if (result != null) {
            return result;
        }

		return result;
	}

	public static SuperCollator getByJavaLocale(String something) {
		if (something == null) {
            return null;
        }

		String[] parts = something.split("_");

		String lang = null, country = null, variant = null;
		Locale javaLocale;

		if (parts.length > 0) {
            lang = parts[0];
        }
		if (parts.length > 1) {
            country = parts[1];
        }
		if (parts.length > 2) {
            variant = parts[2];
        }

		for (SuperCollator loc : values()) {
			javaLocale = loc.getJavaLocale();
			if (javaLocale.getVariant().equalsIgnoreCase(variant)
				&& javaLocale.getCountry().equalsIgnoreCase(country)
				&& javaLocale.getLanguage().equalsIgnoreCase(lang)) {
                return loc;
            }
		}

		for (SuperCollator loc : values()) {
			javaLocale = loc.getJavaLocale();
			if (javaLocale.getCountry().equalsIgnoreCase(country)
				&& javaLocale.getLanguage().equalsIgnoreCase(lang)) {
                return loc;
            }
		}

		for (SuperCollator loc : values()) {
			javaLocale = loc.getJavaLocale();
			if (javaLocale.getLanguage().equalsIgnoreCase(lang)) {
                return loc;
            }
		}

		return null;
	}

	public static SuperCollator getBySqlCollation(String something) {
		for (SuperCollator loc : values()) {
            if (loc.getSqlCollation().equalsIgnoreCase(something)) {
                return loc;
            }
        }
		return null;
	}

	public static SuperCollator getBySqlCharset(String something) {
		for (SuperCollator loc : values()) {
            if (loc.getSqlCharset().equalsIgnoreCase(something)) {
                return loc;
            }
        }
		return null;
	}

	public static SuperCollator getByName(String something) {
		for (SuperCollator loc : values()) {
            if (loc.getName().equalsIgnoreCase(something)) {
                return loc;
            }
        }
		return null;
	}

}
