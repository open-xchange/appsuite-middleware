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

package com.openexchange.groupware.infostore.mbean;

import java.util.List;
import javax.management.MBeanException;

/**
 * {@link FileChecksumsMBean}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface FileChecksumsMBean {

    static final String DOMAIN = "com.openexchange.file.storage";

    /**
     * Provides a listing of all files in a context with a missing checksum property.
     *
     * @param contextId The identifier of the context to list the files for
     * @return A listing of all files in the context with a missing checksum property
     */
    List<String> listFilesWithoutChecksumInContext(int contextId) throws MBeanException;

    /**
     * Provides a listing of all files in a database with a missing checksum property.
     *
     * @param databaseId The read- or write-pool identifier of the database to list the files for
     * @return A listing of all files in the database with a missing checksum property
     */
    List<String> listFilesWithoutChecksumInDatabase(int databaseId) throws MBeanException;

    /**
     * Provides a listing of all files in all contexts with a missing checksum property.
     *
     * @return A listing of all files with a missing checksum property
     */
    List<String> listAllFilesWithoutChecksum() throws MBeanException;

    /**
     * Calculates and stores missing checksums for all files of a specific context.
     *
     * @param contextId The identifier of the context to calculate the missing checksums for
     * @return A listing of all files in the context where the missing checksum was calculated for
     */
    List<String> calculateMissingChecksumsInContext(int contextId) throws MBeanException;

    /**
     * Calculates and stores missing checksums for all files of a database.
     *
     * @param databaseId The read- or write-pool identifier of the database to calculate the missing checksums for
     * @return A listing of all files in the database where the missing checksum was calculated for
     */
    List<String> calculateMissingChecksumsInDatabase(int databaseId) throws MBeanException;

    /**
     * Calculates and stores missing checksums for all files in all contexts.
     *
     * @return A listing of all files where the missing checksum was calculated for
     */
    List<String> calculateAllMissingChecksums() throws MBeanException;

}
