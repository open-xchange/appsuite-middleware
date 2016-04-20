## Mail categories documentation

With OX Middleware version 7.8.2 a feature called mail categories (aka tabbed inbox) is introduced.
This features divides the inbox of the primary email account into categories. Each category acts like a container for emails.
The emails will be categorized into this containers based on email flags. Whereby each mail can only be in one category at a time.
It is possible to enable or disable single categories by the user.
All mails which does not belong to any active category will be categorized in a 'general' category.
There are two sets of categories. Each set contains a different type of categories. The two types are system categories and user categories.

#### System categories
System categories are predefined by the hoster. They include a specific set of rules, which determines if a mail should be put into a category or not.
They also have a fixed name and optional translations. Its possible for the hoster to prevent the deactivation of system categories.


#### User categories
In addition to the system categories the hoster is able to define user categories.
User categories are very similar to system categories. In difference to the system categories they do not include any rules at the beginning and
they can be renamed by the user. For this reason translations are also not available. It is also not possible to prevent deactivation of user categories.


#### Customize categories

Both system and user categories can be customized. The user has three options to do that.

1. Move one or more mails from one category to another
2. Train a category with one or more email addresses
3. Reorganize old mails

With the first option the user is able to move mails between the different categories manually.
The second option allows the user to train a category with one or more given mail addresses.
Once trained all new mails which contains the full mail address in the from header are categorized in the trained category.
If another category is trained with this email as well the old rule will be removed.
In addition to option two the user is also able to reorganize all existing mails within the inbox.
That means that all mails which match the rules of the category will be categorized in this category.


### Configuration

#### Server

For this feature a new configuration file is introduced called mail-categories.properties.
All configurations are config cascade aware and can therefore be overwritten on context or user level.

To properly configure the mail categories feature one has to do the following steps:

1. Set the property com.openexchange.capability.mail_categories to 'true' for the users you want to have access to the mail categories feature

2. Define categories
 * Define system categories
    * Add a category id ([category]) for each system category to com.openexchange.mail.categories.identifiers
    * Add the following configurations for each system category id
      * com.openexchange.mail.categories.[category].flag
      The id of the email flag.
      * com.openexchange.mail.categories.[category].force
      Set to true if the user shouldn't be able to disable the category
      * com.openexchange.mail.categories.[category].active=true
      * com.openexchange.mail.categories.[category].name
      The fallback name of the category
      * com.openexchange.mail.categories.[category].name.de_DE
      A translation entry for each supported language. See also com.openexchange.mail.categories.languages.
  * Define user categories
    * Add a category id ([category]) for each user category (e.g. "uc1, uc2, uc3") to com.openexchange.mail.user.categories.identifiers
    * Add the following configurations for each user category id
      * com.openexchange.mail.categories.[category].flag
      The id of the email flag.
      * com.openexchange.mail.categories.[category].active=true
      * com.openexchange.mail.categories.[category].name
      The default name of the category

In addition it is also possible to configure the fallback name and the translations for the general category. It is done in the exact same way as the other categories.
One just has to use _general_ as the category identifier. In contrary to the other categories only the name and the translation fields are taken into account. Other fields will be ignored.

#### Client

The config tree is extended with additional entries. First of all the capability mail_categories is introduced, which defines if the mail_categories module is available for the user.
In addition the io.ox/mail tree is extended with an entry 'categories':

    "categories": {
         "enabled": true,
         "list": [
           {
             "id": "offers",
             "name": "Offers",
             "active": true,
             "permissions": [
               "teach"
             ]
           },
           {
             "id": "other",
             "name": "other",
             "active": true,
             "permissions": [
               "disable",
               "teach"
             ]
           },
           {
             "id": "uc1",
             "name": "Friends",
             "active": true,
             "permissions": [
               "rename",
               "teach"
             ]
           }
         ]
       }

The categories entry contains two main fields: 'enabled' and 'list'. The 'enabled' field is a boolean flag indicating if the categories should be shown or not and can be overwritten by the client.
The 'list' field contains an array of single category configurations and always contains the 'general' category. Each category config has four fields: 'id', 'name', 'active' and 'permissions'.

| Fieldname   |                                                                                                                                                                             Description |
|:------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| id          |                                                                                                                                                                     category identifier |
| name        | The current name of the category. If the category is a system category and there is a translation for the configured language of the user, the name field contains the translated name. |
| active      |                                                                                                                        A boolean flag indicating whether the category is active or not. |
| Permissions |                                                                                                             A list of permissions. Possible values are: 'rename', 'train' and 'disable' |

The permissions _rename_ and _disable_ indicating whether the fields _name_ and _active_ are writable or not. All other fields are read only and must not be changed by the client.





### HTTP API

#### Mail

###### Get all mails

GET /ajax/mail?action=all

Parameters:

| Name              |                                                                                                                                                                                                                                    Description |
|:------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| session           |                                                                                                                                                                                        A session ID previously obtained from the login module. |
| folder            |                                                                                                                                                                                           Object ID of the folder, whose contents are queried. |
| columns           |                                                                   A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for appointments are defined in Detailed mail data. |
| sort (optional)   | The identifier of a column which determines the sort order of the response or the string “thread” to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified. |
| order (optional)  |                "asc" if the response entires should be sorted in the ascending order, "desc" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified. |
| left_hand_limit   |                                                                                                                                                              A positive integer number to specify the "left-hand" limit of the range to return |
| right_hand_limit  |                                                                                                                                                             A positive integer number to specify the "right-hand" limit of the range to return |
| limit             |                         A positive integer number to specify how many items shall be returned according to given sorting; overrides left_hand_limit/right_hand_limit parameters and is equal to left_hand_limit=0 and right_hand_limit=<limit> |
| filter (optional) |                                                                                                                            The category id to filter for. If set to "General" retrieves all mails which does not belong to any other category. |


Response (not IMAP: with timestamp): An array with mail data. Each array element describes one mail and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.

#### Mail Categories

###### Move mail to category

Move a mail to the given category. Also removes the mail from any other category.

PUT /ajax/mail/categories?action=move

Parameters:

| Name        |                                             Description |
|:------------|--------------------------------------------------------:|
| session     | A session ID previously obtained from the login module. |
| category_id |                                   A category identifier |

Body:

A JSON array of mail identifier, e.g.:

[{"id":ID, "folder_id":FID},{"id":ID, "folder_id":FID}, {...}]


Response:  An empty response if everything went well or a JSON object containing the error information.


###### Get unread counters

Retrieves a list of unread counters for each active category and the 'general' category

GET /ajax/mail/categories?action=unread

Parameters:

| Name                    |                                                                                                       Description |
|:------------------------|------------------------------------------------------------------------------------------------------------------:|
| session                 |                                                           A session ID previously obtained from the login module. |
| category_ids (optional) | A comma separated list of category identifiers. If set only the unread counters of this categories are retrieved. |

Response:  A JSON object with a field for each active category containing the number of unread messages. e.g.:

    {
      "data": {
      "general": 1,
      "offers": 5,
      "other": 3,
      "uc1": 1
      }
    }

###### train Category

Adds a new rule with the given mail addresses to the given category and optionally reorganize all existing mails in the inbox.

PUT /ajax/mail/categories?action=train

| Name                             |                                                                     Description |
|:---------------------------------|--------------------------------------------------------------------------------:|
| session                          |                         A session ID previously obtained from the login module. |
| category_id                      |                                                          A category identifier. |
| apply-for-existing (optional)    | A flag indicating whether old mails should be reorganized. Defaults to 'false'. |
| apply-for-future-ones (optional) |  A flag indicating whether a rule should be created or not. Defaults to 'true'. |

Body:

A JSON array of mail addresses, e.g.:

    ["email1@domain1.tld","email2@domain2.tld"]

Response:  An empty response if everything went well or a JSON object containing the error information.
