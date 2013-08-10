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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.storage.filter;

import java.util.Arrays;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.drive.storage.DriveConstants;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link SynchronizedFileFilter}
 *
 * A {@link FileNameFilter} that only lets through files that are actually synchronized, i.e. temporary files or infostore entries
 * without attached document.
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 */
public class SynchronizedFileFilter extends FileNameFilter {

    public static final SynchronizedFileFilter getInstance() {
        return INSTANCE;
    }

    private static final SynchronizedFileFilter INSTANCE = new SynchronizedFileFilter();

    private final Pattern excudedFilesPattern;

    private SynchronizedFileFilter() {
        super();
        ConfigurationService configService = DriveServiceLookup.getService(ConfigurationService.class);
        String excludedFilesPattern = "thumbs\\.db|desktop\\.ini|\\.ds_store|icon\\\r";
        if (null != configService) {
            excludedFilesPattern = configService.getProperty("com.openexchange.drive.excludedFilesPattern", excludedFilesPattern);
        }
        this.excudedFilesPattern = Pattern.compile(excludedFilesPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    @Override
    public boolean accept(String fileName) throws OXException {
        if (Strings.isEmpty(fileName)) {
            return false; // no empty filenames
        }
        if (fileName.endsWith(DriveConstants.FILEPART_EXTENSION)) {
            return false; // no temporary upload files
        }
        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (0 <= Arrays.binarySearch(DriveConstants.ILLEGAL_FILENAME_CHARS, c)) {
                return false; // no invalid characters
            }
        }
//        if (DriveConstants.FILENAME_VALIDATION_PATTERN.matcher(fileName).matches()) {
//            return false; // no invalid filenames
//        }
        if (excudedFilesPattern.matcher(fileName).matches()) {
            return false; // no excluded files
        }
        return true;
    }

}