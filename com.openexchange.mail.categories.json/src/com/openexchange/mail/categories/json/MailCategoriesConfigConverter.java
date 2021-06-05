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

package com.openexchange.mail.categories.json;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MailCategoriesConfigConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class MailCategoriesConfigConverter implements ResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailCategoriesConfigConverter.class);
    
    private static final MailCategoriesConfigConverter INSTANCE = new MailCategoriesConfigConverter();
    
    public static MailCategoriesConfigConverter getInstance(){
        return INSTANCE;
    }
    
    @Override
    public String getInputFormat() {
        return "mailCategoriesConfig";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        try {
            convert2JSON(requestData, result, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
    

    /**
     * Converts to JSON output format.
     *
     * @param requestData The AJAX request data
     * @param result The AJAX result
     * @param session The associated session
     * @throws JSONException 
     */
    public void convert2JSON(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws JSONException {
        
        Object resultObject = result.getResultObject();
        if (null == resultObject) {
            LOG.warn("Result object is null.");
            result.setResultObject(JSONObject.NULL, "json");
            return;
        }
        
        if (resultObject instanceof List){ 
            @SuppressWarnings("unchecked")
            List<MailCategoryConfig> list = (List<MailCategoryConfig>) resultObject;
            
            if (list==null || list.isEmpty()){
                result.setResultObject(JSONObject.NULL, "json");
                return;
            }
            
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            jsonWriter.array();
            for(MailCategoryConfig config: list){
                jsonWriter.object();
                jsonWriter.key("category").value(config.getCategory());
                jsonWriter.key("flag").value(config.getFlag());
                jsonWriter.key("active").value(config.isActive());
                jsonWriter.key("name").value(config.getName());
                jsonWriter.endObject();
            }
            jsonWriter.endArray();
            result.setResultObject(jsonWriter.getObject(), "json");
            
        } else {
            
            MailCategoryConfig config = (MailCategoryConfig) resultObject;
            
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            jsonWriter.object();
            jsonWriter.key("category").value(config.getCategory());
            jsonWriter.key("flag").value(config.getFlag());
            jsonWriter.key("active").value(config.isActive());
            jsonWriter.key("name").value(config.getName());
            jsonWriter.endObject();
            result.setResultObject(jsonWriter.getObject(), "json");
        }
    }

}
