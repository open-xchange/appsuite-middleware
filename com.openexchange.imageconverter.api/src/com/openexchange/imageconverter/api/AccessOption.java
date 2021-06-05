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

package com.openexchange.imageconverter.api;

import org.apache.commons.lang.ArrayUtils;

/**
 * {@link AccessOption}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10.0
 */
public enum AccessOption {

    /**
     * If a file already exists, its content is truncated and the
     * initial length directly after accessing the file is 0.
     * <br/>
     * If the file didn't exists before, it will be created and the initial
     * file length directly after accessing the file is 0.
     */
    WRITE_MODE_TRUNCATE,

    /**
     * Existing files that are accessed, are not truncated
     * before the first write action happens.
     * Subsequent write actions append content right after the existing
     * content of the file.
     * </br>
     * The existing content of a file is available using the related
     * {@link IFileItemWriteAccess#getInputStream} method.
     * </br>
     * The InputStream of an initially valid file with a length > 0 is not
     * updated after subsequent write actions, but the initial content is
     * available for the whole lifetime of a file access until the file
     * access is closed.
     * </br>
     * If the file didn't exist before, it will be created and the initial
     * file length directly after accessing the file will be 0.
     */
    WRITE_MODE_APPEND;

    // - Default access option combinations ------------------------------------

    public final static AccessOption[] TRUNCATE = { AccessOption.WRITE_MODE_TRUNCATE };

    public final static AccessOption[] APPEND = { AccessOption.WRITE_MODE_APPEND };


    // - public API -------------------------------------------------------------

    /**
     * Initializes a new {@link AccessOption}.
     */
    AccessOption() {
    }

    /**
     * @return
     */
    public static AccessOption[] getStandardOptions() {
        return TRUNCATE;
    }

    /**
     * @param accessOptions
     * @return
     */
    public static AccessOption[] getNormalizedOptions(AccessOption[] accessOptions) {
        AccessOption[] ret = (null == accessOptions) || (0 == accessOptions.length) ?
            getStandardOptions() :
                accessOptions;

        if (!hasOption(ret, WRITE_MODE_TRUNCATE) && !hasOption(ret, WRITE_MODE_APPEND)) {
            ret = appendOption(ret, WRITE_MODE_TRUNCATE);
        }

        return ret;
    }

    /**
     * @param accessOptions
     * @param accessOption
     */
    public static AccessOption[] appendOption(AccessOption[] accessOptions, AccessOption... accessOption) {
        AccessOption[] ret = accessOptions;

        if ((null != ret) && (null != accessOption)) {
            for (final AccessOption curAccessOption : accessOption) {
                if ((null != curAccessOption) && !ArrayUtils.contains(ret, curAccessOption)) {
                    ret = (AccessOption[]) ArrayUtils.add(ret, curAccessOption);
                }
            }
        }

        return ret;
    }

    /**
     * @param accessOptions
     * @param accessOption
     * @return
     */
    public static boolean hasOption(final AccessOption[] accessOptions, final AccessOption accessOption) {
        return ((null != accessOptions) && (null != accessOption) && ArrayUtils.contains(accessOptions, accessOption));
    }
}
