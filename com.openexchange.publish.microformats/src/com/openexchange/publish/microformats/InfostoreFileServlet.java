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

package com.openexchange.publish.microformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.java.Strings;
import com.openexchange.publish.Publication;
import com.openexchange.publish.tools.PublicationSession;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link InfostoreFileServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreFileServlet extends OnlinePublicationServlet {
    
    private static final String CONTEXTID = "contextId";
    private static final String SITE = "site";
    private static final String INFOSTORE_ID = "infoId";
    private static final String INFOSTORE_VERSION = "infoVersion";

    private static final Log LOG = LogFactory.getLog(InfostoreFileServlet.class);
    
    private static OXMFPublicationService infostorePublisher = null;
    
    
    public static void setInfostorePublisher(OXMFPublicationService service) {
        infostorePublisher = service;
    }
    
    private static InfostoreFacade infostore;
    
    public static void setInfostore(InfostoreFacade service) {
        infostore = service;
    }
    
    private static UserService users  = null;
    
    public static void setUsers(UserService service) {
        users = service;
    }
    
    private static UserConfigurationService userConfigs = null;
    
    public static void setUserConfigs(UserConfigurationService service) {
        userConfigs = service;
    }
   
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> args = getPublicationArguments(req);
        try {
            Context ctx = contexts.getContext(Integer.parseInt(args.get(CONTEXTID)));
            Publication publication = infostorePublisher.getPublication(ctx, args.get(SITE));
            if (publication == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("Don't know site " + args.get(SITE));
                return;
            }
            if (!checkProtected(publication, args, resp)) {
                return;
            }
            
            int infoId = Integer.parseInt(args.get(INFOSTORE_ID));
     
            User user = getUser(publication);
            UserConfiguration userConfig = getUserConfiguration(publication);
            
            DocumentMetadata metadata = loadMetadata(publication, infoId, user, userConfig);
            InputStream fileData = loadFile(publication, infoId, user, userConfig);
            
            writeFile(metadata, fileData, req, resp);
            
        } catch (Throwable t) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            t.printStackTrace(resp.getWriter());
            LOG.error(t.getMessage(), t);
        }

    }

    private User getUser(Publication publication) throws UserException {
        return users.getUser(publication.getUserId(), publication.getContext());
    }

    private UserConfiguration getUserConfiguration(Publication publication) throws UserException, UserConfigurationException {
        return userConfigs.getUserConfiguration(publication.getUserId(), publication.getContext());
    }

    
    private DocumentMetadata loadMetadata(Publication publication, int infoId, User user, UserConfiguration userConfig) throws OXException {
        return infostore.getDocumentMetadata(infoId, InfostoreFacade.CURRENT_VERSION, publication.getContext(), user, userConfig);
    }

    private void writeFile(DocumentMetadata metadata, InputStream fileData, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        configureHeaders(metadata, req, resp);
        write(fileData, resp);
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
    
    private void configureHeaders(DocumentMetadata document, HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
        boolean ie = isIE(req);
        resp.setHeader("Content-Disposition", "attachment; filename=\""
             + Helper.encodeFilename(document.getFileName(), "UTF-8", ie) + "\"");
    }

    private final boolean isIE(final HttpServletRequest req) {
        return req.getHeader("User-Agent").contains("MSIE");
    }

    private InputStream loadFile(Publication publication, int infoId, User user, UserConfiguration userConfig) throws OXException {
        return infostore.getDocument(infoId, InfostoreFacade.CURRENT_VERSION, publication.getContext(), user, userConfig);
    }

    private Map<String, String> getPublicationArguments(HttpServletRequest req) throws UnsupportedEncodingException {
        // URL format is: /publications/files/[cid]/[siteName]/[infostoreID]/[version]?secret=[secret]
        
        String[] path = req.getPathInfo().split("/");
        List<String> normalized = new ArrayList<String>(path.length);
        for (int i = 0; i < path.length; i++) {
            if (!path[i].equals("")) {
                normalized.add(path[i]);
            }
        }

        int startIndex = normalized.indexOf("publications");
        if (startIndex == -1) {
            throw new IllegalArgumentException("This does not look like a valid path: " + req.getPathInfo());
        }
        String site = Strings.join(decode(normalized.subList(startIndex + 3, normalized.size()-2), req), "/");
        Map<String, String> args = new HashMap<String, String>();
        args.put(CONTEXTID, normalized.get(startIndex + 2));
        args.put(SITE, site);
        args.put(SECRET, req.getParameter(SECRET));
        args.put(INFOSTORE_ID, normalized.get(normalized.size()-2));
        return args;
    }
    
}
