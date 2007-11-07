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



package com.openexchange.ajax.writer;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONWriter;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

public class InfostoreWriter extends TimedWriter {
	
	public static final Log LOG = LogFactory.getLog(InfostoreWriter.class);
	
	public InfostoreWriter(final JSONWriter w) {
		super(w);
	}
	
	public void writeMetadata(final SearchIterator iter, final Metadata[] cols, final TimeZone tz) throws SearchIteratorException, JSONException, OXException {
		jsonWriter.array();
		
		fillArray(iter,cols,tz);
		jsonWriter.endArray();
		
	}
	
	
	@Override
	protected void fillArray(final SearchIterator iter, final Object[] cols, final TimeZone tz) throws SearchIteratorException, JSONException, OXException {
		final WriterSwitch sw = new WriterSwitch(jsonWriter, tz);
		
		//The array contains one array for every DocumentMetadata, and filled according to the requested columns
		
		while (iter.hasNext()) {
			sw.setDocumentMetadata((DocumentMetadata) iter.next());
			jsonWriter.array();
			for(final Metadata column : (Metadata[]) cols) {
				column.doSwitch(sw);
			}
			jsonWriter.endArray();
		}
	}

	public void write(final DocumentMetadata dm, final TimeZone tz) throws JSONException {
		jsonWriter.object();
		
		final WriterSwitch w = new WriterSwitch(jsonWriter, tz);
		w.setDocumentMetadata(dm);
		for(final Metadata metadata : Metadata.HTTPAPI_VALUES) {
			w.setMetadata(metadata);
			metadata.doSwitch(w);
		}
		jsonWriter.endObject();
	}
	
	private static final class WriterSwitch implements MetadataSwitcher{
		
		private DocumentMetadata dm;
		private JSONWriter writer;
		private TimeZone tz;
		
		public WriterSwitch(final JSONWriter writer, final TimeZone tz) {
			this.writer = writer;
			this.tz = tz;
		}
	
		public void setDocumentMetadata(final DocumentMetadata dm) {
			this.dm = dm;
		}
		
		public void setMetadata(final Metadata current) {
			try {
				writer.key(current.getName());
			} catch (final JSONException e) {
				LOG.debug("",e);
			}
		}
	
		public Object lastModified() {
			writeDate(dm.getLastModified());
			return null;
		}
	
		public Object creationDate() {
			writeDate(dm.getCreationDate());
			return null;
		}
	
		public Object modifiedBy() {
			writeId(dm.getModifiedBy());
			return null;
		}
	
		public Object folderId() {
			writeId(dm.getFolderId());
			return null;
		}
	
		public Object title() {
			writeString(dm.getTitle());
			return null;
		}
		
		public Object version() {
			writeInteger(dm.getVersion());
			return null;
		}
	
		public Object content() {
			writeString(dm.getContent());
			return null;
		}
	
		public Object id() {
			writeId(dm.getId());
			return null;
		}
	
		public Object fileSize() {
			writeInteger(dm.getFileSize());
			return null;
		}
	
		public Object description() {
			writeString(dm.getDescription());
			return null;
		}
	
		public Object url() {
			writeString(dm.getURL());
			return null;
		}
	
		public Object createdBy() {
			writeId(dm.getCreatedBy());
			return null;
		}
	
		public Object fileName() {
			writeString(dm.getFileName());
			return null;
		}
	
		public Object fileMIMEType() {
			writeString(dm.getFileMIMEType());
			return null;
		}
	
		public Object sequenceNumber() {
			return null;
			
		}
	
		public Object categories() {
			final String categoriesString = dm.getCategories();
			if(categoriesString==null || categoriesString.equals("")) {
				try {
					writer.array();
					writer.endArray();
				} catch (final JSONException e) {
					LOG.debug("",e);
				}
				return null;
			}
			final String[] categoriesArray = categoriesString.split("\\s*,\\s*");
			
			try {
				writer.array();
				for(final String cat : categoriesArray) {
					writer.value(cat);
				}
				writer.endArray();
			} catch (final JSONException e) {
				LOG.debug("",e);
			}
			return null;
		}

		public Object versionComment() {
			writeString(dm.getVersionComment());
			return null;
		}

		public Object currentVersion() {
			writeBoolean(dm.isCurrentVersion());
			return null;
		}

		public Object colorLabel() {
			writeInteger(dm.getColorLabel());
			return null;
		}

		public Object lockedUntil() {
			if(dm.getLockedUntil() != null && dm.getLockedUntil().getTime()>System.currentTimeMillis()) {
				writeDate(dm.getLockedUntil());
			} else {
				writeInteger(0);
			}
			return null;
		}
	
		public Object fileMD5Sum() {
			writeString(dm.getFileMD5Sum());
			return null;
		}
		
		private void writeDate(final Date date) {
			if(date != null) {
				final int offset = tz.getOffset(date.getTime());
				long time = date.getTime()+offset;
				// Happens on infinite locks.
				if(time < 0) {
					time = Long.MAX_VALUE;
				}
				writeInteger(time);
			}
		}
		
		private void writeId(final long id) {
			writeString(String.valueOf(id));
		}
	
		private void writeString(String string) {
			if(null == string) {
				string = "";
			}
			try {
				writer.value(string);
			} catch (final JSONException e) {
				LOG.debug("",e);
			}
		}
	
		private void writeInteger(final long l) {
			try {
				writer.value(l);
			} catch (final JSONException e) {
				LOG.debug("",e);
			}
		}
		
		private void writeBoolean(final boolean b) {
			try {
				writer.value(b);
			} catch (final JSONException e) {
				LOG.debug("",e);
			}
		}

		public Object filestoreLocation() {
			writeString(dm.getFilestoreLocation());
			return null;
		}
	}

	@Override
	protected int getId(final Object object) {
		return ((DocumentMetadata)object).getId();
	}
	
}
