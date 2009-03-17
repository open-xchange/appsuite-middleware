package com.openexchange.fitnesse;

import java.io.IOException;
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
import com.openexchange.fitnesse.environment.SymbolHandler;
import com.openexchange.fitnesse.exceptions.FitnesseException;
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
    
    public AJAXClient getClientForUser1(){
        try {
            AJAXSession session = new AJAXSession();
            session.setId(LoginTools.login(session, new LoginRequest("thorben","netline")).getSessionId());
            return new AJAXClient( session );
        } catch (AjaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
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
}
