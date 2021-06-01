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

package com.openexchange.chronos.json.converter.handler;

import java.util.Set;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.json.converter.mapper.EventMapper;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link EventFieldDataHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class EventFieldDataHandler implements DataHandler {

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class[] { String[].class };
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        if (!(data.getData() instanceof String[])) {
            return null;
        }
        String[] fieldNames = (String[]) data.getData();
        if (fieldNames == null || fieldNames.length == 0) {
            return null;
        }
        Set<EventField> parseFields = EventMapper.getInstance().parseFields(fieldNames);
        ConversionResult conversionResult = new ConversionResult();
        conversionResult.setData(parseFields.toArray(new EventField[parseFields.size()]));
        return conversionResult;
    }

}
