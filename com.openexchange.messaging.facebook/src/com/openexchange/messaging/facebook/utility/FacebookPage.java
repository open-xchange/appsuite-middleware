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

package com.openexchange.messaging.facebook.utility;

/**
 * {@link FacebookPage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookPage {

    private String name;

    private String picSmall;

    private long pageId;

    /**
     * Initializes a new {@link FacebookPage}.
     */
    public FacebookPage() {
        super();
    }

    /**
     * Checks if this Facebook page is empty.
     *
     * @return <code>true</code> if this Facebook page is empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return (pageId <= 0) && (null == name) && (null == picSmall);
    }

    /**
     * Gets the URL to the small-sized profile picture.
     * <p>
     * The image can have a maximum width of 50px and a maximum height of 150px.
     *
     * @return The URL to the small-sized profile picture
     */
    public String getPicSmall() {
        return picSmall;
    }

    /**
     * Sets the URL to the small-sized profile picture.
     * <p>
     * The image can have a maximum width of 50px and a maximum height of 150px.
     *
     * @param picSmall The URL to the small-sized profile picture
     */
    public void setPicSmall(final String picSmall) {
        this.picSmall = picSmall;
    }

    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name.
     *
     * @param name The full name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the page ID.
     *
     * @return The page ID
     */
    public long getPageId() {
        return pageId;
    }

    /**
     * Sets the page ID.
     *
     * @param gid The page ID to set
     */
    public void setPageId(final long pageId) {
        this.pageId = pageId;
    }

}
