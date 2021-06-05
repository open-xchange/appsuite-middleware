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

package com.openexchange.threadpool;

/**
 * {@link ThreadRenamer} - Offers methods for thread renaming.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ThreadRenamer {

    /**
     * Renames thread's name to given <tt>newName</tt>.
     *
     * @param newName The new name
     */
    void rename(String newName);

    /**
     * Renames thread's prefix to given <tt>newPrefix</tt>.
     * <p>
     * A thread's name is often built according to following pattern:<br>
     * <tt>&lt;prefix&gt;'-'&lt;number&gt;</tt>, e.g. <tt>&quot;MyThread-001&quot;</tt><br>
     * Hence only the first part is renamed to keep thread's number appendix.
     * <p>
     * <b>Note</b>: If thread's name does not obey described pattern, this method does the same as {@link #rename(String)} does.
     *
     * @param newPrefix The new prefix
     */
    void renamePrefix(String newPrefix);

    /**
     * Restores the original name.
     */
    void restoreName();

}
