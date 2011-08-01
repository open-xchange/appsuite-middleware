
package com.openexchange.admin.console.context.extensioninterfaces;


/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options. To offer your own implementation of this interface
 * to the core. The ServiceLoader mechanism of JDK 6 is used which requests a directory
 * META-INF/services under which a text file whose name is the full-qualified binary name
 * of this interface (com.openexchange.admin.console.context.ContextConsoleCreateInterface). And the
 * content of this file must be the full-qualified binary name of your implementation.
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * 
 */
public interface ContextConsoleDeleteInterface extends ContextConsoleCommonInterface {

}
