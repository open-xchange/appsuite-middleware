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

package com.openexchange.test.common.data.conversion.ical;

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
        while ((line = lines.readLine()) != null) {
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
        for (final String[] line : lines) {
            if (line[0].equals(key)) {
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
            if (key.equals(name) && val.equals(value)) {
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
            if (key.equals(line)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(final String key) {
        return containsLine(key);
    }
}
