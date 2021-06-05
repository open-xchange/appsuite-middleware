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

package com.openexchange.groupware.contact.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.TimeZoneUtils;

/**
 * This switcher is able to convert a given String into a date using
 * SimpleDateFormat and then pass it on to its delegate.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class ContactSwitcherForSimpleDateFormat extends AbstractContactSwitcherWithDelegate {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactSwitcherForSimpleDateFormat.class);

    private static interface DateValidator {

        boolean isValid(String dateString);
    }

    private static final DateValidator DEFAULT_VALIDATOR = new DateValidator() {

        @Override
        public boolean isValid(final String dateString) {
            return true;
        }
    };

    private static final class RegexDateValidator implements DateValidator {

        private final Pattern invalidPattern;

        public RegexDateValidator(final Pattern pattern) {
            super();
            this.invalidPattern = pattern;
        }

        @Override
        public boolean isValid(final String dateString) {
            return !invalidPattern.matcher(dateString).matches();
        }

    }

    private static DateValidator getValidatorFor(final DateFormat dateFormat) {
        final String pattern = getPatternFrom(dateFormat);
        if (null == pattern) {
            return DEFAULT_VALIDATOR;
        }
        try {
            final String invalidRegex = pattern.replaceAll("[a-zA-Z]+", "0+");
            return new RegexDateValidator(Pattern.compile(invalidRegex));
        } catch (RuntimeException e) {
            LOG.error("", e);
            return DEFAULT_VALIDATOR;
        }
    }

    private static String getPatternFrom(final DateFormat dateFormat) {
        if (!(dateFormat instanceof SimpleDateFormat)) {
            return null;
        }
        return ((SimpleDateFormat) dateFormat).toPattern();
    }

    /*
     * Member stuff
     */

    private final List<DateFormat> dateFormats;

    private final List<DateValidator> dateValidators;

    /**
     * Initializes a new {@link ContactSwitcherForSimpleDateFormat}.
     */
    public ContactSwitcherForSimpleDateFormat() {
        super();
        dateFormats = new LinkedList<DateFormat>();
        dateValidators = new LinkedList<DateValidator>();
    }

    private Object[] makeDate(final Object... objects) throws OXException {
        if (objects[1] instanceof String) {
            /*
             * Not parsed by previous ContactSwitcher
             */
            final String dateString = (String) objects[1];
            if (dateString.contains("/") && dateString.split("/").length == 3) {
                return parseDateStringManually(dateString, objects);
            } else {
                return parseDateStringByDateFormat(dateString, objects);
            }            
        }
        return objects;
    }        
    
    private Object[] parseDateStringByDateFormat(String dateString, final Object... objects) throws OXException {
        final int size = dateFormats.size();
        for (int index = 0; index < size; index++) {
            try {
                DateFormat dateFormat = dateFormats.get(index);
                synchronized (dateFormat) {
                    objects[1] = dateFormat.parse(dateString);                     
                }
                /*
                 * Parse successful so far. Is invalid?
                 */
                if (!dateValidators.get(index).isValid(dateString)) {
                    /*
                     * Detected invalid value. Set to null.
                     */
                    objects[1] = null;
                    ((Contact) objects[0]).addWarning(ContactExceptionCodes.DATE_CONVERSION_FAILED.create(dateString));
                }
                return objects;
            } catch (ParseException e) {
                LOG.debug(e.getMessage());
            }
        }        
        throw ContactExceptionCodes.DATE_CONVERSION_FAILED.create((String) objects[1]);
    }
    
    private Object[] parseDateStringManually(String dateString, final Object[] objects) throws OXException {
        try {
            String[] split = dateString.split("/");
            final TimeZone utc = TimeZoneUtils.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat();
            df.setTimeZone(utc);            
            if (Integer.valueOf(split[0]) > 12) {
                df.applyPattern("MM/dd/yyyy");
                objects[1] = df.parse(split[1]+"/"+split[0]+"/"+split[2]);
            } else if (Integer.valueOf(split[1]) > 12) {
                df.applyPattern("dd/MM/yyyy");
                objects[1] = df.parse(split[1]+"/"+split[0]+"/"+split[2]);
            } else {
                parseDateStringByDateFormat(dateString, objects);
            }
        } catch (ParseException e) {
            LOG.debug(e.getMessage());
        } catch (NumberFormatException e) {
            LOG.debug(e.getMessage());
        } 
        return objects;
    }

    /**
     * Adds specified {@link DateFormat date format} to this switcher.
     *
     * @param dateFormat The date format to add
     */
    public void addDateFormat(final DateFormat dateFormat) {
        dateFormats.add(dateFormat);
        dateValidators.add(getValidatorFor(dateFormat));
    }

    /* CHANGED METHODS */
    @Override
    public Object creationdate(final Object... objects) throws OXException {
        try {
            return delegate.creationdate(makeDate(objects));
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "CreationDate");
        }
    }

    @Override
    public Object anniversary(final Object... objects) throws OXException {
        try {
            return delegate.anniversary(makeDate(objects));
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "Anniversary");
        }
    }

    @Override
    public Object birthday(final Object... objects) throws OXException {
        try {
            return delegate.birthday(makeDate(objects));
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "Birthday");
        }
    }

    @Override
    public Object imagelastmodified(final Object... objects) throws OXException {
        try {
            return delegate.imagelastmodified(makeDate(objects));
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "ImageLastModified");
        }
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        try {
            return delegate.lastmodified(makeDate(objects));
        } catch (ClassCastException e) {
            throw ContactExceptionCodes.CONV_OBJ_2_DATE_FAILED.create(e, objects[1], "LastModified");
        }
    }
}
