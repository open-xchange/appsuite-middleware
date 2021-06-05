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

package com.openexchange.file.storage.meta;

import java.util.Comparator;
import com.openexchange.file.storage.File;

/**
 * {@link FileComparator} - A comparator for files.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileComparator implements Comparator<File> {

    private static final FileFieldGet GET = new FileFieldGet();

    private final File.Field by;

    /**
     * Initializes a new {@link FileComparator}.
     *
     * @param by The field to sort by
     */
    public FileComparator(final File.Field by) {
        super();
        this.by = by;
    }

    @Override
    public int compare(final File o1, final File o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }

        Object v1 = by.doSwitch(GET, o1);
        Object v2 = by.doSwitch(GET, o2);
        if (v1 == v2) {
            return 0;
        }
        if (v1 == null) {
            return -1;
        }
        if (v2 == null) {
            return 1;
        }

        if (v1 instanceof Comparable) {
            return ((Comparable<Object>) v1).compareTo(v2);
        }
        return 0;
    }

}
