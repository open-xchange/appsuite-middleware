# Config module
The config module is used to retrieve and set user-specific configuration. The configuration is stored in a tree. Each node of the tree has a name and a value. 
The values of leaf nodes are strings which store the actual configuration data. The values of inner nodes are defined recursively as objects with one field for each child node. 
The name and the value of each field is the name and the value of the corresponding child node, respectively.

The namespace looks like the following:

* /ajax/config/
  * gui – A string containing GUI-specific settings (currently, it is a huge JSON object).
  * fastgui - A string containing GUI-specific settings. This is a JSON object that must be kept small for performance.
  * context_id - the unique identifier of the context (read-only).
  * cookielifetime - the cookie life time in seconds or -1 for session cookie (read-only).
  * identifier – the unique identifier of the user (read-only).
  * contact_id – the unique identifier of the contact data of the user (read-only).
  * language – the configured language of the user.
  * timezone – the configured timezone of the user.
  * availableTimeZones – a JSON object containing all available time zones. The key is the time zone identifier and the value contains its name in users language. (read-only).
  * reloadTimes - Selectable times for GUI reload
  * serverVersion - Version string of the server.
  * currentTime - User timezone specific long of the current server time.
  * maxUploadIdleTimeout - Timeout after that idle uploads are deleted.
  * folder/ – the standard folder of the user
    * tasks – the standard task folder (read-only)
    * calendar – the standard calendar folder (read-only)
    * contacts – the standard contacts folder (read-only)
    * infostore – the private infostore folder (read-only)
    * eas – whether EAS folder selection is enabled (read-only)
  * participants
    * autoSearch - If a search for all users, groups and resources when participant selection dialog is opened. (read-only)
    * showWithoutEmail - If external participants without email should be shown.
    * showDialog – Enables participant selection dialog for appointments and tasks. (read-only)
  * availableModules – Contains a JSON array listing all enabled modules for a user. GUI loads Plugins through this list. To get your plugin listed here, create a subtree below modules/ without a module   * subelement or with a subelement containing true (read-only)
  * minimumSearchCharacters – Minimum number of characters a search pattern must have to prevent large responses and slow queries. (read-only)
  * modules
    * portal
      * gui GUI settings for portal module
      * module
    * mail
      * addresses – all email addresses of the user including the primary address (read-only)
      * appendmailtext
      * allowhtmlimages – Alters default setting whether external images contained in HTML content are allowed or not
      * colorquoted – color quoted lines
      * contactCollectFolder – contact folder id to save mail addresses from sent mails
      * contactCollectEnabled – switch contact collection on/off
      * contactCollectOnMailAccess – enables/disables contact collection for incoming mails. Default is true.
      * contactCollectOnMailTransport – enables/disables contact collection for outgoing mails. Default is true.
      * defaultaddress – primary email address of the user (read-only)
      * deletemail – delete emails or move to trash
      * emoticons – display emoticons as graphics
      * defaultFolder
        * drafts – identifier of the folder with the mail drafts (read-only)
        * inbox – identifier of the folder that gets all incoming mails (read-only)
        * sent – identifier of the folder with the sent mails (read-only)
        * spam – identifier of the folder with the spam mails (read-only)
        * trash – identifier of the folder with the deleted mails (read-only)
      * forwardmessage – forward messages as inline or attachment
      * gui GUI settings for mail module
      * inlineattachments – activate inlining of HTML attachments
      * linewrap
      * module – if mail module is enabled or not
      * phishingheaders – header(s) identifying phishing headers
      * replyallcc – put all recipients on reply all into CC
      * sendaddress – one email address out of the addresses list that are email sent with
      * spambutton – Spam Button should be displayed in GUI or not
      * vcard – attach vcard when sending mails
    * calendar
      * calendar_conflict
      * calendar_freebusy
      * calendar_teamview
      * gui GUI settings for the calendar module
      * module
      * notifyNewModifiedDeleted receive mail notification for new, modified or deleted appointments
      * notifyAcceptedDeclinedAsCreator receive mail notification for accepted or declined appointments created by the user
      * notifyAcceptedDeclinedAsParticipant receive mail notification for accepted or declined appointments that the user participates
      * defaultStatusPrivate Default status for new appointments in private folders, where the user is participant. This does not affect appointments created by this user, which always have the status "accepted". The status are described in [User participant object](#user-participant-object). Default is 0:none 
      * defaultStatusPublic Default status for new appointments in public folders, where the user is participant. This does not affect appointments created by this user, which always have the status "accepted". The status are described in [User participant object](#user-participant-object). Default is 0:none
    * contacts
      * gui GUI settings for the contacts module
      * mailAddressAutoSearch – Define if a search is triggered when the recipient selection dialog is opened or the folder is changed. (read-only)
      * module True if the contact module is enabled for the current user, false otherwise.
      * singleFolderSearch – True if the current user is allowed to search for contacts only in a single folder. False if contact searches across all folders are allowed. (read-only)
      * characterSearch – True if the side bar for searching for contacts by a start letter should be displayed. False if the side bar should be hidden. (read-only)
      * allFoldersForAutoComplete – true if an auto complete search may omit the folder identifier array and search for contacts in all readable folders. This is configured through the contact.properties configuration file. (read-only)
    * tasks
      * gui GUI settings for the tasks module
      * module
      * delegate_tasks
      * notifyNewModifiedDeleted receive mail notification for new, modified or deleted tasks
      * notifyAcceptedDeclinedAsCreator receive mail notification for accepted or declined tasks created by the user
      * notifyAcceptedDeclinedAsParticipant receive mail notification for accepted or declined taks that the user participates
    * infostore
      * gui GUI settings for the infostore module
      * folder – the standard infostore folders (read-only)
        * trash – identifier of the default infostore trash folder (read-only)
        * pictures – identifier of the default infostore pictures folder (read-only)
        * documents – identifier of the default infostore documents folder (read-only)
        * music – identifier of the default infostore music folder (read-only)
        * videos – identifier of the default infostore videos folder (read-only)
        * templates – identifier of the default infostore templates folder (read-only)
      * module
    * interfaces
      * ical
      * vcard
      * syncml
    * folder
      * gui UI settings for the folder tree
      * public_folders
      * read_create_shared_folders
      * tree – Selected folder tree, the user wants to use. Currents trees are 0 for the known OX folder tree and 1 for the new virtual folder tree.
    * com.openexchange.extras
      * module – Extras link in the configuration (read only)
    * com.openexchange.user.passwordchange
      * module – Will load Plug-In which allows to change the Password within the users configuration (read only)
    * com.openexchange.user.personaldata
      * module – Will load Plug-In which allows to edit personal contact information within the users configuration (read only)
    * com.openexchange.group
      * enabled – Specifies whether the user is allowed to edit groups and loads the corresponding Plug-In. (read only)
    * com.openexchange.resource
      * enabled – Specifies whether the user is allowed to edit resources and loads the corresponding Plug-In. (read only)
    * com.openexchange.publish
      * enabled – Specifies whether the user is allowed to publish items. (read only)
    * com.openexchange.subscribe
      * enabled – Specifies whether the user is allowed to subscribe sources. (read only)
    * olox20 [DEPRECATED]
      * active – Tells the UI if the user is allowed to use the OXtender for Microsoft Outlook 2. (read only)
      * module – Is set to false to prevent the UI from trying to load a plugin. (read only)
    * com.openexchange.oxupdater [DEPRECATED]
      * module – Is true if the OXUpdater package is installed and started. (read only)
      * active – Is true if the user is allowed to download the OXUpdater. Otherwise it's false. (read only)
    * com.openexchange.passwordchange
      * showStrength – Show a widget, which displays the current passwort Strength while entering. (default: false)
      * minLength – The minimum length of an entered password. (default: 4)
      * maxLength – The maximum length of an entered password. 0 for unlimited. (default: 0)
      * regexp – Defines the class of allowed special characters as Regular Expression. (default: [^a-z0-9])
      * special – Shows an example of allowed special characters to the user. Should be a subset of "regexp" in a human readable format. (default: $, _, or %)