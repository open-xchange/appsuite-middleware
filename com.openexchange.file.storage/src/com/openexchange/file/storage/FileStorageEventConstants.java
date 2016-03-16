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
 * {@link FileStorageEventConstants}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileStorageEventConstants {

    public static final String UPDATE_TOPIC = "com/openexchange/groupware/infostore/update";

    public static final String CREATE_TOPIC = "com/openexchange/groupware/infostore/insert";

    public static final String DELETE_TOPIC = "com/openexchange/groupware/infostore/delete";

    public static final String ACCESS_TOPIC = "com/openexchange/groupware/infostore/access";

    public static final String ALL_TOPICS = "com/openexchange/groupware/infostore/*";

    public static final String UPDATE_FOLDER_TOPIC = "com/openexchange/groupware/fsfolder/update";

    public static final String CREATE_FOLDER_TOPIC = "com/openexchange/groupware/fsfolder/insert";

    public static final String DELETE_FOLDER_TOPIC = "com/openexchange/groupware/fsfolder/delete";

    public static final String ALL_FOLDER_TOPICS = "com/openexchange/groupware/fsfolder/*";

    public static final String SESSION = "session";

    public static final String SERVICE = "service";

    public static final String ACCOUNT_ID = "accountId";

    /**
     * The folder ID in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String FOLDER_ID = "folderId";

    /**
     * The parent folder ID in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String PARENT_FOLDER_ID = "parentFolderId";

    /**
     * The old parent folder ID (in case of moves) in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String OLD_PARENT_FOLDER_ID = "oldParentFolderId";

    /**
     * The path to the default folder in an array of folder IDs in their absolute/unique form, i.e. all containing the service/account
     * information.
     */
    public static final String FOLDER_PATH = "folderPath";

    /**
     * The object ID in it's absolute/unique form, i.e. containing the service/account information.
     */
    public static final String OBJECT_ID = "objectId";

    public static final String FILE_NAME = "fileName";

    public static final String VERSIONS = "versions";

    public static final String E_TAG = "eTag";

    public static final String HARD_DELETE = "hardDelete";

    /**
     * Indicates that any share-related cleanup has already been processed.
     */
    public static final String SHARE_CLEANUP_DONE = "shareCleanupDone";

}
