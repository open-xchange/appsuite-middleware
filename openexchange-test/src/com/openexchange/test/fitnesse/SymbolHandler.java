package com.openexchange.test.fitnesse;

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
    
    private static SymbolHandler instance;
    
    private SymbolHandler(){
        super();
    }
    
    public static SymbolHandler getInstance(){
        if(instance == null)
            instance = new SymbolHandler();
        return instance;
    }
    
}
