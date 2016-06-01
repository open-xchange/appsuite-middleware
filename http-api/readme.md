# Swagger definition of OX APIs

## Overview
This respository contains the documentation of OX APIs (like OX HTTP API) in Swagger. Swagger enables the
description of a REST API in one YAML or JSON file using the [Swagger Specification](http://swagger.io/specification/).
The descriptions of the OX APIs take place in several YAML files that are translated into one JSON file.
This makes it possible to separate the whole documentation in smaller parts each representing one HTTP request.

### File Structure
Instead of using one big file, each request is placed in a single YAML file. The YAML files of requests that belong together can
be grouped in folders. In case of the OX HTTP API these folders represent the particular modules. The folder naming
is _modulenumber-modulename_, like _01-login_. This naming shall lead to an ordering of the modules in a generated (web) documentation.
It is also possible to divide the requests of a module in several subfolders of the module folder.
If there is only the module folder, he must provide an _index.yaml_ file. If the module folder has subfolders then each of the subfolders
has to have an _index.yaml_ file. In easy words: if a folder contains YAML files that describe requests then the folder must have an _index.yaml_, too.
The _index.yaml_ file lists all request files in the corresponding folder. The place where all module folders are stored is the _ paths_ folder.

To sum up, we look at a folder example of the OX HTTP API. The `http_api` folder contains the Swagger definition of the requests:

```
http_api
|-- paths
    |-- 01-login
	    |-- index.yaml
		|-- LoginRequest.yaml
		|-- FormLoginRequest.yaml
		|-- ...
    |-- 02-config
	|-- ...
	|-- 30-messaging
		|-- account
			|-- index.yaml
			|-- AllRequest.yaml
			|-- ...
		|-- ...
```

The _index.yaml_ of _01-login_ might look as follows:

```yaml
requests:
  - LoginRequest.yaml
  - FormLoginRequest.yaml
  - ...
```

It describes what requests of the module login are available and where they are found providing a key `requests:`
that contains a list of the request files. Because all files (_index.yaml_ and the YAML files of the requests)
are stored in the same folder it simply contains the file names.
The example also shows module folders that consist of subfolders (see _30-messaging_).

Normally each request returns a response. Responses and request bodies are described in a model that stands in a YAML file, too.
The models are stored in a module related folder that is itself located in a base folder named _definitions_. The _definitions_ folder
contains an _index.yaml_ that collects all available models. Each model (resp. response or request body) has a name and a referenced YAML file that
contains the final definition of the response. The _index.yaml_ file might look as follows:

```yaml
# Common response containing the error fields
CommonResponse:
  type: object
  properties:
    $ref: ./common/CommonResponseData.yaml
# Module: login
LoginResponse:
  $ref:  ./login/LoginResponse.yaml
TokenLoginResponse:
  $ref: ./login/TokenLoginResponse.yaml
#...
```

As shown the reponses and request bodies of one module are stored in a folder with the name of the module. In this folder there
must not be an _index.yaml_ file. The folder structure shall simplify the editing and version control.

Up to now the folder structure is:

```
http_api
|-- definitions
    |-- index.yaml
	|-- common
		|-- CommonResponseData.yaml
		|-- ...
	|-- login
		|-- LoginResponse.yaml
		|-- ...
	|-- ...
|-- paths
    |-- 01-login
	    |-- index.yaml
		|-- LoginRequest.yaml
		|-- FormLoginRequest.yaml
		|-- ...
    |-- 02-config
	|-- ...
```

A naming convention might be to let all request bodies end with _Body_ and all responses with _Response_,
like _TaskSearchBody_ or _TasksResponse_ (same is true for the corresponding YAML files).
A YAML file representing a request could have the suffix _Request_ (as shown above).
Moreover, starting each model (and corresponding YAML file) with the singular or plural of the module name leads
to an easier assignment of model objects to a certain module, espacially in generated client APIs.

Furthermore it is possible to outsource globale parameters. These parameters are not automatically added to all
requests but the definition of those parameters must only be written once and can be referenced using the "$ref" tag.
Such parameters are specified in an _index.yaml_ file in a _parameters_ folder on the same level as the _paths_ and _definitions_
folder. For instance this YAML file might contain the following:

```yaml
# "Global" parameters that can be referenced in operation parameters with
# - $ref: "#/parameters/TheParamNameFromBelow"
gblQueryParamSession:
  name: session
  in: query
  type: string
  description: A session ID previously obtained from the login module.
  required: true
#...
```

Global parameters can be referenced in request definitions like models of the _index.yaml_ in the _definitions_ folder.
Example: shows a request with a `session` paramater, an `id` parameter and a response model (see [Short introduction to the specification](#short-introduction-to-the-specification), too)

```yaml
/resource?action=get:
  get:
    operationId: getResource
    tags:
      - resources
    summary: Gets a resource.
    parameters:
      - $ref: "#/parameters/gblQueryParamSession"
      - in: query
        name: id
        type: integer
        description: The ID of the resource.
        required: true
    responses:
      200:
        description: |
          A JSON object containing the resource data. In case of errors the responsible fields in the
          response are filled (see [Error handling](http://oxpedia.org/wiki/index.php?title=HTTP_API#Error_handling)).
        schema:
          $ref: "#/definitions/ResourceResponse"
```

All reference pointers that represent a string starting with "#" indicate an internal reference to a global parameter or
a response or request body model.

Next to this the information of a documentation can be outsourced in an _index.yaml_ file of an _info_ folder at
the base level (next to _paths_, ...). This file contains general information or an overview description like:

```yaml
title: OX HTTP API
description: The overview.
contact:
  name: Open-Xchange GmbH
  email: info@open-xchange.com
  url: https://www.open-xchange.com/en/home
version: 7.8.1
```

Finally all comes together in a last _index.yaml_ file (the base _index.yaml_) that is stored at the base level of the file structure:

```yaml
swagger: '2.0'
# Document information
info:
  $ref: ./info/index.yaml
# API host and basePath for testing requests
host: xyz.open-xchange.com
schemes:
  - https
basePath: /api
# The usual content-types of GET and PUT/POST requests
produces:
  - application/json
consumes:
  - application/x-www-form-urlencoded
# The list of modules and their short descriptions
tags:
  - name: module name
    description: A short description of this module.
# References to the definitions, "global" parameters and paths
definitions:
  $ref: ./definitions/index.yaml
parameters:
  $ref: ./parameters/index.yaml
paths:
  source: ./paths/
```

As you can see, all model definitions are listed below the `definitions` key. It is sufficient to reference
the _index.yaml_ that is stored in the _definitions_ folder. In the dereferencing process the external reference pointers
(those pointing to YAML files) are replaced by the content of the referenced file. Take another look at the `paths` key.
Here we specify the folder that contains the file structure of the modules presented above. If you like to store the request files
(resp. module folders) in another folder then you have to change the `source` key value.

The base _index.yaml_ file must only be created once and needs not to be modified except you add new modules.
If a complete new module (not new requests of existing modules) shall be described using Swagger you should add a new tag for the module in this _index.yaml_ file.
See the `tags` key in the snippet above. Otherwise the requests of the module are not grouped together and the
API class in a generated client uses illegible or default names.

### Swagger definition generation
To get a functioning Swagger definition of the API all YAML files must be put together to generate one
big _swagger.json_ file. This process is done using a simple Node.js application. The magic is done in 
a resolve.js file. The resolve.js script is stored in a level above the Swagger definition file structure of
a certain API.

```
resolve.js  <=====
http_api
|-- index.yaml
|-- info
	|-- index.yaml
|-- definitions
    |-- index.yaml
	|-- ...
|-- parameters
	|-- index.yaml
|-- paths
    |-- 01-login
	    |-- index.yaml
		|-- ...
    |-- 02-config
	|-- ...
```

To run the script, Node.js, js-yaml and json-schema-ref-parser must be installed on the system.
After Node.js is installed you can navigate to the folder with the resolve.js file (for instance with command line tool)
and run `npm install`. This installs the other dependencies using the package.json file in the folder.
Then execute `node resolve.js <BASE_FOLDER>` like `node resolve.js http_api/`. Afterwards the script dereferences all external
references in the _index.yaml_ of the specified folder (which resolves the folder structure from above) and finally outputs a _swagger.json_.

### Swagger Editor

The [Swagger Editor](http://editor.swagger.io) can be used to develop and validate a Swagger definition of an API. 
In the editor view (left) you can write YAML code that has to fit the Swagger specification and in the presentation
view (right) you see the final documentation (this presentation format may differ from visualization tool to visualization tool).
The generated _swagger.json_ from above can be imported over the Swagger Editor menu bar "File" > "Paste JSON...".
The generation of a client API is not possible from this editor due to template changes that must be necessary (see [below](#problems)).

The advantage of the editor is that errors are visualized after loading a _swagger.json_. Next to this it is possible to jump
to the error location which makes it easier to identify positions that do not match the specification.

### Short introduction to the specification
#### Define a new request
A request is depicted in a YAML file as introducted in [File structure](#file-structure). In general the YAML file consists of
key-value assignments. Before we go into detail it follows an example of a request:

```yaml
/tasks?action=list:
  put:
    operationId: getTaskList
    tags:
      - tasks
    summary: Gets a list of tasks.
    consumes:
      - application/json
    parameters:
      - $ref: "#/parameters/gblQueryParamSession"
      - $ref: "#/parameters/gblQueryParamTaskColumns"
      - in: body
        name: body
        description: A JSON array of JSON objects with the id and folder of the tasks.
        required: true
        schema:
          type: array
          items:
            $ref: "#/definitions/TaskListElement"
    responses:
      200:
        description: |
          A JSON object containing an array with data for the requested tasks. Each array element describes one task and
          is itself an array. The elements of each array contain the information specified by the corresponding
          identifiers in the `columns` parameter. In case of errors the responsible fields in the response are
          filled (see [Error handling](#error-handling)).
        schema:
          $ref: "#/definitions/TasksResponse"
```

In the first line stands the request's endpoint. The request endpoint can provide several HTTP methods, like GET, PUT, POST, etc.
In the case above it is only a PUT. The part after the method key describes the HTTP request in detail, specifying
the parameters and responses. Beside it is assigned an operation ID and a tag (see [Specify operation identifiers and tags](#specify-operation-identifiers-and-tags), too).
Additionaly it should be stated a summary of the request and if necessary a description. If a description contains line breaks you should use
the "|" operator like

```yaml
description: |
  This is a description
  
  with line breaks.
```

Another important aspect of the definition of a request is the MIME type that the request consumes and the
one it produces. In the top most _index.yaml_ there are specified the general types. Although it is possible to
list multiple MIME types it is a recommendation to only use one or at least specify the one that is mostly used at the beginning.
The general MIME type for produces is `application/json` and for consumes is `application/x-www-form-urlencoded`.
In the example we have a PUT request that sends data in JSON format, therefore it is necessary to override the general consumes with
`application/json`.

Now we want to look at the parameter definition. The parameters key contains a list of parameters. Each parameter has a
preceded "-". A parameter can be placed in the query, the path, the header, the form-data or the body.

```yaml
- in: query|path|header|formData
  name: parameter name
  type: string|number|integer|boolean|array|file
  description: a parameter description
  required: true|false
```

If you specify form-data parameters you have to consider the consumes type of the request which should be `application/x-www-form-urlencoded` or `multipart/form-data`.
It is also possible to use an extending format for the `type` like `format: int64` (long) or `format: float` (see [Data Types](http://swagger.io/specification/#dataTypeFormat), too).
In case of a body parameter the definition is as follows:

```yaml
- in: body
  name: body name
  description: a body description
  required: true|false
  schema:
    ...
```

The body is described in a schema. That can be of type `object` or `array`. It is recommended to outsource the definition
of the body in a request body model (if it is of type `object`) and use a `$ref: "#/definitions/ModelName"` to reference this body.

A parameter definition can be outsourced too (like shown in the example from above). The difference is to reference a "#/parameters/ParameterName".
This is advisable if the parameter is used in many requests and does not change in it's definition or description.

Finally we have the `responses` key that contains the response definitions. You can specify different responses for
different status codes. Here we only have the status code "200". After that it follows a description of the response and the response schema.
The response schema equals the body schema. Thus the concrete definition can be outsourced in a response model.

To publish the new request do not forget to add the request to the _index.yaml_ file of the corresponding module folder as described in [File structure](#file-structure)!

#### Define response and request body models
A model must be placed in a `definitions` section. As mentioned in the [File structure](#file structure) chapter
the model definitions are stored in own files (like the requests) in a similar folder structure. All model definitions
are aggregated in the _index.yaml_ of the _definitions_ folder. In that file the models get a name and its schema is
referenced by an external reference to the model YAML file (Sample _index.yaml_ file):

```yaml
...
TasksResponse:
  $ref: ./tasks/TasksResponse.yaml
...
```

In the _TasksResponse.yaml_ will stand the concrete response schema like:

```yaml
type: object
properties:
  $ref: ../common/CommonResponseData.yaml
  data:
    type: array
    description: Array of tasks. Each task is described as an array itself.
    items:
      type: array
      items:
        type: object
        description: |
          Array with elements that contain the information of a task specified by the corresponding
          identifiers in the columns parameter. Therefore, the element types can be distinguished.
```

All models should be of type `object`. The `properties` key defines the fields of the object.
In our case, common response fields (like error fields) are got from the _CommonResponseData.yaml_. More precisely
they are imported from the specified YAML file.
The other fields can then be of any type like `string`, `integer`, `array`, and so on, or for instance another `object`.

After a response or request body is referenced in the _index.yaml_ of the _definitions_ folder it can be used in
requests or other models by an internal reference starting with "#", followed by the section name (which is "defintions" in
case of models and "parameters" in case of global parameters) as shown above (like "#/definitions/TasksResponse").

## Generate the documentation as static HTML page
[Bootprint](https://github.com/bootprint/bootprint-openapi) is a tool to convert a _swagger.json_ into a static HTML page.

Prerequisite: `npm` is installed on the system (should be done during installation of Node.js).

1. Install `bootprint` using `npm`:

```sh
npm install -g bootprint
npm install -g bootprint-openapi
```

2. Create a HTML and CSS file from command line (files are stored in the folder `PATH/TO/documentation`):

```sh
bootprint openapi PATH/TO/swagger.json PATH/TO/documentation
```

3. OPTIONAL: Convert to single HTML file using `html-inline`
  * Install `html-inline` using `npm`:
  
  ```sh
  npm install -g html-inline
  ```
  
  * Generate self-contained HTML file `OX_HTTP_API.html` in folder `documentation` from command line:
  
  ```sh
  html-inline PATH/TO/documentation/index.html > PATH/TO/documentation/OX_HTTP_API.html
  ```

## Client API generation using Swagger Codegen
### Codegen introduction
With [Swagger Codegen](https://github.com/swagger-api/swagger-codegen) it is possible to generate client APIs using a
previously created _swagger.json_ file. Swagger Codegen comes with templates for several languages and a command line tool - swagger-codegen-cli.jar - can be executed
and parameterized to generate a concrete client API.

Example: Generation of a Java Client API

```sh
java -jar PATH/TO/swagger-codegen-cli.jar generate -i PATH/TO/swagger.json -l java -o PATH/TO/OUTPUTFOLDER -t PATH/TO/TEMPLATES -c PATH/TO/CONFIGFILE
```

Parameters:
+ _-o_ specifies the folder where the source code of the Java Client API shall be stored (e.g. `codegen/http_api/clients/java`)
+ _-t_ specifies the folder containing the Java template files (e.g. `codegen/templates/java`)
+ _-c_ specifies a JSON file with configuration options for the code generator (see [Configure Codegen](#configure-codegen))

### Problems
Although Swagger Codegen provides tons of templates, however, the current template versions (especially for Java and C#) can not handle
cookies in responses. Therefore it is necessary to edit the templates. For Java, the updated template (can be found in `codegen/templates/java`)
of `ApiClient` class template is extended by a `private List<Cookie> cookies = new ArrayList<Cookie>();`. This list is used in
`getAPIResponse` to add the current cookies to the response builder and to save the received cookies from
the response in the list.

```java
private ClientResponse getAPIResponse(String path, String method, List<Pair> queryParams, Object body, Map<String, String> headerParams, Map<String, Object> formParams, String accept, String contentType, String[] authNames) throws ApiException {
	//...

	// add received cookies to builder
	for(Cookie c : cookies)
	  builder.cookie(c);

	ClientResponse response = null;

	//...

	// if response contains cookies then add them to the list
	if(response.getCookies() != null)
	  cookies.addAll(response.getCookies());

	return response;
}
```

In case of C# (see `codegen/templates/csharp`) it is only necessary to add a

```cs
// set cookie container for automatic cookie support
RestClient.CookieContainer = new CookieContainer();
```

in the constructor of `ApiClient` class template. _Note: in both cases it is necessary to add some imports resp. using directives._

Beside the changes from above a few other modifications are done (relating to the current master branch of Swagger Codegen, commits up to 2016-03-23).
The namespaces of the C# templates can be configured using a config file (see below). Furthermore a `toJson()` method was added
to the _pojo.mustache_ (template file) of the Java templates to simplify the generation of JSON strings from
model objects (for C# this method was already present) in a created Java Client API.

### Configure Codegen

As previously mentioned, it is possible to configure the code generation with a few properties. These properties are
stored in a JSON file. Existing configuration files are for example `codegen/http_api/configs/java.json` and
`codegen/http_api/configs/csharp.json`. These configuration files must be stored individually for each API that
is described using Swagger. It follows a sample config file for the Java Client API generation of OX HTTP API:

```json
{
	"modelPackage": "com.openexchange.clientapi.http.models",
	"apiPackage": "com.openexchange.clientapi.http.modules",
	"invokerPackage": "com.openexchange.clientapi.http.invoker",
	"groupId": "com.openexchange.clientapi.http",
	"artifactId": "OXHttpApi",
	"artifactVersion": "0.0.1",
	"serializableModel": false
}
```

The properties are language specific and must not be existing for each Codegen configuration. Here, the primary
options are the ones for the packages the java files are stored in. The package description uses the common package
structure. The package for the API classes is `modules` and the one for the `ApiClient` and related classes is `invoker`. Response
and request body models are placed in the `models` package. The artifact ID and version are used to specify the user agent name of the client.
The group ID is relevant for the maven POM file generation.

### Specify operation identifiers and tags

For code generation it is important to specify `operationId`s in the definition of a request. The `operationId`
determines the name of the request's method in the client API. Otherwise the name might be illegible. The following example (from above)
shows the allocation of an `operationId`:

```yaml
/resource?action=get:
  get:
    operationId: getResource
    tags:
      - resources
    #...
```

Additionally a tag should be specified with the name of the related module. The Swagger Codegen uses the tag
to name the API class of a certain module. Furthermore the tag is responsible for grouping of requests (e.g. when the _swagger.json_
is visualized as web documentation).

## Examples (Java Client API for OX HTTP API)

After the code generation of the Java Client API is done (see [Codegen introduction](#codegen-introduction)), execute the _POM.xml_ file
in the generated folder using [Apache Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).
This downloads all dependencies (Java libraries) that will be needed to use the Java Client API. Afterwords create a new Java project
including the downloaded libraries and the generated source code. Then it is possible to compile a stand-alone
Client API JAR.

### Snippet: Send a mail
```java
package main;

import java.util.Arrays;

import com.openexchange.swagger.httpapi.invoker.ApiClient;
import com.openexchange.swagger.httpapi.invoker.ApiException;
import com.openexchange.swagger.httpapi.invoker.Configuration;
import com.openexchange.swagger.httpapi.models.LoginResponse;
import com.openexchange.swagger.httpapi.models.MailAttachment;
import com.openexchange.swagger.httpapi.models.SendMailData;
import com.openexchange.swagger.httpapi.modules.LoginApi;
import com.openexchange.swagger.httpapi.modules.MailApi;

public class OXHttpJavaClientApiTest {

	public static void main(String[] args) {
		final ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.setBasePath("https://example.com/appsuite/api");
		
		final LoginApi loginApi = new LoginApi(apiClient);
		final MailApi mailApi = new MailApi(apiClient);
		
		try {
			// log into App Suite
			final LoginResponse loginResponse = loginApi.doLogin("username", "password", null, null, null, null, null);
			// successfully logged in?
			if(loginResponse.getSession() != null && !loginResponse.getSession().isEmpty()) {
				System.out.println("Successfully logged in! Your session: " + loginResponse.getSession());
				
				// build a mail
				final SendMailData mail = new SendMailData();
				mail.setFrom(Arrays.asList(Arrays.asList("Foo Bar", "foo.bar@example.com")));
				mail.setTo(Arrays.asList(Arrays.asList("Bar Foo", "bar.foo@example.com")));
				mail.setSubject("Hello World!");
				mail.setSendtype(0);
				// create the message
				final MailAttachment mailAttachment = new MailAttachment();
				mailAttachment.setContent("<p><b>Hello World!</b></p><p>This is Java Client API speaking.</p>");
				mailAttachment.setContentType("ALTERNATIVE");
				mailAttachment.setDisp("inline");
				mail.setAttachments(Arrays.asList(mailAttachment));
				
				// send the mail
				final String strResult = mailApi.sendMail(loginResponse.getSession(), mail.toJson(), null);
				System.out.println(strResult);
			}
			else {
				System.err.println("Login failed! " + loginResponse.toJson());
			}
		} catch (ApiException e) {
			System.err.println(e);
		}
	}

}
```

### Snippet: Usage of the multiple module
```java
package main;

import java.util.Arrays;
import java.util.List;

import com.openexchange.clientapi.http.invoker.ApiException;
import com.openexchange.clientapi.http.models.LoginResponse;
import com.openexchange.clientapi.http.models.ReminderUpdateBody;
import com.openexchange.clientapi.http.models.SingleRequest;
import com.openexchange.clientapi.http.models.SingleResponse;
import com.openexchange.clientapi.http.modules.LoginApi;
import com.openexchange.clientapi.http.modules.MultipleApi;

public class OXHttpJavaClientApiTest {

	public static void main(String[] args) {
		final ApiClient apiClient = Configuration.getDefaultApiClient();
		apiClient.setBasePath("https://example.com/appsuite/api");
		
		final LoginApi loginApi = new LoginApi(apiClient);
		final MultipleApi multipleApi = new MultipleApi(apiClient);
		
		try {
			// log into App Suite
			final LoginResponse loginResponse = loginApi.doLogin("username", "password", null, null, null, null, null);
			// successfully logged in?
			if(loginResponse.getSession() != null && !loginResponse.getSession().isEmpty()) {
				System.out.println("Successfully logged in! Your session: " + loginResponse.getSession());
			
				// process a reminder range and update request at once
				List<SingleResponse> responses = multipleApi.process(loginResponse.getSession(), Arrays.asList(
						new SingleRequest().module("reminder").action("range").end(1497461067180L),
						new SingleRequest().module("reminder").action("remindAgain").id("51").data(new ReminderUpdateBody().alarm(1429478800000L))
					), null);
				System.out.println(responses);
			}
			else {
				System.err.println("Login failed! " + loginResponse.toJson());
			}
		} catch (ApiException e) {
			System.err.println(e);
		}
	}

}
```