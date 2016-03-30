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

import com.openexchange.exception.OXException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.database.impl.AbstractDocumentListAction;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;

/**
 * {@link AbstractDocumentListActionTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AbstractDocumentListActionTest extends AbstractInfostoreTest {

    protected int existingId = 0;

    protected int notExistingId = 0;

    private final List<DocumentMetadata> clean = new LinkedList<DocumentMetadata>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createDocument();
    }

    public void testExists() throws OXException {
        TestDocumentListAction action = getActionWithDocumentId(existingId);
        action.perform();
        // Hooray! Survived!
    }

    public void testDoesNotExist() {
        TestDocumentListAction action = getActionWithDocumentId(notExistingId);
        try {
            action.perform();
            fail("Should have aborted with document ids that don't exist");
        } catch (OXException x) {
            assertTrue("Message does not indicate non existence", x.getMessage().contains("not exist"));
        }
    }

    private TestDocumentListAction getActionWithDocumentId(int documentID) {
        DocumentMetadata document = new DocumentMetadataImpl();
        document.setId(documentID);

        TestDocumentListAction action = new TestDocumentListAction();
        action.setProvider(getProvider());
        action.setContext(getCtx());
        action.setDocuments(Arrays.asList(document));
        return action;
    }

    protected void createDocument() throws OXException, SQLException {
        DocumentMetadata document = new DocumentMetadataImpl();
        document.setTitle(getName());
        document.setFolderId(getFolderId());

        getInfostore().saveDocumentMetadata(document, InfostoreFacade.NEW, getSession());
        clean.add(document);
        existingId = document.getId();

        notExistingId = IDGenerator.getId(getCtx(), Types.INFOSTORE);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    static final class TestDocumentListAction extends AbstractDocumentListAction {

        TestDocumentListAction() {
            super(null);
        }

        @Override
        public void perform() throws OXException {
            assureExistence();
        }

        @Override
        protected Object[] getAdditionals(DocumentMetadata doc) {
            return null;
        }

        @Override
        protected void undoAction() {
            // Nothing to do.
        }
    }

}
