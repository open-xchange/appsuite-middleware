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

package com.openexchange.mail.search;


/**
 * {@link SearchTermVisitor} - The search term visitor interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SearchTermVisitor {

    /**
     * Calls this visitor with specified AND term.
     *
     * @param term The AND term
     */
    public void visit(ANDTerm term);

    /**
     * Calls this visitor with specified Bcc term.
     *
     * @param term The Bcc term
     */
    public void visit(BccTerm term);

    /**
     * Calls this visitor with specified Bcc term.
     *
     * @param term The Bcc term
     */
    public void visit(BodyTerm term);

    /**
     * Calls this visitor with specified Bcc term.
     *
     * @param term The Bcc term
     */
    public void visit(BooleanTerm term);

    /**
     * Calls this visitor with specified Cc term.
     *
     * @param term The Cc term
     */
    public void visit(CcTerm term);

    /**
     * Calls this visitor with specified flag term.
     *
     * @param term The flag term
     */
    public void visit(FlagTerm term);

    /**
     * Calls this visitor with specified From term.
     *
     * @param term The From term
     */
    public void visit(FromTerm term);

    /**
     * Calls this visitor with specified header term.
     *
     * @param term The header term
     */
    public void visit(HeaderTerm term);

    /**
     * Calls this visitor with specified header-existence term.
     *
     * @param term The header-existence term
     */
    public void visit(HeaderExistenceTerm term);

    /**
     * Calls this visitor with specified NOT term.
     *
     * @param term The NOT term
     */
    public void visit(NOTTerm term);

    /**
     * Calls this visitor with specified OR term.
     *
     * @param term The OR term
     */
    public void visit(ORTerm term);

    /**
     * Calls this visitor with specified received date term.
     *
     * @param term The received date term
     */
    public void visit(ReceivedDateTerm term);

    /**
     * Calls this visitor with specified sent date term.
     *
     * @param term The sent date term
     */
    public void visit(SentDateTerm term);

    /**
     * Calls this visitor with specified size term.
     *
     * @param term The size term
     */
    public void visit(SizeTerm term);

    /**
     * Calls this visitor with specified Subject term.
     *
     * @param term The Subject term
     */
    public void visit(SubjectTerm term);

    /**
     * Calls this visitor with specified To term.
     *
     * @param term The To term
     */
    public void visit(ToTerm term);

    /**
     * Calls this visitor with specified user flag term.
     *
     * @param term The user flag term
     */
    public void visit(UserFlagTerm term);

    /**
     * Calls this visitor with specified file name term.
     *
     * @param term The file name term
     */
    public void visit(FileNameTerm term);

    /**
     * Calls this visitor with specified X-MAILBOX term.
     *
     * @param term The X-MAILBOX term
     */
    public void visit(XMailboxTerm term);
}
