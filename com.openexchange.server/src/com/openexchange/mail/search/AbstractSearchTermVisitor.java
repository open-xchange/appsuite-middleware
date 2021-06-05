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
 * {@link AbstractSearchTermVisitor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractSearchTermVisitor implements SearchTermVisitor {

    /**
     * Initializes a new {@link AbstractSearchTermVisitor}.
     */
    protected AbstractSearchTermVisitor() {
        super();
    }

    @Override
    public void visit(FileNameTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(ANDTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(BccTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(BodyTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(BooleanTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(CcTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(FlagTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(UserFlagTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(FromTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(HeaderTerm term) {
        // Nothing to do
    }
    
    @Override
    public void visit(HeaderExistenceTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(NOTTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(ORTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(ReceivedDateTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(SentDateTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(SizeTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(SubjectTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(ToTerm term) {
        // Nothing to do
    }

    @Override
    public void visit(XMailboxTerm term) {
        // Nothing to do
    }

}
