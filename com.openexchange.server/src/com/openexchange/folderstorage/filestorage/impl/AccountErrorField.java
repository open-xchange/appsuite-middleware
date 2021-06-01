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

package com.openexchange.folderstorage.filestorage.impl;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AccountErrorField} - represents an account error occurred while loading a folder
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class AccountErrorField extends FolderField {

    private static final long serialVersionUID = 5499967205010287547L;
    private static final Logger LOG = LoggerFactory.getLogger(AccountErrorField.class);
    private static final int COLUMN_ID = 3031;
    private static final String COLUMN_NAME = "com.openexchange.folderstorage.accountError";
    private static final AccountErrorField INSTANCE = new AccountErrorField();

    /**
     * Initializes a new {@link AccountErrorField}.
     */
    private AccountErrorField() {
        super(COLUMN_ID, COLUMN_NAME, null);
    }

    /**
     * Gets the singleton instance of the {@link AccountErrorField}
     *
     * @return The singleton instance of the {@link AccountErrorField}
     */
    public static AccountErrorField getInstance() {
        return INSTANCE;
    }

    @Override
    public FolderProperty parse(Object value) {
        try {
            DataHandler dataHandler = ServerServiceRegistry.getServize(ConversionService.class).getDataHandler(DataHandlers.JSON2OXEXCEPTION);
            ConversionResult result = dataHandler.processData(new SimpleData<Object>(value), new DataArguments(), null);
            return new FolderProperty(getName(), result.getData());
        } catch (Exception e) {
            LOG.warn("Error parsing ox exception from \"{}\": {}", value, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Object write(FolderProperty property, ServerSession session) {
        if (null != property) {
            try {
                DataHandler dataHandler = ServerServiceRegistry.getServize(ConversionService.class).getDataHandler(DataHandlers.OXEXCEPTION2JSON);
                ConversionResult result = dataHandler.processData(new SimpleData<Object>(property.getValue()), new DataArguments(), session);
                Object data = result.getData();
                if (null != data && JSONObject.class.isInstance(data)) {
                    ((JSONObject) data).remove("error_stack");
                }
                return data;
            } catch (Exception e) {
                LOG.warn("Error writing ox exception \"{}\": {}", property.getValue(), e.getMessage(), e);
            }
        }
        return getDefaultValue();
    }
}
