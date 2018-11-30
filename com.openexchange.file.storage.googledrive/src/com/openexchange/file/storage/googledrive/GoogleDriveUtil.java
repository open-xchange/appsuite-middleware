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

package com.openexchange.file.storage.googledrive;

import java.io.IOException;
import java.util.ArrayList;
import com.openexchange.java.Strings;

/**
 * {@link GoogleDriveUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public final class GoogleDriveUtil {

    //@formatter:off
    /**
     * Parses the exception code from the given {@link IOException} <p>
     * The errors message is expected to look like
     * <pre> 
     * 400 Bad Request
     * {
     *  "code" : 400,
     *  "errors" : [ {
     *      "domain" : "global",
     *      "location" : "fields",
     *      "locationType" : "parameter",
     *      "message" : "Invalid field selection id.parents",
     *      "reason" : "invalidParameter"
     *  } ],
     *  "message" : "Invalid field selection id.parents"
     * }
     * </pre>
     * 
     * @param e The {@link IOException}
     * @return The status code or <code>-1</code> if no code can be parsed
     */
    //@formatter:on
    public static int getStatusCode(IOException e) {
        if (null != e && Strings.isNotEmpty(e.getMessage())) {
            try {
                Integer status = Strings.isEmpty(e.getMessage()) ? Integer.valueOf(-1) : Integer.valueOf(e.getMessage().substring(0, 3));
                return null == status ? -1 : status.intValue();
            } catch (NumberFormatException nfe) {
                // Ignore and fall through
            }
        }
        return -1;
    }

    /**
     * Sets parent folders to a {@link com.google.api.services.drive.model.File}
     * 
     * @param file The file to set the parent in
     * @param parentIds The identifier of the parent folder
     * @see <a href="https://developers.google.com/drive/api/v3/reference/files/copy">Files: copy</a>
     */
    public static void setParentFolder(com.google.api.services.drive.model.File file, String... parentIds) {
        ArrayList<String> parents = new ArrayList<>(parentIds.length);
        for (String parent : parentIds) {
            parents.add(parent);
        }
        file.setParents(parents);
    }

    /**
     * Get the parents of the given file
     * 
     * @param file The file to get the parents from
     * @return Parents as comma separated list
     */
    public static String getParentFolders(com.google.api.services.drive.model.File file) {
        StringBuilder sb = new StringBuilder();
        for (String parent : file.getParents()) {
            sb.append(parent);
            sb.append(',');
        }
        return sb.substring(0, sb.length() - 1);
    }

}
