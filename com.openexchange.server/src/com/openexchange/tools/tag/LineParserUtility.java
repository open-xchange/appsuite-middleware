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
