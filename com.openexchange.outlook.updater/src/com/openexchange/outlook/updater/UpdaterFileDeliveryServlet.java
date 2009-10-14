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

package com.openexchange.outlook.updater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.OXServlet;


/**
 * {@link UpdaterFileDeliveryServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class UpdaterFileDeliveryServlet extends OXServlet {

    private static final Log LOG = LogFactory.getLog(UpdaterFileDeliveryServlet.class);

    private static ResourceLoader loader;
    
    public static void setResourceLoader(ResourceLoader resourceLoader) {
        loader = resourceLoader;
    }
    
    private static String alias;
    
    public static void setAlias(String value) {
        alias = value;
    }
    
    @Override
    protected void decrementRequests() {
    
    }

    @Override
    protected void incrementRequests() {
    
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Session session = getSession(req);
        BufferedInputStream is = null;
        try {
            ServerSessionAdapter serverSession = new ServerSessionAdapter(session);
            boolean hasWebDAVXML = serverSession.getUserConfiguration().hasWebDAVXML();
            if(!hasWebDAVXML) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("text/plain");
                resp.getWriter().println("You do not have access to the Outlook Interface.");
                return;
            }
            
            resp.setContentType("application/octet-stream");
            Tools.removeCachingHeader(resp);
            
            ServletOutputStream outputStream = resp.getOutputStream();
            InputStream inputStream = loader.get(getFilePath(req));
            if(inputStream == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            is = new BufferedInputStream(inputStream);
            
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while((bytesRead = is.read(buffer))!=-1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Throwable t) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOG.error(t.getMessage(), t);
        } finally {
            if(is != null) {
                is.close();
            }
        }
    }
    
    private String getFilePath(HttpServletRequest req) {
        String path = req.getPathInfo().substring(alias.length()-1);
        return path;
    }

}
