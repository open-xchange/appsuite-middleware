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

package com.openexchange.publish.microformats;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
import com.openexchange.session.Session;


/**
 * Unit tests for {@link InfostoreFileServlet}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.1
 */
@RunWith(PowerMockRunner.class)
public class InfostoreFileServletTest {

    /**
     * Instance to test
     */
    @InjectMocks
    private final InfostoreFileServlet infostoreFileServlet = null;

    /**
     * Mock of the {@link Publication}
     */
    @Mock
    private Publication publication;

    /**
     * {@link IDBasedFileAccessFactory} mock
     */
    @Mock
    private IDBasedFileAccessFactory idBasedFileAccessFactory;

    /**
     * {@link IDBasedFileAccess} mock
     */
    @Mock
    private IDBasedFileAccess idBasedFileAccess;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        InfostoreFileServlet.setFileFactory(this.idBasedFileAccessFactory);
        PowerMockito.when(this.idBasedFileAccessFactory.createAccess((Session) Matchers.any())).thenReturn(idBasedFileAccess);
    }

    @Test
    public void testLoadMetadata_PublicationNull_ReturnEmptyObject() throws OXException {
        OXException exception = null;
        try {
            this.infostoreFileServlet.loadMetadata(null, 1);
        } catch (OXException oe) {
            exception = oe;
        }

        Assert.assertNotNull("Exception expected.", exception);
        Assert.assertTrue("Wrong exception.", PublicationErrorMessage.NOT_FOUND_EXCEPTION.equals(exception));
    }

    @Test
    public void testLoadMetadata_Fine_ReturnMetadata() throws OXException {
        File file = PowerMockito.mock(File.class);
        PowerMockito.when(idBasedFileAccess.getFileMetadata(Matchers.anyString(), Matchers.anyString())).thenReturn(file);

        DocumentMetadata loadMetadata = this.infostoreFileServlet.loadMetadata(this.publication, 1);

        Assert.assertNotNull(loadMetadata);
    }

    @Test(expected = OXException.class)
    public void testLoadMetadata_getFileMetadataThrowsOXException_ReturnOXException() throws OXException {
        PowerMockito.when(idBasedFileAccess.getFileMetadata(Matchers.anyString(), Matchers.anyString())).thenThrow(
            InfostoreExceptionCodes.DELETE_FAILED.create());

        DocumentMetadata loadMetadata = this.infostoreFileServlet.loadMetadata(this.publication, 1);

        Assert.assertNotNull(loadMetadata);
    }

    @Test(expected = OXException.class)
    public void testLoadMetadata_getFileMetadataThrowsOXException_ReturnOXExceptionfd() throws OXException {
        PowerMockito.when(idBasedFileAccess.getFileMetadata(Matchers.anyString(), Matchers.anyString())).thenThrow(
            InfostoreExceptionCodes.NOT_EXIST.create());

        DocumentMetadata loadMetadata = this.infostoreFileServlet.loadMetadata(this.publication, 1);

        Assert.assertNotNull(loadMetadata);
    }
}
