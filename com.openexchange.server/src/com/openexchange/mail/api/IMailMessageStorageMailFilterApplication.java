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

package com.openexchange.mail.api;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailFilterResult;
import com.openexchange.mail.search.SearchTerm;

/**
 * {@link IMailMessageStorageMailFilterApplication} - Extends basic message storage by applying a mail filter expression to a mailbox' messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailMessageStorageMailFilterApplication extends IMailMessageStorage {

    /**
     * Indicates if applying mail filters are supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isMailFilterApplicationSupported() throws OXException;

    /**
     * Applies the given mail filter script to the messages contained in folder specified by full name.
     *
     * @param fullName the folder full name
     * @param mailFilterScript The mail filter script to apply
     * @param searchTerm The search term to filter the messages to which the mail filter script is supposed to be applied; may be <code>null</code> to apply to all messages
     * @param acceptOkFilterResults <code>true</code> in case OK filter results should be kept; otherwise <code>false</code> to discard them (and only return filter results of either ERRORS or WARNINGS)
     * @return The filter results
     * @throws OXException If an error occurs
     */
    List<MailFilterResult> applyMailFilterScript(String fullName, String mailFilterScript, SearchTerm<?> searchTerm, boolean acceptOkFilterResults) throws OXException;

}
