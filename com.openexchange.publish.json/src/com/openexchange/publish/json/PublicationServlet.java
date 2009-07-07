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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.MultipleAdapterServlet;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.publish.json.types.EntityMap;
import com.openexchange.tools.QueryStringPositionComparator;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.session.ServerSession;

import static com.openexchange.publish.json.PublicationJSONErrorMessage.*;

/**
 * {@link PublicationServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationServlet extends MultipleAdapterServlet{


    private static PublicationMultipleHandlerFactory multipleFactory;
    public static void setFactory(PublicationMultipleHandlerFactory factory) {
        multipleFactory = factory;
    }
    
    @Override
    protected MultipleHandler createMultipleHandler() {
        return multipleFactory.createMultipleHandler();
    }

    @Override
    protected boolean requiresBody(String action) {
        return PublicationMultipleHandler.ACTIONS_REQUIRING_BODY.contains(action);
    }
    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return session.getUserConfiguration().isPublication();
    }
    
    @Override
    protected JSONObject modify(HttpServletRequest req, String action, JSONObject request) throws JSONException {
        request.put("__query", req.getQueryString());
        return request;
    }

    
}
