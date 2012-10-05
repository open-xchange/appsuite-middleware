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

package com.openexchange.freebusy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyData}
 * 
 * Data structure hosting a user's or resource's free/busy slots.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyData extends ArrayList<FreeBusySlot> {
    
    private static final long serialVersionUID = 8744420045936359683L;

    protected String participant;
    protected Date from;
    protected Date until;
    protected OXException error;
    
    /**
     * Initializes a new {@link FreeBusyData}.
     */
    public FreeBusyData() {
        this(null, null, null);
    }
    
    /**
     * Initializes a new {@link FreeBusyData}.
     * 
     * @param participant 
     * @param from
     * @param until
     */
    public FreeBusyData(String participant, Date from, Date until) {
        super();
        this.participant = participant;
        this.from = from;
        this.until = until;
    }

    /**
     * Gets the participant, identified either by its internal user-/resource-ID or e-mail address.
     * 
     * @return The participant
     */
    public String getParticipant() {
        return participant;
    }

    /**
     * Gets the lower (inclusive) limit of the covered time-range.
     * 
     * @return The 'from' date
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Gets the upper (exclusive) limit of the covered time-range.
     * 
     * @return The 'until' date
     */
    public Date getUntil() {
        return until;
    }

    /**
     * Gets an exception in case data could not be retrieved.
     * 
     * @return The exception, if present, <code>null</code>, otherwise 
     */
    public OXException getError() {
        return error;
    }

    /**
     * Gets a value indicating whether there is an error or not.
     * 
     * @return <code>true</code> in case of an existing error, <code>false</code>, otherwise
     */
    public boolean hasError() {
        return null != error;
    }

    /**
     * Sets the error.
     * 
     * @param error The exception
     */
    public void setError(OXException error) {
        this.error = error;
    }

    /**
     * Normalizes the contained free/busy slots. 
     */
    public void normalize() {
        /*
         * normalize to interval boundaries
         */
        Iterator<FreeBusySlot> iterator = iterator();
        while (iterator.hasNext()) {
            FreeBusySlot freeBusySlot = iterator.next();
            if (null != freeBusySlot.getEndTime() && freeBusySlot.getEndTime().after(getFrom()) &&
                null != freeBusySlot.getStartTime() && freeBusySlot.getStartTime().before(getUntil())) {
                if (freeBusySlot.getStartTime().before(getFrom())) {
                    freeBusySlot.setStartTime(getFrom());
                }
                if (freeBusySlot.getEndTime().after(getUntil())) {
                    freeBusySlot.setEndTime(getUntil());
                }
            } else {
                // outside range
                iterator.remove();
            }            
        }
        if (1 < size()) {
            /*
             * sort
             */
            Collections.sort(this);
            /*
             * merge ranges 
             */
            
            //TODO...
            
            ArrayList<FreeBusySlot> mergedSlots = new ArrayList<FreeBusySlot>();
            iterator = super.iterator();
            FreeBusySlot current = iterator.next();
            while (iterator.hasNext()) {
                FreeBusySlot next = iterator.next();
                if (current.getEndTime().after(next.getStartTime())) {
                    /*
                     * overlapping ranges
                     */
                    if (current.getStatus().isMoreConflicting(next.getStatus())) {
                        // prefer current timeslot
                        if (current.getEndTime().after(next.getEndTime())) {
                            // ignore next completely
                        } else {
                            // add both
                            mergedSlots.add(current);
                            next.setStartTime(current.getEndTime());
                            current = next;
                        }
                    } else {
                        // prefer next timeslot
                        if (current.getEndTime().before(next.getEndTime())) {
                            if (current.getStartTime().before(next.getStartTime())) {
                                // add both
                                current.setEndTime(next.getStartTime());
                                mergedSlots.add(current);
                            }
                            current = next;
                        } else {
                            // add additional slot
                            FreeBusySlot additional = new FreeBusySlot(next.getEndTime(), current.getEndTime(), current.getStatus());
                            additional.setFolderID(current.getFolderID());
                            additional.setObjectID(current.getObjectID());                            
                            current.setEndTime(next.getStartTime());
                            mergedSlots.add(current);
                            mergedSlots.add(next);
                            current = additional;                                                        
                        }
                    }
                } else {
                    mergedSlots.add(current);
                    current = next;
                }           
            }
            mergedSlots.add(current);
            /*
             * take over normalized slots
             */
            this.clear();
            this.addAll(mergedSlots);
        }
    }    
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(participant).append(" (").append(null != from ? sdf.format(from) : "")
            .append(" - ").append(null != until ? sdf.format(until) : "").append("):").append("\n");
        for (FreeBusySlot fbSlot : this) {
            stringBuilder.append(fbSlot).append("\n");
        }
        return stringBuilder.toString();
    }
    
}
