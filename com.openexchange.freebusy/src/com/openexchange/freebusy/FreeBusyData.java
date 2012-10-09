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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyData}
 * 
 * Data structure hosting a user's or resource's free/busy slots.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyData {
    
    protected String participant;
    protected Date from;
    protected Date until;
    protected OXException error;
    
    protected List<FreeBusySlot> slots;
    
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
        this.slots = new LinkedList<FreeBusySlot>();
        this.participant = participant;
        this.from = from;
        this.until = until;
    }

    public void clear() {
        this.slots.clear();
    }
    
    public void add(FreeBusySlot slot) {
        
    }
    
    public void addAll(Iterable<FreeBusySlot> slots) {
        if (null != slots) {
            for (FreeBusySlot slot : slots) {
                this.add(slot);
            }
        }
    }
    
    public List<FreeBusySlot> getSlots() {
        return Collections.unmodifiableList(this.slots);
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
    
    private static Date[] getTimes(List<FreeBusySlot> slots) {
        Set<Date> times = new HashSet<Date>();
        for (FreeBusySlot freeBusySlot : slots) {
            times.add(freeBusySlot.getStartTime());
            times.add(freeBusySlot.getEndTime());
        }
        Date[] array = times.toArray(new Date[times.size()]);
        Arrays.sort(array);
        return array; 
    }
    
    public void normalize() {
        
        //TODO
        
        Date[] times = getTimes(this.slots);
        
        ArrayList<FreeBusySlot> expandedSlots = new ArrayList<FreeBusySlot>();
        for (FreeBusySlot slot : slots) {
            List<Date> expandedTimes = new ArrayList<Date>();
            expandedTimes.add(slot.getStartTime());
            for (Date time : times) {
                if (slot.getStartTime().before(time) && slot.getEndTime().after(time)) {
                    expandedTimes.add(time);
                }
            }
            expandedTimes.add(slot.getEndTime());
            if (2 == expandedTimes.size()) {
                expandedSlots.add(slot);
            } else {
                for (int i = 0; i < expandedTimes.size() - 1; i++) {
                    FreeBusySlot expandedSlot = (FreeBusySlot)slot.clone();
                    expandedSlot.setStartTime(expandedTimes.get(i));                    
                    expandedSlot.setEndTime(expandedTimes.get(i + 1));
                    expandedSlots.add(expandedSlot);
                }
            }
        }
        
        // condense overlapping slots to most conflicting one
        ArrayList<FreeBusySlot> condensedSlots = new ArrayList<FreeBusySlot>();
        Iterator<FreeBusySlot> iterator = expandedSlots.iterator();
        FreeBusySlot current = iterator.next();
        while (iterator.hasNext()) {
            FreeBusySlot next = iterator.next();
            if (current.getStartTime().equals(next.getStartTime()) && current.getEndTime().equals(next.getEndTime())) {
                if (current.getStatus().isMoreConflicting(next.getStatus())) {
                    // skip next
                    continue;                               
                } else {
                    // replace current
                    current = next;
                    continue;
                }
            }
            condensedSlots.add(current);
            current = next;
        }
        
        // expand consecutive slots again 

    }
    
//    public void normal2ize() {
//        LinkedList<FreeBusySlot> mergedSlots = new LinkedList<FreeBusySlot>();
//        
//        List<Date> times = new ArrayList<Date>();
//         
//        for (FreeBusySlot freeBusySlot : this.slots) {
//            /*
//             * normalize to interval boundaries           
//             */
//            if (null != freeBusySlot.getEndTime() && freeBusySlot.getEndTime().after(getFrom()) &&
//                null != freeBusySlot.getStartTime() && freeBusySlot.getStartTime().before(getUntil())) {
//                if (freeBusySlot.getStartTime().before(getFrom())) {
//                    freeBusySlot.setStartTime(getFrom());
//                }
//                if (freeBusySlot.getEndTime().after(getUntil())) {
//                    freeBusySlot.setEndTime(getUntil());
//                }
//            } else {
//                continue; // outside range, ignore
//            }
//            
//            times.add(freeBusySlot.getStartTime());
//            times.add(freeBusySlot.getEndTime());
//            
//            /*
//             * insert slot
//             */
//            
//            for (int i = 0; i < mergedSlots.size(); i++) {
//                FreeBusySlot mergedSlot = mergedSlots.get(i);                
//                if (freeBusySlot.getStartTime().after(mergedSlot.getEndTime())) {
//                    // [FREE---------]
//                    //                     [BUSY--]
//                    continue;
//                }
//                if (freeBusySlot.getEndTime().before(mergedSlot.getStartTime())) {
//                    //           [FREE---------]
//                    // [BUSY--]
//                    mergedSlots.add(i, freeBusySlot);
//                    break;
//                }
//                if (freeBusySlot.getStartTime().after(mergedSlot.getStartTime()) && 
//                    freeBusySlot.getStartTime().before(mergedSlot.getEndTime())) {
//                    if (freeBusySlot.getStatus().isMoreConflicting(mergedSlot.getStatus())) {
//                        // [FREE---------]
//                        //    [BUSY--]
//                        FreeBusySlot additional = (FreeBusySlot)mergedSlot.clone();
//                        additional.setStartTime(freeBusySlot.getEndTime());                    
//                        mergedSlot.setEndTime(freeBusySlot.getStartTime());
//                        mergedSlots.add(i + 1, freeBusySlot);
//                        mergedSlots.add(i + 2, mergedSlot);
//                        continue;
//                    } else {
//                        // [BUSY---------]
//                        //    [FREE--]
//                        continue;
//                    }
//                }
//                if (freeBusySlot.getStartTime().before(mergedSlot.getStartTime()) &&
//                    freeBusySlot.getEndTime().after(mergedSlot.getEndTime())) {
//                    
//                    if (freeBusySlot.getStatus().isMoreConflicting(mergedSlot.getStatus())) {
//                        //    [FREE-]
//                        // [BUSY---------]
//                        mergedSlots.remove(i);
//                        mergedSlots.add(i, freeBusySlot);
//                    } else {
//                        //    [BUSY-]
//                        // [FREE---------]
//                        
//                    }
//                }
//                
//                
//                if (freeBusySlot.getStartTime().after(mergedSlots.get(i).getStartTime())) {
//                    
//                    
//                }
//                
//        
//                                
//                
//            }
//            
//        }
//        /*
//         * take over merged data
//         */
//        this.slots = mergedSlots;
//     }
//    
//     
//    /**
//     * Normalizes the contained free/busy slots. 
//     */
//    public void n1ormalize() {
//        /*
//         * normalize to interval boundaries
//         */
//        Iterator<FreeBusySlot> iterator = this.slots.iterator();
//        while (iterator.hasNext()) {
//            FreeBusySlot freeBusySlot = iterator.next();
//            if (null != freeBusySlot.getEndTime() && freeBusySlot.getEndTime().after(getFrom()) &&
//                null != freeBusySlot.getStartTime() && freeBusySlot.getStartTime().before(getUntil())) {
//                if (freeBusySlot.getStartTime().before(getFrom())) {
//                    freeBusySlot.setStartTime(getFrom());
//                }
//                if (freeBusySlot.getEndTime().after(getUntil())) {
//                    freeBusySlot.setEndTime(getUntil());
//                }
//            } else {
//                // outside range
//                iterator.remove();
//            }            
//        }
//        if (1 < this.slots.size()) {
//            /*
//             * sort
//             */
//            Collections.sort(this.slots);
//            /*
//             * merge ranges 
//             */
//            
//            //TODO...
//            
//            ArrayList<FreeBusySlot> mergedSlots = new ArrayList<FreeBusySlot>();
//            iterator = this.slots.iterator();
//            FreeBusySlot current = iterator.next();
//            while (iterator.hasNext()) {
//                FreeBusySlot next = iterator.next();
//                if (current.getEndTime().after(next.getStartTime())) {
//                    /*
//                     * overlapping ranges
//                     */
//                    if (current.getStatus().isMoreConflicting(next.getStatus())) {
//                        // prefer current timeslot
//                        if (current.getEndTime().after(next.getEndTime())) {
//                            // ignore next completely
//                        } else {
//                            // add both
//                            mergedSlots.add(current);
//                            next.setStartTime(current.getEndTime());
//                            current = next;
//                        }
//                    } else {
//                        // prefer next timeslot
//                        if (current.getEndTime().before(next.getEndTime())) {
//                            if (current.getStartTime().before(next.getStartTime())) {
//                                // add both
//                                current.setEndTime(next.getStartTime());
//                                mergedSlots.add(current);
//                            }
//                            current = next;
//                        } else {
//                            // add additional slot
//                            FreeBusySlot additional = new FreeBusySlot(next.getEndTime(), current.getEndTime(), current.getStatus());
//                            additional.setFolderID(current.getFolderID());
//                            additional.setObjectID(current.getObjectID());                            
//                            current.setEndTime(next.getStartTime());
//                            mergedSlots.add(current);
//                            mergedSlots.add(next);
//                            current = additional;                                                        
//                        }
//                    }
//                } else {
//                    mergedSlots.add(current);
//                    current = next;
//                }           
//            }
//            mergedSlots.add(current);
//            /*
//             * take over normalized slots
//             */
//            this.clear();
//            this.addAll(mergedSlots);
//        }
//    }    
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(participant).append(" (").append(null != from ? sdf.format(from) : "")
            .append(" - ").append(null != until ? sdf.format(until) : "").append("):").append("\n");
        if (false == hasError()) {
            for (FreeBusySlot fbSlot : this.slots) {
                stringBuilder.append(fbSlot).append("\n");
            }
        } else {
            stringBuilder.append(error.getPlainLogMessage()).append("\n");
        }
        return stringBuilder.toString();
    }
    
}
