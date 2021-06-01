/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.dav;

import static com.openexchange.dav.AttachmentUtils.decodeURI;
import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.groupware.attach.AttachmentMetadata;

/**
 * {@link AttachmentUtilDecodeTest}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
@RunWith(Parameterized.class)
public class AttachmentUtilDecodeTest {

    @Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] { { "/servlet/dav/", "/servlet/dav/" }, { "/servlet/dav/hidden/", "/servlet/dav/" }, { "/dav/", "/" } });
    }

    private final String prefixPath;
    private final String proxyprefixPath;

    /**
     * Initializes a new {@link DAVToolsTest}.
     * 
     * @param prefixPath The prefix path
     * @param proxyPrefixPath The proxy prefix path
     */
    public AttachmentUtilDecodeTest(String prefixPath, String proxyPrefixPath) {
        super();
        this.prefixPath = prefixPath;
        this.proxyprefixPath = proxyPrefixPath;
    }

    @Mock
    private ConfigViewFactory factory;

    @Mock
    private ConfigView view;

    @Before
    public void setUp() throws Exception {
        /*
         * Initialize mocks
         */
        MockitoAnnotations.initMocks(this);

        // Mock used service classes
        PowerMockito.when(factory.getView()).thenReturn(view);
        PowerMockito.when(view.get(UnitTests.PREFIX_PATH_NAME, String.class)).thenReturn(prefixPath);
        PowerMockito.when(view.get(UnitTests.PROXY_PREFIX_PATH_NAME, String.class)).thenReturn(proxyprefixPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_noUri_fail() throws Exception {
        decodeURI(null, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_emptyUri_fail() throws Exception {
        URI uri = new URI("");
        decodeURI(uri, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_noAttachmentUri_fail() throws Exception {
        URI uri = new URI(prefixPath + "caldav/foo");
        decodeURI(uri, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_noMetadata_fail() throws Exception {
        URI uri = new URI(prefixPath + "attachments/");
        decodeURI(uri, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_wrongMetadata_fail() throws Exception {
        URI uri = new URI(prefixPath + "attachments/foo");
        decodeURI(uri, factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecode_wrongMetadataType_fail() throws Exception {
        /*
         * Metadata format 1-1-1-a
         */
        URI uri = new URI(prefixPath + "attachments/MV8xXzFfYQ");
        decodeURI(uri, factory);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDecode_wrongMetadataSyntax_fail() throws Exception {
        /*
         * Metadata format 1-1-1-a
         */
        URI uri = new URI(prefixPath + "attachments/MV8xXzFfYQ/foo");
        decodeURI(uri, factory);
    }

    @Test
    public void testDecode_validUri_MetadataParsed() throws Exception {
        /*
         * Metadata format 1-1-1-1
         */
        URI uri = new URI(prefixPath + "attachments/MS0xLTEtMQ");
        AttachmentMetadata metadata = decodeURI(uri, factory);
        assertThat(I(metadata.getId()), is(I(1)));
        assertThat(I(metadata.getAttachedId()), is(I(1)));
        assertThat(I(metadata.getFolderId()), is(I(1)));
        assertThat(I(metadata.getModuleId()), is(I(1)));
    }
    
    @Test
    public void testDecode_validUriWithSlashSuffix_MetadataParsed() throws Exception {
        /*
         * Metadata format 1-1-1-1
         */
        URI uri = new URI(prefixPath + "attachments/MS0xLTEtMQ/");
        AttachmentMetadata metadata = decodeURI(uri, factory);
        assertThat(I(metadata.getId()), is(I(1)));
        assertThat(I(metadata.getAttachedId()), is(I(1)));
        assertThat(I(metadata.getFolderId()), is(I(1)));
        assertThat(I(metadata.getModuleId()), is(I(1)));
    }
    
    @Test
    public void testDecode_completeValidUri_MetadataParsed() throws Exception {
        /*
         * Metadata format 1-1-1-1
         */
        URI uri = new URI(prefixPath + "attachments/MS0xLTEtMQ/jochen%20bahncard%202018.jpg");
        AttachmentMetadata metadata = decodeURI(uri, factory);
        assertThat(I(metadata.getId()), is(I(1)));
        assertThat(I(metadata.getAttachedId()), is(I(1)));
        assertThat(I(metadata.getFolderId()), is(I(1)));
        assertThat(I(metadata.getModuleId()), is(I(1)));
    }
    
    @Test
    public void testDecode_validUriWithInvalidSuffix_MetadataParsed() throws Exception {
        /*
         * Metadata format 1-1-1-1
         */
        URI uri = new URI(prefixPath + "attachments/MS0xLTEtMQ/isNotRelevantForTheEndocding");
        AttachmentMetadata metadata = decodeURI(uri, factory);
        assertThat(I(metadata.getId()), is(I(1)));
        assertThat(I(metadata.getAttachedId()), is(I(1)));
        assertThat(I(metadata.getFolderId()), is(I(1)));
        assertThat(I(metadata.getModuleId()), is(I(1)));
    }

}
