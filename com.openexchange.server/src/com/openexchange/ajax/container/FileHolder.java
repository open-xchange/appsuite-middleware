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

package com.openexchange.ajax.container;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.fileholder.ByteArrayRandomAccess;
import com.openexchange.ajax.fileholder.FileRandomAccess;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileHolder} - The basic {@link IFileHolder} implementation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added some JavaDoc comments
 */
public class FileHolder implements IFileHolder {

    private static final Field bufField;
    private static final Field markField;
    static {
        Field f;
        try {
            f = ByteArrayInputStream.class.getDeclaredField("buf");
            f.setAccessible(true);
        } catch (@SuppressWarnings("unused") Exception e) {
            f = null;
        }
        bufField = f;
        try {
            f = ByteArrayInputStream.class.getDeclaredField("mark");
            f.setAccessible(true);
        } catch (@SuppressWarnings("unused") Exception e) {
            f = null;
        }
        markField = f;
    }

    private static byte[] bytesFrom(ByteArrayInputStream bais) {
        if (null == bais) {
            return null;
        }
        try {
            Field bufField = FileHolder.bufField;
            Field markfield = FileHolder.markField;
            if (null != bufField && null != markfield) {
                byte[] buf = (byte[]) bufField.get(bais);
                int mark = markfield.getInt(bais);
                if (mark <= 0) {
                    return buf;
                }
                int len = buf.length - mark;
                if (len <= 0) {
                    return null;
                }
                byte [] ret = new byte[len];
                System.arraycopy(buf, mark, ret, 0, len);
                return ret;
            }
        } catch (@SuppressWarnings("unused") Exception e) {
            // Ignore
        }
        return null;
    }

    private static final class FileInputStreamClosure implements InputStreamClosure {

        final File file;

        FileInputStreamClosure(File file) {
            super();
            this.file = file;
        }

        @Override
        public InputStream newStream() throws IOException {
            return new FileInputStream(file);
        }
    }

    /**
     * Generates a new {@link InputStreamClosure} for specified file.
     *
     * @param file The file
     * @return The {@link InputStreamClosure} instance
     */
    public static InputStreamClosure newClosureFor(final File file) {
        return null == file ? null : new FileInputStreamClosure(file);
    }

    // --------------------------------------------------------------------------------- //

    private final Closeable closeable;
    private InputStreamClosure isClosure;
    private InputStream is;
    private long length;
    private String contentType;
    private String name;
    private String disposition;
    private String delivery;

    private File file;
    private byte[] bytes;
    private RandomAccessClosure rac;
    private final List<Runnable> tasks;

    /**
     * Initializes a new {@link FileHolder}.
     *
     * @param is The input stream
     * @param length The stream length
     * @param contentType The stream's MIME type
     * @param name The stream's resource name
     */
    public FileHolder(InputStream is, long length, String contentType, String name) {
        super();
        this.is = is;
        this.length = length;
        this.contentType = contentType;
        this.name = name;
        tasks = new LinkedList<Runnable>();
        file = null;
        closeable = null;

        if (is instanceof ByteArrayInputStream) {
            this.bytes = bytesFrom((ByteArrayInputStream) is);
            if (null != this.bytes) {
                isClosure = new ByteArrayInputStreamClosure(bytes);
                this.length = bytes.length;
            }
        } else {
            this.bytes = null;
        }
    }

    /**
     * Initializes a new {@link FileHolder}.
     *
     * @param isClosure The input stream closure
     * @param length The stream length
     * @param contentType The stream's MIME type
     * @param name The stream's resource name
     */
    public FileHolder(final InputStreamClosure isClosure, final long length, final String contentType, final String name) {
        super();
        this.isClosure = isClosure;
        this.closeable = (isClosure instanceof Closeable) ? (Closeable) isClosure : null;
        this.length = length;
        this.contentType = contentType;
        this.name = name;
        tasks = new LinkedList<Runnable>();

        if (isClosure instanceof FileInputStreamClosure) {
            file = ((FileInputStreamClosure) isClosure).file;
            bytes = null;
        } else if (isClosure instanceof ByteArrayInputStreamClosure) {
            file = null;
            bytes = ((ByteArrayInputStreamClosure) isClosure).bytes;
        } else {
            file = null;
            bytes = null;
        }
    }

    /**
     * Initializes a new {@link FileHolder}.
     *
     * @param file The associated file
     */
    public FileHolder(final File file) {
        this(file, null);
    }

    /**
     * Initializes a new {@link FileHolder}.
     *
     * @param file The file
     * @param contentType The file's MIME type
     */
    public FileHolder(final File file, final String contentType) {
        super();
        this.length = file.length();
        if (contentType == null){
            this.contentType = MimeType2ExtMap.getContentType(file);
        } else {
            this.contentType = contentType;
        }
        this.name = file.getName();
        tasks = new LinkedList<Runnable>();
        this.isClosure = new InputStreamClosure() {

            @Override
            public InputStream newStream() throws OXException, IOException {
                return new FileInputStream(file);
            }
        };
        this.file = file;
        bytes = null;
        closeable = null;
    }

    @Override
    public List<Runnable> getPostProcessingTasks() {
        return tasks;
    }

    @Override
    public void addPostProcessingTask(Runnable task) {
        if (null != task) {
            tasks.add(task);
        }
    }

    /**
     * Sets the random access closure.
     *
     * @param rac The random access closure to set
     * @return This instance
     */
    public FileHolder setRandomAccessClosure(RandomAccessClosure rac) {
        this.rac = rac;
        return this;
    }

    @Override
    public boolean repetitive() {
        if (null != isClosure) {
            return true;
        }
        if (is instanceof ByteArrayInputStream) {
            byte[] bytes = bytesFrom((ByteArrayInputStream) is);
            if (null != bytes) {
                isClosure = new ByteArrayInputStreamClosure(bytes);
                is = null;
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        Streams.close(closeable);
    }

    @Override
    public InputStream getStream() throws OXException {
        final InputStreamClosure isClosure = this.isClosure;
        if (null != isClosure) {
            try {
                return isClosure.newStream();
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
        // Return stream directly
        return is;
    }

    @Override
    public RandomAccess getRandomAccess() throws OXException {
        // Check random access
        RandomAccessClosure raf = this.rac;
        if (null != raf) {
            try {
                return raf.newRandomAccess();
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }

        // Check file
        File file = this.file;
        if (null != file) {
            try {
                return new FileRandomAccess(file);
            } catch (FileNotFoundException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }

        // Check bytes
        byte[] bytes = this.bytes;
        if (null != bytes) {
            return new ByteArrayRandomAccess(bytes);
        }

        // No random access support
        return null;
    }

    /**
     * Sets the input stream
     *
     * @param is The input stream
     */
    public void setStream(final InputStream is) {
        Streams.close(this.is);
        this.is = is;
        this.isClosure = null;
        file = null;
        rac = null;
        length = -1L;
        if (is instanceof ByteArrayInputStream) {
            this.bytes = bytesFrom((ByteArrayInputStream) is);
            if (bytes != null) {
                isClosure = new ByteArrayInputStreamClosure(bytes);
                length = bytes.length;
            }
        } else {
            this.bytes = null;
        }
    }

    @Override
    public long getLength() {
        return length;
    }

    /**
     * Sets the stream length
     *
     * @param length The length
     */
    public void setLength(final long length) {
        this.length = length;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets stream's MIME type.
     *
     * @param contentType The MIME type
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets stream's resource name.
     *
     * @param name The resource name
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    /**
     * Sets the disposition.
     *
     * @param disposition The disposition
     */
    public void setDisposition(final String disposition) {
        this.disposition = disposition;
    }

    /**
     * Sets the delivery
     *
     * @param delivery The delivery to set
     */
    public void setDelivery(final String delivery) {
        this.delivery = delivery;
    }

    @Override
    public String getDelivery() {
        return delivery;
    }

}
