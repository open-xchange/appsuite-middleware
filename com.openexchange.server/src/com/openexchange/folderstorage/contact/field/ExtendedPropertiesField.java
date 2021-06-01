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

package com.openexchange.folderstorage.contact.field;

import static com.openexchange.server.services.ServerServiceRegistry.getServize;
import com.openexchange.contact.common.DataHandlers;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ExtendedPropertiesField}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ExtendedPropertiesField extends FolderField {

    private static final long serialVersionUID = -855927750885741680L;

    /** The column identifier of the field as used in the HTTP API */
    private static final int COLUMN_ID = 3301;

    /** The column name of the field as used in the HTTP API */
    private static final String COLUMN_NAME = "com.openexchange.contacts.extendedProperties";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExtendedPropertiesField.class);
    private static final ExtendedPropertiesField INSTANCE = new ExtendedPropertiesField();

    /**
     * Gets the extended properties field instance.
     *
     * @return The instance
     */
    public static ExtendedPropertiesField getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ExtendedPropertiesField}.
     */
    private ExtendedPropertiesField() {
        super(COLUMN_ID, COLUMN_NAME, null);
    }

    @Override
    public FolderProperty parse(Object value) {
        try {
            DataHandler dataHandler = getServize(ConversionService.class, true).getDataHandler(DataHandlers.JSON2XPROPERTIES.getId());
            ConversionResult result = dataHandler.processData(new SimpleData<>(value), new DataArguments(), null);
            return new FolderProperty(getName(), result.getData());
        } catch (Exception e) {
            LOG.warn("Error parsing extended contacts properties from \"{}\": {}", value, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Object write(FolderProperty property, ServerSession session) {
        if (null == property) {
            return getDefaultValue();
        }
        try {
            DataHandler dataHandler = getServize(ConversionService.class, true).getDataHandler(DataHandlers.XPROPERTIES2JSON.getId());
            ConversionResult result = dataHandler.processData(new SimpleData<>(property.getValue()), new DataArguments(), session);
            return result.getData();
        } catch (Exception e) {
            LOG.warn("Error writing extended contacts properties \"{}\": {}", property.getValue(), e.getMessage(), e);
        }
        return getDefaultValue();
    }

}
