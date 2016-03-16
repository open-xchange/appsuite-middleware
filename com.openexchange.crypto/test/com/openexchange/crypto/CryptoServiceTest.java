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

package com.openexchange.crypto;

import static com.openexchange.crypto.CryptoErrorMessage.BadPassword;
import junit.framework.TestCase;
import com.openexchange.crypto.internal.CryptoServiceImpl;
import com.openexchange.exception.OXException;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class CryptoServiceTest extends TestCase {

    protected String payload = "Hello World!";

    protected String payloadSpecial = "H\u00d4\u00f8\u03a9ll\u00d4\u00f8\u03a9 &/() w\u00d4\u00f8\u03a9RLD!";

    protected String password = "passwordpasswordpasswordpassword";

    protected String passwordSpecial = "p\u00c0\u00dfw\u00d4\u00f8\u03a9rd;";

    protected String badPassword  = "passwordpasswordpasswordpassword1";

    protected CryptoService cryptoService;

    protected final byte[] salt = new byte[] { 0x34, 0x11, 0x45, 0x03, 0x04, 0x05, 0x06, 0x43, 0x23, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0e };

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cryptoService = new CryptoServiceImpl();
    }

    @Override
    public void tearDown() throws Exception {
        cryptoService = null;
        super.tearDown();
    }

    public void testEncrypt() throws Exception {
        String encrypted = cryptoService.encrypt(payload, password);
        assertFalse("Payload was not encrypted.", encrypted.equals(payload));

        encrypted = cryptoService.encrypt(payloadSpecial, passwordSpecial);
        assertFalse("Payload was not encrypted.", encrypted.equals(payloadSpecial));
    }

    public void testDecrypt() throws Exception {
        String encrypted = cryptoService.encrypt(payload, password);
        String decrypted = cryptoService.decrypt(encrypted, password);
        assertEquals("Payload not decrypted.", payload, decrypted);

        encrypted = cryptoService.encrypt(payloadSpecial, passwordSpecial);
        decrypted = cryptoService.decrypt(encrypted, passwordSpecial);
        assertEquals("payload not decrypted.", payloadSpecial, decrypted);
    }

    public void testBadPassword() throws Exception {
        final String encrypted = cryptoService.encrypt(payload, password);
        try {
            cryptoService.decrypt(encrypted, badPassword);
            fail("Exception expected.");
        } catch (final OXException e) {
            assertTrue("Wrong exception thrown.", BadPassword.equals(e));
        }
    }

    public void testSaltUsage() throws Exception {
        EncryptedData encryptedData = cryptoService.encrypt(payload, password, true);
        String decryptedData = cryptoService.decrypt(encryptedData, password, true);
        assertEquals("Payload not decrypted", payload, decryptedData);

        encryptedData = cryptoService.encrypt(payload, password, false);
        decryptedData = cryptoService.decrypt(encryptedData, password, false);
        assertEquals("Payload not decrypted", payload, decryptedData);

        encryptedData = cryptoService.encrypt(payload, password, true);
        encryptedData = new EncryptedData(encryptedData.getData(), salt);
        try {
            decryptedData = cryptoService.decrypt(encryptedData, password, true);
            fail("Exception expected.");
        } catch (final OXException e) {
            assertTrue("Wrong exception thrown.", BadPassword.equals(e));
        }
    }
}
