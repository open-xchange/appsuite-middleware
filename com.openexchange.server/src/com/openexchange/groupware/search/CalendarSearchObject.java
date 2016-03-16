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
