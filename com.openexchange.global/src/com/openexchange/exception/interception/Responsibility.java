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

package com.openexchange.exception.interception;


/**
 * Module / action combination to define one responsibility of an {@link OXExceptionInterceptor}. An {@link OXExceptionInterceptor} is able
 * to deal with (almost) unlimited {@link Responsibility}s
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class Responsibility {

    /**
     * The identifier for all modules/actions.
     */
    public static final String ALL = "*";

    /**
     * The {@link Responsibility} constant for all modules/actions.
     */
    public static final Responsibility RESPONSIBILITY_ALL = new Responsibility(ALL, ALL);

    // ------------------------------------------------------------------------------------------------------------------------

    private final String module;
    private final String action;
    private final int hash;

    /**
     * Initializes a new {@link Responsibility}.
     *
     * @param module The module the {@link OXExceptionInterceptor} is responsible for
     * @param action The module the {@link OXExceptionInterceptor} is responsible for.
     */
    public Responsibility(String module, String action) {
        super();
        if (module == null) {
            throw new IllegalArgumentException("Module might not be null");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action might not be null");
        }

        this.module = module;
        this.action = action;
        hash = module.hashCode() ^ action.hashCode();
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Gets the action
     *
     * @return The action
     */
    public String getAction() {
        return action;
    }

    /**
     * Checks if this responsibility implies given responsibility instance.
     *
     * @param responsibility The responsibility
     * @return <code>true</code> if implied; otherwise <code>false</code>
     */
    public boolean implies(Responsibility responsibility) {
        return implies(responsibility.module, responsibility.action);
    }

    /**
     * Checks if this responsibility implies given module/action combination.
     *
     * @param module The module identifier
     * @param action The action identifier
     * @return <code>true</code> if implied; otherwise <code>false</code>
     */
    public boolean implies(String module, String action) {
        if (null == module || null == action) {
            return false;
        }

        String thisModule = this.module;
        String thisAction = this.action;
        return (ALL.equals(thisModule) || thisModule.equals(module)) && (ALL.equals(thisAction) || thisAction.equals(action));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Responsibility)) {
            return false;
        }
        Responsibility pairo = (Responsibility) o;
        return this.module.equals(pairo.getModule()) && this.action.equals(pairo.getAction());
    }

    @Override
    public int hashCode() {
        return hash;
    }

}
