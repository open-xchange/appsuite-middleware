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

package com.openexchange.ajax.writer;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;

public class InfostoreWriter extends TimedWriter<DocumentMetadata> {

	public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreWriter.class);

	public InfostoreWriter(final JSONWriter w) {
		super(w);
	}

	public void writeMetadata(final SearchIterator<DocumentMetadata> iter, final Metadata[] cols, final TimeZone tz) throws JSONException, OXException {
		jsonWriter.array();

		fillArray(iter,cols,tz);
		jsonWriter.endArray();

	}


	@Override
	protected void fillArray(final SearchIterator<DocumentMetadata> iter, final Object[] cols, final TimeZone tz) throws JSONException, OXException {
		final WriterSwitch sw = new WriterSwitch(jsonWriter, tz);

		//The array contains one array for every DocumentMetadata, and filled according to the requested columns

		while (iter.hasNext()) {
			sw.setDocumentMetadata(iter.next());
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

	public void writeLimited(final DocumentMetadata dm, final Metadata[] fields, final TimeZone tz) throws JSONException {
        jsonWriter.object();

        final WriterSwitch w = new WriterSwitch(jsonWriter, tz);
        w.setDocumentMetadata(dm);
        for(final Metadata metadata : fields) {
            w.setMetadata(metadata);
            metadata.doSwitch(w);
        }
        jsonWriter.endObject();
    }

	private static final class WriterSwitch implements MetadataSwitcher{

		private DocumentMetadata dm;
		private final JSONWriter writer;
		private final TimeZone tz;

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
				LOG.error("",e);
			}
		}

		@Override
		public Object meta() {
		    final Map<String, Object> meta = dm.getMeta();
		    if (null != meta && !meta.isEmpty()) {
		        try {
                    writer.value(new JSONObject(meta));
                } catch (final JSONException e) {
                    LOG.error("",e);
                }
            } else {
                writeNull();
            }
		    return null;
		}

		@Override
        public Object lastModified() {
			writeDate(dm.getLastModified());
			return null;
		}

		@Override
        public Object creationDate() {
			writeDate(dm.getCreationDate());
			return null;
		}

		@Override
        public Object modifiedBy() {
			writeId(dm.getModifiedBy());
			return null;
		}

		@Override
        public Object folderId() {
			writeId(dm.getFolderId());
			return null;
		}

		@Override
        public Object title() {
			writeString(dm.getTitle());
			return null;
		}

		@Override
        public Object version() {
			writeInteger(dm.getVersion());
			return null;
		}

		@Override
        public Object content() {
			writeString(dm.getContent());
			return null;
		}

		@Override
        public Object id() {
			writeId(dm.getId());
			return null;
		}

		@Override
        public Object fileSize() {
			writeInteger(dm.getFileSize());
			return null;
		}

		@Override
        public Object description() {
			writeString(dm.getDescription());
			return null;
		}

		@Override
        public Object url() {
			writeString(dm.getURL());
			return null;
		}

		@Override
        public Object createdBy() {
			writeId(dm.getCreatedBy());
			return null;
		}

		@Override
        public Object fileName() {
			writeString(dm.getFileName());
			return null;
		}

		@Override
        public Object fileMIMEType() {
			writeString(dm.getFileMIMEType());
			return null;
		}

		@Override
        public Object sequenceNumber() {
			return null;

		}

		@Override
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
			final String[] categoriesArray = Strings.splitByComma(categoriesString);

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

		@Override
        public Object versionComment() {
			writeString(dm.getVersionComment());
			return null;
		}

		@Override
        public Object currentVersion() {
			writeBoolean(dm.isCurrentVersion());
			return null;
		}

		@Override
        public Object colorLabel() {
			writeInteger(dm.getColorLabel());
			return null;
		}

		@Override
        public Object lockedUntil() {
			if(dm.getLockedUntil() != null && dm.getLockedUntil().getTime()>System.currentTimeMillis()) {
				writeDate(dm.getLockedUntil());
			} else {
				writeInteger(0);
			}
			return null;
		}

		@Override
        public Object fileMD5Sum() {
			writeString(dm.getFileMD5Sum());
			return null;
		}

        @Override
        public Object objectPermissions() {
            List<ObjectPermission> objectPermissions = dm.getObjectPermissions();
            if (null == objectPermissions) {
                writeNull();
            } else {
                try {
                    writer.array();
                    for (ObjectPermission objectPermission : objectPermissions) {
                        writer.object();
                        writer.key("entity");
                        writeInteger(objectPermission.getEntity());
                        writer.key("group");
                        writeBoolean(objectPermission.isGroup());
                        writer.key("bits");
                        writeInteger(objectPermission.getPermissions());
                        writer.endObject();
                    }
                    writer.endArray();
                } catch (JSONException e) {
                    LOG.debug("", e);
                }
            }
            return null;
        }

		private void writeDate(final Date date) {
			if (date == null) {
                writeNull();
            } else {
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
			writeString(Long.toString(id));
		}

		private void writeString(final String string) {
            if(string == null) {
                writeNull();
                return;
            }
            try {
				writer.value(string);
			} catch (final JSONException e) {
				LOG.error("",e);
			}
		}

		private void writeInteger(final long l) {
			try {
				writer.value(l);
			} catch (final JSONException e) {
				LOG.error("",e);
			}
		}

        private void writeNull() {
            try {
                writer.value(null);
            } catch (final JSONException e) {
                LOG.error("",e);
            }
        }

        private void writeBoolean(final boolean b) {
			try {
				writer.value(b);
			} catch (final JSONException e) {
				LOG.error("",e);
			}
		}

		@Override
        public Object filestoreLocation() {
			writeString(dm.getFilestoreLocation());
			return null;
		}

        @Override
        public Object lastModifiedUTC() {
            if(dm.getLastModified() == null) {
                writeNull();
                return null;
            }
            writeInteger(dm.getLastModified().getTime());
            return null;
        }

        @Override
        public Object numberOfVersions() {
            writeInteger(dm.getNumberOfVersions());
            return null;
        }

        @Override
        public Object shareable() {
            writeBoolean(dm.isShareable());
            return null;
        }
    }

	@Override
	protected int getId(final Object object) {
		return ((DocumentMetadata)object).getId();
	}

}
