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

package com.openexchange.oauth.provider.impl.tools;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;

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
     public void testGenerateClientId_defaultGroup_validClientId() {
        String generateClientId = OAuthClientIdHelper.getInstance().generateClientId(GROUP_ID);

        String encodedGroupId = generateClientId.split(OAuthClientIdHelper.SEPERATOR)[0];
        String decodedGroupId = new String(Base64.decodeBase64(encodedGroupId));

        Assert.assertEquals(GROUP_ID, decodedGroupId);
    }

     @Test
     public void testGenerateClientId_maximumCharacters_validClientId() {
        String generateClientId = OAuthClientIdHelper.getInstance().generateClientId(GROUP_ID_MAX_SIZE);

        String encodedGroupId = generateClientId.split(OAuthClientIdHelper.SEPERATOR)[0];
        String decodedGroupId = new String(Base64.decodeBase64(encodedGroupId));

        Assert.assertEquals(GROUP_ID_MAX_SIZE, decodedGroupId);
    }

     @Test
     public void testGenerateClientId_maximumCharacters_doNotExceed256AllowedColumnCharacters() {
        String generateClientId = OAuthClientIdHelper.getInstance().generateClientId(GROUP_ID_MAX_SIZE);

        Assert.assertTrue("Length of clientid exceeds maximum size of 256 characters", generateClientId.length() < 256);
    }

     @Test
     public void testExtractGroupId_curiousGroupId_fails() {
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
     public void testDecode_ValidEncodedGroupId_correctDecoded() {
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
