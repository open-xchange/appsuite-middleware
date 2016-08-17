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

package com.openexchange.tools;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;
import jonelo.jacksum.algorithm.MD;
import jonelo.jacksum.algorithm.MDgnu;
import jonelo.sugar.util.GeneralProgram;

/**
 * {@link HashUtility} - A utility class for hashes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HashUtility {

    /**
     * Initializes a new {@link HashUtility}.
     */
    private HashUtility() {
        super();
    }

    private static final long getLong(final byte[] array, final int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = ((value << 8) | (array[offset+i] & 0xFF));
        }
        return value;
    }

    private static final String TRANS_ENC = "hex";

    /**
     * Calculates the SHA-256 hash for specified string(s) in <code>hex</code> encoding.
     * <p>
     * The specified strings are concatenated using <code>delim</code> as delimiter. If <code>delim</code> is <code>null</code> only the
     * first string is considered.
     *
     * @param delim The delimiter to user between strings
     * @param strings The strings to get the hash value from
     * @return The calculated SHA-256 hash
     */
    public static String calculateHash(final String delim, final String... strings) {
        final StringBuilder hashMe = new StringBuilder(512);
        final int len = strings.length;
        if (len > 0) {
            hashMe.append(strings[0]);
            if (null != delim) {
                for (int i = 1; i < len; i++) {
                    hashMe.append(delim).append(strings[i]);
                }
            }
        }
        return getSha256(hashMe.toString(), TRANS_ENC);
    }

    /**
     * Gets the SHA-256 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     *
     * @param string The string to hash
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The SHA-256 hash
     */
    public static String getSha256(final String string, final String encoding) {
        try {
            final AbstractChecksum checksum =
                GeneralProgram.isSupportFor("1.4.2") ? new MD("SHA-256") : new MDgnu(jonelo.jacksum.adapt.gnu.crypto.Registry.SHA256_HASH);
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes("UTF-8"));
            return checksum.getFormattedValue();
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        } catch (final UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        }
        return null;
    }

    /**
     * Gets the SHA-256 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     *
     * @param string The string to hash
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The first eight bytes of hashcode's bytes, converted to a long value in little-endian order
     */
    public static long getSha256AsLong(final String string, final String encoding) {
        try {
            final AbstractChecksum checksum =
                GeneralProgram.isSupportFor("1.4.2") ? new MD("SHA-256") : new MDgnu(jonelo.jacksum.adapt.gnu.crypto.Registry.SHA256_HASH);
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes("UTF-8"));
            return getLong(checksum.getByteArray(), 0);
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        } catch (final UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        }
        return 0L;
    }

    /**
     * Gets the MD5 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     *
     * @param string The string to hash
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The MD5 hash
     */
    public static String getMD5(final String string, final String encoding) {
        try {
            final AbstractChecksum checksum =
                GeneralProgram.isSupportFor("1.4.2") ? new MD("MD5") : new MDgnu(jonelo.jacksum.adapt.gnu.crypto.Registry.MD5_HASH);
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes("UTF-8"));
            return checksum.getFormattedValue();
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        } catch (final UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        }
        return null;
    }

    /**
     * Gets the MD5 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     *
     * @param string The string to hash
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The first eight bytes of hashcode's bytes, converted to a long value in little-endian order
     */
    public static long getMD5AsLong(final String string, final String encoding) {
        try {
            final AbstractChecksum checksum =
                GeneralProgram.isSupportFor("1.4.2") ? new MD("MD5") : new MDgnu(jonelo.jacksum.adapt.gnu.crypto.Registry.MD5_HASH);
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes("UTF-8"));
            return getLong(checksum.getByteArray(), 0);
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        } catch (final UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        }
        return 0L;
    }

    /**
     * Gets the SHA-256 hash of specified string using <a href="http://www.jonelo.de/java/jacksum/index.html">Jacksum 1.7.0</a>.
     * <p>
     * Supported algorithms:<br>
     *
     * <pre>
     * Adler32, BSD sum, Bzip2's CRC-32, POSIX cksum, CRC-8, CRC-16, CRC-24, CRC-32 (FCS-32), CRC-64, ELF-32, eMule/eDonkey, FCS-16, GOST R
     * 34.11-94, HAS-160, HAVAL (3/4/5 passes, 128/160/192/224/256 bits), MD2, MD4, MD5, MPEG-2's CRC-32, RIPEMD-128, RIPEMD-160,
     * RIPEMD-256, RIPEMD-320, SHA-0, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, Tiger-128, Tiger-160, Tiger, Tiger2, Tiger Tree Hash,
     * Tiger2 Tree Hash, Unix System V sum, sum8, sum16, sum24, sum32, Whirlpool-0, Whirlpool-1, Whirlpool, and xor8
     * </pre>.
     *
     * @param string The string to hash
     * @param algorithm The hash algorithm to use; e.g. <code>sha-1</code>, <code>sha-256</code>, <code>md5</code>, <code>crc32</code>,
     *            <code>adler32</code>, ...
     * @param encoding The encoding; e.g <code>base64</code>, <code>hex</code>, <code>dec</code>, etc.
     * @return The hash or <code>null</code> if hash could not be generated
     */
    public static String getHash(final String string, final String algorithm, final String encoding) {
        try {
            final AbstractChecksum checksum = JacksumAPI.getChecksumInstance(algorithm);
            checksum.setEncoding(encoding);
            checksum.update(string.getBytes("UTF-8"));
            return checksum.getFormattedValue();
        } catch (final NoSuchAlgorithmException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        } catch (final UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(HashUtility.class).error("", e);
        }
        return null;
    }

}
