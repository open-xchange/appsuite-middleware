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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * {@link Preauth}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Preauth {

    private Preauth() {
        super();
    }

    public static void main(String args[]) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("account", "john.doe@domain.com");
        params.put("by", "name"); // needs to be part of hmac
        params.put("timestamp", "1135280708088");
        params.put("expires", "0");
        String key = "6b7ead4bd425836e8cf0079cd6c1a05acc127acd07c8ee4b61023e19250e929c";
        System.out.printf("preAuth: %s\n", computePreAuth(params, key));
    }

    /**
     * <pre>
     *         Map<String, String> params = new HashMap<String, String>();
     *         params.put("account", "john.doe@domain.com");
     *         params.put("by", "name"); // needs to be part of hmac
     *         params.put("timestamp", "1135280708088");
     *         params.put("expires", "0");
     *         String key = "6b7ead4bd425836e8cf0079cd6c1a05acc127acd07c8ee4b61023e19250e929c";
     *         System.out.printf("preAuth: %s\n", computePreAuth(params, key)
     * </pre>
     * 
     * @param params
     * @param key
     * @return
     */
    public static String computePreAuth(Map<String, String> params, String key) {
        TreeSet<String> names = new TreeSet<String>(params.keySet());
        StringBuilder sb = new StringBuilder();
        for (String name : names) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(params.get(name));
        }
        return getHmac(sb.toString(), key.getBytes());
    }

    private static String getHmac(String data, byte[] key) {
        try {
            ByteKey bk = new ByteKey(key);
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(bk);
            return toHex(mac.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("fatal error", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("fatal error", e);
        }
    }

    static class ByteKey implements SecretKey {

        private final byte[] mKey;

        ByteKey(byte[] key) {
            mKey = key.clone();
            ;
        }

        @Override
        public byte[] getEncoded() {
            return mKey;
        }

        @Override
        public String getAlgorithm() {
            return "HmacSHA1";
        }

        @Override
        public String getFormat() {
            return "RAW";
        }
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(hex[(data[i] & 0xf0) >>> 4]);
            sb.append(hex[data[i] & 0x0f]);
        }
        return sb.toString();
    }

    private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
