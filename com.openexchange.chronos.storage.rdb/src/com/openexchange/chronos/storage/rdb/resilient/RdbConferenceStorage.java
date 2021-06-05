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

package com.openexchange.chronos.storage.rdb.resilient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.storage.ConferenceStorage;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbConferenceStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class RdbConferenceStorage extends RdbResilientStorage implements ConferenceStorage {

    private final ConferenceStorage delegate;

    /**
     * Initializes a new {@link RdbConferenceStorage}.
     *
     * @param services A service lookup reference
     * @param delegate The delegate storage
     * @param handleTruncations <code>true</code> to automatically handle data truncation warnings, <code>false</code>, otherwise
     * @param handleIncorrectStrings <code>true</code> to automatically handle incorrect string warnings, <code>false</code>, otherwise
     * @param unsupportedDataThreshold The threshold defining up to which severity unsupported data errors can be ignored, or <code>null</code> to not ignore any
     *            unsupported data error at all
     */
    public RdbConferenceStorage(ServiceLookup services, ConferenceStorage delegate, boolean handleTruncations, boolean handleIncorrectStrings, ProblemSeverity unsupportedDataThreshold) {
        super(services, handleTruncations, handleIncorrectStrings);
        this.delegate = delegate;
        setUnsupportedDataThreshold(unsupportedDataThreshold, delegate);
    }

    @Override
    public int nextId() throws OXException {
        return delegate.nextId();
    }

    @Override
    public List<Conference> loadConferences(String eventId) throws OXException {
        return delegate.loadConferences(eventId);
    }

    @Override
    public Map<String, List<Conference>> loadConferences(String[] eventIds) throws OXException {
        return delegate.loadConferences(eventIds);
    }

    @Override
    public Set<String> hasConferences(String[] eventIds) throws OXException {
        return delegate.hasConferences(eventIds);
    }

    @Override
    public void deleteConferences(String eventId) throws OXException {
        delegate.deleteConferences(eventId);
    }

    @Override
    public void deleteConferences(List<String> eventIds) throws OXException {
        delegate.deleteConferences(eventIds);
    }

    @Override
    public void deleteConferences(String eventId, int[] conferencesIds) throws OXException {
        delegate.deleteConferences(eventId, conferencesIds);
    }

    @Override
    public boolean deleteAllConferences() throws OXException {
        return delegate.deleteAllConferences();
    }

    @Override
    public void insertConferences(String eventId, List<Conference> conferences) throws OXException {
        runWithRetries(() -> delegate.insertConferences(eventId, conferences), f -> handleObjects(eventId, conferences, f));
    }

    @Override
    public void updateConferences(String eventId, List<Conference> conferences) throws OXException {
        runWithRetries(() -> delegate.updateConferences(eventId, conferences), f -> handleObjects(eventId, conferences, f));
    }

}
