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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import com.google.common.collect.ImmutableSet;

/**
 * {@link DataExportDiagnosticsReport} - A data export diagnostics report as a collection of messages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportDiagnosticsReport implements Iterable<Message> {

    /**
     * The immutable empty data export diagnostics report.
     */
    private static final DataExportDiagnosticsReport EMPTY_REPORT = new DataExportDiagnosticsReport(ImmutableSet.of(), DiagnosticsReportOptions.builder().build());

    /**
     * Gets the immutable empty data export diagnostics report.
     *
     * @return The immutable empty data export diagnostics report
     */
    public static DataExportDiagnosticsReport emptyReport() {
        return EMPTY_REPORT;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Set<Message> messages;
    private final DiagnosticsReportOptions reportOptions;

    /**
     * Initializes a new {@link DataExportDiagnosticsReport}.
     *
     * @param reportOptions The report options
     */
    public DataExportDiagnosticsReport(DiagnosticsReportOptions reportOptions) {
        this(new LinkedHashSet<Message>(), reportOptions);
    }

    /**
     * Initializes a new {@link DataExportDiagnosticsReport}.
     */
    private DataExportDiagnosticsReport(Set<Message> messages, DiagnosticsReportOptions reportOptions) {
        super();
        this.messages = messages;
        this.reportOptions = reportOptions;
    }

    /**
     * Initializes a new {@link DataExportDiagnosticsReport}.
     *
     * @param messages The initial messages
     * @param reportOptions The report options
     */
    public DataExportDiagnosticsReport(Collection<Message> messages, DiagnosticsReportOptions reportOptions) {
        super();
        this.messages = messages == null || messages.isEmpty() ? new LinkedHashSet<Message>() : new LinkedHashSet<Message>(messages);
        this.reportOptions = reportOptions;
    }

    /**
     * Checks whether "permission denied" errors should be added to diagnostics report or not.
     *
     * @return <code>true</code> to add "permission denied" errors; otherwise <code>false</code>
     */
    public boolean isConsiderPermissionDeniedErrors() {
        return reportOptions.isConsiderPermissionDeniedErrors();
    }

    /**
     * Gets the number of messages contained in this diagnostics report.
     *
     * @return The number of messages
     */
    public synchronized int size() {
        return messages.size();
    }

    /**
     * Checks whether there are no messages contained in this diagnostics report.
     *
     * @return <code>true</code> if empty; otherwise <code>false</code>
     */
    public synchronized boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Checks if specified message is contained in this diagnostics report.
     *
     * @param m The message that might be contained
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    public synchronized boolean contains(Message m) {
        return m == null ? false : messages.contains(m);
    }

    /**
     * Returns an iterator over a snapshot of current messages in this diagnostics report.
     *
     * @return An iterator over the current messages
     */
    @Override
    public synchronized Iterator<Message> iterator() {
        return new ArrayList<Message>(messages).iterator();
    }

    /**
     * Adds given message to this diagnostics report if it is not already present.
     *
     * @param m The message to add
     * @return <code>true</code> if this report did not already contain the specified message; otherwise <code>false</code>
     */
    public synchronized boolean add(Message m) {
        return m == null ? false : messages.add(m);
    }

    /**
     * Adds all of the messages in the specified collection to this diagnostics report if they're not already present.
     *
     * @param c The collection to add
     * @return <code>true</code> if this report changed as a result of the call; otherwise <code>false</code>
     */
    public synchronized boolean addAll(Collection<? extends Message> c) {
        return c == null ? false : messages.addAll(c);
    }

    /**
     * Adds all of the messages of the specified diagnostics report to this diagnostics report if they're not already present.
     *
     * @param other The other diagnostics report to add
     * @return <code>true</code> if this report changed as a result of the call; otherwise <code>false</code>
     */
    public synchronized boolean addAll(DataExportDiagnosticsReport other) {
        return other == null ? false : messages.addAll(other.messages);
    }

    /**
     * Removes all of the messages from this diagnostics report. The diagnostics report will be empty after this call returns.
     */
    public synchronized void clear() {
        messages.clear();
    }

    @Override
    public synchronized String toString() {
        return messages.toString();
    }

}
