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
 * This class represents a filestore.
 */
public class Filestore {
    private Integer id;

    private String url;

    /**
     * Size in mega bytes (MB).
     */
    private Long size;

    /**
     * Used space in mega bytes (MB)
     */
    private Long used;

    /**
     * Reserved space in mega bytes (MB)
     */
    private Long reserved;

    private Integer maxContexts;

    private Integer currentContexts;

    /**
     * Initiates an empty filestore object
     */
    public Filestore() {
        super();
    }

    /**
     * Initiates a filestore object with given id set
     *
     * @param id An {@link Integer} containing the id
     */
    public Filestore(Integer id) {
        super();
        this.id = id;
    }

    public Filestore(com.openexchange.admin.rmi.dataobjects.Filestore fs) {
        super();
        this.id = fs.getId();
        this.maxContexts = fs.getMaxContexts();
        this.currentContexts = fs.getCurrentContexts();
        this.reserved = fs.getReserved();
        this.size = fs.getSize();
        this.url = fs.getUrl();
        this.used = fs.getUsed();
    }

    /**
     * Returns the id of this filestore object
     *
     * @return An {@link Integer} containing the id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Sets the id for this filestore object
     *
     * @param id An {@link Integer} containing the id
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Returns the url of this filestore object
     *
     * @return A {@link String} containing the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the url for this filestore object
     *
     * @param url A {@link String} containing the url
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Returns the size of this filestore object (in MB)
     *
     * @return A {@link Long} containing the size (in MB)
     */
    public Long getSize() {
        return this.size;
    }

    /**
     * @param Size (in MB)
     */
    /**
     * Sets the size for this filestore object (in MB)
     *
     * @param size A {@link Long} containing the size (in MB)
     */
    public void setSize(final Long size) {
        this.size = size;
    }

    /**
     * Returns the maximum amount of contexts of this filestore object
     *
     * @return An {@link Integer} containing the maximum amoung of contexts
     */
    public Integer getMaxContexts() {
        return this.maxContexts;
    }

    /**
     * Sets the maximum amount of contexts for this filestore object
     *
     * @param maxContexts A {@link String} containing the maximum amount of contexts
     */
    public void setMaxContexts(final Integer maxContexts) {
        this.maxContexts = maxContexts;
    }

    /**
     * Returns the current amount of contexts of this filestore object
     *
     * @return An {@link Integer} containing the current
     */
    public Integer getCurrentContexts() {
        return this.currentContexts;
    }

    /**
     * Sets the current amount of contexts for this filestore object
     *
     * @param currentContexts An {@link Integer} containing the current amount of contexts
     */
    public void setCurrentContexts(final Integer currentContexts) {
        this.currentContexts = currentContexts;
    }

    /**
     * Returns the currently used size of this filestore object (in MB)
     *
     * @return A {@link Long} containing the currently used size (in MB)
     */
    public Long getUsed() {
        return this.used;
    }

    /**
     * Sets the currently used size for this filestore object (in MB)
     *
     * @param quota_used A {@link Long} containing the currently used size
     */
    public void setUsed(final Long quota_used) {
        this.used = quota_used;
    }

    /**
     * @return the reserved
     */
    public final Long getReserved() {
        return reserved;
    }

    /**
     * @param reserved the reserved to set
     */
    public final void setReserved(Long reserved) {
        this.reserved = reserved;
    }
}
