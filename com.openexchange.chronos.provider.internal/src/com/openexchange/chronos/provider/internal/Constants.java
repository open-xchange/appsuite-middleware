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

package com.openexchange.chronos.provider.internal;

import com.openexchange.folderstorage.ContentType;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Constants}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum Constants {
    ;

    /** The static identifier of the internal calendar provider */
    public static final String PROVIDER_ID = "chronos";

    /** The static identifier of the single default account in the internal calendar provider */
    public static final int ACCOUNT_ID = 0;

    /** The static qualified identifier of the single default account in the internal calendar provider */
    public static final String QUALIFIED_ACCOUNT_ID = "cal://0";

    /** The identifier of the folder tree the calendar provider is using */
    public static final String TREE_ID = com.openexchange.folderstorage.FolderStorage.REAL_TREE_ID;

    /** The used folder storage content type */
    public static final ContentType CONTENT_TYPE = com.openexchange.folderstorage.database.contentType.CalendarContentType.getInstance();

    /** The identifier of the "private" root folder */
    static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    /** The identifier of the "public" root folder */
    static final String PUBLIC_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);

    /** The identifier of the "shared" root folder */
    static final String SHARED_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID);

    /** The prefix used for stored user properties of folders */
    static final String USER_PROPERTY_PREFIX = "cal/";

}
