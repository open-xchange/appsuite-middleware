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

package com.openexchange.resource.json.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.internal.ResourceServiceImpl;
import com.openexchange.resource.json.ResourceAJAXRequest;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchAction extends AbstractResourceAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SearchAction.class);

    /**
     * Initializes a new {@link SearchAction}.
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        ResourceService resourceService = ResourceServiceImpl.getInstance();
        if (null == resourceService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ResourceService.class.getName());
        }

        if (!req.getSession().getUserPermissionBits().hasGroupware()) {
            return new AJAXRequestResult(new JSONArray(0), "json");
        }

        // Appropriate permission is granted
        // Continue processing search request

        JSONObject jData = req.getData();
        if (!jData.hasAndNotNull(SearchFields.PATTERN)) {
            LOG.warn("Missing field \"{}\" in JSON data. Searching for all as fallback", SearchFields.PATTERN);
            return new AllAction(services).perform(req);
        }

        String searchpattern = jData.getString(SearchFields.PATTERN);
        com.openexchange.resource.Resource[] resources = resourceService.searchResources(searchpattern, req.getSession().getContext());
        Date timestamp;
        List<Resource> list = new LinkedList<Resource>();

        if (resources.length > 0) {
            long lastModified = Long.MIN_VALUE;
            for (final com.openexchange.resource.Resource resource : resources) {
                if (lastModified < resource.getLastModified().getTime()) {
                    lastModified = resource.getLastModified().getTime();
                }
                list.add(resource);
            }
            timestamp = new Date(lastModified);
        } else {
            timestamp = new Date(0);
        }

        return new AJAXRequestResult(list, timestamp, "resource");
    }

}
