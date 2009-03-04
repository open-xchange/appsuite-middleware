package com.openexchange.fitnesse.environment;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.kata.IdentitySource;

/**
 * 
 * {@link SymbolHandler}
 *
 * This is part of the environment FitNesse tests run in. The purpose of
 * this construct is to make sure that objects already created can be used 
 * among different testing steps, a feature that our yaml suite lacks.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class SymbolHandler {
    private Map<String,IdentitySource> registeredSymbols;
    
    
    public SymbolHandler() {
        super();
        registeredSymbols = new HashMap<String,IdentitySource>();
    }
    
    public void add(String symbolName, IdentitySource symbolObject) {
        registeredSymbols.put(symbolName, symbolObject);
    }
    
    public void remove(String symbolName){
        registeredSymbols.remove(symbolName);
    }
    
    public IdentitySource get(String symbolName){
        return registeredSymbols.get(symbolName);
    }
    
}
