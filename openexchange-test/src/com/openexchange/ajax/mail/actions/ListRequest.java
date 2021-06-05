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

package com.openexchange.ajax.mail.actions;

import com.openexchange.ajax.framework.CommonListRequest;
import com.openexchange.ajax.mail.FolderAndID;

/**
 * {@link ListRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ListRequest extends CommonListRequest {

    public static final int[] DEFAULT_COLUMNS = { 600, 601, 612, 602, 603, 607, 610, 608, 611, 614, 102, 604, 609 };

    /**
     * Default constructor.
     */
    public ListRequest(final String[][] folderAndMailIds, final int[] columns) {
        super(AbstractMailRequest.MAIL_URL, folderAndMailIds, columns);
    }

    public ListRequest(final String[][] folderAndMailIds, final int[] columns, final boolean failOnError) {
        super(AbstractMailRequest.MAIL_URL, folderAndMailIds, columns, failOnError);
    }

    public ListRequest(final String[][] folderAndMailIds, final String alias) {
        super(AbstractMailRequest.MAIL_URL, folderAndMailIds, alias);
    }

    public ListRequest(final String[][] folderAndMailIds, final String alias, final boolean failOnError) {
        super(AbstractMailRequest.MAIL_URL, folderAndMailIds, alias, failOnError);
    }

    public ListRequest(final FolderAndID[] mailPaths, final int[] columns) {
        this(ListRequest.toFolderAndMailIds(mailPaths), columns, true);
    }

    public static final String[][] toFolderAndMailIds(final FolderAndID[] mailPaths) {
        final String[][] retval = new String[mailPaths.length][];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = new String[] { mailPaths[i].folderId, mailPaths[i].id };
        }
        return retval;
    }
}
