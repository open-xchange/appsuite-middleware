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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.eav;

import java.util.TimeZone;
import com.openexchange.exception.OXException;

/**
 * {@link EAVNodeTypeCoercionVisitor}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EAVNodeTypeCoercionVisitor extends AbstractEAVExceptionHolder implements AbstractNodeVisitor<EAVNode> {

    private final EAVTypeMetadataNode metadata;

    private EAVTypeCoercion coercion = null;

    private final TimeZone defaultTZ;

    public EAVNodeTypeCoercionVisitor(final EAVTypeMetadataNode metadata, final TimeZone defaultTZ, final EAVTypeCoercion.Mode mode) {
        this.metadata = metadata;
        this.defaultTZ = defaultTZ;
        this.coercion = new EAVTypeCoercion(mode);
    }

    @Override
    public void visit(final int index, final EAVNode node) {
        if (!node.isLeaf()) {
            return;
        }
        final EAVTypeMetadataNode metadataNode = metadata.resolve(node.getPath().shiftLeft());
        if (metadataNode == null) {
            return;
        }

        try {
            if (node.getContainerType() != null && metadataNode.getContainerType() != null && node.getContainerType().isMultiple() != metadataNode.getContainerType().isMultiple()) {
                setException(EAVErrorMessage.WRONG_TYPES.create(
                    node.getPath(),
                    node.getTypeDescription(),
                    metadataNode.getTypeDescription()));
                throw BREAK;
            }
            EAVType type = metadataNode.getType();
            if (type == null) {
                type = node.getType();
            }
            if (node.isMultiple()) {
                EAVContainerType containerType = metadataNode.getContainerType();
                if (containerType == null) {
                    containerType = node.getContainerType();
                }
                final Object[] origPayload = (Object[]) node.getPayload();
                final Object[] coercedPayload = coercion.coerceMultiple(node.getType(), origPayload, metadataNode, defaultTZ);
                final Object[] restrictedPayload = containerType.applyRestrictions(type, coercedPayload);
                node.setPayload(type, containerType, restrictedPayload);
            } else {
                final Object payload = coercion.coerce(node.getType(), node.getPayload(), metadataNode, defaultTZ);
                node.setPayload(type, node.getContainerType(), payload);
            }
        } catch (final OXException e) {
            setException(e);
            throw BREAK;
        }
    }

}
