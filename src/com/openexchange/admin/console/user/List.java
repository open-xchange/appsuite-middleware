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
package com.openexchange.admin.console.user;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.User.PASSWORDMECH;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class List extends UserAbstraction {

    private static final String FALSE_STRING = "false";
    private static final String TRUE_STRING = "true";
    public static void main(final String[] args) {
        new List(args);
    }

    public List(final String[] args2) {

        final AdminParser parser = new AdminParser("list");

        setDefaultCommandLineOptions(parser);
        
        setCSVOutputOption(parser);
        
        try {
            parser.ownparse(args2);

            final Context ctx = new Context(DEFAULT_CONTEXT);

            if (parser.getOptionValue(this.contextOption) != null) {
                ctx.setID(Integer.parseInt((String) parser.getOptionValue(this.contextOption)));
            }
            
            final Credentials auth = new Credentials((String) parser.getOptionValue(this.adminUserOption), (String) parser.getOptionValue(this.adminPassOption));

            final OXUserInterface oxu = (OXUserInterface) Naming.lookup(RMI_HOSTNAME + OXUserInterface.RMI_NAME);

            final int[] allusers = oxu.getAll(ctx, auth);
            
            final ArrayList<User> users = new ArrayList<User>();
            for (final int id : allusers) {
                final User user = new User(id);
                users.add(user);
            }
            
            final User[] newusers = oxu.getData(ctx, users.toArray(new User[users.size()]), auth);
            
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(newusers);
            } else {
                sysoutOutput(newusers);
            }
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final StorageException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final NoSuchUserException e) {
            printServerResponse(e.getMessage());
            sysexit(SYSEXIT_NO_SUCH_USER);
        } catch (final IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (final UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (final MissingOptionException e) {
            printError(e.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (final DatabaseUpdateException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final IllegalArgumentException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final IllegalAccessException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        } catch (final InvocationTargetException e) {
            printServerResponse(e.getMessage());
            sysexit(1);
        }
    }

    /**
     * This methods collects the information from the user object and calls the
     * general cvs output method
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * 
     */
    private void precsvinfos(final User[] users) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method[] methods = User.class.getMethods();
        final ArrayList<MethodAndNames> methArrayList = getGetters(methods, new HashSet<String>());
        
        final ArrayList<String> columnnames = new ArrayList<String>();
        for (final MethodAndNames methodandnames : methArrayList) {
            columnnames.add(methodandnames.getName());
        }
        
        final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        for (final User user : users) {
            final ArrayList<String> datarow = new ArrayList<String>();
            for (final MethodAndNames methodandnames : methArrayList) {
                final String returntype = methodandnames.getReturntype();
                if (returntype.equals(JAVA_LANG_STRING)) {
                    datarow.add((String)methodandnames.getMethod().invoke(user, (Object[]) null));
                } else if (returntype.equals(JAVA_LANG_INTEGER)) {
                    datarow.add(String.valueOf(methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_LANG_BOOLEAN)) {
                    datarow.add(booleantostring((Boolean)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_DATE)) {
                    datarow.add(datetostring((Date)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_HASH_SET)) {
                    datarow.add(hashtostring((HashSet)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(PASSWORDMECH_CLASS)) {
                    datarow.add(passwordtostring((PASSWORDMECH)methodandnames.getMethod().invoke(user, (Object[]) null)));
                } else if (returntype.equals(JAVA_UTIL_TIME_ZONE)) {
                    datarow.add(timezonetostring((TimeZone)methodandnames.getMethod().invoke(user, (Object[]) null)));
                }
            }
            data.add(datarow);
            printExtensionsError(user);
        }
        doCSVOutput(columnnames, data);
    }

    private final void sysoutOutput(final User[] users) {
        for (final User user : users) {
            System.out.println(user.toString());
            printExtensionsError(user);
        }
    }

    /**
     * This method is used to define how a boolean value is transferred to string
     * 
     * @param boolean1
     * @return the string representation of this boolean
     */
    private final String booleantostring(final Boolean boolean1) {
        if (null != boolean1) {
            if (boolean1) {
                return TRUE_STRING;
            } else {
                return FALSE_STRING;
            }
        } else {
            return null;
        }
    }

    /**
     * This method is used to define how a date value is transferred to string
     * 
     * @param date
     * @return the string representation of this date
     */
    private final String datetostring(final Date date) {
        if (null != date) {
            return date.toString();
        } else {
            return null;
        }
    }

    private final String hashtostring(final HashSet<?> set) {
        if (null != set && set.size() > 0) {
            final String[] hashvalues = set.toArray(new String[set.size()]);
            final StringBuilder sb = new StringBuilder();
            for (final String value : hashvalues) {
                sb.append(value);
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        } else {
            return null;
        }
    }

    private final String passwordtostring(final PASSWORDMECH passwordmech2) {
        if (passwordmech2 == PASSWORDMECH.CRYPT) {
            return "crypt";
        } else {
            return "sha";
        }
    }

    private final String timezonetostring(final TimeZone zone) {
        return zone.getDisplayName();
    }

    protected void sysexit(final int exitcode) {
        System.exit(exitcode);
    }
}
