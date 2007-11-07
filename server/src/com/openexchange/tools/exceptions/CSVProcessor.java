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

package com.openexchange.tools.exceptions;

import java.io.*;

public class CSVProcessor implements OXErrorCodeProcessor {
	
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CSVProcessor.class);

	private PrintWriter out;
	
	public void done() {
		closeStream();
	}

	private void closeStream() {
		out.close();
	}

	public void process(final OXErrorCode errorCode) {
		openStream();
		write(errorCode);
	}

	private void write(final OXErrorCode errorCode) {
		out.print(quote((errorCode.component == null) ? "" : errorCode.component.getAbbreviation()));
		out.print(';');
        out.print(quote((errorCode.component == null) ? "" : errorCode.component.toString()));
        out.print(';');
		out.print(quote(String.valueOf(errorCode.category.getCode())));
		out.print(';');
        out.print(quote(errorCode.category.name()));
        out.print(';');
		out.print(quote(String.valueOf(errorCode.number)));
		out.print(";");
		out.print(quote(errorCode.message));
		out.print(";");
		out.print(quote(errorCode.description));
		out.print(";");
        out.print(quote(errorCode.clazz == null ? "" : errorCode.clazz.getName()));
        out.println(";");
		out.flush();
	}

	private String quote(final String s) {
		if(s == null) {
			return "";
		}
		return '"'+s.replaceAll("\\\"", "\\\"")+'"';
	}

	private void openStream() {
		if(out != null) {
			return;
		}
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("com.openexchange.tools.exceptions.CSVProcessor.file", "codes.csv"))));
			out.print("\"Component\"");
			out.print(';');
            out.print("\"Component Name\"");
            out.print(';');
			out.print("\"Category Code\"");
			out.print(';');
            out.print("\"Category String\"");
            out.print(';');
			out.print("\"Sequence No\"");
			out.print(";");
			out.print("\"Message\"");
			out.print(";");
			out.print("\"Description\"");
            out.print(";");
            out.print("\"Exception Class\"");
            out.println(";");
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

}
