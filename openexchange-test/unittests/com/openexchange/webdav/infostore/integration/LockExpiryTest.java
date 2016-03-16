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

package com.openexchange.webdav.infostore.integration;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.AbstractInfostoreTest;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.DocumentMetadataResource;
import com.openexchange.groupware.infostore.webdav.FolderCollection;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.results.Delta;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;


/**
 * {@link LockExpiryTest}
 * http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=13238
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class LockExpiryTest extends AbstractInfostoreTest {

    protected WebdavPath testCollection = new WebdavPath("public_infostore","testCollection"+Math.random());

    private InfostoreWebdavFactory factory = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        factory = (InfostoreWebdavFactory) TestWebdavFactoryBuilder.buildFactory();
        factory.beginRequest();
    }

    @Override
    public void tearDown() throws OXException {
        resolveFolder(testCollection).delete();
        factory.endRequest(200);
    }

    public void testExpiredLocksOnInfoitemsHaveThemShowUpInUpdatesResponse() throws OXException, UnsupportedEncodingException, InterruptedException, OXException {
        FolderCollection folderCollection = resolveFolder(testCollection);
        folderCollection.create();

        InfostoreFacade infostoreFacade = factory.getDatabase();

        DocumentMetadataResource resource = createResource();
        final WebdavLock lock = new WebdavLock();

        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);

        lock.setTimeout(2000);

        resource.lock(lock);
        newRequest();

        Date lastModified = infostoreFacade.getDocumentMetadata(resource.getId(), InfostoreFacade.CURRENT_VERSION, factory.getSession()).getLastModified();


        Thread.sleep(2001);

        // The lock has expired by now, so an updates request on the testCollection folder must include the resource
        newRequest();

        Delta<DocumentMetadata> delta = infostoreFacade.getDelta(
            folderCollection.getId(), lastModified.getTime()+10, new Metadata[]{Metadata.ID_LITERAL}, true, factory.getSession());

        boolean found = false;

        SearchIterator<DocumentMetadata> modified = delta.getModified();
        while(modified.hasNext()) {
            DocumentMetadata document = modified.next();
            if(document.getId() == resource.getId()) {
                found = true;
            }
        }

        assertTrue("Did not find document with autoexpired lock in delta", found);
    }


    private FolderCollection resolveFolder(WebdavPath url) throws OXException {
        return (FolderCollection) factory.resolveCollection(url);
    }

    protected DocumentMetadataResource createResource() throws OXException, UnsupportedEncodingException {
        String name = "/testResource"+Math.random();
        WebdavResource resource = factory.resolveResource(testCollection + name);
        assertFalse(resource.exists());

        resource.create();
        resource.putBodyAndGuessLength(new ByteArrayInputStream("Hallo Welt".getBytes(com.openexchange.java.Charsets.UTF_8)));
        newRequest();
        resource = factory.resolveResource(testCollection + name);
        assertTrue(resource.exists());

        return (DocumentMetadataResource) resource;
    }

    private void newRequest() {
        factory.endRequest(200);
        factory.beginRequest();
    }

}
