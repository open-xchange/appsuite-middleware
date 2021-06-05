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

package com.openexchange.tools.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.java.Streams;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * This represents the state of the FileStorage. Only used and unused files are saved.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
class State {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(State.class);

    /**
     * Version long for interoperability.
     */
    private static final long serialVersionUID = 1064393846000L;

    /**
     * Number of entries in a level.
     */
    private final int entries;

    /**
     * Number of levels.
     */
    private final int depth;

    /**
     * Deleted entries that can be reused.
     */
    private final Set<String> unused;

    /**
     * Next empty entry in FileStorage.
     */
    private String nextEntry;

    /**
     * Instantiate the State object.
     *
     * @param entries number of entries in a level
     * @param depth number of levels
     * @param nextEntry next free entry.
     */
    State(final int depth, final int entries, final String nextEntry) {
        this.entries = entries;
        this.depth = depth;
        this.nextEntry = nextEntry;
        unused = new HashSet<String>();
    }

    /**
     * Creates a state object from the {@link InputStream}.
     *
     * @param input input stream to read the state file from.
     * @throws OXException if an input error occurs
     */
    State(final InputStream input) throws OXException {
        super();
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            isr = new InputStreamReader(input, com.openexchange.java.Charsets.ISO_8859_1);
            reader = new BufferedReader(isr);
            String line = reader.readLine();
            if (line == null) {
                throw FileStorageCodes.IOERROR.create("Unable to parse a State object from stream.");
            }
            depth = Integer.parseInt(line);
            line = reader.readLine();
            if (line == null) {
                throw FileStorageCodes.IOERROR.create("Unable to parse a State object from stream.");
            }
            entries = Integer.parseInt(line);
            nextEntry = reader.readLine();
            unused = new HashSet<String>();
            line = reader.readLine();
            while (line != null) {
                unused.add(line);
                line = reader.readLine();
            }
        } catch (UnsupportedEncodingException e) {
            throw FileStorageCodes.ENCODING.create(e);
        } catch (NumberFormatException e) {
            throw FileStorageCodes.NO_NUMBER.create(e);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader, isr);
        }
    }

    /**
     * Saves the state object.
     *
     * @return an inputstream from that the state file can be read.
     * @throws OXException if an error occurs.
     */
    InputStream saveState() throws OXException {
        try {
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
            final OutputStreamWriter osw = new OutputStreamWriter(baos, "ISO-8859-1");
            final BufferedWriter writer = new BufferedWriter(osw);
            writer.write(String.valueOf(depth));
            writer.newLine();
            writer.write(String.valueOf(entries));
            writer.newLine();
            writer.write(nextEntry);
            writer.newLine();
            final Iterator<String> iter = unused.iterator();
            while (iter.hasNext()) {
                writer.write(iter.next());
                writer.newLine();
            }
            writer.close();
            osw.close();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (UnsupportedEncodingException e) {
            throw FileStorageCodes.ENCODING.create(e);
        } catch (IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the next free entry in FileStorage.
     *
     * @return next free entry in filespool
     */
    public String getNextEntry() {
        return nextEntry;
    }

    /**
     * Sets the next free entry in FileStorage.
     *
     * @param nextEntry value for the next free entry
     */
    public void setNextEntry(final String nextEntry) {
        this.nextEntry = nextEntry;
    }

    /**
     * Adds an unused entry. Instead of writing the next file to the next entry, an unused entry should be used first.
     *
     * @param entry the entry to add
     */
    public void addUnused(final String entry) {
        unused.add(entry);
    }

    /**
     * Tests if there are unused slots.
     *
     * @return true if there are unused slots
     */
    public boolean hasUnused() {
        return !unused.isEmpty();
    }

    /**
     * Get an unused slot. The returned slot is removed from the unused slots.
     *
     * @return an unused slot or null if there are no.
     */
    public String getUnused() {
        String retval = null;
        if (!unused.isEmpty()) {
            final Iterator<String> iter = unused.iterator();
            retval = iter.next();
            iter.remove();
        }
        return retval;
    }
}
