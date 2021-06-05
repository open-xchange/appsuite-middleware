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

package com.openexchange.session;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * A {@link SessionSpecificContainerRetrievalService} manages containers bound to a session and its lifecycle.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@SingletonService
public interface SessionSpecificContainerRetrievalService {

    public static final Lifecycle DEFAULT_LIFECYCLE = Lifecycle.HIBERNATE;

    public static enum Lifecycle {
        /**
         * Removes the values once a long-running session goes into hibernation or termination. This is the default.
         */
        HIBERNATE,

        /**
         * Removes values once a session is terminated, either through log out, or when a long running session exceeds even its timeout. DO
         * NOT USE THIS. Or, if you have to, use this responsibly as we have to keep session data stored in hibernating sessions to an
         * absolute minimum.
         */
        TERMINATE;

        /**
         * Find out whether the given lifecycle is included in this lifecycle.
         *
         * @return
         */
        public boolean includes(Lifecycle other) {
            // Terminate includes itself, Hibernate includes both, so only terminate does not include hibernate.
            // Yes I know this can be expressed shorter, but then it becomes even less understandable.
            if (this == TERMINATE && other == HIBERNATE) {
                return false;
            }
            return true;
        }

    }

    /**
     * Creates or retrieves a session scoped container for the given name. Due to the lifecycle management and session migration (which
     * might not migrate these data sets) all values should be considered volatile.
     *
     * @param <T> The type of variable to be stored
     * @param name A namespace for the variables. Typically it should be constructed out of a bundles identifier and a postfix for each
     *            variable that has to be managed. Must not be null.
     * @param lifecycle The lifecycle the session container will be subjected to. May be null, in which case it defaults to
     *            HIBERNATE.
     * @param initial A factory to produce initial values, in case no value has previously been bound to a certain session. May be null, in
     *            which case the initial value will always be null. Note: Initial values do not show up in iterators or #contains calls,
     *            they will only be created on retrieval via #get.
     * @param cleanUp A callback to provide callers the chance to perform clean up operations on a value that will be destroyed either due
     *            to a session changing its lifecycle state, through a destroy call or because it is deleted from the session container by a
     *            client call. May be null, in which case no cleanup will be performed.
     * @return A session scoped container
     */
    public <T> SessionScopedContainer<T> getContainer(String name, Lifecycle lifecycle, InitialValueFactory<T> initial, CleanUp<T> cleanUp);

    /**
     * Destroys a container in the given namespace. CleanUp will be invoked on all values currently bound to any session. May optionally
     * pass a clean up operation to be invoked instead of the registered one. Call this on bundle shutdown to get rid of all callbacks you
     * may have registered.
     *
     * @param name The namespace for the variables.
     * @param cleanUp A clean up operation overriding the registered one. May be null in which case the regular clean up operation is
     *            applied.
     */
    public void destroyContainer(String name, CleanUp<?> cleanUp);

    /**
     * Creates or retrieves a random token container.
     * @param <T> The type of objects to store in the container.
     * @param name A namespace for the variables. Typically it should be constructed out of a bundles identifier and a postfix for each
     *            variable that has to be managed. Must not be null.
     * @param lifecycle The lifecycle the session container will be subjected to. May be null, in which case it defaults to
     *            HIBERNATE.
     * @param cleanUp A callback to provide callers the chance to perform clean up operations on a value that will be destroyed either due
     *            to a session changing its lifecycle state, through a destroy call or because it is deleted from the session container by a
     *            client call. May be null, in which case no cleanup will be performed.
     * @return A random token container
     */
    public <T> RandomTokenContainer<T> getRandomTokenContainer(String name, Lifecycle lifecycle, CleanUp<T> cleanUp);

    /**
     * Destroys a container in the given namespace. CleanUp will be invoked on all values currently bound to any session. May optionally
     * pass a clean up operation to be invoked instead of the registered one. Call this on bundle shutdown to get rid of all callbacks you
     * may have registered.
     *
     * @param name The namespace for the variables.
     * @param cleanUp A clean up operation overriding the registered one. May be null in which case the regular clean up operation is
     *            applied.
     */
    public void destroyRandomTokenContainer(String name, CleanUp<?> cleanUp);


    /**
     * An {@link InitialValueFactory} is used to seed a session scoped container with an initial value.
     */
    public static interface InitialValueFactory<T> {

        public T create();
    }

    /**
     * A {@link CleanUp} operation is invoked once a value has been removed from the session container.
     */
    public static interface CleanUp<T> {

        public void clean(T thing);
    }
}
