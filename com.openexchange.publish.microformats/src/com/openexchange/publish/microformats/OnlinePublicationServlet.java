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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.context.ContextService;
import com.openexchange.publish.Publication;


/**
 * {@link OnlinePublicationServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class OnlinePublicationServlet extends HttpServlet {
    
    protected static final String SECRET = "secret";

    protected static final String PROTECTED = "protected";

    
    protected static ContextService contexts = null;

    public static void setContextService(ContextService service) {
        contexts = service;
    }

    protected boolean checkProtected(Publication publication, Map<String, String> args, HttpServletResponse resp) throws IOException {
        Map<String, Object> configuration = publication.getConfiguration();
        if (configuration.containsKey(PROTECTED) && (Boolean) configuration.get("protected")) {
            String secret = (String) configuration.get(SECRET);
            if (!secret.equals(args.get(SECRET))) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().println("Don't know site this publication");
                return false;
            }
        }
        return true;
    }
    
    // FIXME: Get Default Encoding from config service
    protected Collection<String> decode(List<String> subList, HttpServletRequest req) throws UnsupportedEncodingException {
        String encoding = req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding();
        List<String> decoded = new ArrayList<String>();
        for(String component : subList) {
            String decodedComponent = decode(component, encoding);
            decoded.add(decodedComponent);
        }
        return decoded;
    }
    
    // FIXME use server service for this
    private String decode(String string, String encoding) throws UnsupportedEncodingException {
        String[] chunks = string.split("\\+");
        StringBuilder decoded = new StringBuilder(string.length());
        boolean endsWithPlus = string.endsWith("+");
        for (int i = 0; i < chunks.length; i++) {
            String chunk = chunks[i];
            decoded.append(URLDecoder.decode(chunk, encoding));
            if(i != chunks.length - 1 || endsWithPlus) {
                decoded.append('+');
            }
        }
        
        return decoded.toString();
    }
}
