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

import java.util.Date;
import java.util.Set;
import com.openexchange.groupware.calendar.CalendarDataObject;

public class AppointmentSearchObject extends CalendarSearchObject {

    private boolean excludeRecurringAppointments;
    private boolean excludeNonRecurringAppointments;
    private Date minimumEndDate;
    private Date maximumStartDate;
    private Set<String> locations;
    private Set<Integer> ownStatus;
    private Set<Set<String>> externalParticipants;
    private Set<Integer> folderIDs;
    private Set<Integer> resourceIDs;
    private boolean onlyPrivateAppointments;

	/**
	 * Initializes a new {@link AppointmentSearchObject}.
	 */
	public AppointmentSearchObject() {
		super();
	}

    /**
     * Gets a set of patterns that are matched against the "location" property of an appointment during search. Multiple pattern values are
     * used with a logical <code>AND</code> conjunction.
     *
     * @return The locations
     */
    public Set<String> getLocations() {
        return locations;
    }

    /**
     * Sets the patterns that are matched against the "location" property of an appointment during search. Multiple pattern values are used
     * with a logical <code>AND</code> conjunction.
     *
     * @param locations The locations to set
     */
    public void setLocations(Set<String> locations) {
        this.locations = locations;
    }

    /**
     * Gets the minimum end date, i.e. the timestamp from which appointments should be found. More precise, gets the lower inclusive
     * limit of the queried range. Only appointments which end on or after this date are matched by the search.
     *
     * @return The minimum (inclusive) end date, or <code>null</code> if not specified
     */
    public Date getMinimumEndDate() {
        return minimumEndDate;
    }

    /**
     * Sets the minimum end date, i.e. defines the timestamp from which appointments should be found. More precise, defines the lower
     * inclusive limit of the queried range. Only appointments which end on or after this date are matched by the search.
     *
     * @param minimumEndDate The minimum (inclusive) end date to set
     */
    public void setMinimumEndDate(Date minimumEndDate) {
        this.minimumEndDate = minimumEndDate;
    }

    /**
     * Gets the maximum start date, i.e. the timestamp until which appointments should be found.
     *
     * @return The maximum end date, or <code>null</code> if not specified
     */
    public Date getMaximumStartDate() {
        return maximumStartDate;
    }

    /**
     * Sets the maximum start date, i.e. defines the timestamp until which appointments should be found.
     *
     * @param maximumStartDate The maximum start date to set
     */
    public void setMaximumStartDate(Date maximumStartDate) {
        this.maximumStartDate = maximumStartDate;
    }

    /**
     * Gets the possible own status that are used for filtering. Multiple status values are used with a logical <code>OR</code>
     * conjunction.
     *
     * @return The possible status in a set, with each entry being one of the possible status identifiers
     *         {@link CalendarDataObject#NONE}, {@link CalendarDataObject#ACCEPT}, {@link CalendarDataObject#DECLINE},
     *         {@link CalendarDataObject#TENTATIVE}, or <code>null</code> if not specified
     */
    public Set<Integer> getOwnStatus() {
        return ownStatus;
    }

    /**
     * Sets the possible own status that are used for filtering. Multiple status values are used with a logical <code>OR</code>
     * conjunction.
     *
     * @param ownStatus The possible status to set, with each entry in the set being one of the possible status identifiers
     *                  {@link CalendarDataObject#NONE}, {@link CalendarDataObject#ACCEPT}, {@link CalendarDataObject#DECLINE},
     *                  {@link CalendarDataObject#TENTATIVE}, or <code>null</code> to reset
     */
    public void setOwnStatus(Set<Integer> ownStatus) {
        this.ownStatus = ownStatus;
    }

    /**
     * Gets a set of a set of e-mail addresses that are matched against the external participants of an appointment during search.
     * Each set contains all possible participants that are used with a logical <code>OR</code> conjunction. Each outer set is matched
     * using a logical <code>AND</code> conjunction.
     *
     * @return The external participants
     */
    public Set<Set<String>> getExternalParticipants() {
        return externalParticipants;
    }

    /**
     * Sets the set of sets of e-mail addresses that should be matched against the external participants of an appointment during search.
     * Each set contains all possible participants that are used with a logical <code>OR</code> conjunction. Each outer set is matched
     * using a logical <code>AND</code> conjunction.
     *
     * @param externalParticipants The external participants to set
     */
    public void setExternalParticipants(Set<Set<String>> externalParticipants) {
        this.externalParticipants = externalParticipants;
    }

    /**
     * Gets a value indicating whether recurring appointments should be excluded from the search or not.
     *
     * @return <code>true</code> if recurring appointments are excluded, <code>false</code>, otherwise
     */
    public boolean isExcludeRecurringAppointments() {
        return excludeRecurringAppointments;
    }

    /**
     * Configures if recurring appointments should be excluded from the search or not.
     *
     * @param excludeRecurringAppointments <code>true</code> to exclude recurring appointments, <code>false</code>, otherwise
     */
    public void setExcludeRecurringAppointments(boolean excludeRecurringAppointments) {
        this.excludeRecurringAppointments = excludeRecurringAppointments;
    }

    /**
     * Gets a value indicating whether non-recurring appointments should be excluded from the search or not.
     *
     * @return <code>true</code> if non-recurring appointments are excluded, <code>false</code>, otherwise
     */
    public boolean isExcludeNonRecurringAppointments() {
        return excludeNonRecurringAppointments;
    }

    /**
     * Configures if non-recurring appointments should be excluded from the search or not.
     *
     * @param excludeNonRecurringAppointments <code>true</code> to exclude non-recurring appointments, <code>false</code>, otherwise
     */
    public void setExcludeNonRecurringAppointments(boolean excludeNonRecurringAppointments) {
        this.excludeNonRecurringAppointments = excludeNonRecurringAppointments;
    }

    /**
     * Gets the folder IDs used to restrict the search to. Multiple identifiers are used with a logical <code>OR</code> conjunction. If
     * not specified, all visible calendar folders are used.
     *
     * @return The folder IDs
     */
    public Set<Integer> getFolderIDs() {
        return folderIDs;
    }

    /**
     * Sets the folder IDs used to restrict the search to. Multiple identifiers are used with a logical <code>OR</code> conjunction. If
     * not specified, all visible calendar folders are used.
     *
     * @param folderIDs The folder IDs to set
     */
    public void setFolderIDs(Set<Integer> folderIDs) {
        this.folderIDs = folderIDs;
    }

    /**
     * Gets a set of IDs of internal resources that are matched against the internal participants of an appointment during search. Multiple
     * identifiers are used with a logical <code>OR</code> conjunction.
     *
     * @return The resource IDs
     */
    public Set<Integer> getResourceIDs() {
        return resourceIDs;
    }

    /**
     * Sets the IDs of internal resources that should be matched against the internal participants of an appointment during search. Multiple
     * identifiers are used with a logical <code>OR</code> conjunction.
     *
     * @param resourceIDs The resource IDs to set
     */
    public void setResourceIDs(Set<Integer> resourceIDs) {
        this.resourceIDs = resourceIDs;
    }

    /**
     * Gets the private appointment limitation.
     * Determines if the search should ignore appointments from non-private folders.
     * 
     * @return
     */
    public boolean isOnlyPrivateAppointments() {
        return onlyPrivateAppointments;
    }

    /**
     * Sets the private appointment limitation.
     * Determines if the search should ignore appointments from non-private folders.
     * 
     * @param onlyPrivateAppointments
     */
    public void setOnlyPrivateAppointments(boolean onlyPrivateAppointments) {
        this.onlyPrivateAppointments = onlyPrivateAppointments;
    }
}
