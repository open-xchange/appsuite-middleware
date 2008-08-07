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

package com.openexchange.data.conversion.ical;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ICalFile {

    private final List<String[]> lines = new ArrayList<String[]>();

    public ICalFile(final Reader reader) throws IOException {
        final BufferedReader lines = new BufferedReader(reader);
        String line = null;
        while((line = lines.readLine()) != null) {
            addLine(line);
        }
    }

    private void addLine(final String line) {
        int colonPos = line.indexOf(':');
        final String key;
        final String value;
        if (-1 == colonPos) {
            key = line;
            value = "";
        } else {
            key = line.substring(0, colonPos);
            value = line.substring(colonPos + 1);
        }
        lines.add(new String[]{key, value});
    }

    public List<String[]> getLines() {
        return lines;
    }

    public String getValue(final String key) {
        for(final String[] line : lines) {
            if(line[0].equals(key)) {
                return line[1];
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String[] line : lines) {
            final String key = line[0];
            final String value = line[1];
            sb.append(key);
            if (!"".equals(value)) {
                sb.append(':');
                sb.append(value);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public boolean containsPair(String name, String value) {
        for (final String[] line : lines) {
            final String key = line[0];
            final String val = line[1];
            if(key.equals(name) && val.equals(value)) {
                return true;
            }
        }
        return false;
    }
    public boolean containsLine(String line) {
        for (final String[] l : lines) {
            final String key = l[0];
            if(key.equals(line)) {
                return true;
            }
        }
        return false;
    }
}
