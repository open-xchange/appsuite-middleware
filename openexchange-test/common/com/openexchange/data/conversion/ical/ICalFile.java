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
        final int colonPos = line.indexOf(':');
        final String key;
        final String parameter;
        final String value;
        if (-1 == colonPos) {
            key = line;
            parameter = "";
            value = "";
        } else {
            final String tmp = line.substring(0, colonPos);
            int semicolonPos = tmp.indexOf(';');
            if (semicolonPos != -1) {
                key = tmp.substring(0, semicolonPos);
                parameter = tmp.substring(semicolonPos + 1);
            } else {
                key = tmp;
                parameter = "";
            }
            value = line.substring(colonPos + 1);
        }
        lines.add(new String[] { key, parameter, value });
    }

    public List<String[]> getLines() {
        return lines;
    }

    public String getValue(final String key) {
        for(final String[] line : lines) {
            if(line[0].equals(key)) {
                return line[2];
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
            final String parameter = line[1];
            final String value = line[2];
            sb.append(key);
            if (!"".equals(parameter)) {
                sb.append(';');
                sb.append(parameter);
            }
            if (!"".equals(value)) {
                sb.append(':');
                sb.append(value);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public boolean containsPair(final String name, final String value) {
        for (final String[] line : lines) {
            final String key = line[0];
            final String val = line[2];
            if(key.equals(name) && val.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsEntry(String name, String parameter, String value) {
        for (final String[] line : lines) {
            final String key = line[0];
            final String param = line[1];
            final String val = line[2];
            if (key.equals(name) && param.equals(parameter) && val.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsLine(final String line) {
        for (final String[] l : lines) {
            final String key = l[0];
            if(key.equals(line)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(final String key) {
        return containsLine(key);
    }
}
