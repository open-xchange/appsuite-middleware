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

package com.openexchange.groupware.settings.impl;

import java.util.ArrayList;
import java.util.Arrays;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;

/**
 * {@link ValueSetting}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ValueSetting extends AbstractSetting<ValueSetting> {

    /**
     * Single value of this setting.
     */
    private Object singleValue;

    /**
     * Multi value of this setting.
     */
    private ArrayList<Object> multiValue;

    /**
     * Copy constructor.
     *
     * @param toCopy
     */
    public ValueSetting(final Setting toCopy) {
        super(toCopy.getName(), toCopy.getId(), toCopy.getShared());
//        setParent(toCopy.getParent());
        final Setting[] toCopyElements = toCopy.getElements();
        if (null != toCopyElements) {
            for (final Setting element : toCopyElements) {
                final ValueSetting child = new ValueSetting(element);
                addElement(child);
                child.setParent(this);
            }
        }
    }

    /**
     * @return the multi value.
     */
    @Override
    public Object[] getMultiValue() {
        Object[] retval;
        synchronized (this) {
            if (null == multiValue || 0 == multiValue.size()) {
                retval = null;
            } else {
                retval = multiValue.toArray(new Object[multiValue.size()]);
            }
        }
        return retval;
    }

    @Override
    public boolean isEmptyMultivalue() {
        return multiValue != null && multiValue.isEmpty();
    }

    /**
     * @return the single value.
     */
    @Override
    public Object getSingleValue() {
        return singleValue;
    }

    /**
     * @param value The value to set.
     */
    @Override
    public void setSingleValue(final Object value) {
        this.singleValue = value;
    }

    /**
     * @param value Value to add.
     */
    @Override
    public void addMultiValue(final Object value) {
        if (null != value) {
            synchronized (this) {
                if (null == multiValue) {
                    multiValue = new ArrayList<Object>();
                }
                multiValue.add(value);
            }
        }
    }

    @Override
    public void setEmptyMultiValue() {
        this.multiValue = new ArrayList<Object>();
    }

    @Override
    public void removeElement(final Setting child) throws OXException {
        if (!(child instanceof ValueSetting)) {
            throw SettingExceptionCodes.NOT_ALLOWED.create();
        }
        super.removeElementInternal((ValueSetting) child);
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append(getName());
        out.append('=');
        if (null == getElements()) {
            if (null != multiValue && multiValue.size() > 0) {
                out.append(multiValue);
            } else {
                out.append(singleValue);
            }
        } else {
            out.append('(');
            final ValueSetting[] elements = getElements();
            for (ValueSetting element : elements) {
                out.append(element.toString());
                out.append(',');
            }
            if (elements.length != 0) {
                out.setLength(out.length() - 1);
            }
            out.append(')');
        }
        return out.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof ValueSetting)) {
            return false;
        }
        final ValueSetting other = (ValueSetting) obj;
        if (singleValue != null && !singleValue.equals(other.getSingleValue())) {
            return false;
        }
        if (multiValue != null && !multiValue.equals(Arrays.asList(other.getMultiValue()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int retval = super.hashCode();
        if (singleValue != null) {
            retval ^= singleValue.hashCode();
        }
        if (multiValue != null) {
            retval ^= multiValue.hashCode();
        }
        return retval;
    }
}
