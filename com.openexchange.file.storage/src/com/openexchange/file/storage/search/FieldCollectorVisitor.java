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

package com.openexchange.file.storage.search;

import java.util.EnumSet;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File.Field;

/**
 * {@link FieldCollectorVisitor} - A visitor that collects the fields indicated by passed search term.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class FieldCollectorVisitor implements SearchTermVisitor {

    private final EnumSet<Field> fields;

    /**
     * Initializes a new {@link FieldCollectorVisitor} that is initially empty.
     */
    public FieldCollectorVisitor() {
        super();
        fields = EnumSet.noneOf(Field.class);
    }

    /**
     * Initializes a new {@link FieldCollectorVisitor} that is initially filled with given field.
     *
     * @param field The field
     */
    public FieldCollectorVisitor(final Field field) {
        super();
        fields = EnumSet.of(field);
    }

    /**
     * Initializes a new {@link FieldCollectorVisitor} that is initially filled with given fields.
     *
     * @param first The first field
     * @param rest The other fields
     */
    public FieldCollectorVisitor(final Field first, final Field... rest) {
        super();
        fields = EnumSet.of(first, rest);
    }

    /**
     * Gets the collected fields.
     *
     * @return The fields
     */
    public EnumSet<Field> getFields() {
        return fields;
    }

    private void handleTerm(final SearchTerm<?> term) {
        if (null != term) {
            term.addField(fields);
        }
    }

    @Override
    public void visit(final AndTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final OrTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final NotTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final MetaTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final NumberOfVersionsTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final LastModifiedUtcTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final ColorLabelTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final CurrentVersionTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final VersionCommentTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final FileMd5SumTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final LockedUntilTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final CategoriesTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final SequenceNumberTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final FileMimeTypeTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final FileNameTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final LastModifiedTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final CreatedTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final ModifiedByTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final TitleTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final VersionTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final ContentTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final FileSizeTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final DescriptionTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final UrlTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(final CreatedByTerm term) throws OXException {
        handleTerm(term);
    }

}
