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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos;

/**
 * {@link Attendee}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.1">RFC 5545, section 3.8.4.1</a>
 */
public class Attendee extends CalendarUser {

    CalendarUserType cuType;
    ParticipantRole role;
    ParticipationStatus partStat;
    String comment;
    Boolean rsvp;
    int folderID;
    String member;

    /**
     * Gets the cuType
     *
     * @return The cuType
     */
    public CalendarUserType getCuType() {
        return cuType;
    }

    /**
     * Sets the cuType
     *
     * @param cuType The cuType to set
     */
    public void setCuType(CalendarUserType cuType) {
        this.cuType = cuType;
    }

    /**
     * Gets the role
     *
     * @return The role
     */
    public ParticipantRole getRole() {
        return role;
    }

    /**
     * Sets the role
     *
     * @param role The role to set
     */
    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    /**
     * Gets the partStat
     *
     * @return The partStat
     */
    public ParticipationStatus getPartStat() {
        return partStat;
    }

    /**
     * Sets the partStat
     *
     * @param partStat The partStat to set
     */
    public void setPartStat(ParticipationStatus partStat) {
        this.partStat = partStat;
    }

    /**
     * Gets the comment
     *
     * @return The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment
     *
     * @param comment The comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the rsvp
     *
     * @return The rsvp
     */
    public Boolean isRsvp() {
        return rsvp;
    }

    /**
     * Sets the rsvp
     *
     * @param rsvp The rsvp to set
     */
    public void setRsvp(Boolean rsvp) {
        this.rsvp = rsvp;
    }

    /**
     * Gets the folderID
     *
     * @return The folderID
     */
    public int getFolderID() {
        return folderID;
    }

    /**
     * Sets the folderID
     *
     * @param folderID The folderID to set
     */
    public void setFolderID(int folderID) {
        this.folderID = folderID;
    }

    /**
     * @return the member
     */
    public String getMember() {
        return member;
    }

    /**
     * @param member the member to set
     */
    public void setMember(String member) {
        this.member = member;
    }

}
