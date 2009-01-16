/**
 * 
 */
package com.openexchange.admin.reseller.console;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.admin.rmi.exceptions.StorageException;


/**
 * @author choeger
 *
 */
public class Create extends ResellerAbstraction {

    protected final void setOptions(final AdminParser parser) {
        setCreateOptions(parser);
    }

    /**
     * 
     */
    public Create() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        final Create create = new Create();
        create.start(args);
    }

    public void start(final String[] args) {
        final AdminParser parser = new AdminParser("createadmin");    
        
        setOptions(parser);

        
        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);
            final ResellerAdmin adm = resellerparsing(parser);

            final OXResellerInterface rsi = getResellerInterface();
        
            rsi.create(adm, auth);
            
        } catch (IllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (UnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (MissingOptionException e) {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (MalformedURLException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (RemoteException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (NotBoundException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (InvalidDataException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_DATA);
        } catch (StorageException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } catch (InvalidCredentialsException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (OXResellerException e) {
            printServerException(e, parser);
            sysexit(1);
        }
    }
}
