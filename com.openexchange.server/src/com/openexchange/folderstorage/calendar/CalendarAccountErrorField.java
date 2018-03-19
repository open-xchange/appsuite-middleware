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

package com.openexchange.folderstorage.calendar;

import org.json.JSONObject;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CalendarAccountErrorField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAccountErrorField extends FolderField {


    /** The column identifier of the field as used in the HTTP API */
    private static final int COLUMN_ID = 3204;

    /** The column name of the field as used in the HTTP API */
    private static final String COLUMN_NAME = "com.openexchange.calendar.accountError";

    private static final long serialVersionUID = 2379727517007236596L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarAccountErrorField.class);
    private static final CalendarAccountErrorField INSTANCE = new CalendarAccountErrorField();

    /**
     * Gets the calendar account error field instance.
     *
     * @return The instance
     */
    public static CalendarAccountErrorField getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link CalendarAccountErrorField}.
     */
    private CalendarAccountErrorField() {
        super(COLUMN_ID, COLUMN_NAME, null);
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
    public Object write(FolderProperty property) {
        if (null != property) {
            try {
                DataHandler dataHandler = ServerServiceRegistry.getServize(ConversionService.class).getDataHandler(DataHandlers.OXEXCEPTION2JSON);
                ConversionResult result = dataHandler.processData(new SimpleData<Object>(property.getValue()), new DataArguments(), null);
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
