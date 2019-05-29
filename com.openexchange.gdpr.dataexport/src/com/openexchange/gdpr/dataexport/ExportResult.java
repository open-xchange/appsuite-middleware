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

import java.util.Optional;
import org.json.JSONObject;

/**
 * {@link ExportResult} - Represents the result when calling {@link DataExportProvider#export(java.util.UUID, DataExportSink, java.util.Optional, DataExportTask) export()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ExportResult {

    /** The possible value for an export result */
    public static enum Value {
        /**
         * The export terminated prematurely for any reason.
         */
        INCOMPLETE,
        /**
         * The export has been interrupted manually.
         */
        INTERRUPTED,
        /**
         * The export has been aborted manually.
         */
        ABORTED,
        /**
         * The export completed.
         */
        COMPLETED;
    }

    private static final ExportResult RESULT_INCOMPLETE = new ExportResult(Value.INCOMPLETE, Optional.empty(), Optional.empty());
    private static final ExportResult RESULT_INTERRUPTED = new ExportResult(Value.INTERRUPTED, Optional.empty(), Optional.empty());
    private static final ExportResult RESULT_COMPLETED = new ExportResult(Value.COMPLETED, Optional.empty(), Optional.empty());
    private static final ExportResult RESULT_ABORTED = new ExportResult(Value.ABORTED, Optional.empty(), Optional.empty());

    /**
     * Gets the incomplete result.
     *
     * @param optionalSavePoint The optional save-point
     * @param incompleteReason The optional reason
     * @return The incomplete result
     */
    public static ExportResult incomplete(Optional<JSONObject> optionalSavePoint, Optional<Exception> incompleteReason) {
        return optionalSavePoint.isPresent() ? new ExportResult(Value.INCOMPLETE, optionalSavePoint, incompleteReason) : RESULT_INCOMPLETE;
    }

    /**
     * Gets the interrupted result.
     *
     * @return The interrupted result
     */
    public static ExportResult interrupted() {
        return RESULT_INTERRUPTED;
    }

    /**
     * Gets the completed result.
     *
     * @return The completed result
     */
    public static ExportResult completed() {
        return RESULT_COMPLETED;
    }

    /**
     * Gets the aborted result.
     *
     * @return The aborted result
     */
    public static ExportResult aborted() {
        return RESULT_ABORTED;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Value value;
    private final Optional<JSONObject> jSavePoint;
    private final Optional<Exception> incompleteReason;

    private ExportResult(Value value, Optional<JSONObject> jSavePoint, Optional<Exception> incompleteReason) {
        super();
        this.value = value;
        this.jSavePoint = jSavePoint;
        this.incompleteReason = incompleteReason;
    }

    /**
     * Gets the optional save-point.
     * <p>
     * Only available if this result signals {@link #isIncomplete() incomplete}.
     *
     * @return The optional save-point
     */
    public Optional<JSONObject> getSavePoint() {
        return jSavePoint;
    }

    /**
     * Gets the optional reason for incomplete result
     * <p>
     * Only available if this result signals {@link #isIncomplete() incomplete}.
     *
     * @return The optional incomplete reason
     */
    public Optional<Exception> getIncompleteReason() {
        return incompleteReason;
    }

    /**
     * Gets the value of the export result.
     *
     * @return The value
     */
    public Value getValue() {
        return value;
    }

    /**
     * Checks if export completed.
     *
     * @return <code>true</code> if completed; otherwise <code>false</code>
     */
    public boolean isCompleted() {
        return Value.COMPLETED == value;
    }

    /**
     * Checks if export was interrupted.
     *
     * @return <code>true</code> if interrupted; otherwise <code>false</code>
     */
    public boolean isInterrupted() {
        return Value.INTERRUPTED == value;
    }

    /**
     * Checks if export is incomplete.
     *
     * @return <code>true</code> if incomplete; otherwise <code>false</code>
     */
    public boolean isIncomplete() {
        return Value.INCOMPLETE == value;
    }

    /**
     * Checks if export was aborted.
     *
     * @return <code>true</code> if aborted; otherwise <code>false</code>
     */
    public boolean isAborted() {
        return Value.ABORTED == value;
    }

}
