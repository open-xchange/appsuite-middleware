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

package com.openexchange.file.storage;

import org.apache.commons.io.FilenameUtils;
import com.openexchange.java.Strings;

/**
 * {@link NameBuilder} - Utility class to determine a non-conflicting name through trial & error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NameBuilder {

    private static final String PGP_EXTENSION = ".pgp";

    /**
     * Gets the appropriate {@code NameBuilder} instance for specified name.
     * <p>
     * Provided name is either considered be a folder or a file name dependent on occurrence of extension separator character, which is a dot.
     * If contained, the name is considered be a file name; otherwise a folder name.
     *
     * @param name The name; either a folder or a file name
     * @return The appropriate {@code NameBuilder} instance
     */
    public static NameBuilder nameBuilderFor(String name) {
        if (Strings.isEmpty(name)) {
            return null;
        }

        int index = FilenameUtils.indexOfExtension(name);
        if (index < 0) {
            return new NameBuilder(name);
        }

        String baseName = name.substring(0, index);
        String extension = name.substring(index);

        if (extension.equals(PGP_EXTENSION)) {
            // Don't wrongly split filenames with ".pgp" extension in case original extension is also available. E.g.: "test.txt.pgp" -> "test (1).txt.pgp" instead of "test.txt (1).pgp"
            int i = FilenameUtils.indexOfExtension(baseName);
            if (i > 0) {
                baseName = name.substring(0, i);
                extension = name.substring(i);
            }
        }

        return new NameBuilder(baseName, extension);
    }

    // --------------------------------------------------------------------------

    private final String baseName;
    private final String extension;
    private int count;

    /**
     * Initializes a new {@link NameBuilder}.
     *
     * @param baseName The base name; e.g. <code>"myfolder"</code>
     */
    public NameBuilder(String baseName) {
        this(baseName, null);
    }

    /**
     * Initializes a new {@link NameBuilder}.
     *
     * @param baseName The base name; e.g. <code>"myfile"</code>
     * @param extension The optional extension; e.g. <code>".txt"</code>
     */
    public NameBuilder(String baseName, String extension) {
        super();
        this.baseName = baseName;
        this.extension = Strings.isEmpty(extension) ? null : (extension.startsWith(".") ? extension : "." + extension);
        count = 0;
    }

    /**
     * Advances this instance
     *
     * @return This instance
     */
    public NameBuilder advance() {
        count++;
        return this;
    }

    @Override
    public String toString() {
        if (null == extension) {
            return count == 0 ? baseName : new StringBuilder(baseName).append(" (").append(count).append(')').toString();
        }

        StringBuilder sb = new StringBuilder(baseName);
        if (count > 0) {
            sb.append(" (").append(count).append(')');
        }
        return sb.append(extension).toString();
    }

}
