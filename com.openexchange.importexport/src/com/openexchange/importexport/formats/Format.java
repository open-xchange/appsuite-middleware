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

package com.openexchange.importexport.formats;

/**
 * This enumeration lists formats for import or export.
 *
 * @author Tobias Prinz, mailto:tobias.prinz@open-xchange.com
 *
 */
public enum Format {
    CSV("CSV" , "Comma separated values","text/csv","csv"),
    OUTLOOK_CSV("OUTLOOK_CSV" , "Comma separated values","text/csv","csv"),
    ICAL("ICAL" , "iCal","text/calendar","ics"),
    VCARD("VCARD", "vCard","text/x-vcard","vcf"),
    TNEF("TNEF" , "Transport Neutral Encapsulation Format" , "application/ms-tnef", "tnef");

    private String constantName, mimetype, longName, extension;

    private Format(final String constantName, final String longName, final String mimetype, final String extension) {
        this.constantName = constantName;
        this.longName = longName;
        this.mimetype = mimetype;
        this.extension = extension;
    }

    public String getFullName() {
        return this.longName;
    }

    public String getMimeType() {
        return this.mimetype;
    }

    public String getExtension() {
        return this.extension;
    }

    public String getConstantName() {
        return this.constantName;
    }

    public static Format getFormatByMimeType(final String mimeType) {
        for (final Format f : Format.values()) {
            if (f.getMimeType().equals(mimeType)) {
                return f;
            }
        }
        if ("text/comma-separated-values".equals(mimeType)) {
            return CSV;
        }
        if ("text/vcard".equals(mimeType)) {
            return VCARD;
        }
        if ("text/directory".equals(mimeType)) {
            return VCARD;
        }
        if ("text/calendar".equals(mimeType)) {
            return VCARD;
        }
        if ("text/x-vcalendar".equals(mimeType)) {
            return VCARD;
        }
        return null;
    }

    public static Format getFormatByConstantName(final String constantName) {
        for (final Format f : Format.values()) {
            if (f.getConstantName().equalsIgnoreCase(constantName)) {
                return f;
            }
        }
        return null;
    }

    public static boolean containsConstantName(final String name) {
        for (final Format f : Format.values()) {
            if (name.equalsIgnoreCase(f.constantName)) {
                return true;
            }
        }
        return false;
    }
}
