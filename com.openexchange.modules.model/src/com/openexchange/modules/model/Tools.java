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

package com.openexchange.modules.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Tools {

    public static <T extends Model<T>> Object inject(T thing, AttributeHandler<T> handler, Object initial, List<? extends Attribute<T>> fields) {

        Object carryover = initial;
        for (Attribute<T> attribute : fields) {
            carryover = handler.handle(attribute, carryover, thing);
        }

        return carryover;
    }

    public static <T extends Model<T>> List<Object> each(T thing, AttributeHandler<T> handler, List<? extends Attribute<T>> fields) {

        List<Object> retval = new ArrayList<Object>(fields.size());

        for (Attribute<T> attribute : fields) {
            retval.add(handler.handle(attribute, thing.get(attribute)));
        }

        return retval;
    }

    public static <T extends Model<T>> List<Object> values(T thing, List<? extends Attribute<T>> fields, AttributeHandler<T> modifier) {
        List<Object> retval = new ArrayList<Object>(fields.size());

        for (Attribute<T> attribute : fields) {
            Object value = thing.get(attribute);
            Object overridden = modifier.handle(attribute, value, thing);
            retval.add((overridden != null) ? overridden : value);
        }

        return retval;
    }

    public static <T extends Model<T>> List<Object> values(T thing, List<? extends Attribute<T>> fields) {
        return values(thing, fields, AttributeHandler.DO_NOTHING);
    }

    public static <T extends Model<T>> T copy(T thing, AttributeHandler<T> modifier) {
        T copy = thing.getMetadata().create();
        for (Attribute<T> attribute : thing.getMetadata().getAllFields()) {
            Object value = thing.get(attribute);
            Object overridden = modifier.handle(attribute, value, thing);
            copy.set(attribute, (overridden != null) ? overridden : value);
        }
        return copy;
    }

    public static <T extends Model<T>> T copy(T thing) {
        return (T) copy(thing, AttributeHandler.DO_NOTHING);
    }

    public static <T extends Model<T>> List<T> copy(List<T> things, AttributeHandler<T> modifier) {
        List<T> retval = new ArrayList<T>(things.size());
        for(T thing : things) {
            retval.add(copy(thing, modifier));
        }

        return retval;
    }

    public static <T extends Model<T>> List<T> copy(List<T> things) {
        return copy(things, AttributeHandler.DO_NOTHING);
    }

    public static <T extends Model<T>> void set(Collection<T> things, Attribute<T> attribute, Object value) {
        for (T thing : things) {
            thing.set(attribute, value);
        }
    }

}
