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

package com.openexchange.imap;

import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.AbstractSearchTermVisitor;
import com.openexchange.mail.search.FlagTerm;
import com.openexchange.mail.search.NOTTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;

/**
 * {@link ImapConfigSearchTermVisitor}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ImapConfigSearchTermVisitor extends AbstractSearchTermVisitor{

    boolean needsSourroundingAndTerm = true;

    @Override
    public void visit(ANDTerm term) {
        // visit subterm
        term.getFirstTerm().accept(this);
        term.getSecondTerm().accept(this);

    }

    @Override
    public void visit(FlagTerm term) {
        if ((Math.abs(term.getPattern().intValue()) & MailMessage.FLAG_DELETED) > 0) {
            needsSourroundingAndTerm = false;
        }
    }

    @Override
    public void visit(NOTTerm term) {
        term.getPattern().accept(this);
    }

    @Override
    public void visit(ORTerm term) {
        term.getFirstTerm().accept(this);
        term.getSecondTerm().accept(this);
    }

    /**
     * If necessary surrounds the given term with an AND TERM which also checks whether the deleted flag is set to false.
     *
     * @param currentTerm The term to check
     * @return The eventually adjusted {@link SearchTerm}
     */
    public SearchTerm<?> checkTerm(SearchTerm<?> currentTerm) {
        if (needsSourroundingAndTerm) {
            FlagTerm secondTerm = new FlagTerm(MailMessage.FLAG_DELETED, false);
            return new ANDTerm(currentTerm, secondTerm);
        }
        return currentTerm;
    }
}
