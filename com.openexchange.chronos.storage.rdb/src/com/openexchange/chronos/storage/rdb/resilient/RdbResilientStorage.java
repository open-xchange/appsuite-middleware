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

package com.openexchange.chronos.storage.rdb.resilient;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.storage.rdb.CalendarStorageWarnings;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.server.ServiceLookup;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.function.CheckedRunnable;
import net.jodah.failsafe.function.Predicate;

/**
 * {@link RdbResilientStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class RdbResilientStorage extends CalendarStorageWarnings {

    private static final int MAX_RETRIES = 50;

    protected final ServiceLookup services;
    protected final boolean handleTruncations;
    protected final boolean handleIncorrectStrings;

    /**
     * Initializes a new {@link RdbResilientStorage}.
     *
     * @param services A service lookup reference
     * @param handleTruncations <code>true</code> to automatically handle data truncation warnings, <code>false</code>, otherwise
     * @param handleIncorrectStrings <code>true</code> to automatically handle incorrect string warnings, <code>false</code>, otherwise
     */
    protected RdbResilientStorage(ServiceLookup services, boolean handleTruncations, boolean handleIncorrectStrings) {
        super();
        this.services = services;
        this.handleIncorrectStrings = handleIncorrectStrings;
        this.handleTruncations = handleTruncations;
    }

    /**
     * Configures the severity threshold defining which unsupported data errors can be ignored.
     *
     * @param severityThreshold The threshold defining up to which severity unsupported data errors can be ignored, or
     *            <code>null</code> to not ignore any unsupported data error at all
     * @param delegate The delegate storage
     */
    protected void setUnsupportedDataThreshold(ProblemSeverity severityThreshold, Object delegate) {
        super.setUnsupportedDataThreshold(severityThreshold);
        if (CalendarStorageWarnings.class.isInstance(delegate)) {
            ((CalendarStorageWarnings) delegate).setUnsupportedDataThreshold(severityThreshold);
        }
    }

    /**
     * Executes a runnable with a suitable retry policy and failure predicate to determine if the operation should be retried.
     *
     * @param runnable The runnable to perform
     * @param failurePredicate The failure predicate to decide whether the operation should be retried or not
     */
    protected void runWithRetries(CheckedRunnable runnable, Predicate<? extends Throwable> failurePredicate) throws OXException {
        try {
            Failsafe.with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(failurePredicate)).run(runnable);
        } catch (FailsafeException e) {
            if (OXException.class.isInstance(e.getCause())) {
                throw (OXException) e.getCause();
            }
        } catch (UnsupportedOperationException e) {
            if (false == handleUnsupportedDataError(e)) {
                throw e;
            }
        }
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param objectsPerEventId The objects being stored, mapped to the identifier of the event they're stored for
     * @param failure The exception
     * @return <code>true</code> if the data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    protected <O> boolean handleObjectsPerEventId(Map<String, ? extends Collection<O>> objectsPerEventId, Throwable failure) {
        if (null != objectsPerEventId) {
            for (Entry<String, ? extends Collection<O>> entry : objectsPerEventId.entrySet()) {
                if (handleObjects(entry.getKey(), entry.getValue(), failure)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param eventId The identifier of the event where data is stored for
     * @param objects The objects being stored
     * @param failure The exception
     * @return <code>true</code> if the data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    protected <O> boolean handleObjects(String eventId, Collection<O> objects, Throwable failure) {
        if (null != objects) {
            for (O object : objects) {
                if (handle(eventId, object, failure)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param eventId The identifier of the event where data is stored for
     * @param mappedObjects The objects being stored, mapped to an arbitrary key
     * @param failure The exception
     * @return <code>true</code> if the data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    protected <O> boolean handleMappedObjects(String eventId, Map<?, ? extends Collection<O>> mappedObjects, Throwable failure) {
        if (null != mappedObjects) {
            for (Collection<O> objects : mappedObjects.values()) {
                if (handleObjects(eventId, objects, failure)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param eventId The identifier of the event where data is stored for
     * @param object The object being stored
     * @param failure The exception
     * @return <code>true</code> if the data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    protected <O> boolean handle(String eventId, O object, Throwable failure) {
        if (false == OXException.class.isInstance(failure)) {
            return false;
        }
        OXException e = (OXException) failure;
        try {
            switch (e.getErrorCode()) {
                case "CAL-5071": // Incorrect string [string %1$s, field %2$s, column %3$s]
                case "RDB-0002": // An SQL error cause by an illegal or unsupported character string: ...
                    return handleIncorrectStrings && handleIncorrectString(eventId, object, e);
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return handleTruncations && handleTruncation(eventId, object, e);
                default:
                    return false;
            }
        } catch (Exception x) {
            LOG.warn("Unexpected error during automatic handling of {}", e.getErrorCode(), x);
            addWarning(eventId, CalendarExceptionCodes.UNEXPECTED_ERROR.create(x, x.getMessage()));
            return false;
        }
    }

    private <O> boolean handleIncorrectString(String eventId, O object, OXException e) throws OXException {
        LOG.debug("Incorrect string detected while storing calendar data, replacing problematic characters and trying again.", e);
        if (MappedIncorrectString.replace(e.getProblematics(), object, "")) {
            addWarning(eventId, e);
            return true;
        }
        return false;
    }

    private <O> boolean handleTruncation(String eventId, O object, OXException e) throws OXException {
        LOG.debug("Data truncation detected while storing calendar data, trimming problematic fields and trying again.");
        if (MappedTruncation.truncate(e.getProblematics(), object)) {
            addWarning(eventId, e);
            return true;
        }
        return false;
    }

    /**
     * Tries to handle an {@link UnsupportedOperationException} caused by an {@link CalendarExceptionCodes#UNSUPPORTED_DATA} error. In case
     * the error could be handled, an appropriate warning is tracked (up to the configured problem severity), otherwise, the error is
     * raised.
     * 
     * @param e The unsupported operation exception to handle
     * @return <code>true</code> if handled, <code>false</code>, otherwise
     */
    private boolean handleUnsupportedDataError(UnsupportedOperationException e) throws OXException {
        if (OXException.class.isInstance(e.getCause())) {
            OXException cause = (OXException) e.getCause();
            if (CalendarExceptionCodes.UNSUPPORTED_DATA.equals(cause)) {
                ProblemSeverity severity = (ProblemSeverity) cause.getArgument("severity");
                String eventId = (String) cause.getArgument("eventId");
                EventField field = (EventField) cause.getArgument("field");
                String message = (String) cause.getArgument("message");
                addUnsupportedDataError(eventId, field, severity, message, cause.getCause());
                return true;
            }
        }
        return false;
    }

}
