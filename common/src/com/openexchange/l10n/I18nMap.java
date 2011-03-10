package com.openexchange.l10n;
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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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



import java.util.Locale;

/**
 * @author tobiasp
 *
 */
public enum I18nMap {
	PRC_1(Locale.PRC, "gb2312", "gb2312_chinese_ci"),
	PRC_2(Locale.PRC, "gbk", "gbk_chinese_ci"),
	CHINA_1(Locale.TAIWAN, "big5", "big5_chinese_ci"),
	CHINA_2(Locale.CHINESE, "gb2312", "gb2312_chinese_ci"),
	CHINA_3(Locale.CHINA, "gb2312", "gb2312_chinese_ci"),
	DEFAULT(Locale.getDefault(), "utf8", "utf8_general_ci");
	
	private String collation;
	private String sqlCharset;
	private Locale locale;

	public String getSqlCollation() {
		return collation;
	}

	public String getSqlCharset() {
		return sqlCharset;
	}

	public Locale getJavaLocale() {
		return locale;
	}

	I18nMap(Locale local, String sqlCharset, String collation){
		this.locale = local;
		this.sqlCharset = sqlCharset;
		this.collation = collation;
	}
	
	public static I18nMap get(String something){
		for(I18nMap loc : values()){
			if(loc.getSqlCollation().equalsIgnoreCase(something))
				return loc;
			if(loc.getSqlCharset().equalsIgnoreCase(something))
				return loc;
			
			Locale javaLocale = loc.getJavaLocale();
			if(javaLocale.getCountry().equalsIgnoreCase(something))
				return loc;
			if(javaLocale.getLanguage().equalsIgnoreCase(something))
				return loc;
			if(javaLocale.getVariant().equalsIgnoreCase(something))
				return loc;
		}
		return DEFAULT;
	}

}
