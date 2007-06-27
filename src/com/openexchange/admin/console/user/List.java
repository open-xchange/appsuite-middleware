
package com.openexchange.admin.console.user;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
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
            
            
//          map user data to corresponding module access
            HashMap<Integer, UserModuleAccess> usr2axs = new HashMap<Integer, UserModuleAccess>();
            
            for (User user : newusers) {      
                // fetch module access for every user
                usr2axs.put(user.getId(), oxu.getModuleAccess(ctx, user.getId(), auth));
            }           
            
            
            if (null != parser.getOptionValue(this.csvOutputOption)) {
                precsvinfos(newusers,usr2axs);
            } else {
                sysoutOutput(newusers,usr2axs);
            }
            sysexit(0);
        } catch (final java.rmi.ConnectException neti) {
            printError(neti.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } catch (final java.lang.NumberFormatException num) {
            printInvalidInputMsg("Ids must be numbers!");
            sysexit(1);
        } catch (final MalformedURLException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final RemoteException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (final NotBoundException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final StorageException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (final InvalidCredentialsException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (final NoSuchContextException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } catch (final InvalidDataException e) {
            printServerResponse(e);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (final NoSuchUserException e) {
            printServerResponse(e);
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
            printServerResponse(e);
            sysexit(1);
        } catch (final IllegalArgumentException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final IllegalAccessException e) {
            printServerResponse(e);
            sysexit(1);
        } catch (final InvocationTargetException e) {
            printServerResponse(e);
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
    private void precsvinfos(final User[] users,final HashMap<Integer, UserModuleAccess> access_map) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method[] methods = User.class.getMethods();
        final ArrayList<MethodAndNames> methArrayList = getGetters(methods, new HashSet<String>());
        
        final ArrayList<String> columnnames = new ArrayList<String>();
        for (final MethodAndNames methodandnames : methArrayList) {
            columnnames.add(methodandnames.getName());
        }
        
        //      module access columns
        columnnames.add(UserAbstraction.OPT_ACCESS_CALENDAR);
        columnnames.add(UserAbstraction.OPT_ACCESS_CONTACTS);
        columnnames.add(UserAbstraction.OPT_ACCESS_DELEGATE_TASKS);
        columnnames.add(UserAbstraction.OPT_ACCESS_EDIT_PUBLIC_FOLDERS);
        columnnames.add(UserAbstraction.OPT_ACCESS_FORUM);
        columnnames.add(UserAbstraction.OPT_ACCESS_ICAL);
        columnnames.add(UserAbstraction.OPT_ACCESS_INFOSTORE);
        columnnames.add(UserAbstraction.OPT_ACCESS_PINBOARD_WRITE);
        columnnames.add(UserAbstraction.OPT_ACCESS_PROJECTS);
        columnnames.add(UserAbstraction.OPT_ACCESS_READCREATE_SHARED_FOLDERS);
        columnnames.add(UserAbstraction.OPT_ACCESS_RSS_BOOKMARKS);
        columnnames.add(UserAbstraction.OPT_ACCESS_RSS_PORTAL);
        columnnames.add(UserAbstraction.OPT_ACCESS_SYNCML);
        columnnames.add(UserAbstraction.OPT_ACCESS_TASKS);
        columnnames.add(UserAbstraction.OPT_ACCESS_VCARD);
        columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV);
        columnnames.add(UserAbstraction.OPT_ACCESS_WEBDAV_XML);
        columnnames.add(UserAbstraction.OPT_ACCESS_WEBMAIL);
        
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
            
//          add module access 
            UserModuleAccess access = access_map.get(user.getId());
            datarow.add(String.valueOf(access.getCalendar()));
            datarow.add(String.valueOf(access.getContacts()));
            datarow.add(String.valueOf(access.getDelegateTask()));
            datarow.add(String.valueOf(access.getEditPublicFolders()));
            datarow.add(String.valueOf(access.getForum()));
            datarow.add(String.valueOf(access.getIcal()));
            datarow.add(String.valueOf(access.getInfostore()));
            datarow.add(String.valueOf(access.getPinboardWrite()));
            datarow.add(String.valueOf(access.getProjects()));
            datarow.add(String.valueOf(access.getReadCreateSharedFolders()));
            datarow.add(String.valueOf(access.getRssBookmarks()));
            datarow.add(String.valueOf(access.getRssPortal()));
            datarow.add(String.valueOf(access.getSyncml()));
            datarow.add(String.valueOf(access.getTasks()));
            datarow.add(String.valueOf(access.getVcard()));
            datarow.add(String.valueOf(access.getWebdav()));
            datarow.add(String.valueOf(access.getWebdavXml()));
            datarow.add(String.valueOf(access.getWebmail()));
            
            data.add(datarow);
            printExtensionsError(user);
        }
        doCSVOutput(columnnames, data);
    }

    private final void sysoutOutput(final User[] users,final HashMap<Integer, UserModuleAccess> user_access) {
        for (final User user : users) {
            System.out.println(user.toString());
            System.out.println(user_access.get(user.getId()).toString());
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
}
