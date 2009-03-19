package com.openexchange.fitnesse;

import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.fitnesse.folders.FolderResolver;
import com.openexchange.fitnesse.wrappers.FixtureDataWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.test.fixtures.AppointmentFixtureFactory;
import com.openexchange.test.fixtures.ContactFixtureFactory;
import com.openexchange.test.fixtures.Fixture;
import com.openexchange.test.fixtures.FixtureException;
import com.openexchange.test.fixtures.Fixtures;
import com.openexchange.test.fixtures.TaskFixtureFactory;
import com.openexchange.tools.servlet.AjaxException;

/**
 * 
 * {@link AbstractTableTable} - The superclass for all TableTables
 * used by FitNesse / Slim
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public abstract class AbstractTableTable implements SlimTableTable{
    
    public FitnesseEnvironment environment;
    public FixtureDataWrapper data;

    public AbstractTableTable() {
        super();
        environment = FitnesseEnvironment.getInstance();
    }

    /**
     * Required method for FitNesse calls.
     */
    public final List doTable(List<List<String>> table) throws Exception {
        data = new FixtureDataWrapper(table) ;
        return doTable();
    }
    
    /**
     * Works with the internal FixtureDataWrapper <code>data</code>
     * @return
     * @throws Exception
     */
    public abstract List doTable() throws Exception ;
    
    protected FolderChildObject addFolder(FolderChildObject entry, FixtureDataWrapper data, int defaultFolder) throws FitnesseException {
        if(data.getFolderExpression()  == null) {
            entry.setParentFolderID(defaultFolder);
            return entry;
        }
        entry.setParentFolderID(resolveFolder(data.getFolderExpression()));
        return entry;
    }
    
    protected int resolveFolder(String folderExpression) throws FitnesseException {
        FolderResolver folderResolver = new FolderResolver(environment.getClient(), environment);
        return folderResolver.getFolderId(folderExpression);
        
    }
    
    protected AJAXClient getClient() {
        return environment.getClient();
    }
}
