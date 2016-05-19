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
     * Calls this visitor with specified attachment term.
     *
     * @param term The attachment term
     */
    public void visit(AttachmentTerm term);

}
