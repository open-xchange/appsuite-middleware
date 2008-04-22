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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.mail.search.SearchTerm;

/**
 * {@link MailField} - An enumeration of mail fields to define which fields to
 * prefill.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public enum MailField {

	/**
	 * The mail ID<br>
	 * <b>[low cost]</b>
	 */
	ID(MailListField.ID),
	/**
	 * The folder ID<br>
	 * <b>[low cost]</b>
	 */
	FOLDER_ID(MailListField.FOLDER_ID),
	/**
	 * The Content-Type; includes whether message contains attachments or not<br>
	 * <b>[low cost]</b>
	 */
	CONTENT_TYPE(MailListField.ATTACHMENT),
	/**
	 * From<br>
	 * <b>[low cost]</b>
	 */
	FROM(MailListField.FROM),
	/**
	 * To<br>
	 * <b>[low cost]</b>
	 */
	TO(MailListField.TO),
	/**
	 * Cc<br>
	 * <b>[low cost]</b>
	 */
	CC(MailListField.CC),
	/**
	 * Bcc<br>
	 * <b>[low cost]</b>
	 */
	BCC(MailListField.BCC),
	/**
	 * Subject<br>
	 * <b>[low cost]</b>
	 */
	SUBJECT(MailListField.SUBJECT),
	/**
	 * Size<br>
	 * <b>[low cost]</b>
	 */
	SIZE(MailListField.SIZE),
	/**
	 * Sent date<br>
	 * <b>[low cost]</b>
	 */
	SENT_DATE(MailListField.SENT_DATE),
	/**
	 * Received date<br>
	 * <b>[low cost]</b>
	 */
	RECEIVED_DATE(MailListField.RECEIVED_DATE),
	/**
	 * Flags<br>
	 * <b>[low cost]</b>
	 */
	FLAGS(MailListField.FLAGS),
	/**
	 * Thread level<br>
	 * <b>[low cost]</b>
	 */
	THREAD_LEVEL(MailListField.THREAD_LEVEL),
	/**
	 * <code>Disposition-Notification-To</code><br>
	 * <b>[low cost]</b>
	 */
	DISPOSITION_NOTIFICATION_TO(MailListField.DISPOSITION_NOTIFICATION_TO),
	/**
	 * Priority<br>
	 * <b>[low cost]</b>
	 */
	PRIORITY(MailListField.PRIORITY),
	/**
	 * Color Label<br>
	 * <b>[low cost]</b>
	 */
	COLOR_LABEL(MailListField.COLOR_LABEL),
	/**
	 * To peek the mail body (\Seen flag is left unchanged)<br>
	 * <b>[high cost]</b>
	 */
	BODY(null),
	/**
	 * To fetch all message headers<br>
	 * <b>[high cost]</b>
	 */
	HEADERS(null),
	/**
	 * To fully pre-fill mail incl. headers and peeked body (\Seen flag is left
	 * unchanged)<br>
	 * <b>[high cost]</b>
	 */
	FULL(null);

	private final MailListField listField;

	private MailField(final MailListField listField) {
		this.listField = listField;
	}

	/**
	 * Gets the corresponding instance of {@link MailListField} or
	 * <code>null</code> if none exists.
	 * 
	 * @return The corresponding instance of {@link MailListField} or
	 *         <code>null</code> if none exists.
	 */
	public MailListField getListField() {
		return listField;
	}

	/**
	 * Gets the corresponding instances of {@link MailListField} for specified
	 * instances of {@link MailField}.
	 * <p>
	 * Those mail fields which have no corresponding list field are omitted.
	 * 
	 * @param fields
	 *            The instances of {@link MailField}
	 * @return The corresponding instances of {@link MailListField}
	 */
	public static final MailListField[] toListFields(final MailField[] fields) {
		if (null == fields) {
			return null;
		}
		final List<MailListField> listFields = new ArrayList<MailListField>(fields.length);
		for (final MailField mailField : fields) {
			if (null != mailField.getListField()) {
				listFields.add(mailField.getListField());
			}
		}
		return listFields.toArray(new MailListField[listFields.size()]);
	}

	/**
	 * Gets the corresponding instances of {@link MailListField} for specified
	 * collection of {@link MailField}.
	 * <p>
	 * Those mail fields which have no corresponding list field are omitted.
	 * 
	 * @param fields
	 *            The collection of {@link MailField}
	 * @return The corresponding instances of {@link MailListField}
	 */
	public static final MailListField[] toListFields(final Collection<MailField> fields) {
		if (null == fields) {
			return null;
		}
		return toListFields(fields.toArray(new MailField[fields.size()]));
	}

	/**
	 * Gets the corresponding instances of {@link MailField} for specified
	 * instances of {@link MailListField}.
	 * 
	 * @param listFields
	 *            The instances of {@link MailListField}
	 * @return The corresponding instances of {@link MailField}
	 */
	public static final MailField[] toFields(final MailListField[] listFields) {
		if (null == listFields) {
			return null;
		}
		final MailField[] fields = new MailField[listFields.length];
		for (int i = 0; i < listFields.length; i++) {
			fields[i] = toField(listFields[i]);
		}
		return fields;
	}

	/**
	 * Gets the corresponding instance of {@link MailField} for specified
	 * instance of {@link MailListField}.
	 * 
	 * @param listField
	 *            The instance of {@link MailListField}
	 * @return The corresponding instance of {@link MailField}
	 */
	public static final MailField toField(final MailListField listField) {
		if (null == listField) {
			return null;
		}
		final MailField[] fields = MailField.values();
		for (final MailField mailField : fields) {
			if (mailField.getListField().equals(listField)) {
				return mailField;
			}
		}
		return null;
	}

	private static final MailField[] EMPTY_FIELDS = new MailField[0];

	/**
	 * Creates an array of {@link MailField} corresponding to given
	 * <code>int</code> values.
	 * <p>
	 * This is just a convenience method that invokes {@link #getField(int)} for
	 * every <code>int</code> value.
	 * 
	 * @see #getField(int)
	 * @param fields
	 *            The <code>int</code> values
	 * @return The array of {@link MailField} corresponding to given
	 *         <code>int</code> values
	 */
	public static final MailField[] getFields(final int[] fields) {
		if ((fields == null) || (fields.length == 0)) {
			return EMPTY_FIELDS;
		}
		final MailField[] retval = new MailField[fields.length];
		for (int i = 0; i < fields.length; i++) {
			retval[i] = getField(fields[i]);
		}
		return retval;
	}

	/**
	 * Maps specified <code>int</code> value to a mail field. A negative
	 * <code>int</code> value is mapped to {@link MailField#BODY}.
	 * <p>
	 * Mail fields which do not hold a corresponding list field are not mappable
	 * to an <code>int</code> value; in consequence they are ignored
	 * 
	 * @param field
	 *            The <code>int</code> value
	 * @return The mapped {@link MailField} or <code>null</code> if no
	 *         corresponding mail field could be found
	 */
	public static final MailField getField(final int field) {
		if (field < 0) {
			return MailField.BODY;
		}
		final MailField[] fields = MailField.values();
		for (final MailField mailField : fields) {
			final MailListField listField = mailField.getListField();
			if ((listField != null) && (listField.getField() == field)) {
				return mailField;
			}
		}
		return null;
	}

	/**
	 * Gets the mail fields addressed by given search term
	 * 
	 * @param searchTerm
	 *            The search term
	 * @return The addressed mail fields
	 */
	public static Set<MailField> getMailFieldsFromSearchTerm(final SearchTerm<?> searchTerm) {
		final Set<MailField> set = new HashSet<MailField>();
		if (searchTerm instanceof com.openexchange.mail.search.HeaderTerm) {
			set.add(MailField.HEADERS);
		} else if (searchTerm instanceof com.openexchange.mail.search.FlagTerm) {
			set.add(MailField.FLAGS);
		} else if (searchTerm instanceof com.openexchange.mail.search.FromTerm) {
			set.add(MailField.FROM);
		} else if (searchTerm instanceof com.openexchange.mail.search.ToTerm) {
			set.add(MailField.TO);
		} else if (searchTerm instanceof com.openexchange.mail.search.CcTerm) {
			set.add(MailField.CC);
		} else if (searchTerm instanceof com.openexchange.mail.search.BccTerm) {
			set.add(MailField.BCC);
		} else if (searchTerm instanceof com.openexchange.mail.search.SubjectTerm) {
			set.add(MailField.SUBJECT);
		} else if (searchTerm instanceof com.openexchange.mail.search.SizeTerm) {
			set.add(MailField.SIZE);
		} else if (searchTerm instanceof com.openexchange.mail.search.SentDateTerm) {
			set.add(MailField.SENT_DATE);
		} else if (searchTerm instanceof com.openexchange.mail.search.ReceivedDateTerm) {
			set.add(MailField.RECEIVED_DATE);
		} else if (searchTerm instanceof com.openexchange.mail.search.ANDTerm) {
			final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ANDTerm) searchTerm)
					.getPattern();
			set.addAll(getMailFieldsFromSearchTerm(terms[0]));
			set.addAll(getMailFieldsFromSearchTerm(terms[1]));
		} else if (searchTerm instanceof com.openexchange.mail.search.ORTerm) {
			final com.openexchange.mail.search.SearchTerm<?>[] terms = ((com.openexchange.mail.search.ORTerm) searchTerm)
					.getPattern();
			set.addAll(getMailFieldsFromSearchTerm(terms[0]));
			set.addAll(getMailFieldsFromSearchTerm(terms[1]));
		} else if (searchTerm instanceof com.openexchange.mail.search.BodyTerm) {
			set.add(MailField.BODY);
		}
		return set;
	}
}
