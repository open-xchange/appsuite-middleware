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

package com.openexchange.pooling;

/**
 * {@link PoolConfig}
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - extracted
 * @since v7.10.1
 */
public class PoolConfig implements Cloneable {

    /**
     * Creates a new builder instance.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder instance pre-filled with given pool configuration's values.
     *
     * @param source The source pool configuration
     * @return A new builder instance
     */
    public static Builder builder(PoolConfig source) {
        return new Builder(source);
    }

    /** The builder for an instance of <code>PoolConfig</code> */
    public static class Builder {

        private int maxIdle;
        private long maxIdleTime;
        private int maxActive;
        private long maxWait;
        private long maxLifeTime;
        private ExhaustedActions exhaustedAction;
        private boolean testOnActivate;
        private boolean testOnDeactivate;
        private boolean testOnIdle;
        private boolean testThreads;

        Builder() {
            super();
            maxIdle = -1;
            maxIdleTime = 60000;
            maxActive = -1;
            maxWait = 10000;
            maxLifeTime = -1;
            exhaustedAction = ExhaustedActions.GROW;
            testOnActivate = true;
            testOnDeactivate = true;
            testOnIdle = false;
            testThreads = false;
        }

        Builder(PoolConfig source) {
            super();
            maxIdle = source.maxIdle;
            maxIdleTime = source.maxIdleTime;
            maxActive = source.maxActive;
            maxWait = source.maxWait;
            maxLifeTime = source.maxLifeTime;
            exhaustedAction = source.exhaustedAction;
            testOnActivate = source.testOnActivate;
            testOnDeactivate = source.testOnDeactivate;
            testOnIdle = source.testOnIdle;
            testThreads = source.testThreads;
        }

        public Builder withMaxIdle(int maxIdle) {
            this.maxIdle = maxIdle;
            return this;
        }

        public Builder withMaxIdleTime(long maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
            return this;
        }

        public Builder withMaxActive(int maxActive) {
            this.maxActive = maxActive;
            return this;
        }

        public Builder withMaxWait(long maxWait) {
            this.maxWait = maxWait;
            return this;
        }

        public Builder withMaxLifeTime(long maxLifeTime) {
            this.maxLifeTime = maxLifeTime;
            return this;
        }

        public Builder withExhaustedAction(ExhaustedActions exhaustedAction) {
            this.exhaustedAction = exhaustedAction;
            return this;
        }

        public Builder withTestOnActivate(boolean testOnActivate) {
            this.testOnActivate = testOnActivate;
            return this;
        }

        public Builder withTestOnDeactivate(boolean testOnDeactivate) {
            this.testOnDeactivate = testOnDeactivate;
            return this;
        }

        public Builder withTestOnIdle(boolean testOnIdle) {
            this.testOnIdle = testOnIdle;
            return this;
        }

        public Builder withTestThreads(boolean testThreads) {
            this.testThreads = testThreads;
            return this;
        }

        public PoolConfig build() {
            return new PoolConfig(maxIdle, maxIdleTime, maxActive, maxWait, maxLifeTime, exhaustedAction, testOnActivate, testOnDeactivate, testOnIdle, testThreads);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    public final int maxIdle;

    public final long maxIdleTime;

    public final int maxActive;

    public final long maxWait;

    public final long maxLifeTime;

    public final ExhaustedActions exhaustedAction;

    public final boolean testOnActivate;

    public final boolean testOnDeactivate;

    public final boolean testOnIdle;

    public final boolean testThreads;

    private int hash = 0;

    PoolConfig(int maxIdle, long maxIdleTime, int maxActive, long maxWait, long maxLifeTime, ExhaustedActions exhaustedAction, boolean testOnActivate, boolean testOnDeactivate, boolean testOnIdle, boolean testThreads) {
        super();
        this.maxIdle = maxIdle;
        this.maxIdleTime = maxIdleTime;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        this.maxLifeTime = maxLifeTime;
        this.exhaustedAction = exhaustedAction;
        this.testOnActivate = testOnActivate;
        this.testOnDeactivate = testOnDeactivate;
        this.testOnIdle = testOnIdle;
        this.testThreads = testThreads;
    }

    @Override
    public PoolConfig clone() {
        try {
            return (PoolConfig) super.clone();
        } catch (final CloneNotSupportedException e) {
            // Will not appear!
            throw new Error("Assertion failed!", e);
        }
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            int prime = 31;
            h = 1;
            h = prime * h + ((exhaustedAction == null) ? 0 : exhaustedAction.hashCode());
            h = prime * h + maxActive;
            h = prime * h + maxIdle;
            h = prime * h + (int) (maxIdleTime ^ (maxIdleTime >>> 32));
            h = prime * h + (int) (maxLifeTime ^ (maxLifeTime >>> 32));
            h = prime * h + (int) (maxWait ^ (maxWait >>> 32));
            h = prime * h + (testOnActivate ? 1231 : 1237);
            h = prime * h + (testOnDeactivate ? 1231 : 1237);
            h = prime * h + (testOnIdle ? 1231 : 1237);
            h = prime * h + (testThreads ? 1231 : 1237);
            hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PoolConfig)) {
            return false;
        }
        PoolConfig other = (PoolConfig) obj;
        if (exhaustedAction != other.exhaustedAction) {
            return false;
        }
        if (maxActive != other.maxActive) {
            return false;
        }
        if (maxIdle != other.maxIdle) {
            return false;
        }
        if (maxIdleTime != other.maxIdleTime) {
            return false;
        }
        if (maxLifeTime != other.maxLifeTime) {
            return false;
        }
        if (maxWait != other.maxWait) {
            return false;
        }
        if (testOnActivate != other.testOnActivate) {
            return false;
        }
        if (testOnDeactivate != other.testOnDeactivate) {
            return false;
        }
        if (testOnIdle != other.testOnIdle) {
            return false;
        }
        if (testThreads != other.testThreads) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Database pooling options:\n\tMaximum idle connections: ");
        sb.append(maxIdle);
        sb.append("\n\tMaximum idle time: ");
        sb.append(maxIdleTime);
        sb.append("ms\n\tMaximum active connections: ");
        sb.append(maxActive);
        sb.append("\n\tMaximum wait time for a connection: ");
        sb.append(maxWait);
        sb.append("ms\n\tMaximum life time of a connection: ");
        sb.append(maxLifeTime);
        sb.append("ms\n\tAction if connections exhausted: ");
        sb.append(exhaustedAction.toString());
        sb.append("\n\tTest connections on activate  : ");
        sb.append(testOnActivate);
        sb.append("\n\tTest connections on deactivate: ");
        sb.append(testOnDeactivate);
        sb.append("\n\tTest idle connections         : ");
        sb.append(testOnIdle);
        sb.append("\n\tTest threads for bad connection usage (SLOW): ");
        sb.append(testThreads);
        return sb.toString();
    }

    /** The default pool configuration */
    public static final PoolConfig DEFAULT_CONFIG = builder()
        .withMaxIdle(-1)
        .withMaxIdleTime(60000)
        .withMaxActive(-1)
        .withMaxWait(10000)
        .withMaxLifeTime(-1)
        .withExhaustedAction(ExhaustedActions.BLOCK)
        .withTestOnActivate(false)
        .withTestOnDeactivate(true)
        .withTestOnIdle(false)
        .withTestThreads(false)
        .build();

}
