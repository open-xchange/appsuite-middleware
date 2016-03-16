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

package com.openexchange.file.storage.json.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.java.Strings;

/**
 * {@link FileFieldCollector}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileFieldCollector implements ServiceTrackerCustomizer<AdditionalFileField, AdditionalFileField> {

    private final ConcurrentMap<Integer, AdditionalFileField> knownFields;
    private final BundleContext context;

    /**
     * Initializes a new {@link FileFieldCollector}.
     *
     * @param context the bundle context
     */
    public FileFieldCollector(BundleContext context) {
        super();
        this.context = context;
        knownFields = new ConcurrentHashMap<Integer, AdditionalFileField>();
    }

    /**
     * Gets all additionally registered file fields.
     *
     * @return The fields, or an empty list if there are none
     */
    public List<AdditionalFileField> getFields() {
        return new ArrayList<AdditionalFileField>(knownFields.values());
    }

    /**
     * Gets an additionally registered file field by its numerical column identifier.
     *
     * @param columnID the column identifier
     * @return The field, or <code>null</code> if not found
     */
    public AdditionalFileField getField(int columnID) {
        return knownFields.get(Integer.valueOf(columnID));
    }

    /**
     * Gets the additionally registered file fields by their numerical column identifiers, leaving out unknown column identifiers.
     *
     * @param columnIDs The column identifiers
     * @return The additionally registered file fields, with unknown columns missing in the result
     */
    public List<AdditionalFileField> getFields(int[] columnIDs) {
        List<AdditionalFileField> fields = new ArrayList<AdditionalFileField>();
        for (int columnID : columnIDs) {
            AdditionalFileField field = getField(columnID);
            if (null != field) {
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * Gets an additionally registered file field by its numerical column identifier or field name.
     *
     * @param columnNumberOrName A string representation of the column identifier, or the field name
     * @return The field, or <code>null</code> if not found
     */
    public AdditionalFileField getField(String columnNumberOrName) {
        if (Strings.isEmpty(columnNumberOrName)) {
            return null;
        }
        /*
         * try to interpret as numerical column identifier first
         */
        try {
            return knownFields.get(Integer.valueOf(columnNumberOrName));
        } catch (NumberFormatException e) {
            /*
             * try field names as fallback
             */
            for (AdditionalFileField field : getFields()) {
                if (columnNumberOrName.equals(field.getColumnName())) {
                    return field;
                }
            }
        }
        /*
         * not found
         */
        return null;
    }

    @Override
    public AdditionalFileField addingService(final ServiceReference<AdditionalFileField> reference) {
        AdditionalFileField field = context.getService(reference);
        Integer columnID = Integer.valueOf(field.getColumnID());
        AdditionalFileField existingField = knownFields.putIfAbsent(columnID, field);
        if (null != existingField) {
            org.slf4j.LoggerFactory.getLogger(FileFieldCollector.class).warn(
                "Collision in file fields. Field '{}' : {} has already been taken. Ignoring second service.", field.getColumnName(), field.getColumnID());

        }
        return field;
    }

    @Override
    public void modifiedService(ServiceReference<AdditionalFileField> reference, AdditionalFileField service) {
        // Nothing to do
    }

    @Override
    public void removedService(ServiceReference<AdditionalFileField> reference, AdditionalFileField service) {
        try {
            AdditionalFileField field = service;
            Integer columnID = Integer.valueOf(field.getColumnID());
            knownFields.remove(columnID);
        } finally {
            context.ungetService(reference);
        }
    }

}
