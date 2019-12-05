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

package com.openexchange.hazelcast.serialization;

import java.io.IOException;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
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
