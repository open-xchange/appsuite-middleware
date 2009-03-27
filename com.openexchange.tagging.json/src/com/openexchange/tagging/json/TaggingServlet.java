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

package com.openexchange.tagging.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.session.Session;
import com.openexchange.tagging.Tagged;
import com.openexchange.tagging.TaggingService;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link TaggingServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class TaggingServlet extends PermissionServlet {
    
    private static final int POST = 0;

    private static final int GET = 1;

    private static final int PUT = 3;
    
    private static final Log LOG = LogFactory.getLog(TaggingServlet.class);
    
    private static final String GET_TAGS = "getTags";
    private static final String SAVE_TAGS = "saveTags";
    
    
    
    private static TaggingService taggingService = null;
    
    public static void setTaggingService(TaggingService service) {
        taggingService = service;
    }
    
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, POST);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, GET);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, PUT);
    }

    private void perform(final HttpServletRequest req, final HttpServletResponse resp, final int method) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            final Response response = doAction(req, method);
            ResponseWriter.write(response, resp.getWriter());
        } catch (final AbstractOXException e) {
            LOG.error("perform", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("perform", e);
        }
    }

    private Response doAction(final HttpServletRequest req, final int method) throws AbstractOXException, JSONException, IOException {
        switch (method) {
        case PUT:
        case POST:
            return writeAction(req);
        default:
            return readAction(req);
        }
    }

    private Response readAction(final HttpServletRequest req) throws JSONException {
        final Response response = new Response();

        final String action = req.getParameter("action");

        final Session session = getSessionObject(req);

        if (action.equals(GET_TAGS)) {
            int id = Integer.valueOf(req.getParameter("id"));
            int folderId = Integer.valueOf(req.getParameter("folder"));
            String tags = getTags(id, folderId, session);
            JSONObject content = new JSONObject();
            content.put("tags", tags);
            response.setData(content);
        } 

        return response;
    }
    
    private Response writeAction(final HttpServletRequest req) throws JSONException, IOException {
        final Response response = new Response();
        final Session session = getSessionObject(req);

        final String action = req.getParameter("action");
        if (action.equals(SAVE_TAGS)) {
            final JSONObject tagSet = new JSONObject(getBody(req));
            final List<Tagged> tagged = getTagged(tagSet, session);
            save(tagged);
        } 

        response.setData(1);
        return response;
    }
    
    private String getTags(int id, int folderId, Session session) {
        Collection<String> tags = taggingService.getTags(session.getContextId(), id, folderId);
        if(tags.isEmpty()) {
            return "";
        }
        StringBuilder list = new StringBuilder();
        for(String tag : tags) {
            list.append(tag).append(", ");
        }
        list.setLength(list.length()-2);
        return list.toString();
    }
    
    private List<Tagged> getTagged(JSONObject tagSet, Session session) throws JSONException {
        String[] tags = tagSet.getString("tags").split("\\s*,\\s*");
        int objectId = tagSet.getInt("id");
        int folderId = tagSet.getInt("folder");
        int contextId = session.getContextId();
        
        List<Tagged> tagged = new ArrayList<Tagged>(tags.length);
        for(String tag : tags) {
            tagged.add( new Tagged(contextId, objectId, folderId, tag) );
        }
        
        return tagged;
    }
    
    private void save(List<Tagged> tagged) {
        if(tagged.isEmpty()) {
            return;
        }
        Tagged first = tagged.get(0);
        removeAllTags(first.getContextId(), first.getObjectId(), first.getFolderId());
        
        for (Tagged t : tagged) {
            taggingService.tagObject(t);
        }
    }

    private void removeAllTags(int contextId, int objectId, int folderId) {
        Collection<String> tags = taggingService.getTags(contextId, objectId, folderId);
        for(String tag : tags) {
            taggingService.removeTag(new Tagged(contextId, objectId, folderId, tag));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.ajax.PermissionServlet#hasModulePermission(com.openexchange.tools.session.ServerSession)
     */
    @Override
    protected boolean hasModulePermission(ServerSession session) {
        // TODO Auto-generated method stub
        return false;
    }

}
