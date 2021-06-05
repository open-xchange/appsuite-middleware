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

package com.openexchange.resource.json.resultconverter;

import java.util.Collection;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.results.CollectionDelta;
import com.openexchange.resource.Resource;
import com.openexchange.resource.json.ResourceWriter;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ResourceJsonResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ResourceJsonResultConverter implements ResultConverter {

    /**
     * Initializes a new {@link ResourceJsonResultConverter}.
     */
    public ResourceJsonResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "resource";
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
            convert0(requestData, result, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private void convert0(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session) throws OXException, JSONException {
        Object resultObject = result.getResultObject();
        if (resultObject instanceof Resource) {
            result.setResultObject(ResourceWriter.writeResource(Anonymizers.anonymizeIfGuest((Resource) resultObject, Module.RESOURCE, session)), "json");
        } else if (resultObject instanceof CollectionDelta) {
            CollectionDelta<Resource> collectionDelta = (CollectionDelta<Resource>) resultObject;

            List<Resource> resources = collectionDelta.getNewAndModified();
            JSONArray jResources = new JSONArray(resources.size());

            if (Anonymizers.isGuest(session)) {
                AnonymizerService<Resource> anonymizer = Anonymizers.optAnonymizerFor(Module.RESOURCE);
                for (Resource resource : resources) {
                    resource = anonymizer.anonymize(resource, session);

                    jResources.put(ResourceWriter.writeResource(resource));
                }
            } else {
                for (Resource resource : resources) {
                    jResources.put(ResourceWriter.writeResource(resource));
                }
            }

            List<Resource> deleted = collectionDelta.getDeleted();
            JSONArray jDeletedResources;

            if (null == deleted) {
                jDeletedResources = new JSONArray(0);
            } else {
                jDeletedResources = new JSONArray(deleted.size());

                if (Anonymizers.isGuest(session)) {
                    AnonymizerService<Resource> anonymizer = Anonymizers.optAnonymizerFor(Module.RESOURCE);
                    for (Resource resource : deleted) {
                        resource = anonymizer.anonymize(resource, session);
                        jDeletedResources.put(ResourceWriter.writeResource(resource));
                    }
                } else {
                    for (Resource resource : deleted) {
                        jDeletedResources.put(ResourceWriter.writeResource(resource));
                    }
                }
            }

            JSONObject jResult = new JSONObject(3);
            jResult.put("new", jResources).put("modified", jResources);
            jResult.put("deleted", jDeletedResources);

            result.setResultObject(jResult, "json");
        } else if (AJAXServlet.ACTION_ALL.equalsIgnoreCase(requestData.getAction())) {
            Collection<Resource> resources = (Collection<Resource>) resultObject;

            JSONArray jIdentifiers = new JSONArray(resources.size());
            for (Resource resource : resources) {
                jIdentifiers.put(resource.getIdentifier());
            }

            result.setResultObject(jIdentifiers, "json");
        } else {
            Collection<Resource> resources = (Collection<Resource>) resultObject;

            JSONArray jResources = new JSONArray(resources.size());
            if (Anonymizers.isGuest(session)) {
                AnonymizerService<Resource> anonymizer = Anonymizers.optAnonymizerFor(Module.RESOURCE);
                for (Resource resource : resources) {
                    resource = anonymizer.anonymize(resource, session);
                    jResources.put(ResourceWriter.writeResource(resource));
                }
            } else {
                for (Resource resource : resources) {
                    jResources.put(ResourceWriter.writeResource(resource));
                }
            }

            result.setResultObject(jResources, "json");
        }

    }

}
