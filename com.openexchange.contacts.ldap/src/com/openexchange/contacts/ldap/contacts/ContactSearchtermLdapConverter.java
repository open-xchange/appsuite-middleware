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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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


package com.openexchange.contacts.ldap.contacts;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.parser.ContactSearchTermConverter;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;

public class ContactSearchtermLdapConverter implements ContactSearchTermConverter {

    private static final String FOLDER_AJAXNAME = ContactField.FOLDER_ID.getAjaxName();

    private static final Object DISPLAYNAME_AJAXNAME = ContactField.DISPLAY_NAME.getAjaxName();

    private static final transient Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactSearchtermLdapConverter.class));

    private Mappings mappings;

    private String charset;

    private StringBuilder bob;

    //private boolean foundAnd = false; // If an AND connection to a greater equal is found (normal GUI operation) used for LDAP workaround

    private int state = 0; // The fix state. If an AND connection to a greater equal is found (normal GUI operation) turns to 1,
                           // if a greater equal is found turns to 2. If a corresponding less is found turns to 3.

    private char greaterChar;

    private String greaterField; // What field is used for the greaterequal operation

    private boolean distributionlistActive = false; // If distributionlists are activated or not. We need to remember this because
                                                    // otherwise distributionlists might appear after a search

    private int oldGreaterPos; // Stores the position where the last greater equals was found

    private List<String> folders;

    public Mappings getMappings() {
        return mappings;
    }

    public void setMappings(final Mappings mappings) {
        this.mappings = mappings;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(final String charset) {
        this.charset = charset;
    }

    public boolean isDistributionlistActive() {
        return distributionlistActive;
    }

    public void setDistributionlistActive(final boolean distributionlistActive) {
        this.distributionlistActive = distributionlistActive;
    }

    @Override
    public List<String> getFolders() {
        return folders;
    }

    @Override
    public <T> void parse(final SearchTerm<T> term) {
        folders = new LinkedList<String>();
        bob = new StringBuilder(traverseViaInOrder(term));
    }

    protected <T> String traverseViaInOrder(final SearchTerm<T> term) {
        if (term instanceof SingleSearchTerm) {
            return traverseViaInorder((SingleSearchTerm) term);
        } else if (term instanceof CompositeSearchTerm) {
            return traverseViaInorder((CompositeSearchTerm) term);
        } else {
            LOG.error("Got a search term that was neither Composite nor Single. How? Problem: " + term.getClass().getCanonicalName());
        }
        return "";
    }

    protected String traverseViaInorder(final SingleSearchTerm term) {
        boolean nextIsDisplayName = false; // display name can be different for contact and distribution list, so two fields might be
                                           // needed
        boolean nextIsFolder = false; // folders are not part of the ldap search

        final Operand<?>[] operands = term.getOperands();
        final Operation operation = term.getOperation();

        final List<String> ops = new LinkedList<String>();
        for (final Operand<?> o : operands) {
            String value = ContactField.class.isInstance(o.getValue()) ? ((ContactField)o.getValue()).getAjaxName() : (String)o.getValue();
            if (o.getType() == Operand.Type.COLUMN) {
                nextIsFolder = FOLDER_AJAXNAME.equals(o.getValue()) || ContactField.FOLDER_ID.equals(o.getValue());
                if (distributionlistActive) {
                    nextIsDisplayName = DISPLAYNAME_AJAXNAME.equals(value) || ContactField.GIVEN_NAME.getAjaxName().equals(value) ||
                        ContactField.SUR_NAME.getAjaxName().equals(value);
                }
                value = translateFromJSONtoLDAP(value);
                if (nextIsFolder) {
                    continue;
                } else if (null == value || 0 == value.length()) {
                    LOG.debug("Skipping search term '" + term + "' referencing unmapped contact property '" + o.getValue() + "'.");
                    return "";
                }
            } else if (nextIsFolder) {
                folders.add(value);
                continue;
            }
            ops.add(LdapContactInterface.escapeLDAPSearchFilter(value));
        }

        if (nextIsFolder) {
            return "";
        }

        if ("<".equals(operation.getOperation())) {
            final String fieldnameString = ops.get(0);
            if (fieldnameString != null && fieldnameString.equals(greaterField) && 2 == state) {
                final List<String> allLettersBetween = allLettersBetween(greaterChar, ops.get(ops.size() - 1).charAt(0));
                final StringBuilder sb = new StringBuilder();
                sb.append("(|");
                for (final String letter : allLettersBetween) {
                    sb.append("(");
                    if (distributionlistActive && nextIsDisplayName) {
                        sb.append("|(");
                    }
                    sb.append(fieldnameString);
                    sb.append("=");
                    sb.append(letter);
                    sb.append("*)");
                    if (distributionlistActive && nextIsDisplayName) {
                        sb.append("(");
                        sb.append(mappings.getDistributionlistname());
                        sb.append("=");
                        sb.append(letter);
                        sb.append("*))");
                    }
                }
                sb.append(")");
                state = 3;
                return sb.toString();
            }
        }

        if (1 == state && ">=".equals(operation.getOperation())) {
            greaterChar = ops.get(ops.size() - 1).charAt(0);
            greaterField = ops.get(0);
            state = 2;
        }

        final StringBuilder str = new StringBuilder();
        String tmp = new Formatter().format(operation.getLdapRepresentation(), ops.toArray()).toString();
        str.append("(").append(tmp).append(")");
        if (distributionlistActive && nextIsDisplayName) {
            ops.remove(0);
            ops.add(0, mappings.getDistributionlistname());
            tmp = new Formatter().format(operation.getLdapRepresentation(), ops.toArray()).toString();
            str.insert(0, "(|").append("(").append(tmp).append("))");
        }
        return str.toString();
    }

    protected String traverseViaInorder(final CompositeSearchTerm term) {
        final Operation operation = term.getOperation();
        final SearchTerm<?>[] operands = term.getOperands();

        if ("and".equals(operation.getOperation())) {
            state = 1;
        }
        final List<String> ops = new LinkedList<String>();
        for (int i = 0; i < operands.length; i++) {
            final String traverseViaInOrder = traverseViaInOrder(operands[i]);
            if (2 == state) {
                oldGreaterPos = i;
            }
            ops.add(traverseViaInOrder);
        }

        if (3 == state) {
            // Statement was fixed, so remove the old greater than ...
            ops.remove(oldGreaterPos);
            // ... and reset the state machine for new funny adventures
            state = 0;
            oldGreaterPos = 0;
            greaterChar = '\u0000';
            greaterField = null;
        }
        final String tmp = new Formatter().format(operation.getLdapRepresentation(), Strings.join(ops, "")).toString();
        final StringBuilder str = new StringBuilder();
        str.append("(").append(tmp).append(")");
        return str.toString();
    }

    private String translateFromJSONtoLDAP(final String value) {
        if (mappings == null) {
            return value;
        }
        final ContactField field = Mappings.getBySimilarity(value);
        if (field == null) {
            return null;
        }
        return mappings.get(field);
    }

    private static List<String> allLettersBetween(final char string, final char string2) {
        final List<String> retval = new ArrayList<String>();
        if (string < string2) {
            for (int i = string; i < string2; i++) {
                retval.add(String.valueOf((char)i));
            }
            return retval;
        } else {
            return null;
        }
    }


    public String getQueryString() {
        return bob.toString();
    }
}
