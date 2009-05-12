
package com.openexchange.mail.api;


public interface IMailProperties {

    /**
     * Checks if default folders (e.g. "Sent Mail", "Drafts") are supposed to be created below personal namespace folder (INBOX) even though
     * mail server indicates to create them on the same level as personal namespace folder.
     * <p>
     * <b>Note</b> that personal namespace folder must allow subfolder creation.
     * 
     * @return <code>true</code> if default folders are supposed to be created below personal namespace folder; otherwise <code>false</code>
     */
    public boolean isAllowNestedDefaultFolderOnAltNamespace();

    /**
     * Gets the max. allowed size (in bytes) for attachment for being displayed.
     * 
     * @return The max. allowed size (in bytes) for attachment for being displayed
     */
    public int getAttachDisplaySize();

    /**
     * Gets the default separator character.
     * 
     * @return The default separator character
     */
    public char getDefaultSeparator();

    /**
     * Indicates whether subscription shall be ignored or not.
     * 
     * @return <code>true</code> if subscription shall be ignored; otherwise <code>false</code>
     */
    public boolean isIgnoreSubscription();

    /**
     * Indicates whether subscription is supported or not.
     * 
     * @return <code>true</code> if subscription is supported; otherwise <code>false</code>
     */
    public boolean isSupportSubscription();

    /**
     * Gets the mail fetch limit.
     * 
     * @return The mail fetch limit
     */
    public int getMailFetchLimit();
    
    /**
     * Gets the max. number of connections.
     * 
     * @return The max. number of connections
     */
    public int getMaxNumOfConnections();

    /**
     * Indicates if user flags are enabled.
     * 
     * @return <code>true</code> if user flags are enabled; otherwise <code>false</code>
     */
    public boolean isUserFlagsEnabled();

    /**
     * Indicates if watcher is enabled.
     * 
     * @return <code>true</code> if watcher is enabled; otherwise <code>false</code>
     */
    public boolean isWatcherEnabled();

    /**
     * Gets the watcher frequency.
     * 
     * @return The watcher frequency
     */
    public int getWatcherFrequency();

    /**
     * Indicates if watcher is allowed to close exceeded connections.
     * 
     * @return <code>true</code> if watcher is allowed to close exceeded connections; otherwise <code>false</code>
     */
    public boolean isWatcherShallClose();

    /**
     * Gets the watcher time.
     * 
     * @return The watcher time
     */
    public int getWatcherTime();

    /**
     * Gets the mail access cache shrinker-interval seconds.
     * 
     * @return The mail access cache shrinker-interval seconds
     */
    public int getMailAccessCacheShrinkerSeconds();

    /**
     * Gets the mail access cache idle seconds.
     * 
     * @return The mail access cache idle seconds.
     */
    public int getMailAccessCacheIdleSeconds();

    /**
     * Waits for loading this properties.
     * 
     * @throws InterruptedException If another thread interrupted the current thread before or while the current thread was waiting for
     *             loading the properties.
     */
    public void waitForLoading() throws InterruptedException;
}
