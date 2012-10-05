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
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.groupware.container.Appointment;


/**
 * {@link FreeBusySlot}
 * 
 * Defines a free/busy time-slot.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusySlot implements Comparable<FreeBusySlot> {
    
    private Date startTime;
    private Date endTime;
    private BusyStatus status;
    private String objectID;
    private String folderID;
    private boolean fullTime;
    
    /**
     * Initializes a new {@link FreeBusySlot}.
     */
    public FreeBusySlot() {
        super();
    }

    /**
     * Initializes a new {@link FreeBusySlot}.
     * 
     * @param start the start time
     * @param end the end time
     * @param status the busy status
     */
    public FreeBusySlot(Date start, Date end, BusyStatus status) {
        super();
        this.startTime = start;
        this.endTime = end;
        this.status = status;
    }

    /**
     * Initializes a new {@link FreeBusySlot}.
     * 
     * @param appointment The appointment to create the free/busy slot for
     */
    public FreeBusySlot(Appointment appointment) {
        this(appointment.getStartDate(), appointment.getEndDate(), BusyStatus.valueOf(appointment));
        this.fullTime = appointment.getFullTime();
        if (appointment.containsObjectID() && 0 < appointment.getObjectID()) {
            this.objectID = String.valueOf(appointment.getObjectID());
        }
        if (appointment.containsParentFolderID() && 0 < appointment.getParentFolderID()) {
            this.folderID = String.valueOf(appointment.getParentFolderID());
        }
    }

    /**
     * Gets the startTime
     *
     * @return The startTime
     */
    public Date getStartTime() {
        return startTime;
    }
    
    /**
     * Sets the startTime
     *
     * @param startTime The startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Gets the endTime
     *
     * @return The endTime
     */
    public Date getEndTime() {
        return endTime;
    }
    
    /**
     * Sets the endTime
     *
     * @param endTime The endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Gets the status
     *
     * @return The status
     */
    public BusyStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the status
     *
     * @param status The status to set
     */
    public void setStatus(BusyStatus status) {
        this.status = status;
    }

    /**
     * Gets the objectID
     *
     * @return The objectID
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * Sets the objectID
     *
     * @param objectID The objectID to set
     */
    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
    
    /**
     * Gets the folderID
     *
     * @return The folderID
     */
    public String getFolderID() {
        return folderID;
    }
    
    /**
     * Sets the folderID
     *
     * @param folderID The folderID to set
     */
    public void setFolderID(String folderID) {
        this.folderID = folderID;
    }
    
    /**
     * Gets the isFullTime
     *
     * @return The isFullTime
     */
    public boolean isFullTime() {
        return fullTime;
    }
    
    /**
     * Sets the isFullTime
     *
     * @param isFullTime The isFullTime to set
     */
    public void setFullTime(boolean isFullTime) {
        this.fullTime = isFullTime;
    }
    

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return (null != startTime ? sdf.format(startTime) : "[]") + " - " + (null != endTime ? sdf.format(endTime) : "[]") + " (" + status + ")"; 
    }

    @Override
    public int compareTo(FreeBusySlot o) {
        int value = null == o ? 1 : getStartTime().compareTo(o.getStartTime());
        return 0 == value ? getEndTime().compareTo(o.getEndTime()) : value;
    }
    
}
