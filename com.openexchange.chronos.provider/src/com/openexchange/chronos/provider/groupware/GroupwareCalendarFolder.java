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

package com.openexchange.chronos.provider.groupware;

import java.util.Date;
import com.openexchange.chronos.provider.CalendarFolder;

/**
 * {@link GroupwareCalendarFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public interface GroupwareCalendarFolder extends CalendarFolder {

    /**
     * Indicates if this folder is a default folder.
     *
     * @return <code>true</code> if this folder is a default folder; otherwise <code>false</code>
     */
    boolean isDefaultFolder();

    /**
     * Get the identifier of the parent folder
     *
     * @return The identifier of the parent or <code>null</code>
     */
    String getParentId();

    /**
     * Gets the entity which lastly modified this folder.
     *
     * @return The entity which lastly modified this folder or <code>-1</code>
     */
    int getModifiedBy();

    /**
     * Gets the entity which created this folder.
     *
     * @return The entity which created this folder or <code>-1</code>
     */
    int getCreatedBy();

    /**
     * Gets the creation date as {@link Date}.
     *
     * @return The creation date
     */
    Date getCreationDate();

    /**
     * The {@link GroupwareFolderType} of this folder. Possible values are
     * <ul>
     * <li> {@link GroupwareFolderType#PRIVATE} </li>
     * <li> {@link GroupwareFolderType#PUBLIC} </li>
     * <li> {@link GroupwareFolderType#SHARED} </li>
     * </ul>
     *
     * @return A {@link GroupwareFolderType}
     */
    GroupwareFolderType getType();

}
