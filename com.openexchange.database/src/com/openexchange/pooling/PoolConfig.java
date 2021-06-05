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
        private boolean alwaysCheckOnActivate;

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
            alwaysCheckOnActivate = false;
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
            alwaysCheckOnActivate = source.alwaysCheckOnActivate;
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

        public Builder withAlwaysCheckOnActivate(boolean alwaysCheckOnActivate) {
            this.alwaysCheckOnActivate = alwaysCheckOnActivate;
            return this;
        }

        public PoolConfig build() {
            return new PoolConfig(maxIdle, maxIdleTime, maxActive, maxWait, maxLifeTime, exhaustedAction, testOnActivate, testOnDeactivate, testOnIdle, testThreads, alwaysCheckOnActivate);
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

    public final boolean alwaysCheckOnActivate;

    private int hash = 0;

    PoolConfig(int maxIdle, long maxIdleTime, int maxActive, long maxWait, long maxLifeTime, ExhaustedActions exhaustedAction, boolean testOnActivate, boolean testOnDeactivate, boolean testOnIdle, boolean testThreads, boolean alwaysCheckOnActivate) {
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
        this.alwaysCheckOnActivate = alwaysCheckOnActivate;
    }

    @Override
    public PoolConfig clone() {
        try {
            return (PoolConfig) super.clone();
        } catch (CloneNotSupportedException e) {
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
            h = prime * h + (alwaysCheckOnActivate ? 1231 : 1237);
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
        if (alwaysCheckOnActivate != other.alwaysCheckOnActivate) {
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
        sb.append("\n\tAlways explicitly check connection validity on activate: ");
        sb.append(alwaysCheckOnActivate);
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
        .withAlwaysCheckOnActivate(false)
        .build();

}
