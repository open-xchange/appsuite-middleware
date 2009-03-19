package com.openexchange.fitnesse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.kata.IdentitySource;
import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.fitnesse.environment.CleanupHandler;
import com.openexchange.fitnesse.environment.DefineUsers;
import com.openexchange.fitnesse.environment.SymbolHandler;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.fitnesse.folders.PermissionDefinition;
import com.openexchange.tools.servlet.AjaxException;


/**
 * 
 * {@link FitnesseEnvironment}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class FitnesseEnvironment {
    
    private static FitnesseEnvironment instance;
    private SymbolHandler symbols;
    private CleanupHandler cleanup;
    private Map<String, PermissionDefinition> permissionDefinitions = new HashMap<String, PermissionDefinition>();
    private Map<String, AJAXClient> clients = new HashMap<String, AJAXClient>();
    private AJAXClient currentClient;

    private String firstUser = null;
    
    private String hostname;
    private String protocol;
    
    private FitnesseEnvironment() {
        super();
        symbols = new SymbolHandler();
        cleanup = new CleanupHandler();
        
        try {
            System.setProperty("test.propfile", "/Users/fla/Documents/workspace/openexchange-test/conf/test.properties"); //TODO: move to setup
            AJAXConfig.init();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    public static FitnesseEnvironment getInstance(){
        if(instance == null)
            instance = new FitnesseEnvironment();
        return instance;
    }
    
    public static void purgeInstance(){
        instance.cleanup.perform();
        instance = null;
    }
    
    public AJAXClient getClient(){
        if(currentClient == null) {
            currentClient = clients.get(firstUser);
        }
        return currentClient;
    }
    
    public void registerStep(Step step){
        cleanup.add(step);
    }
    
    public void registerSymbol(String symbolName, IdentitySource symbolObject) throws FitnesseException{
        IdentitySource oldSymbol = symbols.get(symbolName);
        if(oldSymbol != null && oldSymbol.getType() != symbolObject.getType()) {
            throw new FitnesseException("Can not override symbol "+symbolName+" of type "+oldSymbol.getType().getName()+" with type "+symbolObject.getType().getName());
        }
        symbols.add(symbolName, symbolObject);
    }
    
    public IdentitySource getSymbol(String symbolName){
        return symbols.get(symbolName);
    }

    /**
     * @param permissions
     */
    public void registerPermissions(PermissionDefinition permissions) {
        permissionDefinitions.put(permissions.getFixtureName(), permissions);
    }
    
    public PermissionDefinition getPermissions(String permissionRef) {
        return permissionDefinitions.get(permissionRef);
    }

    /**
     * @param username
     * @param password
     * @throws JSONException 
     * @throws SAXException 
     * @throws IOException 
     * @throws AjaxException 
     */
    public void login(String username, String password) throws AjaxException, IOException, SAXException, JSONException {
        if(firstUser == null) {
            firstUser = username;
        }
        AJAXSession session = new AJAXSession();
        session.setId(LoginTools.login(session, new LoginRequest(username,password), getProtocol(), getHostname()).getSessionId());
        AJAXClient client = new AJAXClient( session );
        client.setHostname(getHostname());
        client.setProtocol(getProtocol());
        clients.put(username, client);        
    }

    /**
     * @param username
     * @throws FitnesseException 
     */
    public void switchUser(String username) throws FitnesseException {
        if(! clients.containsKey(username) )
            throw new FitnesseException("User '"+username+"' not found. Was it declared using "+DefineUsers.class.getName()+"?");
        currentClient = clients.get(username);
    }

    
    public String getHostname() {
        return hostname;
    }

    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    
    public String getProtocol() {
        return protocol;
    }

    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    
}
