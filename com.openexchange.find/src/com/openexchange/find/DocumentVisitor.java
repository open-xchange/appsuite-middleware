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

package com.openexchange.find;

import com.openexchange.find.calendar.CalendarDocument;
import com.openexchange.find.contacts.ContactsDocument;
import com.openexchange.find.drive.FileDocument;
import com.openexchange.find.drive.FolderDocument;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.find.tasks.TasksDocument;

/**
 * A {@link DocumentVisitor} has to be used to process the {@link Document}s of a {@link SearchResult}.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since 7.6.0
 */
public interface DocumentVisitor {

    /**
     * Handles given mail document.
     * 
     * @param mailDocument The mail document
     */
    void visit(MailDocument mailDocument);

    /**
     * Handles given file document.
     * 
     * @param fileDocument The file document
     */
    void visit(FileDocument fileDocument);

    /**
     * Handles given task document.
     *
     * @param TasksDocument The task document
     */
    void visit(TasksDocument taskDocument);

    /**
     * Handles given contact document.
     * 
     * @param contactDocument The file document
     */
    void visit(ContactsDocument contactDocument);

    /**
     * Handles given calendar document.
     * 
     * @param calendarDocument The calendar document
     */
    void visit(CalendarDocument calendarDocument);

    /**
     * Handles given folder document
     *
     * @param folderDocument The folder document
     */
    void visit(FolderDocument folderDocument);

}
