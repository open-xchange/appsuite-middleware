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

package com.openexchange.multifactor;

/**
 * {@link ParameterlessMultifactorDevice} - A convenience class which wrapps an existing {@link MultifactorDevice} and hides all it's provider specific parameters.
 * <br>
 * <br>
 * Useful for supporting a more defense programming style when returning multifactor devices to a caller.
 * i.e. useful to prevent the revelation of security related parameters, like shared secrets, phone numbers or other sensitive informations to a caller.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class ParameterlessMultifactorDevice extends AbstractMultifactorDevice {

    /**
     * Initializes a new {@link ParameterlessMultifactorDevice}.
     *
     * @param delegate The {@link MultifasctorDevice} to hide all parameters for
     */
    public ParameterlessMultifactorDevice(MultifactorDevice delegate) {
        super(delegate.getId(),
              delegate.getProviderName(),
              delegate.getName(),
              null /* NO PARAMTERS ! */);
        setBackup(delegate.isBackup());
        enable(delegate.isEnabled());
    }

    @Override
    protected void setParameter(String name, Object value) {
        throw new UnsupportedOperationException();
    }
}
