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

package com.openexchange.file.storage;

/**
 * {@link FileStorageCapability}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum FileStorageCapability {

    /**
     * Support for sequence numbers of files and folders.
     */
    SEQUENCE_NUMBERS,

    /**
     * Support for saving files without creating a new version.
     */
    IGNORABLE_VERSION,

    /**
     * Support for storing multiple versions of a document.
     */
    FILE_VERSIONS,

    /**
     * Support for reading and writing files at specific offsets.
     */
    RANDOM_FILE_ACCESS,

    /**
     * Support for searching files by advanced search terms.
     */
    SEARCH_BY_TERM,

    /**
     * Support for E-Tags of folders.
     */
    FOLDER_ETAGS,

    /**
     * Support for recursive E-Tags of folders.
     */
    RECURSIVE_FOLDER_ETAGS,

    /**
     * Support for thumbnail images of files.
     */
    THUMBNAIL_IMAGES,

    /**
     * Support for persistent folder- and file-IDs, i.e. identifiers don't change during rename operations.
     */
    PERSISTENT_IDS,

    /**
     * Support for efficient retrieval of file metadata and contents considering a client-supplied E-Tag.
     */
    EFFICIENT_RETRIEVAL,

    /**
     * Support for locking/unlocking files.
     */
    LOCKS,

    /**
     * Support for individual permissions per file.
     */
    OBJECT_PERMISSIONS,

    /**
     * Support for pagination/ranges.
     */
    RANGES,

    /**
     * Support for storing extended metadata attributes like notes or categories for files.
     */
    EXTENDED_METADATA,

    /**
     * Support for moving multiple files at once.
     */
    MULTI_MOVE,

    /**
     * File storage only supports read-only access
     */
    READ_ONLY,

    /**
     * File storage contains mail attachments
     */
    MAIL_ATTACHMENTS,

    /**
     * Automatic add new file version if file already exists
     */
    AUTO_NEW_VERSION,


    ;
}
