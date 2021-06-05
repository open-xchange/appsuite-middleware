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

package com.openexchange.contact.provider.folder;

import java.util.Date;
import java.util.List;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link AnnualDateFolderSearchAware}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public interface AnnualDateFolderSearchAware extends FolderSearchAware {

    /**
     * Searches for contacts whose birthday falls into the specified period.
     *
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     *
     * @param folderIds The identifiers of the folders to perform the search in, or <code>null</code> to search in all folders
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose birthdays start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose birthdays end before this date should be returned.
     * @return the contacts found with the search
     * @throws OXException
     */
    List<Contact> searchContactsWithBirthday(List<String> folderIds, Date from, Date until) throws OXException;

    /**
     * Searches for contacts whose anniversary falls into the specified period.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     *
     * @param folderIds The identifiers of the folders to perform the search in, or <code>null</code> to search in all folders
     * @param from Specifies the lower inclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries start on or after this date should be returned.
     * @param until Specifies the upper exclusive limit of the queried range, i.e. only
     *            contacts whose anniversaries end before this date should be returned.
     * @return the contacts found with the search
     * @throws OXException
     */
    List<Contact> searchContactsWithAnniversary(List<String> folderIds, Date from, Date until) throws OXException;

}
