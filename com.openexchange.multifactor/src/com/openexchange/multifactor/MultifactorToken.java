package com.openexchange.multifactor;
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

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link MultifactorToken} - Represents a token which can be used for Multifactor authentication.
 * <p>
 * Some multifactor mechanism (for example SMS) require the input of a token in order to authenticate.
 * The provider needs to temporary store this tokens for a limited amount of time.
 * </p>
 *
 * <p>
 * This base class can be used to implement a concrete token holding a value for unlimited or a specific amount of time.
 * </p>
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorToken<T> {

    private final T                 value;
    private final Instant           createdOn;
    private final Optional<Instant> endOfLife;
    private final Duration          lifeTime;

    /**
     * Initializes a new {@link MultifactorToken} with a unlimited lifetime
     *
     * @param value The token value
     */
    public MultifactorToken(T value) {
        this(value, null);
    }

    /**
     * Initializes a new {@link MultifactorToken} with a limited lifetime
     *
     * @param value The token value
     * @param lifeTime The lifetime of the value
     */
    public MultifactorToken(T value, Duration lifeTime) {
        this.value = value;
        this.lifeTime = lifeTime;
        this.createdOn = Instant.now();
        this.endOfLife = lifeTime != null ?
            Optional.of(this.createdOn.plus(lifeTime)) :
            Optional.empty();
    }

    /**
     * Gets the token's value
     *
     * @return The value
     */
    public T getValue() {
        return value;
    }

    /**
     * Returns whether the token is expired or not
     *
     * @return True, if the token is expired, false otherwise
     */
    public boolean isExpired() {
       return endOfLife.map(
           eol -> Instant.now().isAfter(eol) ? Boolean.TRUE : Boolean.FALSE
       ).orElse(Boolean.FALSE).booleanValue();
    }

    /**
     * Gets the life time of the token
     *
     * @return The token's life time
     */
    public Optional<Duration> getLifeTime() {
        return Optional.ofNullable(lifeTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MultifactorToken)) {
           return false;
        }

        final MultifactorToken<?> t = (MultifactorToken<?>) obj;
        return this.value.equals(t.value) &&
               this.createdOn.equals(t.createdOn) &&
               this.endOfLife.equals(t.endOfLife);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, createdOn, endOfLife.orElse(null));
    }
}
