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

package com.openexchange.subscribe.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.subscribe.helpers.DocumentMetadataHolder;
import com.openexchange.subscribe.helpers.HTTPToolkit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link DocumentMetadataHolderFolderUpdaterStrategy}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DocumentMetadataHolderFolderUpdaterStrategy implements FolderUpdaterStrategy<DocumentMetadataHolder> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocumentMetadataHolderFolderUpdaterStrategy.class);

    final UserService users;
    final UserPermissionService userPermissions;
    private final InfostoreFacade infostore;

    public DocumentMetadataHolderFolderUpdaterStrategy(final UserService users, final UserPermissionService userPermissions, final InfostoreFacade infostore) {
        super();
        this.users = users;
        this.userPermissions = userPermissions;
        this.infostore = infostore;
    }

    @Override
    public int calculateSimilarityScore(final DocumentMetadataHolder original, final DocumentMetadataHolder candidate, final Object session) throws OXException {
        int score = 0;
        final DocumentMetadata dm1 = original.documentMetadata;
        final DocumentMetadata dm2 = candidate.documentMetadata;
        if (isSame(dm1.getTitle(), dm2.getTitle())) {
            score += 3;
        }
        if (isSame(dm1.getFileName(), dm2.getFileName())) {
            score += 3;
        }
        return score;
    }

    private boolean isSame(final String s1, final String s2) {
        if (null == s1) {
            return null == s2;
        }
        return s1.equals(s2);
    }

    @Override
    public void closeSession(final Object session) throws OXException {
        // Nothing to do
    }

    @Override
    public Collection<DocumentMetadataHolder> getData(final TargetFolderDefinition target, final Object session) throws OXException {
        final List<DocumentMetadataHolder> list = new ArrayList<DocumentMetadataHolder>();
        final InfostoreSession sess = (InfostoreSession) session;

        final SearchIterator<DocumentMetadata> documents = infostore.getDocuments(target.getFolderIdAsInt(), ServerSessionAdapter.valueOf(sess.user.getId(), target.getContext().getContextId())).results();
        try {
            while (documents.hasNext()) {
                list.add(new DocumentMetadataHolder(null, documents.next()));
            }
        } finally {
            documents.close();
        }

        return list;
    }

    @Override
    public int getThreshold(final Object session) throws OXException {
        return 2;
    }

    @Override
    public boolean handles(final FolderObject folder) {
        return FolderObject.INFOSTORE == folder.getModule();
    }

    @Override
    public void save(final DocumentMetadataHolder newElement, final Object session, Collection<OXException> errors) throws OXException {
        final InfostoreSession sess = (InfostoreSession) session;
        final InputStream file = grabFile(newElement);
        newElement.documentMetadata.setId(InfostoreFacade.NEW);
        newElement.documentMetadata.setFolderId(sess.folderId);
        newElement.documentMetadata.setVersion(InfostoreFacade.NEW);

        if (file == null) {
            infostore.saveDocumentMetadata(newElement.documentMetadata, InfostoreFacade.NEW, sess.serverSession);
        } else {
            try {
                infostore.saveDocument(newElement.documentMetadata, file, InfostoreFacade.NEW, sess.serverSession);
            } finally {
                Streams.close(file);
            }
        }
    }

    private static InputStream grabFile(final DocumentMetadataHolder newElement) {
        String dataLink = newElement.dataLink;
        if (dataLink == null) {
            return null;
        }
        try {
            return HTTPToolkit.grabStream(dataLink, false);
        } catch (final IOException e) {
            LOG.debug("", e);
        }
        return null;
    }

    @Override
    public Object startSession(final TargetFolderDefinition target) throws OXException {
        return new InfostoreSession(target);
    }

    @Override
    public void update(final DocumentMetadataHolder original, final DocumentMetadataHolder update, final Object session) throws OXException {
        if (null != update.documentMetadata.getLastModified() && original.documentMetadata.getLastModified().after(
            update.documentMetadata.getLastModified())) {
            return;
        }
        final InfostoreSession sess = (InfostoreSession) session;
        final InputStream file = grabFile(update);

        update.documentMetadata.setId(original.documentMetadata.getId());
        update.documentMetadata.setFolderId(sess.folderId);
        update.documentMetadata.setVersion(InfostoreFacade.NEW);

        if (file == null) {
            infostore.saveDocumentMetadata(update.documentMetadata, original.documentMetadata.getSequenceNumber(), sess.serverSession);
        } else {
            try {
                infostore.saveDocument(update.documentMetadata, file, original.documentMetadata.getSequenceNumber(), sess.serverSession);
            } finally {
                try {
                    file.close();
                } catch (final IOException e) {
                    LOG.debug("", e);
                }
            }
        }

    }

    private class InfostoreSession {

        public int folderId;
        public User user;
        public UserPermissionBits permissionBits;
        public ServerSession serverSession;

        public InfostoreSession(final TargetFolderDefinition target) throws OXException, OXException, OXException {
            user = users.getUser(target.getUserId(), target.getContext());
            permissionBits = userPermissions.getUserPermissionBits(target.getUserId(), target.getContext());

            serverSession = new ServerSessionAdapter(new TargetFolderSession(target), target.getContext(), user);
            folderId = target.getFolderIdAsInt();

        }
    }

}
