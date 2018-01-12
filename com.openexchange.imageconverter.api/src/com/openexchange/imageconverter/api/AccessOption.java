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
 *    on the web site http: *www.open-xchange.com/EN/legal/index.html.
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
 *    http: *www.open-xchange.com/EN/developer/. The contributing author shall be
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
    WRITE_MODE_APPEND,

    ACCESS_MODE_WAIT;

    // - Default access option combinations ------------------------------------

    public final static AccessOption[] TRUNCATE = { AccessOption.WRITE_MODE_TRUNCATE };

    public final static AccessOption[] TRUNCATE_WAIT = { AccessOption.WRITE_MODE_TRUNCATE, ACCESS_MODE_WAIT };

    public final static AccessOption[] APPEND = { AccessOption.WRITE_MODE_APPEND };

    public final static AccessOption[] FILE_BASED_APPEND_WAIT = { AccessOption.WRITE_MODE_APPEND, ACCESS_MODE_WAIT };


    // - public API -------------------------------------------------------------

    /**
     * Initializes a new {@link AccessOption}.
     */
    private AccessOption() {
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
