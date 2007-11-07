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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.tools.versit.old;

public class VCard21 extends OldObjectDefinition {

	public VCard21(String[] propertyNames, OldPropertyDefinition[] properties) {
		super(propertyNames, properties);
		Name = "VCARD";
	}

	private static final String[] TypeParamNames = { "ENCODING", "CHARSET",
			"LANGUAGE", "VALUE", "TYPE" };

	private static final OldParamDefinition[] ImageParams = {
			Encoding,
			TextParam,
			TextParam,
			ValueParam,
			new OldParamDefinition(new String[] { "GIF", "CGM", "WMF", "BMP",
					"MET", "PMB", "DIB", "PICT", "TIFF", "PS", "PDF", "JPEG",
					"MPEG", "MPEG2", "AVI", "QTIME" }) };

	private static final OldParamDefinition[] AdrParams = {
			Encoding,
			TextParam,
			TextParam,
			ValueParam,
			new OldParamDefinition(new String[] { "DOM", "INTL", "POSTAL",
					"PARCEL", "HOME", "WORK" }) };

	private static final OldPropertyDefinition ImageProperty = new OldBinaryPropertyDefinition(
			TypeParamNames, ImageParams);

	public static final VCard21 definition = new VCard21(
			new String[] { "FN", "N", "PHOTO", "BDAY", "ADR", "LABEL", "TEL",
					"EMAIL", "MAILER", "TZ", "GEO", "TITLE", "ROLE", "LOGO",
					"AGENT", "ORG", "NOTE", "REV", "SOUND", "URL", "UID",
					"VERSION", "KEY" },
			new OldPropertyDefinition[] {
					DefaultProperty,
					new OldNPropertyDefinition(DefaultParamNames, DefaultParams),
					ImageProperty,
					new OldDatePropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldNPropertyDefinition(TypeParamNames, AdrParams),
					new OldPropertyDefinition(TypeParamNames, AdrParams),
					new OldPropertyDefinition(TypeParamNames,
							new OldParamDefinition[] {
									Encoding,
									TextParam,
									TextParam,
									ValueParam,
									new OldParamDefinition(new String[] {
											"PREF", "WORK", "HOME", "VOICE",
											"FAX", "MSG", "CELL", "PAGER",
											"BBS", "MODEM", "CAR", "ISDN",
											"VIDEO" }) }),
					new OldPropertyDefinition(TypeParamNames,
							new OldParamDefinition[] {
									Encoding,
									TextParam,
									TextParam,
									ValueParam,
									new OldParamDefinition(new String[] {
											"AOL", "AppleLink", "ATTMail",
											"CIS", "eWorld", "INTERNET",
											"IBMMail", "MCIMail", "POWERSHARE",
											"PRODIGY", "TLX", "X400" }) }),
					DefaultProperty,
					new OldTZPropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldGeoPropertyDefinition(DefaultParamNames,
							DefaultParams),
					DefaultProperty,
					DefaultProperty,
					ImageProperty,
					new OldAgentPropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldCompoundPropertyDefinition(DefaultParamNames,
							DefaultParams),
					DefaultProperty,
					DateTimeProperty,
					new OldSoundPropertyDefinition(TypeParamNames,
							new OldParamDefinition[] {
									Encoding,
									TextParam,
									TextParam,
									ValueParam,
									new OldParamDefinition(new String[] {
											"WAVE", "PCM", "AIFF" }) }),
					new OldURIPropertyDefinition(TypeParamNames,
							new OldParamDefinition[] {
									Encoding,
									TextParam,
									TextParam,
									ValueParam,
									new OldParamDefinition(new String[] {
											"X509", "PGP" }) }),
					DefaultProperty, DefaultProperty, DefaultProperty });

}
