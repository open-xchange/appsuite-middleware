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
