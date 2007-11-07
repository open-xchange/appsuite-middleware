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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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


package com.openexchange.tools.encoding;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import javax.mail.internet.MimeUtility;

/**
 * This class contains some helpers for encoding.
 * It only contains simple methods that encode some things. 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Helper {

	private Helper() {
		super();
	}
	
	/**
 	 * Encodes a filename according RFC2047 and RFC2231.
	 * This is used to encode file names for use in http headers as content-disposition for downloading
	 * a file to the client. Encoding is only done if the original filename contains non ascii
	 * characters. Return the header to the client with the following format:
	 * <ul>
	 * <li>header attribute name: Content-Disposition</li>
	 * <li>header attribute value: filename=&quot;<with this method encoded filename>&quot;</li>
	 * </ul>
	 * @param orig filename containing non ascii characters
	 * @param encoding Character encoding to be used.
	 * @param internetExplorer set this true if the client is a Microsoft InternetExplorer
	 * @return the encoded filename that can be put directly into the filename of the content-disposition header
	 * @throws UnsupportedEncodingException if the given encoding is not supported by java.
	 */
	public static String encodeFilename(final String orig, final String encoding, final boolean internetExplorer) throws UnsupportedEncodingException {
		String encoded = orig;
		boolean isAscii = true;
		final char[] namechars = orig.toCharArray();
		for (int i = orig.length(); isAscii && --i>=0;) {
			isAscii &= namechars[i] < 0x7f; // non-ascii characters
			isAscii &= namechars[i] > 0x21; // space and control characters
			isAscii &= namechars[i] != '\u002a'; // *
			isAscii &= namechars[i] != '\u0025'; // %
			isAscii &= namechars[i] != '\''; // '
		}
		if (!isAscii) {
			if (internetExplorer) {
                try {
                    final Charset charset = Charset.forName(encoding);
                    encoded = URLCoder.encode(orig, charset);
                } catch (UnsupportedCharsetException uce) {
                    throw new UnsupportedEncodingException(uce.getMessage());
                }
			} else {
				encoded = MimeUtility.encodeText(orig, encoding, "B");
			}
		}
		return encoded;
	}
    
    /**
     * At some cases JavaMail is not able to fetch multi-encoded words or broken
     * encodings. Some mailers which are not mime compliant produces such crap.
     * We'll try to find and decode such Strings here using the JavaMail API.
     * 
     * @author Stefan Preuss <stefan.preuss@open-xchange.com>
     * @param data The string which should be encoded
     * @return The (may) correct encoded String
     */
    public static String decodeText(final String data) {
        int start = 0, i;
        final StringBuffer sb = new StringBuffer();
        while ((i = data.indexOf("=?", start)) >= 0) {
            sb.append(data.substring(start, i));
            final  int end = data.indexOf("?=", i);
            if (end < 0) {
                break;
            }
            final String s = data.substring(i, end + 2);
            try {
                sb.append(MimeUtility.decodeWord(s));
            } catch (Exception e) {
                sb.append(s);
            }
            start = end + 2;
        }
        if (start == 0) {
            return data;
        }
        if (start < data.length()) {
            sb.append(data.substring(start));
        }
        return sb.toString();
    }
}
