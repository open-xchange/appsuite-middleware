package com.openexchange.admin.console.context;

import java.rmi.Naming;
import java.util.ArrayList;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.user.UserAbstraction;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public class GetModuleAccessForContext extends ContextAbstraction {
   
    public GetModuleAccessForContext(final String[] args2) {

        final AdminParser parser = new AdminParser("getmoduleaccessforcontext");

        setOptions(parser);

        String successtext = null;
        
        try {
            parser.ownparse(args2);
            final Context ctx = contextparsing(parser);
            
            parseAndSetContextName(parser, ctx);
            
            successtext = nameOrIdSetInt(this.ctxid, this.contextname, "context");
            
            final Credentials auth = credentialsparsing(parser);

            // get rmi ref
            final OXContextInterface oxres = (OXContextInterface) Naming.lookup(RMI_HOSTNAME +OXContextInterface.RMI_NAME);

            // Fetch access object
            UserModuleAccess access = oxres.getModuleAccess(ctx, auth);
            
            // output access object
            doCsvOutput(access);   
            
            // exit application
            sysexit(0);
        } catch (final Exception e) {
            printErrors(successtext, null, e, parser);
        }

    }

   

	private void doCsvOutput(UserModuleAccess access) throws InvalidDataException {
		final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		
        final ArrayList<String> datarow = new ArrayList<String>();
        
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
		
		doCSVOutput(getAccessColums(),data);		
	}
	
	private static ArrayList<String> getAccessColums(){
		final ArrayList<String> columnnames = new ArrayList<String>();
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
        return columnnames;
	}

	public static void main(final String args[]) {
        new GetModuleAccessForContext(args);
    }

    private void setOptions(final AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setContextOption(parser, NeededQuadState.eitheror);        
        setContextNameOption(parser, NeededQuadState.eitheror);
    }
}
