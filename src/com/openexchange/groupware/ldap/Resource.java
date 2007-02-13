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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.groupware.ldap;

import java.util.Date;

/**
 * This is the data container class for resources.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Resource {

    /**
     * The identifier of the resource.
     */
    private int identifier = -1;

    /**
     * This is the name of this resource that can have character restrictions.
     */
    private String simpleName;

    /**
     * The display name of the resource. Currently the identifier is also used
     * as display name so this attribute will be filled with the identifier. But
     * the identifier is limited in the characters that can be used.
     */
    private String displayName;

    /**
     * Mail address of this resource.
     */
    private String mail;

    /**
     * If a resource is not available, it can't be booked.
     */
    private boolean available;

    /**
     * Description of this resource.
     */
    private String description;

    /**
     * Timestamp of the last modification of this resource.
     */
    private Date lastModified;

    /**
     * Default constructor.
     */
    public Resource() {
        super();
    }

    /**
     * Setter for identifier.
     * @param identifier identifier.
     */
    public void setIdentifier(final int identifier) {
        this.identifier = identifier;
    }

    /**
     * Getter for identifier.
     * @return the identifier.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Getter for displayName.
     * @return the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns if the resource is available. If a resource is available, it can
     * be booked.
     * @return <code>true</code> if the resource is available.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Setter for displayName.
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Setter for available.
     * @param available <code>true</code> if the resource is available.
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }

    /**
     * Setter for description.
     * @param description Description.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Getter for description.
     * @return Description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified The lastModified to set.
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the mail
     */
    public String getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    void setMail(final String mail) {
        this.mail = mail;
    }
}
