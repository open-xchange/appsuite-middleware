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

import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.infostore.InfostoreSearchIterator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.publish.Publication;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * Unit tests for {@link IDBasedFolderAccessFolderLoader}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
public class IDBasedFolderAccessFolderLoaderTest {

    /**
     * Class under test
     */
    @InjectMocks
    private IDBasedFolderAccessFolderLoader idBasedFolderAccessFolderLoader;

    /**
     * Mock
     */
    @Mock
    private IDBasedFolderAccessFactory idBasedFolderAccessFactory;

    /**
     * Mock
     */
    @Mock
    private IDBasedFileAccessFactory idBasedFileAccessFactory;

    /**
     * Mock
     */
    @Mock
    private Publication publication;

    /**
     * Mock
     */
    @Mock
    private IDBasedFolderAccess idBasedFolderAccess;

    /**
     * Mock
     */
    @Mock
    private IDBasedFileAccess idBasedFileAccess;

    /**
     * Mock
     */
    @Mock
    private FileStorageFolder fileStorageFolder;

    /**
     * Mock that will be returned in a collection
     */
    @Mock
    private TimedResult<File> timedResult;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoad_PublicationNull_ReturnEmptyCollection() throws OXException {
        Collection<? extends Object> load = this.idBasedFolderAccessFolderLoader.load(null, null);

        Assert.assertEquals(0, load.size());
    }

    @Test
    public void testLoad_NoDocumentFound_ReturnEmptyCollection() throws OXException {
        PowerMockito.when(this.idBasedFileAccessFactory.createAccess((Session) Matchers.any())).thenReturn(this.idBasedFileAccess);
        PowerMockito.when(this.idBasedFolderAccessFactory.createAccess((Session) Matchers.any())).thenReturn(this.idBasedFolderAccess);
        PowerMockito.when(this.idBasedFolderAccess.getFolder(Matchers.anyString())).thenReturn(this.fileStorageFolder);

        Collection<? extends Object> load = this.idBasedFolderAccessFolderLoader.load(this.publication, null);

        Assert.assertEquals(0, load.size());
    }

    @Test
    public void testLoad_SearchIteratorEmpty_ReturnEmptyCollection() throws OXException {
        PowerMockito.when(this.idBasedFileAccessFactory.createAccess((Session) Matchers.any())).thenReturn(this.idBasedFileAccess);
        PowerMockito.when(this.idBasedFolderAccessFactory.createAccess((Session) Matchers.any())).thenReturn(this.idBasedFolderAccess);
        PowerMockito.when(this.idBasedFolderAccess.getFolder(Matchers.anyString())).thenReturn(this.fileStorageFolder);
        PowerMockito.when(this.idBasedFileAccess.getDocuments(Matchers.anyString())).thenReturn(this.timedResult);

        SearchIterator<DocumentMetadata> iterator = PowerMockito.mock(SearchIterator.class);
        PowerMockito.when(iterator.hasNext()).thenReturn(Boolean.FALSE);
        SearchIterator<File> searchIterator = new InfostoreSearchIterator(iterator);
        PowerMockito.when(this.timedResult.results()).thenReturn(searchIterator);

        Collection<? extends Object> load = this.idBasedFolderAccessFolderLoader.load(this.publication, null);

        Assert.assertEquals(0, load.size());
    }

    @Test
    public void testLoad_FoundOne_ReturnOneFolder() throws OXException {
        PowerMockito.when(this.idBasedFileAccessFactory.createAccess((Session) Matchers.any())).thenReturn(this.idBasedFileAccess);
        PowerMockito.when(this.idBasedFolderAccessFactory.createAccess((Session) Matchers.any())).thenReturn(this.idBasedFolderAccess);
        PowerMockito.when(this.idBasedFolderAccess.getFolder(Matchers.anyString())).thenReturn(this.fileStorageFolder);
        PowerMockito.when(this.idBasedFileAccess.getDocuments(Matchers.anyString())).thenReturn(this.timedResult);

        SearchIterator<DocumentMetadata> iterator = PowerMockito.mock(SearchIterator.class);
        DocumentMetadata documentMetadata = PowerMockito.mock(DocumentMetadata.class);
        PowerMockito.when(iterator.next()).thenReturn(documentMetadata);
        SearchIterator<File> searchIterator = new InfostoreSearchIterator(iterator);
        PowerMockito.when(searchIterator.hasNext()).thenReturn(true, false);
        PowerMockito.when(this.timedResult.results()).thenReturn(searchIterator);

        Collection<? extends Object> load = this.idBasedFolderAccessFolderLoader.load(this.publication, null);

        Assert.assertEquals(1, load.size());
    }
}
