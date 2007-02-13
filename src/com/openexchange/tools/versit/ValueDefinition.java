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



package com.openexchange.tools.versit;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Viktor Pracht
 */
public class ValueDefinition {

	private final HashMap Encodings;

	private static final HashMap NoEncodings = new HashMap();

	// public static final ValueDefinition Default = new ValueDefinition();

	public ValueDefinition() {
		Encodings = NoEncodings;
	}

	public ValueDefinition(String[] encodingNames, Encoding[] encodings) {
		Encodings = new HashMap();
		for (int i = 0; i < encodingNames.length; i++)
			Encodings.put(encodingNames[i].toUpperCase(), encodings[i]);
	}

	public Encoding getEncoding(String name) {
		return (Encoding) Encodings.get(name.toUpperCase());
	}

	public Object parse(Scanner s, Property property) throws IOException {
		StringBuffer sb = new StringBuffer();
		while (!(s.peek < ' ' && s.peek != '\t' || s.peek == 0x7f))
			sb.append((char) s.read());
		String text = sb.toString();
		Parameter encodingParam = property.getParameter("ENCODING");
		if (encodingParam != null) {
			String EncName = encodingParam.getValue(0).getText();
			Encoding encoding = getEncoding(EncName);
			if (encoding == null)
				throw new VersitException(s, "Unknown encoding: " + EncName);
			text = encoding.decode(text);
		}
		return createValue(new StringScanner(s, text), property);
	}

	public Object createValue(StringScanner s, Property property)
			throws IOException {
		return s.getRest();
	}

	public void write(FoldingWriter fw, Property property) throws IOException {
		String value = writeValue(property.getValue());
		Parameter encodingParam = property.getParameter("ENCODING");
		if (encodingParam != null) {
			String enc_name = encodingParam.getValue(0).getText();
			Encoding encoding = getEncoding(enc_name);
			if (encoding == null)
				throw new IOException("Unknown encoding: " + enc_name);
			value = encoding.encode(value);
		}
		fw.writeln(value);
	}

	public String writeValue(Object value) {
		return value.toString();
	}

}
