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

package com.openexchange.publish.online.infostore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationDataLoaderService;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link InfostorePublicationServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostorePublicationServlet extends HttpServlet {

    private static final Log LOG = LogFactory.getLog(InfostorePublicationServlet.class);

    private static final String SELF_DESTRUCT = "selfDestruct";

    private static final String DESTROY_DOCUMENT = "destroyDocument";
    
    private static PublicationDataLoaderService loader = null;
    private static InfostoreDocumentPublicationService publisher = null;
    private static ContextService contexts = null;
    private static UserService users = null;
    private static UserConfigurationService userConfigs = null;
    
    private static InfostoreFacade infostore = null;
    
    public static void setUserService(UserService service) {
        users = service;
    }
    
    public static void setUserConfigService(UserConfigurationService service) {
        userConfigs = service;
    }
    
    public static void setInfostoreFacade(InfostoreFacade service) {
        infostore = service;
    }
    
    public static void setPublicationDataLoaderService(PublicationDataLoaderService service) {
        loader = service;
    }
    
    public static void setInfostoreDocumentPublicationService(InfostoreDocumentPublicationService service) {
        publisher = service;
    }
    
    public static void setContextService(ContextService service) {
        contexts  = service;
    }
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String[] path = req.getPathInfo().split("/");
            Context ctx = getContext(path);
            String secret = getSecret(path);
            Publication publication = getPublication(secret, ctx);
            if(publication == null) {
                resp.getWriter().println("Not Found");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            DocumentMetadata document = loadDocumentMetadata(publication);
            InputStream is = loadContent(publication);
            configureHeaders(document, req, resp);
            write(is, resp);
            if(mustSelfDestruct(publication)) {
                destroy(publication);
                if(mustDestroyDocument(publication) && ! hasMorePublications(publication.getContext(), document)) {
                    ServerSessionAdapter session = new ServerSessionAdapter(new PublicationSession(publication), publication.getContext());
                    destroy(session, document);
                }
            }
            
        } catch (Exception x) {
            resp.getWriter().print(x.toString());
            LOG.error(x.getMessage(), x);
        }
    }

    private void destroy(ServerSession session, DocumentMetadata document) throws OXException {
        infostore.removeDocument(new int[]{document.getId()}, Long.MAX_VALUE, session);
    }

    private boolean hasMorePublications(Context ctx, DocumentMetadata document) throws PublicationException {
        return !publisher.getAllPublications(ctx, String.valueOf(document.getId())).isEmpty();
    }

    private boolean mustDestroyDocument(Publication publication) {
        return publication.getConfiguration().get(DESTROY_DOCUMENT) == Boolean.TRUE;
    }

    private void destroy(Publication publication) throws PublicationException {
        publisher.delete(publication);
    }

    private boolean mustSelfDestruct(Publication publication) {
        return publication.getConfiguration().get(SELF_DESTRUCT) == Boolean.TRUE;
    }
    
    private final boolean isIE(final HttpServletRequest req) {
        return req.getHeader("User-Agent").contains("MSIE");
    }


    private void configureHeaders(DocumentMetadata document, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
        boolean ie = isIE(req);
        resp.setHeader("Content-Disposition", "attachment; filename=\""
             + Helper.encodeFilename(document.getFileName(), "UTF-8", ie) + "\"");
    }

    private DocumentMetadata loadDocumentMetadata(Publication publication) throws Exception {
        
        int id = Integer.valueOf(publication.getEntityId());
        int version = InfostoreFacade.CURRENT_VERSION;
        Context ctx = publication.getContext();
        User user = loadUser(publication);
        UserConfiguration userConfig = loadUserConfig(publication);
        
        DocumentMetadata document = infostore.getDocumentMetadata(id, version, ctx, user, userConfig);
        return document;
    }

    private UserConfiguration loadUserConfig(Publication publication) throws UserConfigurationException {
        return userConfigs.getUserConfiguration(publication.getUserId(), publication.getContext());
    }

    private User loadUser(Publication publication) throws UserException {
        return users.getUser(publication.getUserId(), publication.getContext());
    }

    private void write(InputStream is, HttpServletResponse resp) throws IOException {
        BufferedInputStream bis = null;
        OutputStream output = null;
        try {
            bis = new BufferedInputStream(is);
            output = new BufferedOutputStream(resp.getOutputStream());
            int i;
            while((i = bis.read()) != -1) {
                output.write(i);
            }
            output.flush();
        } finally {
            if(bis != null) {
                bis.close();
            }
        }
    }

    private InputStream loadContent(Publication publication) throws PublicationException {
        Collection<? extends Object> load = loader.load(publication);
        if(load == null || load.isEmpty()) {
            return new ByteArrayInputStream(new byte[0]);
        }
        return (InputStream) load.iterator().next();
    }

    private Publication getPublication(String secret, Context ctx) throws PublicationException {
        return publisher.getPublication(ctx, secret);
    }

    private String getSecret(String[] path) {
        return path[path.length-1];
    }

    private Context getContext(String[] path) throws ContextException {
        int cid = -1;
        for(int i = 0; i < path.length; i++) {
            if(path[i].equals("documents") && path.length > i+1) {
                try {
                    cid = Integer.parseInt(path[i+1]);
                    break;
                } catch (NumberFormatException x) {
                }
            }
        }
        if(cid == -1) {
            throw new IllegalArgumentException("URL did not contain context id");
        }
        return contexts.getContext(cid);
    }
    

}
