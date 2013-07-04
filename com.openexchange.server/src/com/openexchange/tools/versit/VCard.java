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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.versit;

import com.openexchange.tools.versit.encodings.BASE64Encoding;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.DateTimeValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.DateValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.FloatValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.ListValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.URIValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2426.UTCOffsetValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2426.VCardValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.BinaryValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.TextValueDefinition;

/**
 * {@link VCard} - <a href="http://tools.ietf.org/html/rfc2426">vCard MIME Directory Profile</a>
 *
 * @author <a href="mailto:viktor.pracht@open-xchange.com">Viktor Pracht</a>
 */
public class VCard {

    // Empty arrays

    private static final String[] NoNames = {};

    private static final ObjectDefinition[] NoChildren = {};

    private static final ParameterDefinition[] NoParameters = {};

    // Arrays of encodings

    private static final String[] BEncodingName = { "B" };

    private static final Encoding[] BEncoding = { new BASE64Encoding() };

    // Value definitions

    private static final ValueDefinition CommaList = new ListValueDefinition(',', TextValueDefinition.Default);

    private static final ValueDefinition SemicolonList = new ListValueDefinition(';', TextValueDefinition.Default);

    private static final ValueDefinition DoubleList = new ListValueDefinition(';', CommaList);

    private static final ValueDefinition BinaryValue = new BinaryValueDefinition(BEncodingName, BEncoding);

    // Arrays of value definitions

    private static final String[] BinaryValueNames = { "BINARY", "URI", "URL" };

    private static final ValueDefinition[] BinaryValues = { BinaryValue, URIValueDefinition.Default, URIValueDefinition.Default };

    private static final String[] DateValueNames = { "DATE", "DATE-TIME" };

    private static final ValueDefinition[] DateValues = { DateValueDefinition.Default, DateTimeValueDefinition.Default };

    private static final String[] TZValueNames = { "UTC-OFFSET", "TEXT" };

    private static final ValueDefinition[] TZValues = { UTCOffsetValueDefinition.Default, TextValueDefinition.Default };

    private static final String[] AgentValueNames = { "VCARD", "TEXT", "URI" };

    private static final ValueDefinition[] AgentValues = {
        VCardValueDefinition.Default, TextValueDefinition.Default, URIValueDefinition.Default };

    private static final String[] KeyValueNames = { "BINARY", "TEXT" };

    private static final ValueDefinition[] KeyValues = { BinaryValue, TextValueDefinition.Default };

    // Property definitions

    private static final PropertyDefinition DefaultProperty = new PropertyDefinition(TextValueDefinition.Default);

    private static final PropertyDefinition DoubleListProperty = new PropertyDefinition(DoubleList);

    private static final PropertyDefinition CommaListProperty = new PropertyDefinition(CommaList);

    private static final PropertyDefinition BinaryProperty = new PropertyDefinition(
        BinaryValue,
        BinaryValueNames,
        BinaryValues,
        NoNames,
        NoParameters);

    // Arrays of property definitions

    private static final String[] PropertyNames3 = {
        "NAME", "PROFILE", "SOURCE", "FN", "N", "NICKNAME", "PHOTO", "BDAY", "ADR", "LABEL", "TEL", "EMAIL", "MAILER", "TZ", "GEO",
        "TITLE", "ROLE", "LOGO", "AGENT", "ORG", "CATEGORIES", "NOTE", "PRODID", "REV", "SORT-STRING", "SOUND", "UID", "URL", "VERSION",
        "CLASS", "KEY", "X-OPEN-XCHANGE-CTYPE", "IMPP", "X-PHONETIC-FIRST-NAME", "X-PHONETIC-LAST-NAME" };

    private static final PropertyDefinition[] Properties3 = {
        DefaultProperty, DefaultProperty, DefaultProperty, DefaultProperty, DoubleListProperty, CommaListProperty, BinaryProperty,
        new PropertyDefinition(DateValueDefinition.Default, DateValueNames, DateValues, NoNames, NoParameters), DoubleListProperty,
        DefaultProperty, DefaultProperty, DefaultProperty, DefaultProperty,
        new PropertyDefinition(UTCOffsetValueDefinition.Default, TZValueNames, TZValues, NoNames, NoParameters),
        new PropertyDefinition(new ListValueDefinition(';', FloatValueDefinition.Default)), DefaultProperty, DefaultProperty,
        BinaryProperty, new PropertyDefinition(VCardValueDefinition.Default, AgentValueNames, AgentValues, NoNames, NoParameters),
        new PropertyDefinition(SemicolonList), CommaListProperty, DefaultProperty, DefaultProperty,
        new PropertyDefinition(DateTimeValueDefinition.Default, DateValueNames, DateValues, NoNames, NoParameters), DefaultProperty,
        BinaryProperty, DefaultProperty, new PropertyDefinition(URIValueDefinition.Default), DefaultProperty, DefaultProperty,
        new PropertyDefinition(BinaryValue, KeyValueNames, KeyValues, NoNames, NoParameters), DefaultProperty, DefaultProperty, 
        DefaultProperty, DefaultProperty };

    // Arrays of object definitions

    private static final String[] Versions = { "3.0" };

    private static final ObjectDefinition[] Definitions = { new ObjectDefinition(PropertyNames3, Properties3, NoNames, NoChildren) };

    // Versioned object definition

    public static final VersionedObjectDefinition definition = new VersionedObjectDefinition(Versions, Definitions);

}
