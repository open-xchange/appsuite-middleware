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

package com.openexchange.importexport;

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
