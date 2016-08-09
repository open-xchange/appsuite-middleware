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

import java.util.EnumSet;

/**
 * {@link Attendee}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.1">RFC 5545, section 3.8.4.1</a>
 */
public class Attendee extends CalendarUser {

    private CalendarUserType cuType;
    private ParticipantRole role;
    private ParticipationStatus partStat;
    private String comment;
    private Boolean rsvp;
    private int folderId;
    private String member;

    private final EnumSet<AttendeeField> setFields;

    /**
     * Initializes a new {@link Attendee}.
     */
    public Attendee() {
        super();
        this.setFields = EnumSet.noneOf(AttendeeField.class);
    }

    /**
     * Gets a value indicating whether a specific property is set in the attendee or not.
     *
     * @param field The field to check
     * @return <code>true</code> if the field is set, <code>false</code>, otherwise
     */
    public boolean isSet(AttendeeField field) {
        return setFields.contains(field);
    }

    /**
     * Gets the calendar user address of the attendee.
     *
     * @return The calendar user address
     */
    @Override
    public String getUri() {
        return uri;
    }

    /**
     * Sets the calendar user address of the attendee.
     *
     * @param value The calendar user address to set
     */
    @Override
    public void setUri(String value) {
        uri = value;
        setFields.add(AttendeeField.URI);
    }

    /**
     * Removes the calendar user address of the attendee.
     */
    public void removeUri() {
        uri = null;
        setFields.remove(AttendeeField.URI);
    }

    /**
     * Gets a value indicating whether the calendar user address of the attendee has been set or not.
     *
     * @return <code>true</code> if the calendar user address is set, <code>false</code>, otherwise
     */
    public boolean containsUri() {
        return isSet(AttendeeField.URI);
    }

    /**
     * Gets the common name of the attendee.
     *
     * @return The common name
     */
    public String getCn() {
        return cn;
    }

    /**
     * Sets the common name of the attendee.
     *
     * @param value The common name to set
     */
    public void setCn(String value) {
        cn = value;
        setFields.add(AttendeeField.CN);
    }

    /**
     * Removes the common name of the attendee.
     */
    public void removeCn() {
        cn = null;
        setFields.remove(AttendeeField.CN);
    }

    /**
     * Gets a value indicating whether the common name of the attendee has been set or not.
     *
     * @return <code>true</code> if the common name is set, <code>false</code>, otherwise
     */
    public boolean containsCn() {
        return isSet(AttendeeField.CN);
    }

    /**
     * Gets the entity identifier of the attendee.
     *
     * @return The entity identifier
     */
    @Override
    public int getEntity() {
        return entity;
    }

    /**
     * Sets the entity identifier of the attendee.
     *
     * @param value The entity identifier to set
     */
    @Override
    public void setEntity(int value) {
        entity = value;
        setFields.add(AttendeeField.ENTITY);
    }

    /**
     * Removes the entity identifier of the attendee.
     */
    public void removeEntity() {
        entity = 0;
        setFields.remove(AttendeeField.ENTITY);
    }

    /**
     * Gets a value indicating whether the entity identifier of the attendee has been set or not.
     *
     * @return <code>true</code> if the entity identifier is set, <code>false</code>, otherwise
     */
    public boolean containsEntity() {
        return isSet(AttendeeField.ENTITY);
    }

    /**
     * Gets the user who is acting on behalf of the attendee.
     *
     * @return The user who is acting on behalf
     */
    @Override
    public String getSentBy() {
        return sentBy;
    }

    /**
     * Sets the user who is acting on behalf of the attendee.
     *
     * @param value The user who is acting on behalf to set
     */
    @Override
    public void setSentBy(String value) {
        sentBy = value;
        setFields.add(AttendeeField.SENT_BY);
    }

    /**
     * Removes the user who is acting on behalf of the attendee.
     */
    public void removeSentBy() {
        sentBy = null;
        setFields.remove(AttendeeField.SENT_BY);
    }

    /**
     * Gets a value indicating whether the user who is acting on behalf of the attendee has been set or not.
     *
     * @return <code>true</code> if the user who is acting on behalf is set, <code>false</code>, otherwise
     */
    public boolean containsSentBy() {
        return isSet(AttendeeField.SENT_BY);
    }

    /**
     * Gets the calendar user type of the attendee.
     *
     * @return The calendar user type
     */
    public CalendarUserType getCuType() {
        return cuType;
    }

    /**
     * Sets the calendar user type of the attendee.
     *
     * @param value The calendar user type to set
     */
    public void setCuType(CalendarUserType value) {
        cuType = value;
        setFields.add(AttendeeField.CU_TYPE);
    }

    /**
     * Removes the calendar user type of the attendee.
     */
    public void removeCuType() {
        cuType = null;
        setFields.remove(AttendeeField.CU_TYPE);
    }

    /**
     * Gets a value indicating whether the calendar user type of the attendee has been set or not.
     *
     * @return <code>true</code> if the calendar user type is set, <code>false</code>, otherwise
     */
    public boolean containsCuType() {
        return isSet(AttendeeField.CU_TYPE);
    }

    /**
     * Gets the participation role of the attendee.
     *
     * @return The participation role
     */
    public ParticipantRole getRole() {
        return role;
    }

    /**
     * Sets the participation role of the attendee.
     *
     * @param value The participation role to set
     */
    public void setRole(ParticipantRole value) {
        role = value;
        setFields.add(AttendeeField.ROLE);
    }

    /**
     * Removes the participation role of the attendee.
     */
    public void removeRole() {
        role = null;
        setFields.remove(AttendeeField.ROLE);
    }

    /**
     * Gets a value indicating whether the participation role of the attendee has been set or not.
     *
     * @return <code>true</code> if the participation role is set, <code>false</code>, otherwise
     */
    public boolean containsRole() {
        return isSet(AttendeeField.ROLE);
    }

    /**
     * Gets the participation status of the attendee.
     *
     * @return The participation status
     */
    public ParticipationStatus getPartStat() {
        return partStat;
    }

    /**
     * Sets the participation status of the attendee.
     *
     * @param value The participation status to set
     */
    public void setPartStat(ParticipationStatus value) {
        partStat = value;
        setFields.add(AttendeeField.PARTSTAT);
    }

    /**
     * Removes the participation status of the attendee.
     */
    public void removePartStat() {
        partStat = null;
        setFields.remove(AttendeeField.PARTSTAT);
    }

    /**
     * Gets a value indicating whether the participation status of the attendee has been set or not.
     *
     * @return <code>true</code> if the participation status is set, <code>false</code>, otherwise
     */
    public boolean containsPartStat() {
        return isSet(AttendeeField.PARTSTAT);
    }

    /**
     * Gets the attendee's comment.
     *
     * @return The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the attendee's comment.
     *
     * @param value The comment to set
     */
    public void setComment(String value) {
        comment = value;
        setFields.add(AttendeeField.COMMENT);
    }

    /**
     * Removes the attendee's comment.
     */
    public void removeComment() {
        comment = null;
        setFields.remove(AttendeeField.COMMENT);
    }

    /**
     * Gets a value indicating whether the attendee's comment has been set or not.
     *
     * @return <code>true</code> if the comment is set, <code>false</code>, otherwise
     */
    public boolean containsComment() {
        return isSet(AttendeeField.COMMENT);
    }

    /**
     * Gets the RSVP expectation of the attendee.
     *
     * @return The RSVP expectation
     */
    public Boolean getRsvp() {
        return rsvp;
    }

    /**
     * Gets the RSVP expectation of the attendee.
     *
     * @return The RSVP expectation
     */
    public Boolean isRsvp() {
        return rsvp;
    }

    /**
     * Sets the RSVP expectation of the attendee.
     *
     * @param value The RSVP expectation to set
     */
    public void setRsvp(Boolean value) {
        rsvp = value;
        setFields.add(AttendeeField.RSVP);
    }

    /**
     * Removes the RSVP expectation of the attendee.
     */
    public void removeRsvp() {
        rsvp = null;
        setFields.remove(AttendeeField.RSVP);
    }

    /**
     * Gets a value indicating whether the RSVP expectation of the attendee has been set or not.
     *
     * @return <code>true</code> if the RSVP expectation is set, <code>false</code>, otherwise
     */
    public boolean containsRsvp() {
        return isSet(AttendeeField.RSVP);
    }

    /**
     * Gets the identifier of the folder where the event is located in of the attendee.
     *
     * @return The identifier of the folder where the event is located in
     */
    public int getFolderID() {
        return folderId;
    }

    /**
     * Sets the identifier of the folder where the event is located in of the attendee.
     *
     * @param value The identifier of the folder where the event is located in to set
     */
    public void setFolderID(int value) {
        folderId = value;
        setFields.add(AttendeeField.FOLDER_ID);
    }

    /**
     * Removes the identifier of the folder where the event is located in of the attendee.
     */
    public void removeFolderID() {
        folderId = 0;
        setFields.remove(AttendeeField.FOLDER_ID);
    }

    /**
     * Gets a value indicating whether the identifier of the folder where the event is located in of the attendee has been set or not.
     *
     * @return <code>true</code> if the identifier of the folder where the event is located in is set, <code>false</code>, otherwise
     */
    public boolean containsFolderID() {
        return isSet(AttendeeField.FOLDER_ID);
    }

    /**
     * Gets the group- or list membership of the attendee.
     *
     * @return The group- or list membership
     */
    public String getMember() {
        return member;
    }

    /**
     * Sets the group- or list membership of the attendee.
     *
     * @param value The group- or list membership to set
     */
    public void setMember(String value) {
        member = value;
        setFields.add(AttendeeField.MEMBER);
    }

    /**
     * Removes the group- or list membership of the attendee.
     */
    public void removeMember() {
        member = null;
        setFields.remove(AttendeeField.MEMBER);
    }

    /**
     * Gets a value indicating whether the group- or list membership of the attendee has been set or not.
     *
     * @return <code>true</code> if the group- or list membership is set, <code>false</code>, otherwise
     */
    public boolean containsMember() {
        return isSet(AttendeeField.MEMBER);
    }

    @Override
    public String toString() {
        return "Attendee [cuType=" + cuType + ", partStat=" + partStat + ", uri=" + uri + ", entity=" + entity + "]";
    }

}
