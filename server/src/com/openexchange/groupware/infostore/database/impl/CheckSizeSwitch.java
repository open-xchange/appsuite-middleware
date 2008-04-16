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

package com.openexchange.groupware.infostore.database.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.infostore.Classes;

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_CHECKSIZESWITCH,
		component=EnumComponent.INFOSTORE
)
public class CheckSizeSwitch implements MetadataSwitcher {
	
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(CheckSizeSwitch.class);
	
	@OXThrows(
			category = Category.TRUNCATED,
			desc = "The User entered values that are to long for the database schema.",
			exceptionId = 0,
			msg = "Some fields have values, that are too long"
	)
	public static void checkSizes(final DocumentMetadata metadata) throws OXException {
		boolean error = false;
		
		final CheckSizeSwitch checkSize = new CheckSizeSwitch();
		final GetSwitch get = new GetSwitch(metadata);
		
		final List<Metadata> tooLongData = new ArrayList<Metadata>();
		for(final Metadata m : Metadata.VALUES) {
			checkSize.setValue(m.doSwitch(get));
			if (!((Boolean)m.doSwitch(checkSize)).booleanValue()) {
				tooLongData.add(m);
				error = true;
			}
		}
		
		if(error) {
			
			final OXException x = EXCEPTIONS.create(0);
			for(final Metadata m : tooLongData) {
				x.addTruncatedId(m.getId());
			}
			throw x;
		}
	}
	
	private int length;
	
	public void setValue(final Object value) {
		length = 0;
		
		if (value!= null && value instanceof String) {
			final String s = (String) value;
			try {
				length = s.getBytes("UTF-8").length;
			} catch (final UnsupportedEncodingException e) {
				length = s.length();
			}
		}
	}
	
	public Object categories() {
		return Boolean.valueOf(length < 255);
	}

	public Object colorLabel() {
		return Boolean.TRUE;
	}

	public Object content() {
		return Boolean.TRUE;
	}

	public Object createdBy() {
		return Boolean.TRUE;
	}

	public Object creationDate() {
		return Boolean.TRUE;
	}

	public Object currentVersion() {
		return Boolean.TRUE;
	}

	public Object description() {
		return Boolean.TRUE;
	}

	public Object fileMD5Sum() {
		return Boolean.TRUE;
	}

	public Object fileMIMEType() {
		return Boolean.valueOf(length <= 255);
	}

	public Object fileName() {
		return Boolean.valueOf(length <= 255);
	}

	public Object fileSize() {
		return Boolean.TRUE;
	}

	public Object folderId() {
		return Boolean.TRUE;
	}

	public Object id() {
		return Boolean.TRUE;
	}

	public Object lastModified() {
		return Boolean.TRUE;
	}

	public Object lockedUntil() {
		return Boolean.TRUE;
	}

	public Object modifiedBy() {
		return Boolean.TRUE;
	}

	public Object sequenceNumber() {
		return Boolean.TRUE;
	}

	public Object title() {
		return Boolean.valueOf(length <= 128);
	}

	public Object url() {
		return Boolean.valueOf(length <= 256);
	}

	public Object version() {
		return Boolean.TRUE;
	}

	public Object versionComment() {
		return Boolean.TRUE;
	}

	public Object filestoreLocation() {
		return Boolean.TRUE;
	}

}
