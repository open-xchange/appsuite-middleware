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

package com.openexchange.subscribe.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.SubscriptionSession;
import com.openexchange.subscribe.helpers.DocumentMetadataHolder;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link DocumentMetadataHolderFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DocumentMetadataHolderFolderUpdaterStrategy implements FolderUpdaterStrategy<DocumentMetadataHolder> {

    private static final Log LOG = LogFactory.getLog(DocumentMetadataHolderFolderUpdaterStrategy.class);
    
    private UserService users;
    private UserConfigurationService userConfigs;
    private InfostoreFacade infostore;
    
    
    
    public DocumentMetadataHolderFolderUpdaterStrategy(UserService users, UserConfigurationService userConfigs, InfostoreFacade infostore) {
        super();
        this.users = users;
        this.userConfigs = userConfigs;
        this.infostore = infostore;
    }

    public int calculateSimilarityScore(DocumentMetadataHolder original, DocumentMetadataHolder candidate, Object session) throws AbstractOXException {
        int score = 0;
        DocumentMetadata dm1 = original.documentMetadata;
        DocumentMetadata dm2 = candidate.documentMetadata;
    
        if(dm1.getTitle().equals(dm2.getTitle())) {
            score += 3;
        }
        
        if(dm1.getFileName().equals(dm2.getFileName())) {
            score += 3;
        }
        
        return score;
    }

    public void closeSession(Object session) throws AbstractOXException {

    }

    public Collection<DocumentMetadataHolder> getData(Subscription subscription, Object session) throws AbstractOXException {
        List<DocumentMetadataHolder> list = new ArrayList<DocumentMetadataHolder>();
        InfostoreSession sess = (InfostoreSession) session;
        
        SearchIterator documents = infostore.getDocuments(subscription.getFolderIdAsInt(), subscription.getContext(), sess.user, sess.userConfig).results();
        try {
            while(documents.hasNext()) {
                list.add(new DocumentMetadataHolder(null, (DocumentMetadata) documents.next()));
            }
        } finally {
            documents.close();
        }
        
        return list;
    }

    public int getThreshhold(Object session) throws AbstractOXException {
        return 2;
    }

    public boolean handles(FolderObject folder) {
        return folder.getModule() == FolderObject.INFOSTORE;
    }

    public void save(DocumentMetadataHolder newElement, Object session) throws AbstractOXException {
        InfostoreSession sess = (InfostoreSession) session;
        InputStream file = grabFile(newElement);
        newElement.documentMetadata.setId(InfostoreFacade.NEW);
        newElement.documentMetadata.setFolderId(sess.folderId);
        newElement.documentMetadata.setVersion(InfostoreFacade.NEW);

        if(file == null) {
            infostore.saveDocumentMetadata(newElement.documentMetadata, InfostoreFacade.NEW, sess.serverSession);
        } else {
            try {
                infostore.saveDocument(newElement.documentMetadata, file, InfostoreFacade.NEW, sess.serverSession);
            } finally {
                try {
                    file.close();
                } catch (IOException e) {
                    LOG.debug(e.getMessage(), e);
                }
            }
        }
    }

    private InputStream grabFile(DocumentMetadataHolder newElement) {
        if(newElement.dataLink == null) {
            return null;
        }
        try {
            URL url = new URL(newElement.dataLink);
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            return new BufferedInputStream(urlCon.getInputStream());
        } catch (MalformedURLException e) {
            LOG.debug(e.getMessage(), e);
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
        }
        return null;
    }

    public Object startSession(Subscription subscription) throws AbstractOXException {
        return new InfostoreSession(subscription);
    }

    public void update(DocumentMetadataHolder original, DocumentMetadataHolder update, Object session) throws AbstractOXException {
        if(null != update.documentMetadata.getLastModified() && original.documentMetadata.getLastModified().after(update.documentMetadata.getLastModified())) {
            return;
        }
        InfostoreSession sess = (InfostoreSession) session;
        InputStream file = grabFile(update);

        update.documentMetadata.setId(original.documentMetadata.getId());
        update.documentMetadata.setFolderId(sess.folderId);
        update.documentMetadata.setVersion(InfostoreFacade.NEW);
        
        if(file == null) {
            infostore.saveDocumentMetadata(update.documentMetadata, original.documentMetadata.getSequenceNumber(), sess.serverSession);
        } else {
            try {
                infostore.saveDocument(update.documentMetadata, file, original.documentMetadata.getSequenceNumber(), sess.serverSession);
            } finally {
                try {
                    file.close();
                } catch (IOException e) {
                    LOG.debug(e.getMessage(), e);
                }
            }
        }
        
    }
    
    private class InfostoreSession {
        public int folderId;
        public User user;
        public UserConfiguration userConfig;
        public ServerSession serverSession;
        
        public InfostoreSession(Subscription subscription) throws UserConfigurationException, UserException, SubscriptionException {
            user = users.getUser(subscription.getUserId(), subscription.getContext());
            userConfig = userConfigs.getUserConfiguration(subscription.getUserId(), subscription.getContext());
       
            serverSession = new ServerSessionAdapter(new SubscriptionSession(subscription), subscription.getContext(), user);
            folderId = subscription.getFolderIdAsInt();
        }
    }

}
