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



package com.openexchange.tools;

import com.openexchange.groupware.calendar.CalendarCommonCollection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;


/**
 * StringCollection (written as Blur.java some years ago ;)
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class StringCollection {
    
    static final byte[] DSO = "\\".getBytes();
    static final byte[] DSOR = "\\\\".getBytes();
    static final byte[] DAP = "'".getBytes();
    static final byte[] DAPR = "\\'".getBytes();
    
    private StringCollection() { }
    
    
    public static final String disarmSQLString(final String s) {
        return new String(replaceGivenBytes(replaceGivenBytes(s.getBytes(), DSO, DSOR), DAP, DAPR));
    }
    
    /**
     * public static byte[] replaceGivenBytes(byte b[], byte replace[], byte replacement[])<BR>
     * Replace (replace) with (replacement) in source (b)<BR><BR>
     *
     * @param byte b[]
     * @param byte replace[]
     * @param byte replacement[]
     * @return byte[]
     */
    public static final byte[] replaceGivenBytes(final byte b[], final byte replace[], final byte replacement[]) {
        byte r[] = new byte[(b.length+(replacement.length*2))];
        int c = 0;
        final int l = replace.length;
        for (int a  = 0; a < b.length; a++) {
            boolean found = false;
            int fc = 1;
            if (b[a] == replace[0]) {
                found = true;
                for (int n = 1; n < l; n++) {
                	final int m = a+n;
                    if (m < b.length) {
                        if (b[(a+n)] == replace[n]) {
                            found = true;
                            fc++;
                        } else {
                            found = false;
                        }
                    } else {
                        found = false;
                    }
                }
            }
            if (r.length < (c+replacement.length)) {
                r = expandArray(r, c, (c+replacement.length));
            }
            if (found && fc == replace.length) {
                System.arraycopy(replacement, 0, r, c, replacement.length);
                c = c + replacement.length;
                a = (a + l)-1;
            } else {
                r[c] = b[a];
                c++;
            }
        }
        r = blurTrim(r, c);
        return r;
    }

    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String
     * for subqueries<BR><BR>
     *
     * @param int arr[]
     * @return SQLInString or null
     */
    public static final String getSqlInString(final int arr[]) {
    	final StringBuffer sb = new StringBuffer();
        if (arr.length > 0) {
            sb.append('(');
            for (int a = 0; a < arr.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(arr[a]);
                } else {
                    sb.append(arr[a]);
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }    
    
    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String
     * for subqueries<BR><BR>
     *
     * @param Object arr[]
     * @return SQLInString or null
     */
    public static final String getSqlInString(final Object arr[]) {
    	final StringBuffer sb = new StringBuffer();
        if (arr.length > 0) {
            sb.append('(');
            for (int a = 0; a < arr.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(arr[a]);
                } else {
                    sb.append(arr[a]);
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }
    
    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String
     * for subqueries<BR><BR>
     *
     * @param int arr[][]
     * @return SQLInString or null
     */
    public static final String getSqlInString(final int arr[][]) {
    	final StringBuffer sb = new StringBuffer();
        if (arr.length > 0) {
            sb.append('(');
            for (int a = 0; a < arr.length; a++) {
                if (a > 0) {
                    sb.append(',');
                    sb.append(arr[a][0]);
                } else {
                    sb.append(arr[a][0]);
                }
            }
        } else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }
    
    
    /**
     * public static final String getSqlInString<BR>
     * returns a normal (number based) SQL IN String
     * for subqueries<BR><BR>
     *
     * @param int i
     * @param int arr[]
     * @return SQLInString or null
     */
    public static final String getSqlInString(final int i, final int arr[]) {
    	final StringBuffer sb = new StringBuffer();
        sb.append('(');
        sb.append(i);
        if (arr.length > 0) {
            for (int a = 0; a < arr.length; a++) {
                sb.append(',');
                sb.append(arr[a]);
            }
        }
        sb.append(')');
        return sb.toString();
    }
    
    /**
     * public static final String getSqlInStringFromMap<BR>
     * returns a normal (number based) SQL IN String
     * for subqueries<BR><BR>
     *
     * @param Map
     * @return SQLInString or null
     */
    public static final String getSqlInStringFromMap(final Map m) {
        final StringBuffer sb = new StringBuffer();
        sb.append('(');
        if (m != null) {
        	final int size = m.size();
        	final Iterator it = m.keySet().iterator();
            boolean first = true;
            for (int k = 0; k < size; k++) {
            	final String temp = it.next().toString();
                if (!first) {
                    sb.append(',');
                    sb.append(temp);
                } else {
                    first = false;
                    sb.append(temp);
                }
            }
        }  else {
            return null;
        }
        sb.append(')');
        return sb.toString();
    }
    
    
    /**
     * public static byte[] blurTrim(byte b[], int c)<BR>
     * Same as String.trim() but should be faster because we know the end (c).<BR><BR>
     *
     * @param byte b[]
     * @param int c
     * @return byte[]
     */
    public static final byte[] blurTrim(final byte b[], final int c) {
    	final byte r[] = new byte[c];
        System.arraycopy(b, 0, r, 0, c);
        return r;
    }
    
    /**
     * public static byte[] expandArray(byte b[], int c, int l)<BR>
     * Expand a byte array.<BR><BR>
     *
     * @param byte b[]
     * @param int c (last position in b)
     * @param int l (last position in b + replacement.length)
     * @return byte[]
     */
    public static final byte[] expandArray(final byte b[], final int c, final int l) {
    	final byte r[] = new byte[((b.length+l)*2)];
        System.arraycopy(b, 0, r, 0, c);
        return r;
    }
    
    public static final String date2SQLString(final Date d) {
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return (sdf.format(d));
    }
    
    public static final String date2String(final Date d) {
    	final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return (sdf.format(d));
    }
    
    public static final String getSelect(final int[] cols, final String table) throws IndexOutOfBoundsException {
    	final StringBuffer sb = new StringBuffer(256);
        sb.append("SELECT ");
        int x = 0;
        for (int a = 0; a < cols.length; a++) {
        	final String temp = CalendarCommonCollection.getFieldName(cols[a]);
            if (temp != null) {
                if (x != 0) {
                    sb.append(',');
                    sb.append(temp);
                } else {
                    sb.append(temp);
                    x++;
                }
            }
        }
        sb.append(" FROM ");
        sb.append(table);
        return sb.toString();
    }
    
    public static String convertArray2String(final int i[]) {
		if (i == null) {
			return null;
		}
		
        final StringBuffer sb = new StringBuffer();
        for (int a = 0; a < i.length; a++) {
            sb.append(i[a]);
            sb.append(',');
        }
        
        return sb.delete(sb.length()-1, sb.length()).toString();
    }
    
    public static String convertArray2String(final String s[]) {
		if (s == null) {
			return null;
		}
		
		final StringBuffer sb = new StringBuffer();
        for (int a = 0; a < s.length; a++) {
            sb.append(s[a]);
            sb.append(',');
        }
        
        return sb.delete(sb.length()-1, sb.length()).toString();
    }
    
    public static int[] convertStringArray2IntArray(final String s[]) {
        int[] i = new int[s.length];
        for (int a = 0; a < i.length; a++) {
            i[a] = Integer.parseInt(s[a]);
        }
        return i;
    }
    
    public static boolean isEmpty(final String s) {
        final int length = s.length();
    	for (int a = 0; a < length; a++) {
            if (!Character.isWhitespace(s.charAt(a))) {
                return false;
            }
        }
        return true;
    }
    
    public static final String convertArraytoString(final Object[] o) {
    	final StringBuilder sb = new StringBuilder();
        for (int a = 0; a < o.length; a++) {
            sb.append(o[a]);
        }
        return sb.toString();
    }
    
    public static final String getStackAsString() {   
    	final Throwable t = new Throwable();
        t.fillInStackTrace();        
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.toString();
    }


}

