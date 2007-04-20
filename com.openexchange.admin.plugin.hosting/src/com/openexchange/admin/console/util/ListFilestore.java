//package com.openexchange.admin.console.util;
//
//import java.net.MalformedURLException;
//import java.rmi.Naming;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;
//
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//import org.apache.commons.cli.PosixParser;
//
//import com.openexchange.admin.rmi.OXUtilInterface;
//import com.openexchange.admin.rmi.dataobjects.Credentials;
//import com.openexchange.admin.rmi.dataobjects.Filestore;
//import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
//import com.openexchange.admin.rmi.exceptions.InvalidDataException;
//import com.openexchange.admin.rmi.exceptions.StorageException;
//
///**
// * 
// * @author d7
// *
// */
//public class ListFilestore extends UtilAbstraction {
//
//    private final static String GENERAL_UTILITY_NAME = "listFilestore";
//
//    public ListFilestore(final String[] args2) {
//
//        final CommandLineParser parser = new PosixParser();
//
//        final Options options = getOptions();
//
//        try {
//            final CommandLine cmd = parser.parse(options, args2);
//
//            final Credentials auth = new Credentials(cmd.getOptionValue(OPT_NAME_ADMINUSER_SHORT), cmd.getOptionValue(OPT_NAME_ADMINPASS_SHORT));
//
//            // get rmi ref
//            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(OXUtilInterface.RMI_NAME);
//
//            final String searchpattern = cmd.getOptionValue(OPT_NAME_SEARCH_PATTERN_SHORT);
//            // Setting the options in the dataobject
//
//            final Filestore[] filestores = oxutil.listFilestores(searchpattern, auth);
//            
//            // FIXME: Format output right
//            for (final Filestore filestore : filestores) {
//                System.out.println(filestore);
//            }
//        } catch (final java.rmi.ConnectException neti) {
//            printError(neti.getMessage());
//        } catch (final java.lang.NumberFormatException num) {
//            printInvalidInputMsg("Ids must be numbers!");
//        } catch (final org.apache.commons.cli.MissingArgumentException as) {
//            printError("Missing arguments on the command line: " + as.getMessage());
//            printHelpText(GENERAL_UTILITY_NAME, options);
//        } catch (final org.apache.commons.cli.UnrecognizedOptionException ux) {
//            printError("Unrecognized options on the command line: " + ux.getMessage());
//            printHelpText(GENERAL_UTILITY_NAME, options);
//        } catch (final org.apache.commons.cli.MissingOptionException mis) {
//            printError("Missing options on the command line: " + mis.getMessage());
//            printHelpText(GENERAL_UTILITY_NAME, options);
//        } catch (final ParseException e) {
//            e.printStackTrace();
//        } catch (final MalformedURLException e) {
//            printServerResponse(e.getMessage());
//        } catch (final RemoteException e) {
//            printServerResponse(e.getMessage());
//        } catch (final NotBoundException e) {
//            printNotBoundResponse(e);
//        } catch (final StorageException e) {
//            printServerResponse(e.getMessage());
//        } catch (final InvalidCredentialsException e) {
//            printServerResponse(e.getMessage());
//        } catch (final InvalidDataException e) {
//            printServerResponse(e.getMessage());
//        }
//
//    }
//
//    public static void main(final String args[]) {
//        new ListFilestore(args);
//    }
//
//    private Options getOptions() {
//        final Options retval = getDefaultCommandLineOptions();
//
//        retval.addOption(addDefaultArgName(getShortLongOpt(OPT_NAME_SEARCH_PATTERN_SHORT, OPT_NAME_SEARCH_PATTERN_LONG, "a search pattern for the filestores to list", true, true)));
//        return retval;
//    }
//
//}
