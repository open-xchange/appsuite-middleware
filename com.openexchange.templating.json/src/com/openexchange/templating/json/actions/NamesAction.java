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

package com.openexchange.templating.json.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.json.TemplatingAJAXRequest;

/**
 * {@link NamesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NamesAction extends AbstractTemplatingAction implements AJAXActionService {

    /**
     * Initializes a new {@link NamesAction}.
     *
     * @param services
     */
    public NamesAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final TemplatingAJAXRequest req) throws OXException, JSONException {
        boolean onlyBasic = false;
        boolean onlyUser = false;
        String[] filter = null;

        final String param = req.getParameter("only");
        if (null != param) {
            final Set<String> only = new HashSet<String>(Arrays.asList(param.split("\\s*,\\s*")));
            if (only.contains("basic")) {
                onlyBasic = true;
                only.remove("basic");
            } else if (only.contains("user")) {
                onlyUser = true;
                only.remove("user");
            }
            filter = only.toArray(new String[only.size()]);
        }

        final TemplateService templates = services.getService(TemplateService.class);

        if (onlyBasic) {
            return new AJAXRequestResult(ARRAY(templates.getBasicTemplateNames(filter)), "json");
        }

        if (onlyUser) {
            final List<String> basicTemplateNames = templates.getBasicTemplateNames(filter);
            final List<String> templateNames = templates.getTemplateNames(req.getSession(), filter);
            templateNames.removeAll(basicTemplateNames);
            return new AJAXRequestResult(ARRAY(templateNames), "json");
        }

        return new AJAXRequestResult(ARRAY(templates.getTemplateNames(req.getSession(), filter)), "json");
    }

    private static JSONArray ARRAY(final List<String> templateNames) {
        final JSONArray array = new JSONArray();
        for (final String name : templateNames) {
            array.put(name);
        }
        return array;
    }

}
