package com.openexchange.multifactor;
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
           eol -> Instant.now().isAfter(eol)
       ).orElse(false);
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

        if(obj == this) {
            return true;
        }

        if(!(obj instanceof MultifactorToken)) {
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
