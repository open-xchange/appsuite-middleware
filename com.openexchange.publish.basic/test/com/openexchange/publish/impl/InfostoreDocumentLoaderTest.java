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

package com.openexchange.publish.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.publish.Publication;
import com.openexchange.publish.services.SimInfostoreFacade;


/**
 * {@link InfostoreDocumentLoaderTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class InfostoreDocumentLoaderTest extends TestCase {

    private SimInfostoreFacade infostoreFacade;
    private int cid;
    private int folder;
    private int id1;
    private byte[] bytes1;
    private Publication publication;
    private InfostoreDocumentLoader loader;

    @Override
    public void setUp() {
        infostoreFacade = new SimInfostoreFacade();

        cid = -1;
        folder = 12;
        id1 = 1337;

        bytes1 = new byte[]{1,2,3};

        infostoreFacade.simulateDocument(cid, folder, id1, "Document 1", bytes1);

        publication = new Publication();
        publication.setContext(new SimContext(cid));
        publication.setEntityId("1337");
        publication.setModule("infostore");

        loader = new InfostoreDocumentLoader(infostoreFacade);
    }

    public void testLoadDocument() throws OXException, IOException {
        Collection<? extends Object> loaded = loader.load(publication, null);
        assertNotNull("Loaded was null!", loaded);
        assertEquals("Expected one document", 1, loaded.size());
        InputStream is = (InputStream) loaded.iterator().next();

        assertEquals(1, is.read());
        assertEquals(2, is.read());
        assertEquals(3, is.read());
        assertEquals(-1, is.read());

    }

}
