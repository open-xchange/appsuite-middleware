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

package com.openexchange.tools.tag;

import com.openexchange.tools.file.TagFiller;

/**
 * Utility Class for Methode parseLine.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class LineParserUtility {

    /**
     * Private Constructor to disable instantiation.
     */
    private LineParserUtility() {
    }

    /**
     * Parses one input line and replaces all tags in square brackets. If null or the same string that is passed by tag to the replace
     * method is returned by the replace method of TagFiller this parseLine method does not replace the tag.
     *
     * @param line Input line to parse.
     * @param fill Implementation of the interface TagFiller to replace tags.
     * @param data To pass some data in an object.
     * @return A string with replaced tags.
     */
    public static String parseLine(final String line, final TagFiller fill, final Object data) {
        final StringBuffer retval = new StringBuffer();
        int start = line.indexOf('[');
        int ende = -1;
        if (start != -1) {
            retval.append(line.substring(0, start));
        } else {
            retval.append(line);
        }
        while (start != -1) {
            ende = line.indexOf(']', start);
            if (ende != -1) {
                final String tag = line.substring(start + 1, ende);
                String replaced = null;
                if (data != null) {
                    replaced = fill.replace(tag, data);
                } else {
                    replaced = fill.replace(tag);
                }
                if (null != replaced) {
                    retval.append(replaced);
                } else {
                    retval.append('[');
                    retval.append(tag);
                    retval.append(']');
                }

                start = line.indexOf('[', ende);
                if (start != -1) {
                    retval.append(line.substring(ende + 1, start));
                }
            } else {
                retval.append(line.substring(start));
            }
        }
        if (ende != -1) {
            retval.append(line.substring(ende + 1));
        }
        return retval.toString();
    }
}
