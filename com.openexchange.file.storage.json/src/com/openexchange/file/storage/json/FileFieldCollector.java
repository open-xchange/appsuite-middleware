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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.json;

import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.customizer.file.AdditionalFileField;

/**
 * {@link FileFieldCollector} - A collector for registered instances of <code>AdditionalFileField</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface FileFieldCollector {

    /** The empty collector instance */
    public static final FileFieldCollector EMPTY = new FileFieldCollector() {

        @Override
        public List<AdditionalFileField> getFields(int[] columnIDs) {
            return Collections.emptyList();
        }

        @Override
        public List<AdditionalFileField> getFields() {
            return Collections.emptyList();
        }

        @Override
        public AdditionalFileField getField(String columnNumberOrName) {
            return null;
        }

        @Override
        public AdditionalFileField getField(int columnID) {
            return null;
        }
    };

    /**
     * Gets all additionally registered file fields.
     *
     * @return The fields, or an empty list if there are none
     */
    List<AdditionalFileField> getFields();

    /**
     * Gets an additionally registered file field by its numerical column identifier.
     *
     * @param columnID the column identifier
     * @return The field, or <code>null</code> if not found
     */
    AdditionalFileField getField(int columnID);

    /**
     * Gets the additionally registered file fields by their numerical column identifiers, leaving out unknown column identifiers.
     *
     * @param columnIDs The column identifiers
     * @return The additionally registered file fields, with unknown columns missing in the result
     */
    List<AdditionalFileField> getFields(int[] columnIDs);

    /**
     * Gets an additionally registered file field by its numerical column identifier or field name.
     *
     * @param columnNumberOrName A string representation of the column identifier, or the field name
     * @return The field, or <code>null</code> if not found
     */
    AdditionalFileField getField(String columnNumberOrName);

}
