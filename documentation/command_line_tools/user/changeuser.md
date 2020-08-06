---
title: changeuser
icon: far fa-circle
tags: Administration, Command Line tools, User
package: open-xchange-admin
---

# NAME

changeuser - modifies a user.

# SYNOPSIS

**changeuser** [OPTION]...

# DESCRIPTION

This command line tool allows to modify attributes of an existing user in a given context. The displayname must be unique in one context.

# OPTIONS

**-c**, **--contextid** *contextId*
: The id of the context

**-i**, **--userid** *userId*
: Id of the user 

**-u**, **--username** *username*
: Username of the user

**-d**, **--displayname** *displayName*
: Display name of the user

**-g**, **--givenname** *givenName*
: Given name for the user

**-s**, **--surname** *surname*
: Surname of the user

**-p**, **--password** *password*
: Password for the user

**-e**, **--email** *email*
: Primary mail address

**-l**, **--language** *language*
: Language for the user (de_DE,en_US,fr_FR)

**-t**, **--timezone** *timezone*
: Timezone of the user (Europe/Berlin)

**-x**, **--department** *department*
: Department of the user

**-z**, **--company** *company*
: Company of the user

**-a**, **--aliases** *aliases*
: E-Mail aliases of the user, separated by ","

**--access-combination-name** *access-combination-name*
: Access combination name

**--addguipreferences** *addguipreferences*
: Add a GUI setting (key=value) 

**--removeguipreferences** *removeguipreferences*
: Remove a GUI setting 

**--access-denied-portal** *on/off*
: Denies portal access (Default is off)

**--capabilities-to-add** *capabilities-to-add*
: The capabilities to add as a comma-separated string (from 7.2.0 on)

**--capabilities-to-remove** *capabilities-to-remove*
: The capabilities to remove as a comma-separated string (from 7.2.0 on)

**--capabilities-to-drop** *capabilities-to-drop*
: The capabilities to drop; e.g. cleanse from storage; as a comma-separated string (from 7.6.0 on) 

**-A**, **--adminuser** *masterAdmin*
: Master admin user name for authentication. Optional, depending on your configuration.

**-P**, **--adminpass** *masterAdminPassword*
: Master admin password for authentication. Optional, depending on your configuration.

**-h**, **--help**
: Prints a help text.

**--environment**
: Show info about commandline environment.

**--nonl**
: Remove all newlines (\\n) from output.

**--responsetimeout**
: The optional response timeout in seconds when reading data from server (default: 0s; infinite).

For the GUI preferences please also see http://www.open-xchange.com/wiki/index.php?title=Gui_path.

# EXTENDED OPTIONS

**--email1** *string*
: 	Email1

**--birthday** *datevalue*
: 	Birthday

**--anniversary** *datevalue*
: 	Anniversary

**--branches** *string*
: 	Branches

**--business_category** *string*
: 	Business_category

**--postal_code_business** *string*
: 	Postal_code_business

**--state_business** *string*
: 	State_business

**--street_business** *string*
: 	Street_business

**--telephone_callback** *string*
: 	Telephone_callback

**--city_home** *string*
: 	City_home

**--commercial_register** *string*
: 	Commercial_register

**--country_home** *string*
: 	Country_home

**--email2** *string*
: 	Email2

**--email3** *string*
: 	Email3

**--employeetype** *string*
: 	EmployeeType

**--fax_business** *string*
: 	Fax_business

**--fax_home** *string*
: 	Fax_home

**--fax_other** *string*
: 	Fax_other

**--imapserver** *string*
: 	ImapServer

**--imaplogin** *string*
: 	ImapLogin

**--smtpserver** *string*
: 	SmtpServer

**--instant_messenger1** *string*
: 	Instant_messenger1

**--instant_messenger2** *string*
: 	Instant_messenger2

**--telephone_ip** *string*
: 	Telephone_ip

**--telephone_isdn** *string*
: 	Telephone_isdn

**--mail_folder_drafts_name** *string*
: 	Mail_folder_drafts_name

**--mail_folder_sent_name** *string*
: 	Mail_folder_sent_name

**--mail_folder_spam_name** *string*
: 	Mail_folder_spam_name

**--mail_folder_trash_name** *string*
: 	Mail_folder_trash_name

**--mail_folder_archive_full_name** *string*
: 	Mail_folder_archive_full_name

**--manager_name** *string*
: 	Manager_name

**--marital_status** *string*
: 	Marital_status

**--cellular_telephone1** *string*
: 	Cellular_telephone1

**--cellular_telephone2** *string*
: 	Cellular_telephone2

**--info** *string*
: 	Info

**--nickname** *string*
: 	Nickname

**--number_of_children** *string*
: 	Number_of_children

**--note** *string*
: 	Note

**--number_of_employee** *string*
: 	Number_of_employee

**--telephone_pager** *string*
: 	Telephone_pager

**--password_expired** *booleanvalue*
: 	Password_expired

**--telephone_assistant** *string*
: 	Telephone_assistant

**--telephone_business1** *string*
: 	Telephone_business1

**--telephone_business2** *string*
: 	Telephone_business2

**--telephone_car** *string*
: 	Telephone_car

**--telephone_company** *string*
: 	Telephone_company

**--telephone_home1** *string*
: 	Telephone_home1

**--telephone_home2** *string*
: 	Telephone_home2

**--telephone_other** *string*
: 	Telephone_other

**--postal_code_home** *string*
: 	Postal_code_home

**--profession** *string*
: 	Profession

**--telephone_radio** *string*
: 	Telephone_radio

**--room_number** *string*
: 	Room_number

**--sales_volume** *string*
: 	Sales_volume

**--city_other** *string*
: 	City_other

**--country_other** *string*
: 	Country_other

**--middle_name** *string*
: 	Middle_name

**--postal_code_other** *string*
: 	Postal_code_other

**--state_other** *string*
: 	State_other

**--street_other** *string*
: 	Street_other

**--spouse_name** *string*
: 	Spouse_name

**--state_home** *string*
: 	State_home

**--street_home** *string*
: 	Street_home

**--suffix** *string*
: 	Suffix

**--tax_id** *string*
: 	Tax_id

**--telephone_telex** *string*
: 	Telephone_telex

**--telephone_ttytdd** *string*
: 	Telephone_ttytdd

**--uploadfilesizelimitperfile** *string*
: 	Upload file size limit per file for mail attachments

**--uploadfilesizelimit** *string*
: 	Total upload file size limit for mail attachments

**--url** *string*
: 	Url

**--userfield01** *string*
: 	Userfield01

**--userfield02** *string*
: 	Userfield02

**--userfield03** *string*
: 	Userfield03

**--userfield04** *string*
: 	Userfield04

**--userfield05** *string*
: 	Userfield05

**--userfield06** *string*
: 	Userfield06

**--userfield07** *string*
: 	Userfield07

**--userfield08** *string*
: 	Userfield08

**--userfield09** *string*
: 	Userfield09

**--userfield10** *string*
: 	Userfield10

**--userfield11** *string*
: 	Userfield11

**--userfield12** *string*
: 	Userfield12

**--userfield13** *string*
: 	Userfield13

**--userfield14** *string*
: 	Userfield14

**--userfield15** *string*
: 	Userfield15

**--userfield16** *string*
: 	Userfield16

**--userfield17** *string*
: 	Userfield17

**--userfield18** *string*
: 	Userfield18

**--userfield19** *string*
: 	Userfield19

**--userfield20** *string*
: 	Userfield20

**--city_business** *string*
: 	City_business

**--country_business** *string*
: 	Country_business

**--assistant_name** *string*
: 	Assistant_name

**--telephone_primary** *string*
: 	Telephone_primary

**--categories** *string*
: 	Categories

**--mail_folder_confirmed_ham_name** *string*
: 	Mail_folder_confirmed_ham_name

**--mail_folder_confirmed_spam_name** *string*
: 	Mail_folder_confirmed_spam_name

**--gui_spam_filter_capabilities_enabled** *booleanvalue*
: 	GUI_spam_filter_capabilities_enabled

**--mailenabled** *true/false*
: 	Mailenabled

**--defaultsenderaddress** *stringvalue*
: 	DefaultSenderAddress

**--title** *string*
: 	Title

**--position** *string*
: 	Position

**--access-calendar** *on/off*
: 	Calendar module (Default is off)

**--access-contacts** *on/off*
: 	Contact module access (Default is on)

**--access-delegate-tasks** *on/off*
: 	Delegate tasks access (Default is off)

**--access-edit-public-folder** *on/off*
: 	Edit public folder access (Default is off)

**--access-ical** *on/off*
: 	Ical module access (Default is off)

**--access-infostore** *on/off*
: 	Infostore module access (Default is off)

**--access-read-create-shared-Folders** *on/off*
: 	Read create shared folder access (Default is off)

**--access-syncml** *on/off*
: 	Syncml access (Default is off)

**--access-active-sync** *on/off*
: 	Exchange Active Sync access (Default is off)

**--access-usm** *on/off*
: 	Universal Sync Module access (Default is off)

**--access-tasks** *on/off*
: 	Tasks access (Default is off)

**--access-vcard** *on/off*
: 	Vcard access (Default is off)

**--access-webmail** *on/off*
:   Webmail access (Default is on)

**--access-publication** *on/off*
: 	[DEPRECATED] Publication permission (Default is on). Note: access-publication needs access-infostore and is optional for Groupware+ and premium

**--access-subscription** *on/off*
: 	Subscription permission (Default is on)

**--access-edit-group** *on/off*
: 	Edit group access (Default is off)

**--access-edit-resource** *on/off*
: 	Edit resource access (Default is off)

**--access-edit-password** *on/off*
: 	Edit password access (Default is off)

**--access-collect-email-addresses** *on/off*
: 	Edit collect email addresses (Default is off)

**--access-multiple-mail-accounts** *on/off*
: 	Use multiple mail account feature (Default is off)

**--access-global-address-book-disabled** *on/off*
: 	Access to global address book (Default is off). Note: Setting this option to true is only allowed in combination with PIM and Webmail rights. Note: There is a 'restoregaddefaults' script to restore the default permissions of the global addressbook folder.

**--access--voipnow** *on/off*
: 	Access to VoiceOverIP feature.

**--access-public-folder-editable** *on/off*
: 	Access to public folders. Allows or denies to see public folders.

**--foldertree** *0/1*
: 	0 sets the OX standard folder tree and 1 sets the Outlook-like folder tree.

**--access-olox20** *on/off*
: 	[DEPRECATED] Access to Olox2.0

**--default-folder-mode**
: 	The mode how the default folders should be created. 'default', 'default-deletable', 'no-default-folders'. If not selected, 'default' is applied. 

# Importing CSV Files
With the `--csv-import <CSV file>` option a full path to a CSV file with user data to import can be specified. This option makes mandatory command line options obsolete, except credential options (if needed). But they have to be set in the CSV file.

With this option you can specify a csv file (a full pathname must be given) with the data which should be imported. The columnnames in the CSV file must be the same as the long-options of the command line tools, without the prefix "--".

This option will normally be used to fill new large installations with the new data. So instead of calling the command line tools in a shell script every time, just a csv file needs to be created, containing the whole data.

Note that the credentials of the masteradmin in the createcontext call must be given on the command line with the -A and -P options nevertheless - if authentication is enabled. If the changeuser command line tool is used, the credentials are part of the csv file, and cannot be set as options on the command line itself. The reason for this different behavior is that different contexts have different credentials for the admin user, so they must be set in every line of the csv file. Opposed to this the credentials of the masteradmin are always the same. 

# MODULE ACCESS

With Open-Xchange it is possible to limit the access to the available modules per context i. e., all users in one context per default get the same access rights. The rights though can be changed per user. Currently, following modules are implemented: access-calendar, access-contacts, access-delegate-tasks, access-edit-public-folder, access-ical, access-infostore, access-read-create-shared-Folders, access-tasks, access-vcard, access-webdav, access-syncml and access-webmail. There are several combinations possible and four are supported (not mentioned modules need to be disabled). This limitation is needed because some modules depend on access to others. There are different Open-Xchange packages available for the customer: Webmail+, PIM+, Groupware+, Premium. These packages have to be configured per context i. e., all users in a context need to use the same package. Each package consists of a combination of modules that has to be set up appropriately. The following sections quickly introduce the packages and their module configuration. Open-Xchange also provides the possibility to use "access combination names" when creating and changing contexts/users. If you want to change the package acess rights for a context, you can simply add the "access-combination-name" switch to the appropriate tool (createcontext,changeuser,changecontext etc.). 

# Webmail+

If there are no access rights specified when creating a new user Webmail+ is used as default. Webmail+ is a base package that allows access to the webmail interface and a personal address book. To grant access to this package, the following modules have to be set to "on" for all users in a context: 

 - access-contacts 	Access combination name: webmail_plus
 - access-webmail 	Access combinationname: webmail_plus 

# PIM+

PIM+ is another base package that gives access to the webmailer, personal address book, calendar and tasks. Group appointments and delegating tasks are not supported. To grant access to this package, the following modules have to be set to "on" for all users in a context:
 
 - access-contacts 	Access combination name: pim_plus
 - access-webmail 	Access combination name: pim_plus
 - access-calendar 	Access combination name: pim_plus
 - access-delegate-tasks 	Access combination name: pim_plus
 - access-tasks 	Access combination name: pim_plus

# Groupware+

Groupware+ is an upsell package that provides full groupware functionality: private, shared and public folders, conflict handling for appointments, team view. Furthermore, the InfoStore is available. To grant access to this package, the following modules have to be set to "on" for all users in a context:


 - access-contacts 	Access combination name: groupware_plus
 - access-webmail 	Access combination name: groupware_plus
 - access-calendar 	Access combination name: groupware_plus
 - access-delegate-tasks 	Access combination name: groupware_plus
 - access-tasks 	Access combination name: groupware_plus
 - access-edit-public-folder 	Access combination name: groupware_plus
 - access-infostore 	Access combination name: groupware_plus
 - access-read-create-shared-Folders 	Access combination name: groupware_plus

# Premium

Premium is a desktop integration package. It provides the functionality of the "Groupware+" package and comes with interfaces to integrate with other software: The OXtender for MS Outlook and the WebDAV interface to integrate the InfoStore with desktops. To grant access to this package, the following modules have to be set to "on" for all users in a context:

 - access-contacts 	Access combination name: premium
 - access-webmail 	Access combination name: premium
 - access-calendar 	Access combination name: premium
 - access-delegate-tasks 	Access combination name: premium
 - access-tasks 	Access combination name: premium
 - access-edit-public-folder 	Access combination name: premium
 - access-infostore 	Access combination name: premium
 - access-read-create-shared-Folders 	Access combination name: premium
 - access-ical 	Access combination name: premium
 - access-vcard 	Access combination name: premium
 - access-webdav 	Access combination name: premium

# All

The setting all is equivalent to premium for ordinary users. For context administrators, it adds the right publicfoldereditable, which allows the admin to change the access rights to public folders for groups.
Package access configuration

This section provides a quick overview about the different packages that can be configured per context and the required access configuration:

|Module      | 	Webmail+| 	PIM+| 	Groupware+| 	Premium|
|---|---|---|---|---|
|access-calendar |off |	on |	on |	on|
|access-contacts |	on |	on |	on |	on|
|access-delegate-tasks |	off| 	on |	on |	on|
|access-edit-public-folder |	off |	off| 	on |	on|
|access-ical 	|off 	|off| 	off |	on|
|access-infostore |	off |	off| 	on| 	on|
|access-read-create-shared-Folders |	off 	|off| 	on |	on|
|access-syncml |	off |	off 	|off |	off|
|access-tasks |	off |	on |	on |	on|
|access-vcard |	off |	off |	off| 	on|
|access-webdav 	|off 	|off 	|off |	on|
|access-webmail |	on| 	on| 	on| 	on| 

# EXAMPLES

**changeuser -A masterAdmin -P masterPassword -c 123 -u jd -d "john doe" -g John -s Doe -p userpw -e jd@example.com**

Changes the specified user.

# SEE ALSO

[deleteuser(1)](deleteuser), [listuser(1)](listuser), [createuser(1)](createuser), [getusercapabilities(1)](getusercapabilities)