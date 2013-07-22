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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.file.external;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link FileStorageExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class FileStorageExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link FileStorageExceptionMessage}.
     */
    private FileStorageExceptionMessage() {
        super();
    }

    // An IO error occurred: %s
    public final static String IOERROR_MSG = "An IO error occurred: %s";

    // May be used to turn the IOException of getInstance into a proper OXException
    public final static String INSTANTIATIONERROR_MSG = "File store could not be accessed: %s";

    // Cannot create directory "%1$s" in file storage.
    public final static String CREATE_DIR_FAILED_MSG = "Cannot create directory \"%1$s\" in file storage.";

    // Unsupported encoding.
    public final static String ENCODING_MSG = "Unsupported encoding.";

    // Number parsing problem.
    public final static String NO_NUMBER_MSG = "Number parsing problem.";

    // File storage is full.
    public final static String STORE_FULL_MSG = "File storage is full.";

    // Depth mismatch while computing next entry.
    public final static String DEPTH_MISMATCH_MSG = "'Depth' mismatch while computing next entry.";

    // Cannot remove lock file.
    public final static String UNLOCK_MSG = "Cannot remove lock file.";

    // Cannot create lock file here %1$s. Please check for a stale .lock file, inappropriate permissions or usage of the file store for too long a time.
    public final static String LOCK_MSG = "Cannot create lock file here %1$s. Please check for a stale .lock file, inappropriate permissions or usage of the file store for too long a time.";

    // Eliminating the file storage failed.
    public final static String NOT_ELIMINATED_MSG = "Eliminating the file storage failed.";

    // File does not exist in file storage "%1$s". Consider running consistency tool.
    public final static String FILE_NOT_FOUND_MSG = "File does not exist in file store \"%1$s\". Consider running the consistency tool.";

    // The requested range (offset: %1$d, length: %2$d) for the file \"%3$s\" (current size: %4$d) is invalid.
    public final static String INVALID_RANGE_MSG = "The requested range (offset: %1$d, length: %2$d) for the file \"%3$s\" (current size: %4$d) is invalid.";

    // The specified offset %1$d for the file \"%2$s\" (current size: %3$d) is invalid.
    public final static String INVALID_OFFSET_MSG = "The specified offset %1$d for the file \"%2$s\" (current size: %3$d) is invalid.";

    // The specified length %1$d for the file \"%2$s\" (current size: %3$d) is invalid.
    public final static String INVALID_LENGTH_MSG = "The specified length %1$d for the file \"%2$s\" (current size: %3$d) is invalid.";

}
