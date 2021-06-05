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

    @Override
    public void visit(MediaDateTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CaptureDateTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CameraIsoSpeedTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CameraApertureTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CameraExposureTimeTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CameraFocalLengthTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(WidthTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(HeightTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CameraModelTerm term) throws OXException {
        handleTerm(term);
    }

    @Override
    public void visit(CameraMakeTerm term) throws OXException {
        handleTerm(term);
    }

}
