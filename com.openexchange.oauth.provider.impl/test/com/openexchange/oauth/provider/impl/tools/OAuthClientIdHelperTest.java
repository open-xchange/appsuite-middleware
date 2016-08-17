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

package com.openexchange.oauth.provider.impl.tools;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.impl.tools.OAuthClientIdHelper;

/**
 * {@link OAuthClientIdHelperTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class OAuthClientIdHelperTest {

    private final static String GROUP_ID = "default";

    private final static String ENCODED_GROUP_ID = "dGhpc1Nob3VsZEJlTWF4aW11bTMyQ2hhcmFjdGVyc3M";

    private final static String GROUP_ID_MAX_SIZE = "thisShouldBeMaximum32Characterss";

    private final static String TYPICAL_CLIENT_ID = "dGhpc1Nob3VsZEJlTWF4aW11bTMyQ2hhcmFjdGVyc3M/d48ce8ec590c461f992158508d9ac99b04b5988d5ff14586bfe34209c22b8a67";

    private final static String BROKEN_CLIENT_ID = "dGhpc1Nob3V/sZEJlTWF4aW/11bTMyQ2hhcmFjdGVyc3M/d48ce8ec590c461f992158508d9ac99b04b5988d5ff14586bfe34209c22b8a67";

    @Test
    public void testGenerateClientId_defaultGroup_validClientId() throws Exception {
        String generateClientId = OAuthClientIdHelper.getInstance().generateClientId(GROUP_ID);

        String encodedGroupId = generateClientId.split(OAuthClientIdHelper.SEPERATOR)[0];
        String decodedGroupId = new String(Base64.decodeBase64(encodedGroupId));

        Assert.assertEquals(GROUP_ID, decodedGroupId);
    }

    @Test
    public void testGenerateClientId_maximumCharacters_validClientId() throws Exception {
        String generateClientId = OAuthClientIdHelper.getInstance().generateClientId(GROUP_ID_MAX_SIZE);

        String encodedGroupId = generateClientId.split(OAuthClientIdHelper.SEPERATOR)[0];
        String decodedGroupId = new String(Base64.decodeBase64(encodedGroupId));

        Assert.assertEquals(GROUP_ID_MAX_SIZE, decodedGroupId);
    }

    @Test
    public void testGenerateClientId_maximumCharacters_doNotExceed256AllowedColumnCharacters() throws Exception {
        String generateClientId = OAuthClientIdHelper.getInstance().generateClientId(GROUP_ID_MAX_SIZE);

        Assert.assertTrue("Length of clientid exceeds maximum size of 256 characters", generateClientId.length() < 256);
    }

    @Test
    public void testExtractGroupId_curiousGroupId_fails() throws Exception {
        boolean failed = false;
        try {
            OAuthClientIdHelper.getInstance().extractEncodedGroupId(BROKEN_CLIENT_ID);
        } catch (OXException e) {
            Assert.assertTrue(OAuthProviderExceptionCodes.BAD_CONTEXT_GROUP_IN_CLIENT_ID.equals(e));
            failed = true;
        }

        Assert.assertTrue(failed);
    }

    @Test
    public void testExtractGroupId_validGroupId_correctExtracted() throws Exception {
        String extractedGroupId = OAuthClientIdHelper.getInstance().extractEncodedGroupId(TYPICAL_CLIENT_ID);

        Assert.assertTrue("Extracted encoded group id not correct", TYPICAL_CLIENT_ID.substring(0, extractedGroupId.length()).equals(ENCODED_GROUP_ID));
    }

    @Test
    public void testDecode_ValidEncodedGroupId_correctDecoded() throws Exception {
        String extractedGroupId = OAuthClientIdHelper.getInstance().decode(ENCODED_GROUP_ID);

        Assert.assertEquals("Extracted group id not correct", GROUP_ID_MAX_SIZE, extractedGroupId);
    }

    @Test
    public void testGetGroupIdFrom_validGroupId_correctExtracted() throws Exception {
        String extractedGroupId = OAuthClientIdHelper.getInstance().getGroupIdFrom(TYPICAL_CLIENT_ID);

        Assert.assertTrue("Extracted group id not correct", extractedGroupId.equals(GROUP_ID_MAX_SIZE));
    }

    @Test
    public void testGetClientFrom_valid_correctCodeExtracted() throws Exception {
        String extractedClientCode = OAuthClientIdHelper.getInstance().getBaseTokenFrom(TYPICAL_CLIENT_ID);

        Assert.assertTrue("Extracted client code not correct", TYPICAL_CLIENT_ID.endsWith(extractedClientCode));
    }

    @Test
    public void testGetClientFrom_valid_noSeperatorIncluded() throws Exception {
        String extractedClientCode = OAuthClientIdHelper.getInstance().getBaseTokenFrom(TYPICAL_CLIENT_ID);

        Assert.assertFalse("Extracted client code not correct", extractedClientCode.contains(OAuthClientIdHelper.SEPERATOR));
    }
}
