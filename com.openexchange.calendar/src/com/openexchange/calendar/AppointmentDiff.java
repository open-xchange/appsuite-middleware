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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Change;
import com.openexchange.groupware.container.Differ;
import com.openexchange.groupware.container.Difference;

/**
 * An {@link AppointmentDiff} contains the update to an appointment
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AppointmentDiff {

    private static Map<Integer, Differ<? super Appointment>> specialDiffer = new HashMap<Integer, Differ<? super Appointment>>();

    private final List<FieldUpdate> updates;

    private final Set<String> differingFieldNames;

    static {
        for (final Differ<? super Appointment> differ : Appointment.differ) {
            specialDiffer.put(differ.getColumn(), differ);
        }
    }
    
    public static AppointmentDiff compare(final Appointment original, final Appointment update, final int...skip) {
        final Set<Integer> skipList = new HashSet<Integer>(skip.length);
        for (final int columnToSkip : skip) {
            skipList.add(columnToSkip);
        }
        final AppointmentDiff retval = new AppointmentDiff();
        
        
        for (final int column : Appointment.ALL_COLUMNS) {
            if (skipList.contains(column)) {
                continue;
            }
            if (specialDiffer.containsKey(column)) {
                final Difference difference = specialDiffer.get(column).getDifference(original, update);
                if (difference != null) {
                    final FieldUpdate fieldUpdate = retval.new FieldUpdate();
                    fieldUpdate.setFieldNumber(column);
                    fieldUpdate.setFieldName(CalendarField.getByColumn(column).getJsonName());
                    fieldUpdate.setOriginalValue(original.get(column));
                    fieldUpdate.setNewValue(update.get(column));
                    fieldUpdate.setExtraInfo(difference);
                    retval.addUpdate(fieldUpdate);
                }
            } else if (Differ.isDifferent(original, update, column)) {
                final FieldUpdate fieldUpdate = retval.new FieldUpdate();
                fieldUpdate.setFieldNumber(column);
                fieldUpdate.setFieldName(CalendarField.getByColumn(column).getJsonName());
                fieldUpdate.setOriginalValue(original.get(column));
                fieldUpdate.setNewValue(update.get(column));
                retval.addUpdate(fieldUpdate);
            }
        }

        return retval;
    }

    public AppointmentDiff() {
        updates = new ArrayList<FieldUpdate>();
        differingFieldNames = new HashSet<String>();
    }

    public Set<String> getDifferingFieldNames() {
        return differingFieldNames;
    }

    public List<FieldUpdate> getUpdates() {
        return updates;
    }
    
    public void addUpdate(final FieldUpdate fieldUpdate) {
        updates.add(fieldUpdate);
        differingFieldNames.add(fieldUpdate.getFieldName());
    }

    public boolean anyFieldChangedOf(final String...fields) {
        for (final String field : fields) {
            if (differingFieldNames.contains(field)) {
                return true;
            }
        }
        return false;
    }
    
	public boolean anyFieldChangedOf(Collection<String> fields) {
        for (String field : fields) {
            if (differingFieldNames.contains(field)) {
                return true;
            }
        }
        return false;
	}

    

	public boolean anyFieldChangedOf(final int...fields) {
		for (final int field : fields) {
			for (final FieldUpdate upd : updates) {
				if (upd.getFieldNumber() == field) {
					return true;
				}
			}
		}
		return false;
	}

    
    public boolean onlyTheseChanged(final String...fields) {
        if (differingFieldNames.size() > fields.length) {
            return false;
        }
        final Set<String> copy = new HashSet<String>(differingFieldNames);
        for (final String field : fields) {
            copy.remove(field);
        }
        return copy.isEmpty();
    }
    
    public boolean exactlyTheseChanged(String... fields) {
        if (!onlyTheseChanged(fields)) {
            return false;
        }

        for (String field : fields) {
            if (!differingFieldNames.contains(field)) {
                return false;
            }
        }
        return true;
    }

    
    public FieldUpdate getUpdateFor(final String field) {
        for (final FieldUpdate update : updates) {
            if (update.getFieldName().equals(field)) {
                return update;
            }
        }
        return null;
    }



    public class FieldUpdate {

        private int fieldNumber;

        private String fieldName;

        private Object originalValue;

        private Object newValue;
        private Object extraInfo;

        public int getFieldNumber() {
            return fieldNumber;
        }

        public void setFieldNumber(final int fieldNumber) {
            this.fieldNumber = fieldNumber;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(final String fieldName) {
            this.fieldName = fieldName;
        }

        public Object getOriginalValue() {
            return originalValue;
        }

        public void setOriginalValue(final Object originalValue) {
            this.originalValue = originalValue;
        }

        public Object getNewValue() {
            return newValue;
        }

        public void setNewValue(final Object newValue) {
            this.newValue = newValue;
        }

        
        public Object getExtraInfo() {
            return extraInfo;
        }

        
        public void setExtraInfo(final Object extraInfo) {
            this.extraInfo = extraInfo;
        }
        
        

    }
    
    
    // Diagnostic Methods
    
    public boolean isAboutStateChangesOnly() {
        
        // First, let's see if any fields besides the state tracking fields have changed
        HashSet<String> differing = new HashSet<String>(differingFieldNames);
        
        for(String field: new String[]{AppointmentFields.PARTICIPANTS, AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS}) {
            differing.remove(field);
        }
        if (!differing.isEmpty()) {
            return false;
        }
        
        // Hm, okay, so now let's see if any participants were added or removed. That also means this mail is not only about state changes.
        for(String field: new String[]{AppointmentFields.PARTICIPANTS, AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS}) {
            FieldUpdate update = getUpdateFor(field);
            if (update == null) {
                continue;
            }
            Difference extraInfo = (Difference) update.getExtraInfo();
            if (!extraInfo.getAdded().isEmpty()) {
                return false;
            }
            if (!extraInfo.getRemoved().isEmpty()) {
                return false;
            }

        }
        
        return true;
    }

    public boolean isAboutCertainParticipantsStateChangeOnly(String identifier) {
		if (!isAboutStateChangesOnly()) {
			return false;
		}
		
        for(String field: new String[]{AppointmentFields.PARTICIPANTS, AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS}) {
            FieldUpdate update = getUpdateFor(field);
            if (update == null) {
                continue;
            }
            Difference extraInfo = (Difference) update.getExtraInfo();
            List<Change> changed = extraInfo.getChanged();
            if (changed.size() > 1) {
            	return false;
            }
            Change change = changed.get(0);
			if (!change.getIdentifier().equals(identifier)) {
				return false;
			}
        }
        return true;		
	}

	public boolean isAboutDetailChangesOnly() {
		HashSet<String> differing = new HashSet<String>(differingFieldNames);
        
		
        for(String field: new String[]{AppointmentFields.PARTICIPANTS, AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS}) {
            differing.remove(field);
        }
        // If any other field than the participants fields as changed and the participant fields were not changed, we're done, as no state changes could have occurred
        if (!differing.isEmpty() && !anyFieldChangedOf(AppointmentFields.PARTICIPANTS, AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS)) {
            return true;
        }
        
        // Hm, okay, so now let's see if any participants state has changed. That means, that something other than a detail field has changed
        for(String field: new String[]{AppointmentFields.PARTICIPANTS, AppointmentFields.USERS, AppointmentFields.CONFIRMATIONS}) {
            FieldUpdate update = getUpdateFor(field);
            if (update == null) {
                continue;
            }
            Difference extraInfo = (Difference) update.getExtraInfo();
            List<Change> changed = extraInfo.getChanged();
            if (!changed.isEmpty()) {
            	return false; // A state has been changed, this is not about details only
            }

        }
        
        return true;
	}






}
