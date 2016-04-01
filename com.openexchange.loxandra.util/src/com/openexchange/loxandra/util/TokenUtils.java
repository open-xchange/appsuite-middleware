package com.openexchange.loxandra.util;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

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

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TokenUtils {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TokenUtils.class);

	/**
	 * Method for calculating unique tokens for each node
	 * in a Cassandra cluster.<br/><br/>
	 *
	 * Formula: (2^127 / numberOfNodes) * n, where n=0..numberOfNodes-1
	 *
	 * @param numberOfNodes number of nodes
	 * @return tokens BigInteger array with tokens
	 */
	public static BigInteger[] calculateTokens(int numberOfNodes) {
		int i = 0;
		BigInteger[] tokens = new BigInteger[numberOfNodes];

		while (i < numberOfNodes) {
			BigInteger n = BigInteger.valueOf(numberOfNodes);
			BigInteger pow = BigInteger.valueOf(2).pow(127);
            BigInteger token = pow.divide(n).multiply(BigInteger.valueOf(i));
            tokens[i] = token;
            log.info("Node {}: {}", i, token);

            i++;
		}

		return tokens;
	}

	/**
	 * Generates tokens based on the size of the cluster. Works both
	 * ways, i.e. grow and shrink.<br/><br/>
	 *
	 * Originally written in Python by <a href="http://paul.querna.org/">Paul Querna</a><br/>
	 * Ported in Java by <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
	 *
	 * @param currentNodes
	 * @param futureNodes
	 *
	 * @return ArrayList with new tokens
	 */
	public static BigInteger[] resizeCluster(int currentNodes, int futureNodes) {
		BigInteger[] tokens = new BigInteger[futureNodes];
		log.debug("Current tokens");
		BigInteger[] cn = calculateTokens(currentNodes);
		log.debug("Future tokens");
		BigInteger[] fn = calculateTokens(futureNodes);

		int c = 0;
		int f = 0;

		ArrayList<String> p = new ArrayList<String>();
		log.debug("Re-Balanced");
		while (c < cn.length && f < fn.length) {
			BigInteger current = cn[c];
			BigInteger future = fn[f];

			if (future.compareTo(current) == 0) {
				p = pending(p);
				log.debug("Node {} stays at {}", c, current );
				tokens[f] = current;
				c++;
			}

			if (future.compareTo(current) == 1) {
				p = pending(p);
				log.debug("Node {} ===> {}", c, future);
				tokens[f] = future;
				c++;
			}

			if (future.compareTo(current) == -1) {
				p.add("New node at: " + future);
			}
			f++;
		}

		if (p.size() > 1) {
			p = pending(p);
		}

		while (f < fn.length) {
			log.debug("New node at: {}", fn[f]);
			tokens[f] = fn[f];
			f++;
		}

		return tokens;
	}

	/**
	 * Print out any pending nodes. Works in conjunction with {@link #resizeCluster(int, int)} method.
	 * @param pending
	 * @return an ArrayList
	 */
	private static ArrayList<String> pending(ArrayList<String> pending) {
		Iterator<String> iter = pending.iterator();
		while (iter.hasNext()) {
			String type = iter.next();
			log.debug(type);
		}
		pending.clear();
		return pending;
	}

	/**
	 * Generates a SHA1 String
	 * @param value to hash
	 * @return the hashed value
	 */
	public static String generateSHA1(String value) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(value.getBytes());
		byte[] b = md.digest();
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < b.length; j++) {
			buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
			buf.append(hexDigit[b[j] & 0x0f]);
		}
		return buf.toString();
	}
}
