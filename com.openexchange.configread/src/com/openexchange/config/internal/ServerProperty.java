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

package com.openexchange.config.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.config.cascade.ConfigCascadeExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link ServerProperty}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ServerProperty implements BasicProperty {

    private final String value;
    private final Map<String, String> metadata;

    /**
     * Initializes a new {@link ServerProperty}.
     *
     * @param value The property's value
     * @param metadata The optional metadata of the property
     */
    public ServerProperty(String value, Map<String, String> metadata) {
        super();
        this.value = value;
        this.metadata = metadata;
    }

    /**
     * Gets the metadata reference.
     *
     * @return The metadata reference
     */
    Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public String get(String metadataName) {
        return null == metadata ? null : metadata.get(metadataName);
    }

    @Override
    public boolean isDefined() {
        return null != value;
    }

    /**
     * Unsupported.
     */
    @Override
    public void set(String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_SET_PROPERTY.create("", "server");
    }

    /**
     * Unsupported.
     */
    @Override
    public void set(String metadataName, String value) throws OXException {
        throw ConfigCascadeExceptionCodes.CAN_NOT_DEFINE_METADATA.create(metadataName, "server");
    }

    @Override
    public List<String> getMetadataNames() throws OXException {
        return new ArrayList<String>(metadata.keySet());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("ServerProperty [");
        if (value != null) {
            builder.append("value=").append(value).append(", ");
        }
        builder.append("defined=").append(isDefined()).append(", ");
        if (metadata != null) {
            builder.append("metadata=").append(metadata);
        }
        builder.append("]");
        return builder.toString();
    }

}
