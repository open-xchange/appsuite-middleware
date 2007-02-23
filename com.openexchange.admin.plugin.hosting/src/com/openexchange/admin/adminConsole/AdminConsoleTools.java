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
package com.openexchange.admin.adminConsole;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class AdminConsoleTools {
    
    
    public static Hashtable<String, Comparable> parseInput( String args[], String SHELL_COMMAND ) {
        Hashtable<String, Comparable> hash = new Hashtable<String, Comparable>();
        String param = null;
        
        for ( int a = 0; a < args.length; a++ ) {
            param = args[a];
            for ( int i = 1; i < args.length; i++ ) {
                if ( ( ( a + i ) < args.length ) && !args[a+i].startsWith( "-" ) ) {
                    param += " " + args[a+i];
                } else {
                    break;
                }
            }
            
            if ( param.startsWith( "--"+SHELL_COMMAND+"=" ) ) {
                StringTokenizer st = new StringTokenizer( param, "=" );
                if ( st.countTokens() == 2 ) {
                    st.nextToken();
                    String command = st.nextToken().toLowerCase();
                    if ( command.indexOf( " " ) != -1 ) {
                        command = command.substring( 0, command.indexOf( " " ) );
                    }
                    hash.put( SHELL_COMMAND, command );
                }
            } else {
                if ( param.startsWith( "--" ) && param.indexOf( "=" ) != -1 ) {
                    int pos = param.indexOf( "=" );
                    String paramNAME = param.substring( 2, pos );
                    String paramVALUE = param.substring( pos + 1 );
                   
                    if ( paramVALUE.equalsIgnoreCase( "true" ) || paramVALUE.equalsIgnoreCase( "false" ) ) {
                        hash.put( paramNAME, Boolean.valueOf( paramVALUE ) );
                    } else {
                        hash.put( paramNAME, paramVALUE );
                    }
                }
            }
        }
        
        return hash;
    }
    
    public static boolean inputcontains(String[] args, String SHELL_COMMAND) {
    	for ( int a = 0; a < args.length; a++ ) {
    		if (args[a].startsWith("--" + SHELL_COMMAND)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static void debugIT( Object val ) {
        System.out.println( "***** DEBUG VALUE *****" );
        
        System.out.println( "Object Type: " + val.getClass() );
        if ( val instanceof Vector ) {
            Vector v = (Vector)val;
            System.out.println( "Elements in Object: " + v.size() );
            for ( int i = 0; i < v.size(); i++ ) {
                Object o = v.get(i);
                System.out.println( "- Element["+i+"] Type("+o.getClass()+"), Value=" + o.toString() );
            }
        } else
        
        if ( val instanceof Hashtable ) {
            Hashtable h = (Hashtable)val;
            System.out.println( "Elements in Object: " + h.size() );
            Enumeration en = h.keys();
            int i = 0;
            while( en.hasMoreElements() ) {
                Object o_key = en.nextElement();
                Object o_val = h.get( o_key );
                System.out.println( "- Element["+i+"] Key-Type("+o_key.getClass()+"), Value=" + o_key.toString() );
                System.out.println( "- Element["+i+"] Value-Type("+o_val.getClass()+"), Value=" + o_val.toString() );
                i++;
            }
        } else {
            System.out.println( "No parser for Object Type: " + val.getClass() );
        }
        
        System.out.println( "***********************" );
    }
    
}
