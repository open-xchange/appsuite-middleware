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
    private volatile Object singleValue;

    /**
     * Multi value of this setting.
     */
    private volatile ArrayList<Object> multiValue;

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
            ArrayList<Object> multiValue = this.multiValue;
            if (null == multiValue || multiValue.isEmpty()) {
                retval = null;
            } else {
                retval = multiValue.toArray(new Object[multiValue.size()]);
            }
        }
        return retval;
    }

    @Override
    public boolean isEmptyMultivalue() {
        ArrayList<Object> multiValue = this.multiValue;
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
            ArrayList<Object> multiValue = this.multiValue;
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
        Object singleValue = this.singleValue;
        if (singleValue != null && !singleValue.equals(other.getSingleValue())) {
            return false;
        }
        ArrayList<Object> multiValue = this.multiValue;
        if ((multiValue!= null && other.getMultiValue()==null) || (multiValue==null && other.getMultiValue()!=null)) {
            return false;
        }
        if (multiValue != null && other.getMultiValue() != null && !multiValue.equals(Arrays.asList(other.getMultiValue()))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int retval = super.hashCode();
        Object singleValue = this.singleValue;
        if (singleValue != null) {
            retval ^= singleValue.hashCode();
        }
        ArrayList<Object> multiValue = this.multiValue;
        if (multiValue != null) {
            retval ^= multiValue.hashCode();
        }
        return retval;
    }
}
