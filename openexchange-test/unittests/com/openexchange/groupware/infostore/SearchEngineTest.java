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
package com.openexchange.groupware.infostore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.search.impl.SearchEngineImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.test.TestInit;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionFactory;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class SearchEngineTest extends TestCase {

    private SearchEngineImpl searchEngine;
    private Context ctx = null;
    private User user = null;
    private int folderId;
    private ServerSession session;
    private List<DocumentMetadata> clean;
    private DBProvider provider = null;
    private InfostoreFacade infostore = null;

    @Override
    public void setUp() throws Exception {
        clean = new ArrayList<DocumentMetadata>();
        TestInit.loadTestProperties();
        Init.startServer();
        final ContextStorage ctxstor = ContextStorage.getInstance();
        final int contextId = ctxstor.getContextId("defaultcontext");
        ctx = ctxstor.getContext(contextId);
        user = UserStorage.getInstance().getUser(UserStorage.getInstance().getUserId("thorben", ctx), ctx); //FIXME
        session = ServerSessionFactory.createServerSession(user.getId(), ctx, "blupp");
        folderId = _getPrivateInfostoreFolder(ctx,user,session);
        provider = new DBPoolProvider();
        searchEngine = new SearchEngineImpl(provider);
        infostore = new InfostoreFacadeImpl(provider);
    }

    public int _getPrivateInfostoreFolder(final Context context, final User usr, final ServerSession sess) throws OXException {
        final OXFolderAccess oxfa = new OXFolderAccess(context);
        return oxfa.getDefaultFolder(usr.getId(), FolderObject.INFOSTORE).getObjectID();
    }

    // Bug 10968

    public void testRequestedFieldOrderMayVary() {
        assertSurvivesOrder(new Metadata[]{Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL, Metadata.TITLE_LITERAL});
        assertSurvivesOrder(new Metadata[]{Metadata.FOLDER_ID_LITERAL, Metadata.ID_LITERAL, Metadata.TITLE_LITERAL});
        assertSurvivesOrder(new Metadata[]{Metadata.FOLDER_ID_LITERAL, Metadata.TITLE_LITERAL, Metadata.ID_LITERAL});
        assertSurvivesOrder(new Metadata[]{Metadata.FOLDER_ID_LITERAL, Metadata.TITLE_LITERAL});
    }

    // Bug 11569

    public void testSearchForPercent() throws OXException, OXException {
        final DocumentMetadata doc1 = createWithTitle("100%");
        createWithTitle("Hallo");

        List<Integer> folderIDs = Collections.singletonList(Integer.valueOf(folderId));
        SearchIterator<DocumentMetadata> iter = searchEngine.search(session, "%", folderIDs, Collections.<Integer>emptyList(), new Metadata[] { Metadata.ID_LITERAL, Metadata.TITLE_LITERAL }, Metadata.TITLE_LITERAL, InfostoreSearchEngine.ASC, 0, 10);

        List<DocumentMetadata> documents = SearchIterators.asList(iter);
        assertTrue(0 < documents.size());
        boolean found = false;
        for (DocumentMetadata document : documents) {
            assertTrue(null != document.getTitle() && document.getTitle().contains("%"));
            if (doc1.getTitle().equals(document.getTitle())) {
                found = true;
            }
        }
        assertTrue(found);
    }

    private DocumentMetadata createWithTitle(final String title) throws OXException {
        final DocumentMetadata metadata = new DocumentMetadataImpl() ;
        metadata.setTitle(title);
        metadata.setFolderId(folderId);
        try {
            infostore.startTransaction();
            infostore.saveDocumentMetadata(metadata, InfostoreFacade.NEW, session);
            infostore.commit();
            clean.add( metadata );
        } catch (final OXException x) {
            try {
                infostore.rollback();
            } catch (final OXException e) {
                e.printStackTrace();
            }
            throw x;
        } finally {
            try {
                infostore.finish();
            } catch (final OXException e) {
                e.printStackTrace();
            }
        }
        return metadata;

    }

    private void assertSurvivesOrder(final Metadata[] metadata) {
        List<Integer> folderIDs = Collections.<Integer>singletonList(Integer.valueOf(folderId));
        try {
           searchEngine.search(session, "*", folderIDs, Collections.<Integer>emptyList(), metadata, metadata[0], InfostoreSearchEngine.ASC, 0, 10);
        } catch (final Exception x) {
            fail(x.getMessage());
            x.printStackTrace();
        }
    }


}
