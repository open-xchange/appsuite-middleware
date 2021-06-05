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

package com.openexchange.file.storage.googledrive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                return Strings.isEmpty(e.getMessage()) ? -1 : Integer.parseInt(e.getMessage().substring(0, 3));
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
        if (parentIds == null) {
            return;
        }
        int length = parentIds.length;
        if (length <= 0) {
            return;
        }
        if (length == 1) {
            file.setParents(Collections.singletonList(parentIds[0]));
        } else {
            List<String> parents = new ArrayList<>(length);
            for (String parent : parentIds) {
                parents.add(parent);
            }
            file.setParents(parents);
        }
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
