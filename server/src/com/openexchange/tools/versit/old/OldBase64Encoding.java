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

package com.openexchange.tools.versit.old;

import java.io.IOException;

import com.openexchange.tools.encoding.Base64;

/**
 * @author Viktor Pracht
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class OldBase64Encoding implements OldEncoding {

	public static final OldEncoding Default = new OldBase64Encoding();

	/**
     * Default constructor.
     */
    private OldBase64Encoding() {
        super();
    }

    public byte[] decode(final OldScanner s) throws IOException {
		final StringBuilder sb = new StringBuilder();
		boolean newline = false;
		while (!newline || s.peek != -1 && s.peek != -2) {
			newline = s.peek == -1 || s.peek == -2;
			sb.append((char) s.read());
		}
		return Base64.decode(sb.toString());
	}

	public void encode(final OldFoldingWriter fw, final byte[] b) throws IOException {
		fw.rawStart();
        fw.write(Base64.encode(b));
		fw.rawEnd();
	}
}
