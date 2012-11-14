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
 *    of the original copyright holder_MSG = s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder_MSG = s) and/or original author_MSG = s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright _MSG = C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.aws.s3.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link OXAWSS3ExceptionMessages}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class OXAWSS3ExceptionMessages implements LocalizableStrings {

    public final static String S3_IOERROR_MSG = "IO error.";

    public final static String S3_SAVE_FAILED_MSG = "Saving new file failed: %1$s";

    public final static String S3_GET_FILE_FAILED_MSG = "Get file %1$s failed: %2$s";

    public final static String S3_GET_FILELIST_FAILED_MSG = "Get file listing failed: %1$s";

    public final static String S3_GET_FILESIZE_FAILED_MSG = "Get size of file %1$s failed: %2$s";

    public final static String S3_GET_MIMETYPE_FAILED_MSG = "Get mime type of file %1$s failed: %2$s";

    public final static String S3_DELETE_FILE_FAILED_MSG = "Delete file %1$s failed: %2$s";

    public final static String S3_DELETE_MULTIPLE_FAILED_MSG = "Delete %1$i failed: %2$s";

    public final static String S3_CREATE_HASH_FAILED_MSG = "Creating md5 hash of file %1$s failed: %2$s";

    public final static String S3_INITIALIZATION_ERROR_MSG = "Could not initialize filestorage: %1$s";

    public final static String S3_DELETE_ALL_ERROR_MSG = "Deletion of all files in bucket %1$s failed: %2$s";

    public final static String S3_VERSIONING_DISABLED_MSG = "Versioning is disabled";

    /**
     * Initializes a new {@link OXAWSS3ExceptionMessages}.
     */
    private OXAWSS3ExceptionMessages() {
        super();
    }

}
