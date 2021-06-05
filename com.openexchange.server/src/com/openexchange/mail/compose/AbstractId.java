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

package com.openexchange.mail.compose;

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import com.openexchange.java.Charsets;
import com.openexchange.java.util.UUIDs;

/**
 * {@link AbstractId} - Represents an identifier for a composition space resource.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public abstract class AbstractId {

    /**
     * The primary delimiter: <code>"."</code>.
     */
    public static final String PRIMARY_DELIM = ".";

    /** The service identifier */
    protected final String serviceId;

    /** The resource identifier */
    protected final UUID id;

    /**
     * Initializes a new {@link AbstractId}.
     *
     * @param serviceId The identifier of the composition space service
     * @param id The identifier of the resource
     */
    protected AbstractId(String serviceId, UUID id) {
        super();
        this.serviceId = serviceId;
        this.id = id;
    }

    /**
     * Initializes a new {@link AbstractId}.
     *
     * @param compositeId The composite identifier for the composition space resource
     * @throws IllegalArgumentException If passed composite identifier is <code>null</code> or invalid
     */
    protected AbstractId(String compositeId) {
        super();
        if (null == compositeId) {
            throw new IllegalArgumentException("Composite identifier must not be null");
        }

        List<String> unmangled = unmangle(compositeId);
        serviceId = unmangled.get(0);
        id = UUIDs.fromUnformattedString(unmangled.get(1));
    }

    /**
     * Gets the service identifier
     *
     * @return The service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets the resource identifier
     *
     * @return The resource identifier
     */
    public UUID getId() {
        return id;
    }

    @Override
    public String toString() {
        return new StringBuilder(encodeQP(serviceId)).append(PRIMARY_DELIM).append(encodeQP(UUIDs.getUnformattedString(id))).toString();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Parses specified mangled identifier into its String components.
     *
     * @param mangled The mangled identifier
     * @return The identifier's components
     */
    private static List<String> unmangle(String mangled) {
        if (null == mangled) {
            return null;
        }

        List<String> list = new ArrayList<String>(2);
        // Find first delimiter
        int pos = mangled.indexOf(PRIMARY_DELIM, 0);
        if (pos < 0) {
            list.add(CompositionSpaceServiceFactory.DEFAULT_SERVICE_ID);
            list.add(decodeQP(mangled));
        } else {
            list.add(decodeQP(mangled.substring(0, pos)));
            list.add(decodeQP(mangled.substring(pos + PRIMARY_DELIM.length())));
        }
        return list;
    }

    private static final BitSet PRINTABLE_CHARS;
    // Static initializer for printable chars collection
    static {
        final BitSet bitSet = new BitSet(256);
        for (int i = '0'; i <= '9'; i++) {
            bitSet.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            bitSet.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            bitSet.set(i);
        }
        bitSet.set('.');
        bitSet.set('-');
        bitSet.set('_');
        PRINTABLE_CHARS = bitSet;
    }

    private static String encodeQP(String string) {
        try {
            return Charsets.toAsciiString(QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, string.getBytes(com.openexchange.java.Charsets.UTF_8)));
        } catch (UnsupportedCharsetException e) {
            // Cannot occur
            return string;
        }
    }

    private static String decodeQP(String string) {
        try {
            return new String(QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(string)), com.openexchange.java.Charsets.UTF_8);
        } catch (DecoderException e) {
            return string;
        }
    }

}
