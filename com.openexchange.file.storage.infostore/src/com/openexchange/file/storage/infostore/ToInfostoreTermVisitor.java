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

package com.openexchange.file.storage.infostore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.search.AndTerm;
import com.openexchange.file.storage.search.CategoriesTerm;
import com.openexchange.file.storage.search.ColorLabelTerm;
import com.openexchange.file.storage.search.ContentTerm;
import com.openexchange.file.storage.search.CreatedByTerm;
import com.openexchange.file.storage.search.CreatedTerm;
import com.openexchange.file.storage.search.CurrentVersionTerm;
import com.openexchange.file.storage.search.DescriptionTerm;
import com.openexchange.file.storage.search.FileMd5SumTerm;
import com.openexchange.file.storage.search.FileMimeTypeTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.FileSizeTerm;
import com.openexchange.file.storage.search.LastModifiedTerm;
import com.openexchange.file.storage.search.LastModifiedUtcTerm;
import com.openexchange.file.storage.search.LockedUntilTerm;
import com.openexchange.file.storage.search.MetaTerm;
import com.openexchange.file.storage.search.ModifiedByTerm;
import com.openexchange.file.storage.search.NotTerm;
import com.openexchange.file.storage.search.NumberOfVersionsTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.file.storage.search.SearchTermVisitor;
import com.openexchange.file.storage.search.SequenceNumberTerm;
import com.openexchange.file.storage.search.TitleTerm;
import com.openexchange.file.storage.search.UrlTerm;
import com.openexchange.file.storage.search.VersionCommentTerm;
import com.openexchange.file.storage.search.VersionTerm;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.search.ComparisonType;

/**
 * {@link ToInfostoreTermVisitor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ToInfostoreTermVisitor implements SearchTermVisitor {

    private com.openexchange.groupware.infostore.search.SearchTerm<?> infstoreTerm;

    /**
     * Initializes a new {@link ToInfostoreTermVisitor}.
     */
    public ToInfostoreTermVisitor() {
        super();
    }

    /**
     * Gets the infostore search term.
     *
     * @return The infostore search term
     */
    public com.openexchange.groupware.infostore.search.SearchTerm<?> getInfostoreTerm() {
        return infstoreTerm;
    }

    @Override
    public void visit(final AndTerm term) throws OXException {
        final List<SearchTerm<?>> terms = term.getPattern();
        final int size = terms.size();
        final List<com.openexchange.groupware.infostore.search.SearchTerm<?>> infostoreTerms = new ArrayList<com.openexchange.groupware.infostore.search.SearchTerm<?>>(size);
        for (int i = 0; i < size; i++) {
            final SearchTerm<?> searchTerm = terms.get(i);
            final ToInfostoreTermVisitor newVisitor = new ToInfostoreTermVisitor();
            searchTerm.visit(newVisitor);
            infostoreTerms.add(newVisitor.getInfostoreTerm());
        }
        infstoreTerm = new com.openexchange.groupware.infostore.search.AndTerm(infostoreTerms);
    }

    @Override
    public void visit(final OrTerm term) throws OXException {
        final List<SearchTerm<?>> terms = term.getPattern();
        final int size = terms.size();
        final List<com.openexchange.groupware.infostore.search.SearchTerm<?>> infostoreTerms = new ArrayList<com.openexchange.groupware.infostore.search.SearchTerm<?>>(size);
        for (int i = 0; i < size; i++) {
            final SearchTerm<?> searchTerm = terms.get(i);
            final ToInfostoreTermVisitor newVisitor = new ToInfostoreTermVisitor();
            searchTerm.visit(newVisitor);
            infostoreTerms.add(newVisitor.getInfostoreTerm());
        }
        infstoreTerm = new com.openexchange.groupware.infostore.search.OrTerm(infostoreTerms);
    }

    @Override
    public void visit(final NotTerm term) throws OXException {
        final ToInfostoreTermVisitor newVisitor = new ToInfostoreTermVisitor();
        term.getPattern().visit(newVisitor);
        infstoreTerm = new com.openexchange.groupware.infostore.search.NotTerm(newVisitor.getInfostoreTerm());
    }

    @Override
    public void visit(final MetaTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.MetaTerm(term.getPattern());
    }

    @Override
    public void visit(final NumberOfVersionsTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.NumberOfVersionsTerm(new ComparablePatternImpl<Number>(term.getPattern()));
    }

    @Override
    public void visit(final LastModifiedUtcTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.LastModifiedUtcTerm(new ComparablePatternImpl<Date>(term.getPattern()));
    }

    @Override
    public void visit(final ColorLabelTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.ColorLabelTerm(new ComparablePatternImpl<Number>(term.getPattern()));
    }

    @Override
    public void visit(final CurrentVersionTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.CurrentVersionTerm(term.getPattern().booleanValue());
    }

    @Override
    public void visit(final VersionCommentTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.VersionCommentTerm(term.getPattern(), term.isIgnoreCase());
    }

    @Override
    public void visit(final FileMd5SumTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.FileMd5SumTerm(term.getPattern());
    }

    @Override
    public void visit(final LockedUntilTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.LockedUntilTerm(new ComparablePatternImpl<Date>(term.getPattern()));
    }

    @Override
    public void visit(final CategoriesTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.CategoriesTerm(term.getPattern());
    }

    @Override
    public void visit(final SequenceNumberTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.SequenceNumberTerm(new ComparablePatternImpl<Number>(term.getPattern()));
    }

    @Override
    public void visit(final FileMimeTypeTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.FileMimeTypeTerm(term.getPattern());
    }

    @Override
    public void visit(final FileNameTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.FileNameTerm(term.getPattern(), term.isIgnoreCase(), term.isSubstringSearch());
    }

    @Override
    public void visit(final LastModifiedTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.LastModifiedTerm(new ComparablePatternImpl<Date>(term.getPattern()));
    }

    @Override
    public void visit(final CreatedTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.CreatedTerm(new ComparablePatternImpl<Date>(term.getPattern()));
    }

    @Override
    public void visit(final ModifiedByTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.ModifiedByTerm(new ComparablePatternImpl<Number>(term.getPattern()));
    }

    @Override
    public void visit(final TitleTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.TitleTerm(term.getPattern(), term.isIgnoreCase(), term.isSubstringSearch());
    }

    @Override
    public void visit(final VersionTerm term) throws OXException {
        String sVersion = term.getPattern();
        infstoreTerm = new com.openexchange.groupware.infostore.search.VersionTerm(FileStorageFileAccess.CURRENT_VERSION == sVersion ? InfostoreFacade.CURRENT_VERSION : Integer.parseInt(sVersion.trim()));
    }

    @Override
    public void visit(final ContentTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.ContentTerm(term.getPattern(), term.isIgnoreCase(), term.isSubstringSearch());
    }

    @Override
    public void visit(final FileSizeTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.FileSizeTerm(new ComparablePatternImpl<Number>(term.getPattern()));
    }

    @Override
    public void visit(final DescriptionTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.DescriptionTerm(term.getPattern(), term.isIgnoreCase(), term.isSubstringSearch());
    }

    @Override
    public void visit(final UrlTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.UrlTerm(term.getPattern(), term.isIgnoreCase(), term.isSubstringSearch());
    }

    @Override
    public void visit(final CreatedByTerm term) throws OXException {
        infstoreTerm = new com.openexchange.groupware.infostore.search.CreatedByTerm(new ComparablePatternImpl<Number>(term.getPattern()));
    }

    private static final class ComparablePatternImpl<T> implements com.openexchange.groupware.infostore.search.ComparablePattern<T> {

        private final com.openexchange.file.storage.search.ComparablePattern<T> cp;

        /**
         * Initializes a new {@link ToInfostoreTermVisitor.ComparablePatternImpl}.
         */
        ComparablePatternImpl(final com.openexchange.file.storage.search.ComparablePattern<T> cp) {
            super();
            this.cp = cp;
        }

        @Override
        public ComparisonType getComparisonType() {
            switch (cp.getComparisonType()) {
            case EQUALS:
                return com.openexchange.groupware.infostore.search.ComparisonType.EQUALS;
            case LESS_THAN:
                return com.openexchange.groupware.infostore.search.ComparisonType.LESS_THAN;
            case GREATER_THAN:
                return com.openexchange.groupware.infostore.search.ComparisonType.GREATER_THAN;
            default:
                return null;
            }
        }

        @Override
        public T getPattern() {
            return cp.getPattern();
        }

    }

}
