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

package com.openexchange.publish.online.infostore.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.publish.Publication;


/**
 * Unit tests for {@link InfostorePublicationUtils}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
public class PublicationInfostoreUtilsTest {

    /**
     * Mock of the {@link Publication}
     */
    @Mock
    private Publication publication;

    /**
     * Mock of the {@link IDBasedFileAccessFactory}
     */
    @Mock
    private IDBasedFileAccessFactory fileAccessFactory = null;

    /**
     * Mock of the {@link IDBasedFileAccess}
     */
    @Mock
    private IDBasedFileAccess fileAccess = null;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadDocumentMetadata_PublicationNull_ThrowException() throws OXException {
        InfostorePublicationUtils.loadDocumentMetadata(null, this.fileAccessFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadDocumentMetadata_FactoryNull_ThrowException() throws OXException {
        InfostorePublicationUtils.loadDocumentMetadata(this.publication, null);
    }

    @Test
    public void testLoadDocumentMetadata_NoFileAccess_ReturnNull() throws OXException {
        DocumentMetadata loadDocumentMetadata = InfostorePublicationUtils.loadDocumentMetadata(this.publication, this.fileAccessFactory);

        Assert.assertNull(loadDocumentMetadata);
    }

    @Test
    public void testLoadDocumentMetadata_NoFileAccess_ReturndfdNull() throws OXException {
        PowerMockito.when(this.fileAccessFactory.createAccess((com.openexchange.session.Session) Matchers.any())).thenReturn(
            this.fileAccess);

        DocumentMetadata loadDocumentMetadata = InfostorePublicationUtils.loadDocumentMetadata(this.publication, this.fileAccessFactory);

        Assert.assertNotNull(loadDocumentMetadata);
    }

}
