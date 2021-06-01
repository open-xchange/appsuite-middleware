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

package com.openexchange.java;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * {@link Functions}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class Functions {

    /**
     * {@link OXFunction} Represents an exception aware function that accepts one argument and produces a result.
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.5
     * @param <T> The type of the input to the function
     * @param <R> The type of the result of the function
     * @param <E> The type of exception
     */
    @FunctionalInterface
    public interface OXFunction<T, R, E extends Exception> {

        /**
         * Applies this function to the given argument.
         *
         * @param t The function argument
         * @return The function result
         * @throws E In case the result can't be formulated
         */
        R apply(T t) throws E;

        /**
         * Applies this function to the given argument. In case of error
         * the exception will be given to the consumer
         *
         * @param t The function argument
         * @param log The consumer to log the exception
         * @return The function result or an empty optional
         */
        default Optional<R> consumeError(T t, Consumer<Exception> log) {
            try {
                return Optional.ofNullable(apply(t));
            } catch (Exception e) {
                log.accept(e);
            }
            return Optional.empty();
        }
    }

    /**
     * {@link OXBiFunction} Represents an exception aware function that accepts two arguments and produces a result.
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     * @param <T> The type of the input to the function
     * @param <U> the type of the second argument to the function
     * @param <R> The type of the result of the function
     * @param <E> The type of exception
     */
    @FunctionalInterface
    public interface OXBiFunction<T, U, R, E extends Exception> {

        /**
         * Applies this function to the given argument.
         *
         * @param t The function argument
         * @param u The second function argument
         * @return The function result
         * @throws E In case the result can't be formulated
         */
        R apply(T t, U u) throws E;
    }
}
