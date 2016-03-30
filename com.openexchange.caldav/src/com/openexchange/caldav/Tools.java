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

package com.openexchange.caldav;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.tasks.TaskExceptionCode;
import com.openexchange.webdav.protocol.WebdavPath;


/**
 * {@link Tools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Tools {

    public static final String OAUTH_SCOPE = "caldav";

    public static Date getLatestModified(Date lastModified1, Date lastModified2) {
        return lastModified1.after(lastModified2) ? lastModified1 : lastModified2;
    }

    public static Date getLatestModified(Date lastModified, CommonObject object) {
        return getLatestModified(lastModified, object.getLastModified());
    }

    public static Date getLatestModified(Date lastModified, UserizedFolder folder) {
        return getLatestModified(lastModified, folder.getLastModifiedUTC());
    }

    /**
     * Parses a numerical identifier from a string, wrapping a possible
     * NumberFormatException into an OXException.
     *
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    public static int parse(String id) throws OXException {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new OXException(e);
        }
    }

    /**
     * Gets the resource name from an url, i.e. the path's name without the
     * filename extension.
     *
     * @param url the webdav path
     * @param fileExtension the extension
     * @return the resource name
     */
    public static String extractResourceName(WebdavPath url, String fileExtension) {
        return null != url ? extractResourceName(url.name(), fileExtension) : null;
    }

    /**
     * Gets the resource name from a filename, i.e. the resource name without the
     * filename extension.
     *
     * @param filename the filename
     * @param fileExtension the extension
     * @return the resource name
     */
    public static String extractResourceName(String filename, String fileExtension) {
        String name = filename;
        if (null != fileExtension) {
            String extension = fileExtension.toLowerCase();
            if (false == extension.startsWith(".")) {
                extension = "." + extension;
            }
            if (null != name && extension.length() < name.length() && name.toLowerCase().endsWith(extension)) {
                return name.substring(0, name.length() - extension.length());
            }
        }
        return name;
    }

    public static boolean isDataTruncation(final OXException e) {
        return OXCalendarExceptionCodes.TRUNCATED_SQL_ERROR.equals(e) || TaskExceptionCode.TRUNCATED.equals(e);
    }

    public static boolean isIncorrectString(OXException e) {
        return OXCalendarExceptionCodes.INVALID_CHARACTER.equals(e) || TaskExceptionCode.INCORRECT_STRING.equals(e);
    }

    public static String formatAsUTC(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    private Tools() {
    	// prevent instantiation
    }

}
