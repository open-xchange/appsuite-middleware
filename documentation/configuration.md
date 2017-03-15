---
title: Configuration
description: 
icon: fa-puzzle-piece
---

With release 7.8.3 Open-Xchange starts to use code based predefined values for properties. This means that new properties aren't added to files contained in 
/opt/open-xchange/etc and its folders anymore. The administrator is (of course) able to change the provided defaults by adding the property to the file of his choice (below /opt/open-xchange/etc). 
Those properties are now described in this section. The properties are categorized by feature.  

All properties will be described by using the layout of the table below:

| Key 		  		  | `com.openexchange.foo` 	| 
| :---        		  | :---      					|
| Description 		  | Defines the foo 			|
| Default 			  | true						|
| Version 			  | 7.8.3						|
| Reloadable          | true						|
| Configcascade Aware | false						|
| Related 			  | `com.openexchange.bar` 	|
| File 			      | 							|

---

| Key 		  		  | `com.openexchange.bar` 	| 
| :---        		  | :---      					|
| Description 		  | Defines the bar 			|
| Default 			  | false						|
| Version 			  | 7.8.0						|
| Reloadable          | false						|
| Configcascade Aware | true						|
| Related 			  | `com.openexchange.foo` 	|
| File 			      | foobar.properties			|

The information within the columns have the following meaning:

  * **Key**: The key of the property. This key has to be added to the file of the administrators choice to overwrite the default value.
  * **Description**: A description of the property.
  * **Default**: The default value of the property (as defined within the code).
  * **Version**: The first version the property is available with.
  * **Reloadable**: Defines whether the value is reloadable or not.
  * **Configcascade Aware**: Defines whether the property is configcascade aware or not.
  * **Related**: Contains information about other properties that are related to the currently described one.
  * **File**: Describes the file where the property is defined or where it should be defined.