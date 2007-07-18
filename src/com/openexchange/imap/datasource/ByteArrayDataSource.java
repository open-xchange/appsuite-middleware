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



package com.openexchange.imap.datasource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * ByteArrayDataSource
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class ByteArrayDataSource implements DataSource {
	
	private String contentType;
	
	private String name;
	
	private byte[] bytes;
	
	private ByteArrayOutputStream baos;

	public ByteArrayDataSource() {
		super();
		contentType = "text/plain";
		name = null;
		bytes = null;
	}

	public ByteArrayDataSource(String contentType, String name, byte[] bytes) {
		this.contentType = contentType;
		this.name = name;
		this.bytes = bytes;
	}
	
	/**
	 * Set <code>byte[]</code> to read from.
	 * 
	 * @param bytes
	 */
	public void setByteArray(final byte[] bytes) {
		this.bytes = bytes;
	}

	/**
	 * Get written <code>byte[]</code>
	 * 
	 * @return
	 */
	public byte[] getByteArray() {
		return baos.toByteArray();
	}

	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public InputStream getInputStream() throws IOException {
		if (bytes == null) {
			throw new IOException("No data");
		}
		return new ByteArrayInputStream(bytes);
	}

	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("getOutputStream() not supported by this DataSource");
	}

}
