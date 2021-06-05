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

package com.openexchange.file.storage.json.osgi;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.file.storage.json.FileFieldCollector;
import com.openexchange.java.Strings;

/**
 * {@link OSGiFileFieldCollector}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OSGiFileFieldCollector implements ServiceTrackerCustomizer<AdditionalFileField, AdditionalFileField>, FileFieldCollector {

    private final ConcurrentMap<Integer, AdditionalFileField> knownFields;
    private final BundleContext context;

    /**
     * Initializes a new {@link OSGiFileFieldCollector}.
     *
     * @param context the bundle context
     */
    public OSGiFileFieldCollector(BundleContext context) {
        super();
        this.context = context;
        knownFields = new ConcurrentHashMap<Integer, AdditionalFileField>();
    }

    @Override
    public List<AdditionalFileField> getFields() {
        return new ArrayList<AdditionalFileField>(knownFields.values());
    }

    @Override
    public AdditionalFileField getField(int columnID) {
        return knownFields.get(Integer.valueOf(columnID));
    }

    @Override
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

    @Override
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

    // -------------------------------------------- Service Tracker Stuff --------------------------------------------------

    @Override
    public AdditionalFileField addingService(final ServiceReference<AdditionalFileField> reference) {
        AdditionalFileField field = context.getService(reference);
        Integer columnID = Integer.valueOf(field.getColumnID());
        AdditionalFileField existingField = knownFields.putIfAbsent(columnID, field);
        if (null != existingField) {
            org.slf4j.LoggerFactory.getLogger(OSGiFileFieldCollector.class).warn(
                "Collision in file fields. Field '{}' : {} has already been taken. Ignoring second service.", field.getColumnName(), I(field.getColumnID()));

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
