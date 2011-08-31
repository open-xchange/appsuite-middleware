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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.image;

/**
 * {@link ImageLocation} - An image location description.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageLocation {

    private final String accountId;

    private final String folder;

    private final String id;

    private final String imageId;

    private String registrationName;

    /**
     * Initializes a new {@link ImageLocation}.
     * 
     * @param folder The folder identifier
     * @param id The object identifier
     * @param imageId The image identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public ImageLocation(final String accountId, final String folder, final String id, final String imageId) {
        super();
        this.accountId = accountId;
        this.folder = folder;
        this.id = id;
        this.imageId = imageId;
    }

    /**
     * Gets the account identifier
     * 
     * @return The account identifier
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Gets the folder identifier
     * 
     * @return The folder identifier
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the object identifier
     * 
     * @return The object identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the image identifier
     * 
     * @return The image identifier
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * Gets the registration name
     * 
     * @return The registration name
     */
    public String getRegistrationName() {
        return registrationName;
    }

    /**
     * Sets the registration name
     * 
     * @param registrationName The registration name to set
     */
    public void setRegistrationName(final String registrationName) {
        this.registrationName = registrationName;
    }

}
