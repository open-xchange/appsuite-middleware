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

package com.openexchange.hazelcast.serialization;

import java.io.IOException;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.serialization.osgi.Services;

/**
 * {@link PortableCheckForBooleanConfigOptionCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class PortableCheckForBooleanConfigOptionCallable extends AbstractCustomPortable implements Callable<Boolean> {

    /** The class identifier */
    public static final int CLASS_ID = 900;

    private static final String FIELD_NAMED = "name";
    private static final String FIELD_DEFAULT_VALUE = "def";

    private String name;
    private boolean defaultValue;

    /**
     * Initializes a new {@link PortableCheckForBooleanConfigOptionCallable}.
     */
    public PortableCheckForBooleanConfigOptionCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableCheckForBooleanConfigOptionCallable}.
     *
     * @param name The name of the boolean property; e.g. <code>"com.openexchange.mymodule.enabled"</code>
     * @param defaultValue The default value to assume
     */
    public PortableCheckForBooleanConfigOptionCallable(String name, boolean defaultValue) {
        super();
        this.name = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public Boolean call() throws Exception {
        boolean defaulValue = true;
        ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        return Boolean.valueOf(null == configurationService ? defaulValue : configurationService.getBoolProperty(name, defaulValue));
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(FIELD_NAMED, name);
        writer.writeBoolean(FIELD_DEFAULT_VALUE, defaultValue);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.name = reader.readUTF(FIELD_NAMED);
        this.defaultValue = reader.readBoolean(FIELD_DEFAULT_VALUE);
    }

}
