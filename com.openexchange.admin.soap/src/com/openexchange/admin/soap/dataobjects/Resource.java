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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.soap.dataobjects;


/**
 * This dataobject stores all the data which is related to a resource
 */
public class Resource {
    private Integer id;

    private String name;

    private String displayname;

    private String description;

    private String email;

    private Boolean available;

    /**
     * Instantiates a new {@link Resource} object
     */
    public Resource() {
        super();
        init();
    }

    /**
     * Instantiates a new {@link Resource} object with the given id
     *
     * @param id An {@link Integer} object containing the id
     */
    public Resource(final Integer id) {
        super();
        init();
        this.id = id;
    }

    public Resource(final com.openexchange.admin.rmi.dataobjects.Resource res) {
        super();
        this.id = res.getId();
        this.name = res.getName();
        this.displayname = res.getDisplayname();
        this.description = res.getDescription();
        this.email = res.getEmail();
        this.available = res.getAvailable();
    }

    private void init() {
        this.id = null;
        this.name = null;
        this.displayname = null;
        this.description = null;
        this.email = null;
        this.available = null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#getId()
     */
    public Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#setId(java.lang.Integer)
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#getName()
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#setName(java.lang.String)
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the displayname of this resource
     *
     * @return A {@link String} containing the displayname
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * Sets the displayname for this resource
     *
     * @param displayname A {@link String} containing the displayname
     */
    public void setDisplayname(final String displayname) {
        this.displayname = displayname;
    }

    /**
     * Returns the E-Mail of this resource
     *
     * @return A {@link String} object containing the E-Mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for this resource
     *
     * @param email A {@link String} object containing the E-Mail address
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * This attribute is not used
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     * This attribute is not used
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    /**
     * Returns the description of this resource
     *
     * @return A {@link String} object containing the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this resource
     *
     * @param description A {@link String} object containing the description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

}
