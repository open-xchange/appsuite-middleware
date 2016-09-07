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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.cluster.lock.internal;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link ClusterLockExceptionCodes}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum ClusterLockExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Cluster is locked for action \"%1$s\". Try again later.
     */
    CLUSTER_LOCKED("Cluster is locked for action \"%1$s\". Try again later.", Category.CATEGORY_TRY_AGAIN, 1),
    /**
     * Cluster is locked for the period \"%1$s\" for action \"%2$s\". Try again in \"%3$s.
     */
    CLUSTER_PERIODIC_LOCKED("Cluster is locked for the period \"%1$s\" for action \"%2$s\". Try again in \"%3$s.", Category.CATEGORY_TRY_AGAIN, 2),
    /**
     * Timed-out while waiting to acquire lock. Try again
     */
    TIMEOUT("Timed-out while waiting to acquire lock. Try again", CATEGORY_TRY_AGAIN, 3),
    /**
     * Interrupted while trying to acquire a cluster lock for the cluster task '%1$s'
     */
    INTERRUPTED("Interrupted while trying to acquire a cluster lock for the cluster task '%1$s'", CATEGORY_ERROR, 4),
    /**
     * Unable to acquire cluster lock for task '%1$s' on this node. Another node is currently performing the same task. Try again later.
     */
    UNABLE_TO_ACQUIRE_CLUSTER_LOCK("Unable to acquire cluster lock for the cluster task '%1$s' on this node (waiting time of '%2$s' %3$s expired). Another node is currently performing the same task. Try again later.", Category.CATEGORY_TRY_AGAIN, 5),

    ;

    private static final String PREFIX = "SRV";

    private final String message;

    private final Category category;

    private final int number;

    private String displayMessage;

    private ClusterLockExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(Throwable t, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, t, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#equals(com.openexchange.exception.OXException)
     */
    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getNumber()
     */
    @Override
    public int getNumber() {
        return number;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getCategory()
     */
    @Override
    public Category getCategory() {
        return category;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getPrefix()
     */
    @Override
    public String getPrefix() {
        return PREFIX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.OXExceptionCode#getMessage()
     */
    @Override
    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.exception.DisplayableOXExceptionCode#getDisplayMessage()
     */
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

}
