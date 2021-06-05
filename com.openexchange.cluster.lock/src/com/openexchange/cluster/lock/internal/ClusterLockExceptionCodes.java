/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
     * Unable to acquire cluster lock for the cluster task '%1$s' on this node (waiting time of '%2$s' %3$s expired). Another node is currently performing the same task. Try again later.
     */
    UNABLE_TO_ACQUIRE_CLUSTER_LOCK_EXPIRED("Unable to acquire cluster lock for the cluster task '%1$s' on this node (waiting time of '%2$s' %3$s expired). Another node is currently performing the same task. Try again later.", Category.CATEGORY_TRY_AGAIN, 5),
    /**
     * Unable to acquire cluster lock for the cluster task '%1$s' on this node. Another node is currently performing the same task. Try again later.
     */
    UNABLE_TO_ACQUIRE_CLUSTER_LOCK("Unable to acquire cluster lock for the cluster task '%1$s' on this node. Another node is currently performing the same task. Try again later.", ClusterLockExceptionMessages.UNABLE_TO_ACQUIRE_CLUSTER_LOCK, CATEGORY_TRY_AGAIN, 6),
    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", CATEGORY_ERROR, 7)

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

    private ClusterLockExceptionCodes(final String message, String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
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

    @Override
    public boolean equals(OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

}
