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

package com.openexchange.groupware.search;

import java.util.Set;
import com.openexchange.groupware.container.Participant;

public class CalendarSearchObject extends SearchObject {

    public static final String NO_TITLE = null;

	private String title = NO_TITLE;

	private Participant[] participants;

	private boolean searchInNote;
	
	private Set<String> queries;
	
	private Set<String> titles;
	
	private Set<String> attachmentNames;
	
	private Set<String> notes;
	
	private Set<Integer> userIDs;
	
	private boolean hasInternalParticipants;
	
	private boolean hasExternalParticipants;

	/**
	 * Initializes a new CalendarSearchObject
	 */
	public CalendarSearchObject() {
		super();
	}

	public Participant[] getParticipants() {
		return participants;
	}

	public void setParticipants(final Participant[] participants) {
		this.participants = participants;
	}

	public boolean isSearchInNote() {
		return searchInNote;
	}

	public void setSearchInNote(final boolean searchInNote) {
		this.searchInNote = searchInNote;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}
	

    /**
     * Gets a set of general query patterns that are matched against all searchable properties of an appointment during search.
     * Multiple pattern values are used with a logical <code>AND</code> conjunction.<p/>
     *
     * @return The queries
     */
    public Set<String> getQueries() {
        return queries;
    }

    /**
     * Sets the general query patterns that are matched against all searchable properties of an appointment during search. Multiple
     * pattern values are used with a logical <code>AND</code> conjunction.
     *
     * @param queries The queries to set
     */
    public void setQueries(Set<String> queries) {
        this.queries = queries;
    }
    
    /**
     * Gets a set of patterns that are matched against the "title" property of an appointment during search. Multiple pattern values are
     * used with a logical <code>AND</code> conjunction.
     *
     * @return The titles
     */
    public Set<String> getTitles() {
        return titles;
    }

    /**
     * Sets the patterns that are matched against the "title" property of an appointment during search. Multiple pattern values are used
     * with a logical <code>AND</code> conjunction.
     *
     * @param titles The titles to set
     */
    public void setTitles(Set<String> titles) {
        this.titles = titles;
    }
    
    /**
     * Gets a set of attachment names that are matched against the appointment's attachments during search. Multiple pattern values are
     * used with a logical <code>AND</code> conjunction.
     *
     * @return The attachment names
     */
    public Set<String> getAttachmentNames() {
        return attachmentNames;
    }

    /**
     * Sets the patterns that are matched against the appointment's attachments during search. Multiple pattern values are used
     * with a logical <code>AND</code> conjunction.
     *
     * @param attachmentNames The attachment names to set
     */
    public void setAttachmentNames(Set<String> attachmentNames) {
        this.attachmentNames = attachmentNames;
    }
    
    /**
     * Gets a set of patterns that are matched against the "note" property of an appointment during search. Multiple pattern values are
     * used with a logical <code>AND</code> conjunction.
     *
     * @return The notes
     */
    public Set<String> getNotes() {
        return notes;
    }

    /**
     * Sets the patterns that are matched against the "note" property of an appointment during search. Multiple pattern values are used
     * with a logical <code>AND</code> conjunction.
     *
     * @param notes The notes to set
     */
    public void setNotes(Set<String> notes) {
        this.notes = notes;
    }
    
    /**
     * Gets a set of IDs of internal users that are matched against the internal participants of an appointment during search. Multiple
     * identifiers are used with a logical <code>OR</code> conjunction.
     *
     * @return The user IDs
     */
    public Set<Integer> getUserIDs() {
        return userIDs;
    }

    /**
     * Sets the IDs of internal users that should be matched against the internal participants of an appointment during search. Multiple
     * identifiers are used with a logical <code>OR</code> conjunction.
     *
     * @param userIDs The user IDs to set
     */
    public void setUserIDs(Set<Integer> userIDs) {
        this.userIDs = userIDs;
    }
    
    /**
     * Gets the hasInternalParticipants
     *
     * @return The hasInternalParticipants
     */
    public boolean hasInternalParticipants() {
        return hasInternalParticipants;
    }
    
    /**
     * Sets the hasInternalParticipants
     *
     * @param hasInternalParticipants The hasInternalParticipants to set
     */
    public void setHasInternalParticipants(boolean hasInternalParticipants) {
        this.hasInternalParticipants = hasInternalParticipants;
    }
    
    /**
     * Gets the hasExternalParticipants
     *
     * @return The hasExternalParticipants
     */
    public boolean hasExternalParticipants() {
        return hasExternalParticipants;
    }

    /**
     * Sets the hasExternalParticipants
     *
     * @param hasExternalParticipants The hasExternalParticipants to set
     */
    public void setHasExternalParticipants(boolean hasExternalParticipants) {
        this.hasExternalParticipants = hasExternalParticipants;
    }

    /**
     * Gets the hasParticipants
     *
     * @return The hasParticipants
     */
    public boolean hasParticipants() {
        return (hasInternalParticipants || hasExternalParticipants);
    }
}
