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

package com.openexchange.pgp.keys.parsing.impl;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.pgp.keys.parsing.KeyRingParserResult;

/**
 * {@link PGPKeyParserImplTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class AsciiArmoredKeyParserTest {

    private static final String EXAMPLE_SECRET_KEY_RING = "-----BEGIN PGP PRIVATE KEY BLOCK-----\r\nVersion: GnuPG v1\r\n\r\n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\r\n=Cr6o\r\n-----END PGP PRIVATE KEY BLOCK-----\r\n";
    private static final String EXAMPLE_SECRET_KEY_RING_USER_ID = "Test User 1 <test1@localhost>";
    private static final String EXAMPLE_PUBLIC_KEY_RING = "-----BEGIN PGP PUBLIC KEY BLOCK-----\r\nVersion: BCPG v1.60\r\n\r\nmQENBFv2e/UBCACfVGySMaaZQhHw3al9/oxq3XFbgaRhu34pjE1bqUxlIOKf+9qJ\r\n6kZtdt9GZQch61fAY6h/7yHrqbFsaRuQtNb9hgqLgNF4d9JbgducV4jIJlOEKN9X\r\nyxti1xH/HNcoY99m0/4APTHjry7+uNIN0qWJg8b2X/fbsKpoMvjfsABLOYwJSDbV\r\nrYM0WiVblLGIfeBuIPGS7h6c86SX4A2RmruamTh4uHbai0TLtPI7vAG7X4Lq3veF\r\ngIH3QSDsw/000UzFSynUnJ9/8bxkG6bjnYJxL0GjbxD+t5+1d8ugwPXE0oSI1xi9\r\n5X221bb68QDelmKypm/g+bt60M6v+MKLbH2FABEBAAG0F1Rlc3QxIDx0ZXN0MUBs\r\nb2NhbGhvc3Q+iQE6BBMBAgAkBQJb9nv1AhsDBAsJCAcGFQoJCAsCBRYCAwEAAp4B\r\nBQkSzAMAAAoJELEaIvCkUXL4Yn4H/1NyX8MA+6/Q5lpiR5q4SrKCB5GF59Qh9EDo\r\nJgc1wKOYpsxJ1jZ6+SshgNqVs2BykKNwDhC2CFMgwi7SKRmxPj/F6UOmw032wOFN\r\nkJrrHlb1SzYA1rQypyUiNEoT3ISbY/zJ3qOB7rZP92/zgiPjWSV7vlx4s0q6+HTd\r\n5pvXL5+NDFKH26ysAEyxU7TGi0ijn7bw21H6XV7fJjK02mUHkkipOwDZK19WAGdd\r\na6kFRX7xxu2UBLtbMb5d3cbLvG0QbmoH+Dp3h6liwbOZ4kGXI2r++lkbbIGQz5Ay\r\nyz+n6DrksR1aAQNd+3cd8HLFkWoaTmJ3bzVhxz6izByaARaRWte5AQ0EW/Z79QEI\r\nAMqfebkbBA0J1g3qYfUxJQb8Bcoqd7fsGJqGi6oISotQq1LyJ2z4Oy7Y8H+2VUT3\r\nJJhMbGy8RdXtKWi+ml+L1e3l87jVEMnRrxlMi5UdGHwBZUl/rBO+A9QdUfut7/vU\r\nwvn7KF8oUOdm7VFKyxspYFnrLP3H6jf7DzCg68T/sE61gSqnZG7nmJADHiVDcEUO\r\n0iyK0EP50CozIh2NtJoQ89bsEhd+8ugkJ4Ouxd5ZN7sJvmyU+w4xppJOaTr8LLdN\r\nICVcFSKtuJOCvkdcYq3eQZJiMgcnzJr1PIyJnMsvD1vzAzsChcUXd2y+Ypoh4Ft1\r\ng82O+1pM0QJOpHvkTfPmh9MAEQEAAYkBOgQYAQIAJAUCW/Z79QIbDAQLCQgHBhUK\r\nCQgLAgUWAgMBAAKeAQUJEswDAAAKCRCxGiLwpFFy+JjgB/9W5CUIJR45JzX0dUA2\r\nE4xdcxaQLWurPbrFDbDCjkpLVg9Hq1l7aO+CGBUL9NLB8EJhUWj63HPTwNkXV+S1\r\nmFSpMGuQGEtYRqV1F5kpQErdYJvzEyDsi3Jh51myzLboRm3sHS5eoPoUuA0OZ411\r\nkGQEd/5L8KKxcgnpZZm9SsliLzSqEfrNT2M+2mamtibPSf0hCRkE3X0Ejj+BBUqS\r\nPPrAYYOmLbrBds23Eqhk7BBgI3geQ37cIKylzCt5pDMR4qNl2J0F8pKo58YTLo/L\r\nKIs2i8wOKvqd+P+M3CBIt464nuo5f9b2yzXti994wC2+igv+B3tcewbrl0VCckHt\r\nznam\r\n=fLLm\r\n-----END PGP PUBLIC KEY BLOCK-----";
    private static final String EXAMPLE_MULTIPLE_PUBLIC_KEY_RINGS = "-----BEGIN PGP PUBLIC KEY BLOCK-----\r\nVersion: BCPG v1.60\r\n\r\nmQENBFv2e/UBCACfVGySMaaZQhHw3al9/oxq3XFbgaRhu34pjE1bqUxlIOKf+9qJ\r\n6kZtdt9GZQch61fAY6h/7yHrqbFsaRuQtNb9hgqLgNF4d9JbgducV4jIJlOEKN9X\r\nyxti1xH/HNcoY99m0/4APTHjry7+uNIN0qWJg8b2X/fbsKpoMvjfsABLOYwJSDbV\r\nrYM0WiVblLGIfeBuIPGS7h6c86SX4A2RmruamTh4uHbai0TLtPI7vAG7X4Lq3veF\r\ngIH3QSDsw/000UzFSynUnJ9/8bxkG6bjnYJxL0GjbxD+t5+1d8ugwPXE0oSI1xi9\r\n5X221bb68QDelmKypm/g+bt60M6v+MKLbH2FABEBAAG0F1Rlc3QxIDx0ZXN0MUBs\r\nb2NhbGhvc3Q+iQE6BBMBAgAkBQJb9nv1AhsDBAsJCAcGFQoJCAsCBRYCAwEAAp4B\r\nBQkSzAMAAAoJELEaIvCkUXL4Yn4H/1NyX8MA+6/Q5lpiR5q4SrKCB5GF59Qh9EDo\r\nJgc1wKOYpsxJ1jZ6+SshgNqVs2BykKNwDhC2CFMgwi7SKRmxPj/F6UOmw032wOFN\r\nkJrrHlb1SzYA1rQypyUiNEoT3ISbY/zJ3qOB7rZP92/zgiPjWSV7vlx4s0q6+HTd\r\n5pvXL5+NDFKH26ysAEyxU7TGi0ijn7bw21H6XV7fJjK02mUHkkipOwDZK19WAGdd\r\na6kFRX7xxu2UBLtbMb5d3cbLvG0QbmoH+Dp3h6liwbOZ4kGXI2r++lkbbIGQz5Ay\r\nyz+n6DrksR1aAQNd+3cd8HLFkWoaTmJ3bzVhxz6izByaARaRWte5AQ0EW/Z79QEI\r\nAMqfebkbBA0J1g3qYfUxJQb8Bcoqd7fsGJqGi6oISotQq1LyJ2z4Oy7Y8H+2VUT3\r\nJJhMbGy8RdXtKWi+ml+L1e3l87jVEMnRrxlMi5UdGHwBZUl/rBO+A9QdUfut7/vU\r\nwvn7KF8oUOdm7VFKyxspYFnrLP3H6jf7DzCg68T/sE61gSqnZG7nmJADHiVDcEUO\r\n0iyK0EP50CozIh2NtJoQ89bsEhd+8ugkJ4Ouxd5ZN7sJvmyU+w4xppJOaTr8LLdN\r\nICVcFSKtuJOCvkdcYq3eQZJiMgcnzJr1PIyJnMsvD1vzAzsChcUXd2y+Ypoh4Ft1\r\ng82O+1pM0QJOpHvkTfPmh9MAEQEAAYkBOgQYAQIAJAUCW/Z79QIbDAQLCQgHBhUK\r\nCQgLAgUWAgMBAAKeAQUJEswDAAAKCRCxGiLwpFFy+JjgB/9W5CUIJR45JzX0dUA2\r\nE4xdcxaQLWurPbrFDbDCjkpLVg9Hq1l7aO+CGBUL9NLB8EJhUWj63HPTwNkXV+S1\r\nmFSpMGuQGEtYRqV1F5kpQErdYJvzEyDsi3Jh51myzLboRm3sHS5eoPoUuA0OZ411\r\nkGQEd/5L8KKxcgnpZZm9SsliLzSqEfrNT2M+2mamtibPSf0hCRkE3X0Ejj+BBUqS\r\nPPrAYYOmLbrBds23Eqhk7BBgI3geQ37cIKylzCt5pDMR4qNl2J0F8pKo58YTLo/L\r\nKIs2i8wOKvqd+P+M3CBIt464nuo5f9b2yzXti994wC2+igv+B3tcewbrl0VCckHt\r\nznam\r\n=fLLm\r\n-----END PGP PUBLIC KEY BLOCK-----\r\n-----BEGIN PGP PUBLIC KEY BLOCK-----\r\nVersion: BCPG v1.60\r\n\r\nmQENBFv2e/UBCACfVGySMaaZQhHw3al9/oxq3XFbgaRhu34pjE1bqUxlIOKf+9qJ\r\n6kZtdt9GZQch61fAY6h/7yHrqbFsaRuQtNb9hgqLgNF4d9JbgducV4jIJlOEKN9X\r\nyxti1xH/HNcoY99m0/4APTHjry7+uNIN0qWJg8b2X/fbsKpoMvjfsABLOYwJSDbV\r\nrYM0WiVblLGIfeBuIPGS7h6c86SX4A2RmruamTh4uHbai0TLtPI7vAG7X4Lq3veF\r\ngIH3QSDsw/000UzFSynUnJ9/8bxkG6bjnYJxL0GjbxD+t5+1d8ugwPXE0oSI1xi9\r\n5X221bb68QDelmKypm/g+bt60M6v+MKLbH2FABEBAAG0F1Rlc3QxIDx0ZXN0MUBs\r\nb2NhbGhvc3Q+iQE6BBMBAgAkBQJb9nv1AhsDBAsJCAcGFQoJCAsCBRYCAwEAAp4B\r\nBQkSzAMAAAoJELEaIvCkUXL4Yn4H/1NyX8MA+6/Q5lpiR5q4SrKCB5GF59Qh9EDo\r\nJgc1wKOYpsxJ1jZ6+SshgNqVs2BykKNwDhC2CFMgwi7SKRmxPj/F6UOmw032wOFN\r\nkJrrHlb1SzYA1rQypyUiNEoT3ISbY/zJ3qOB7rZP92/zgiPjWSV7vlx4s0q6+HTd\r\n5pvXL5+NDFKH26ysAEyxU7TGi0ijn7bw21H6XV7fJjK02mUHkkipOwDZK19WAGdd\r\na6kFRX7xxu2UBLtbMb5d3cbLvG0QbmoH+Dp3h6liwbOZ4kGXI2r++lkbbIGQz5Ay\r\nyz+n6DrksR1aAQNd+3cd8HLFkWoaTmJ3bzVhxz6izByaARaRWte5AQ0EW/Z79QEI\r\nAMqfebkbBA0J1g3qYfUxJQb8Bcoqd7fsGJqGi6oISotQq1LyJ2z4Oy7Y8H+2VUT3\r\nJJhMbGy8RdXtKWi+ml+L1e3l87jVEMnRrxlMi5UdGHwBZUl/rBO+A9QdUfut7/vU\r\nwvn7KF8oUOdm7VFKyxspYFnrLP3H6jf7DzCg68T/sE61gSqnZG7nmJADHiVDcEUO\r\n0iyK0EP50CozIh2NtJoQ89bsEhd+8ugkJ4Ouxd5ZN7sJvmyU+w4xppJOaTr8LLdN\r\nICVcFSKtuJOCvkdcYq3eQZJiMgcnzJr1PIyJnMsvD1vzAzsChcUXd2y+Ypoh4Ft1\r\ng82O+1pM0QJOpHvkTfPmh9MAEQEAAYkBOgQYAQIAJAUCW/Z79QIbDAQLCQgHBhUK\r\nCQgLAgUWAgMBAAKeAQUJEswDAAAKCRCxGiLwpFFy+JjgB/9W5CUIJR45JzX0dUA2\r\nE4xdcxaQLWurPbrFDbDCjkpLVg9Hq1l7aO+CGBUL9NLB8EJhUWj63HPTwNkXV+S1\r\nmFSpMGuQGEtYRqV1F5kpQErdYJvzEyDsi3Jh51myzLboRm3sHS5eoPoUuA0OZ411\r\nkGQEd/5L8KKxcgnpZZm9SsliLzSqEfrNT2M+2mamtibPSf0hCRkE3X0Ejj+BBUqS\r\nPPrAYYOmLbrBds23Eqhk7BBgI3geQ37cIKylzCt5pDMR4qNl2J0F8pKo58YTLo/L\r\nKIs2i8wOKvqd+P+M3CBIt464nuo5f9b2yzXti994wC2+igv+B3tcewbrl0VCckHt\r\nznam\r\n=fLLm\r\n-----END PGP PUBLIC KEY BLOCK-----";

    /**
     * Internal method to check if collection of user IDS contains a given user ID
     *
     * @param secretKey The secret key
     * @param userId The user ID
     * @return true, if the secret key contains the given user ID, false otherwise.
     */
    private boolean hasUserId(Iterator userIdIterator, String userId) {
        while (userIdIterator.hasNext()) {
            String keyUserId = (String) userIdIterator.next();
            if (userId.equals(keyUserId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Internal method to check if a secret key contains a given user ID
     *
     * @param secretKey The secret key
     * @param userId The user ID
     * @return true, if the secret key contains the given user ID, false otherwise.
     */
    private boolean hasUserId(PGPSecretKey secretKey, String userId) {
        return hasUserId(secretKey.getUserIDs(), userId);
    }

    /**
     * Internal method to check if a public key contains a given user ID
     *
     * @param secretKey The secret key
     * @param userId The user ID
     * @return true, if the secret key contains the given user ID, false otherwise.
     */
    private boolean hasUserId(PGPPublicKey publicKey, String userId) {
        return hasUserId(publicKey.getUserIDs(), userId);
    }

    @Test
    public void testParseValidPublicKeyRing() throws Exception {
        //Parse example data
        AsciiArmoredKeyParser parser = new AsciiArmoredKeyParser();
        KeyRingParserResult parserResult = parser.parse(new ByteArrayInputStream(EXAMPLE_PUBLIC_KEY_RING.getBytes()));
        Assert.assertNotNull("The parsing result must not be null", parserResult);

        //Extract PGPPunblicKeyRing
        List<PGPPublicKeyRing> publicKeyRings = parserResult.toPublicKeyRings();
        Assert.assertTrue("The parsing result should contain one public key ring", publicKeyRings.size() == 1);
    }

    @Test
    public void testParseMultipleValidPublicKeyRings() throws Exception {
        //Parse example data
        AsciiArmoredKeyParser parser = new AsciiArmoredKeyParser();
        KeyRingParserResult parserResult = parser.parse(new ByteArrayInputStream(EXAMPLE_MULTIPLE_PUBLIC_KEY_RINGS.getBytes()));
        Assert.assertNotNull("The parsing result must not be null", parserResult);

        //Extract PGPPunblicKeyRing
        List<PGPPublicKeyRing> publicKeyRings = parserResult.toPublicKeyRings();
        Assert.assertTrue("The parsing result should contain two public key ring", publicKeyRings.size() == 2);
    }

    @Test
    public void testParseValidPrivateKeyRing() throws Exception {

        //Parse example data
        AsciiArmoredKeyParser parser = new AsciiArmoredKeyParser();
        KeyRingParserResult parserResult = parser.parse(new ByteArrayInputStream(EXAMPLE_SECRET_KEY_RING.getBytes()));
        Assert.assertNotNull("The parsing result must not be null", parserResult);

        //Extract PGPSecretKeyRing
        List<PGPSecretKeyRing> secretKeyRings = parserResult.toSecretKeyRings();
        Assert.assertTrue("The parsing result should contain one secret key ring", secretKeyRings.size() == 1);

        //Extract public key
        PGPSecretKeyRing secretKeyRing = secretKeyRings.get(0);
        PGPPublicKey publicKey = secretKeyRing.getPublicKey();
        Assert.assertNotNull("The secret key ring should also contain a public key", publicKey);
        Assert.assertTrue("The public key should contain the correct user ID", hasUserId(publicKey, EXAMPLE_SECRET_KEY_RING_USER_ID));

        //Extract signing Key
        PGPSecretKey signingKey = parserResult.toSigningKey();
        Assert.assertNotNull("The parsing result should contain an signing key", signingKey);

        //Check if the key contains the user
        Assert.assertTrue("The signing key should contain the correct user ID", hasUserId(signingKey, EXAMPLE_SECRET_KEY_RING_USER_ID));
    }

}
