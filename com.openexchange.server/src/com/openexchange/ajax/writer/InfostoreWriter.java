/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.groupware.infostore.InfostoreFolderPath;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * Writes a <code>DocumentMetadata</code> to its JSON representation
 *
 * @deprecated Only used for testing
 */
@Deprecated
public class InfostoreWriter extends TimedWriter<DocumentMetadata> {

    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreWriter.class);

    public InfostoreWriter(final JSONWriter w) {
        super(w);
    }

    public void writeMetadata(final SearchIterator<DocumentMetadata> iter, final Metadata[] cols, final TimeZone tz) throws JSONException, OXException {
        jsonWriter.array();

        fillArray(iter, cols, tz);
        jsonWriter.endArray();

    }

    @Override
    protected void fillArray(final SearchIterator<DocumentMetadata> iter, final Object[] cols, final TimeZone tz) throws JSONException, OXException {
        final WriterSwitch sw = new WriterSwitch(jsonWriter, tz);

        //The array contains one array for every DocumentMetadata, and filled according to the requested columns

        while (iter.hasNext()) {
            sw.setDocumentMetadata(iter.next());
            jsonWriter.array();
            for (final Metadata column : (Metadata[]) cols) {
                column.doSwitch(sw);
            }
            jsonWriter.endArray();
        }
    }

    public void write(final DocumentMetadata dm, final TimeZone tz) throws JSONException {
        jsonWriter.object();

        final WriterSwitch w = new WriterSwitch(jsonWriter, tz).setDocumentMetadata(dm);
        for (Metadata metadata : Metadata.HTTPAPI_VALUES) {
            w.setMetadata(metadata);
            metadata.doSwitch(w);
        }
        jsonWriter.endObject();
    }

    public void writeLimited(final DocumentMetadata dm, final Metadata[] fields, final TimeZone tz) throws JSONException {
        jsonWriter.object();

        final WriterSwitch w = new WriterSwitch(jsonWriter, tz).setDocumentMetadata(dm);
        for (Metadata metadata : fields) {
            w.setMetadata(metadata);
            metadata.doSwitch(w);
        }
        jsonWriter.endObject();
    }

    private static final class WriterSwitch implements MetadataSwitcher {

        private DocumentMetadata dm;
        private final JSONWriter writer;
        private final TimeZone tz;

        /**
         * Initializes a new {@link WriterSwitch}.
         */
        WriterSwitch(JSONWriter writer, TimeZone tz) {
            super();
            this.writer = writer;
            this.tz = tz;
        }

        WriterSwitch setDocumentMetadata(final DocumentMetadata dm) {
            this.dm = dm;
            return this;
        }

        void setMetadata(final Metadata current) {
            try {
                writer.key(current.getName());
            } catch (JSONException e) {
                LOG.error("", e);
            }
        }

        @Override
        public Object meta() {
            final Map<String, Object> meta = dm.getMeta();
            if (null != meta && !meta.isEmpty()) {
                try {
                    writer.value(new JSONObject(meta));
                } catch (JSONException e) {
                    LOG.error("", e);
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
            if (categoriesString == null || categoriesString.equals("")) {
                try {
                    writer.array();
                    writer.endArray();
                } catch (JSONException e) {
                    LOG.debug("", e);
                }
                return null;
            }
            final String[] categoriesArray = Strings.splitByComma(categoriesString);

            try {
                writer.array();
                for (final String cat : categoriesArray) {
                    writer.value(cat);
                }
                writer.endArray();
            } catch (JSONException e) {
                LOG.debug("", e);
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
            if (dm.getLockedUntil() != null && dm.getLockedUntil().getTime() > System.currentTimeMillis()) {
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
                long time = date.getTime() + offset;
                // Happens on infinite locks.
                if (time < 0) {
                    time = Long.MAX_VALUE;
                }
                writeInteger(time);
            }
        }

        private void writeId(final long id) {
            writeString(Long.toString(id));
        }

        private void writeString(final String string) {
            if (string == null) {
                writeNull();
                return;
            }
            try {
                writer.value(string);
            } catch (JSONException e) {
                LOG.error("", e);
            }
        }

        private void writeInteger(final long l) {
            try {
                writer.value(l);
            } catch (JSONException e) {
                LOG.error("", e);
            }
        }

        private void writeNull() {
            try {
                writer.value(null);
            } catch (JSONException e) {
                LOG.error("", e);
            }
        }

        private void writeBoolean(final boolean b) {
            try {
                writer.value(b);
            } catch (JSONException e) {
                LOG.error("", e);
            }
        }

        @Override
        public Object filestoreLocation() {
            writeString(dm.getFilestoreLocation());
            return null;
        }

        @Override
        public Object lastModifiedUTC() {
            Date lastModified = dm.getLastModified();
            if (lastModified == null) {
                writeNull();
                return null;
            }
            writeInteger(lastModified.getTime());
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

        @Override
        public Object origin() {
            // This is a legacy class... Don't care
            InfostoreFolderPath originFolderPath = dm.getOriginFolderPath();
            if (null != originFolderPath) {
                writeString(originFolderPath.toString());
            }
            return null;
        }

        @Override
        public Object captureDate() {
            return null;
        }

        @Override
        public Object geolocation() {
            return null;
        }

        @Override
        public Object width() {
            return null;
        }

        @Override
        public Object height() {
            return null;
        }

        @Override
        public Object cameraMake() {
            return null;
        }

        @Override
        public Object cameraModel() {
            return null;
        }

        @Override
        public Object cameraIsoSpeed() {
            return null;
        }

        @Override
        public Object cameraAperture() {
            return null;
        }

        @Override
        public Object cameraExposureTime() {
            return null;
        }

        @Override
        public Object cameraFocalLength() {
            return null;
        }

        @Override
        public Object mediaMeta() {
            return null;
        }

        @Override
        public Object mediaStatus() {
            return null;
        }

        @Override
        public Object mediaDate() {
            return null;
        }
    }

    @Override
    protected int getId(final Object object) {
        return ((DocumentMetadata) object).getId();
    }

}
