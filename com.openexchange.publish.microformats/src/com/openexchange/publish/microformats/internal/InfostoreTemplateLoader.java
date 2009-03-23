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

package com.openexchange.publish.microformats.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import com.openexchange.ajax.Infostore;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.publish.Site;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link InfostoreTemplateLoader}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreTemplateLoader {

    private static final InfostoreFacade INFOSTORE = Infostore.FACADE;

    public String loadTemplate(Site site) {
        SearchIterator iterator = null;
        try {
            Context ctx = Contexts.load(site.getContextId());
            User user = Users.load(ctx, site.getOwnerId());
            UserConfiguration userConfig = UserConfigurations.load(ctx, site.getOwnerId());

            OXFolderAccess acc = new OXFolderAccess(ctx);
            int folderId;
            folderId = acc.getDefaultFolder(site.getOwnerId(), FolderObject.INFOSTORE).getObjectID();
            TimedResult documents = INFOSTORE.getDocuments(folderId, ctx, user, userConfig);
            
            iterator = documents.results();
            
            while( iterator.hasNext() ) {
                DocumentMetadata document = (DocumentMetadata) iterator.next();
                
                String title = document.getTitle();
                if(title == null) {
                    title = "";
                }
                
                String fileName = document.getFileName();
                if(fileName == null){
                    fileName = "";
                }
                
                if(title.contains(site.getName()) || fileName.contains(site.getName())) {
                    return load(document, ctx, user, userConfig);
                }
            }
            
            
        } catch (OXException e) {
            e.printStackTrace();
        } catch (ContextException e) {
            e.printStackTrace();
        } catch (SearchIteratorException e) {
            e.printStackTrace();
        } finally {
            try {
                iterator.close();
            } catch (SearchIteratorException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String load(DocumentMetadata document, Context ctx, User user, UserConfiguration userConfig) {
        if(document.getVersion() != 0) {
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();
            try {
                InputStream stream = INFOSTORE.getDocument(document.getId(), document.getVersion(), ctx, user, userConfig);
                reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                String line = null;
                while((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }
                return builder.toString();
            } catch (OXException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return document.getDescription();
        
    }
}
