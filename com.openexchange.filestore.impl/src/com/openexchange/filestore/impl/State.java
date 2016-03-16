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

package com.openexchange.filestore.impl;

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
import com.openexchange.java.UnsynchronizedByteArrayOutputStream;

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
     * Creates a state object from the inputstream.
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
            depth = Integer.parseInt(reader.readLine());
            entries = Integer.parseInt(reader.readLine());
            nextEntry = reader.readLine();
            unused = new HashSet<String>();
            String line = reader.readLine();
            while (line != null) {
                unused.add(line);
                line = reader.readLine();
            }
        } catch (final UnsupportedEncodingException e) {
            throw FileStorageCodes.ENCODING.create(e);
        } catch (final NumberFormatException e) {
            throw FileStorageCodes.NO_NUMBER.create(e);
        } catch (final IOException e) {
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
        BufferedWriter writer = null;
        try {
            final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(baos, "ISO-8859-1"));
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
            writer.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (final UnsupportedEncodingException e) {
            throw FileStorageCodes.ENCODING.create(e);
        } catch (final IOException e) {
            throw FileStorageCodes.IOERROR.create(e, e.getMessage());
        } finally {
            Streams.close(writer);
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
