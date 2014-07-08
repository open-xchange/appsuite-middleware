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

package com.openexchange.quota.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A {@link QuotaAndUsage} encapsulates storage and object quotas and their current
 * usages for a certain account in a certain module. Whether a quota is defined at
 * all is indicated by the {@link #hasObjectQuota()} and {@link #hasStorageQuota()}
 * methods.
 *
 * Instances of {@link QuotaAndUsage} are created with the builder pattern. Two different
 * builders exists to construct either a single {@link QuotaAndUsage} or a list of several
 * ones, belonging to different accounts. See {@link QuotaAndUsage#newBuilder(String, String)}
 * and {@link QuotaAndUsage#newMultiBuilder()} for details.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class QuotaAndUsage {

    public static final long UNLIMITED = -1L;

    private final String accountID;

    private final String accountName;

    private final boolean hasObjectQuota;

    private final long maxObjects;

    private final long usedObjects;

    private final boolean hasStorageQuota;

    private final long maxStorage;

    private final long usedStorage;

    private QuotaAndUsage(Builder builder) {
        super();
        accountID = builder.accountID;
        accountName = builder.accountName;
        hasObjectQuota = builder.hasObjectQuota;
        maxObjects = builder.maxObjects;
        usedObjects = builder.usedObjects;
        hasStorageQuota = builder.hasStorageQuota;
        maxStorage = builder.maxStorage;
        usedStorage = builder.usedStorage;
    }


    /**
     * Creates a new {@link Builder} and presets the accounts id and name.
     *
     * @param accountID The accounts id, never <code>null</code>.
     * @param accountName The accounts name, never <code>null</code>.
     * @return The builder.
     */
    public static Builder newBuilder(String accountID, String accountName) {
        return new Builder(accountID, accountName);
    }

    /**
     * Creates a new {@link MultiBuilder}. {@link MultiBuilder#push(String, String)} must
     * be called before setting any other values for every {@link QuotaAndUsage} that
     * shall be built.
     *
     * @return The builder.
     */
    public static MultiBuilder newMultiBuilder() {
        return new MultiBuilder();
    }

    /**
     * Creates a new {@link MultiBuilder}. No initial <code>push()</code> call
     * is necessary, the first {@link QuotaAndUsage} will be initialized with the
     * given account id and name.
     *
     * @param accountID The accounts id, never <code>null</code>.
     * @param accountName The accounts name, never <code>null</code>.
     * @return The builder.
     */
    public static MultiBuilder newMultiBuilder(String accountID, String accountName) {
        return new MultiBuilder(accountID, accountName);
    }

    public String getAccountID() {
        return accountID;
    }

    public String getAccountName() {
        return accountName;
    }

    public boolean hasObjectQuota() {
        return hasObjectQuota;
    }

    public long getMaxObjects() {
        return maxObjects;
    }

    public long getUsedObjects() {
        return usedObjects;
    }

    public boolean hasStorageQuota() {
        return hasStorageQuota;
    }

    public long getMaxStorage() {
        return maxStorage;
    }

    public long getUsedStorage() {
        return usedStorage;
    }


    /**
     * A {@link Builder} helps to construct a single {@link QuotaAndUsage}.
     *
     * Per default both (storage and object) quotas are undefined. They are defined with the first
     * call to their corresponding <code>setMax</code> or <code>setUsed</code> method.
     * If a quota is unlimited, the according <code>setMax</code> method does not need
     * to be called, its default value is always {@link QuotaAndUsage#UNLIMITED}. It's
     * sufficient to call the respective <code>setUsed</code> method then.
     * <br>
     * Example:
     * <pre>
     * QuotaAndUsage quotaAndUsage = QuotaAndUsage.newBuilder(accountID, accountName)
     *     .setMaxStorage(maxBytes)
     *     .setUsedStorage(currentUsage)
     *     .build();
     * </pre>
     */
    public static final class Builder {

        private final String accountID;

        private final String accountName;

        private boolean hasObjectQuota = false;

        private long maxObjects = UNLIMITED;

        private long usedObjects = 0;

        private boolean hasStorageQuota = false;

        private long maxStorage = UNLIMITED;

        private long usedStorage = 0;

        Builder(String accountID, String accountName) {
            super();
            this.accountID = accountID;
            this.accountName = accountName;
        }

        public Builder setMaxObjects(long maxObjects) {
            this.maxObjects = maxObjects;
            hasObjectQuota = true;
            return this;
        }

        public Builder setUsedObjects(long usedObjects) {
            this.usedObjects = usedObjects;
            hasObjectQuota = true;
            return this;
        }

        public Builder setMaxStorage(long maxStorage) {
            this.maxStorage = maxStorage;
            hasStorageQuota = true;
            return this;
        }

        public Builder setUsedStorage(long usedStorage) {
            this.usedStorage = usedStorage;
            hasStorageQuota = true;
            return this;
        }

        public QuotaAndUsage build() {
            return new QuotaAndUsage(this);
        }
    }

    /**
     * A {@link MultiBuilder} helps to construct a list of {@link QuotaAndUsage}s.
     * This is useful when iterating over a collection of accounts. To start a new
     * {@link QuotaAndUsage} instance you have to call <code>push()</code>.
     *
     * Per default both (storage and object) quotas are undefined. They are defined with the first
     * call to their corresponding <code>setMax</code> or <code>setUsed</code> method.
     * If a quota is unlimited, the according <code>setMax</code> method does not need
     * to be called, its default value is always {@link QuotaAndUsage#UNLIMITED}. It's
     * sufficient to call the respective <code>setUsed</code> method then.
     * <br>
     * Example:
     * <pre>
     * MultiBuilder builder = QuotaAndUsage.newMultiBuilder();
     * for (Account account : accounts) {
     *     builder.push(account.getID(), account.getName())
     *            .setMaxStorage(account.getMaxBytes())
     *            .setUsedStorage(account.getCurrentUsage())
     * }
     * List<QuotaAndUsage> quotasAndUsages = builder.build();
     * </pre>
     */
    public static final class MultiBuilder {

        private final Stack<Builder> builders = new Stack<Builder>();

        private Builder current;

        MultiBuilder() {
            super();
        }

        MultiBuilder(String accountID, String accountName) {
            super();
            push(accountID, accountName);
        }

        public MultiBuilder setMaxObjects(long maxObjects) {
            checkCurrent();
            current.setMaxObjects(maxObjects);
            return this;
        }

        public MultiBuilder setUsedObjects(long usedObjects) {
            checkCurrent();
            current.setUsedObjects(usedObjects);
            return this;
        }

        public MultiBuilder setMaxStorage(long maxStorage) {
            checkCurrent();
            current.setMaxStorage(maxStorage);
            return this;
        }

        public MultiBuilder setUsedStorage(long usedStorage) {
            checkCurrent();
            current.setUsedStorage(usedStorage);
            return this;
        }

        public MultiBuilder push(String accountID, String accountName) {
            if (current != null) {
                builders.push(current);
            }

            current = new Builder(accountID, accountName);
            return this;
        }

        public List<QuotaAndUsage> build() {
            if (current != null) {
                builders.push(current);
            }

            List<QuotaAndUsage> quotasAndUsages = new ArrayList<QuotaAndUsage>(builders.size());
            for (Builder builder : builders) {
                quotasAndUsages.add(builder.build());
            }

            return quotasAndUsages;
        }

        private void checkCurrent() {
            if (current == null) {
                throw new IllegalStateException("You must call 'push' before setting any values!");
            }
        }

    }

}
