/*
 * Copyright (C) 2016 Open-Xchange GmbH
 * ==============================================================================================
 * Task:		 creates a Swagger JSON file by merging several YAML files in a given folder
 *				 structure
 * Created:		 03-15-2016
 * Last Changed: 04-14-2016
 */
var yaml = require('js-yaml');
var fs = require('fs');
var path = require('path');
var $RefParser = require('json-schema-ref-parser');
var jsonFileName = "swagger.json";
var customFileName = false;
var scriptName = path.basename(process.argv[1]);
var scriptUsage = "Usage: $>node " + scriptName + " <BASE_FOLDER> [<NAME_OF_JSONFILE>]";
var scriptExample = "Example: $>node " + scriptName + " http_api/";

// check if mandatory command line arguments are missing
if(process.argv.length < 3) {
	console.log("ERROR: missing mandatory command line arguments!");
	console.log(scriptUsage);
	console.log(scriptExample);
	process.exit(-1);
}

var baseFolder = process.argv[2];

if(process.argv.length >= 4) {
	var value = process.argv[3];
	if(isValidFileName(value)) {
		if(value.indexOf(".json", value.length - ".json".length) !== -1)
			jsonFileName = value;
		else
			jsonFileName = value + ".json";
		
		customFileName = true;
	}
}

try {
	// load index.yaml (starting point) of the base folder
	var baseIndexFile = yaml.safeLoad(fs.readFileSync(path.join(baseFolder, 'index.yaml'), 'utf8'));
	if (baseIndexFile.paths.source) {
		console.log("Resolve paths and collect index.yaml files in \"" + baseFolder + "\" ...")
		var folder = path.join(baseFolder, baseIndexFile.paths.source);

		// locate all index.yaml files in the paths folder
		var indexFileFolders = [];
		var result = loadIndexFileFolders(folder, indexFileFolders);
		var resolvedPaths = [];
		
		// output result of resolving the paths folder
		console.log("Finished searching for valid path folders.");
		console.log("> Total requests found:   " + result.requestCount);
		if(result.errorCount > 0 || result.warningCount > 0) {
			console.log("#################################################################################");
			if(result.errorCount > 0) {
				console.log("# Errors: " + result.errorCount);
				for(var i = 0; i < result.errors.length; i++)
					console.log("#  > " + result.errors[i]);
			}
			if(result.warningCount > 0) {
				console.log("# WARNINGS: " + result.warningCount);
				for(var i = 0; i < result.warnings.length; i++)
					console.log("#  > " + result.warnings[i]);
			}
			console.log("#################################################################################");
		}

		// copy the content of each index file inside the base index.yaml (temporary)
		for (var i = 0; i < indexFileFolders.length; i++) {
			var indexFileFolder = indexFileFolders[i];    

			var indexFile = yaml.safeLoad(fs.readFileSync(path.join(indexFileFolder, 'index.yaml'), 'utf8'));
			if (indexFile.requests) {
				for (var j = 0; j < indexFile.requests.length; j++) {
					var pathFile = path.join(indexFileFolder, indexFile.requests[j]);
					var pathDefinition = yaml.safeLoad(fs.readFileSync(pathFile, 'utf8'));

					resolvedPaths.push(pathDefinition);
				}
			}
		}
	}
	else
		drawError("No source key found in main index.yaml!", null);

	// do we have some paths definitions?
	if (resolvedPaths && resolvedPaths.length > 0) {
		// resolve all external $ref statements in base index.yaml file but no internal ("#/.../...")
		$RefParser.dereference(path.join(baseFolder, "index.yaml"), {$refs: {internal: false }}).then(function(file) {
			// remove the source key
			file.paths.source = null;
			delete file.paths['source'];

			// add all resolved paths to the paths key of the file
			var mainPathsDef = file.paths;
			for (var i = 0, len = resolvedPaths.length; i < len; i++)
				mainPathsDef = extendPathsDef(mainPathsDef, resolvedPaths[i]);

			file.paths = mainPathsDef;

			console.log("External reference pointers successfully dereferenced!");
			console.log("Writing " + jsonFileName + " ...");
			fs.writeFile(path.join(baseFolder, jsonFileName), JSON.stringify(file), function(err) {
				if (err)
					drawError("during writing the JSON file", err);
				else
					console.log(jsonFileName + " written successfully!");
				
				drawInfo();
			});
		})
		.catch(function(err) {
			drawError("during dereferencing", err);
		});
	}
}
catch (err) {
	drawError("during resolve process", err);
}

function extendPathsDef() {
	if (arguments.length === 0)
		return;
	
	var x = arguments.length === 1 ? this : arguments[0];
	var y;

	for (var i = 1; i < arguments.length; i++) {
		y = arguments[i];
		for (var key in y) {
			if (!(y[key] instanceof Function))
				x[key] = y[key];
		}
	}

	return x;
}

function loadIndexFileFolders(fsPath, indexFolders) {
	var indexFileFound = false;
	var requestFileCount = 0;
	var result = {errorCount:0, errors:[], warningCount:0, warnings:[],requestCount:0};
	
	try {
		var dirContent = fs.readdirSync(fsPath);
		console.log("Enter folder " + fsPath + " ...");
		
		// recursivly inspect all subfolders and save the index.yaml locations inside the array
		for (var i = 0, len = dirContent.length; i < len; i++) {
			var relativeFilePath = path.join(fsPath, dirContent[i]);
			var fso = fs.statSync(relativeFilePath);

			if (fso.isDirectory()) {
				var innerResult = loadIndexFileFolders(relativeFilePath, indexFolders);
				result.errorCount += innerResult.errorCount;
				result.errors = result.errors.concat(innerResult.errors);
				result.warningCount += innerResult.warningCount;
				result.warnings = result.warnings.concat(innerResult.warnings);
				result.requestCount += innerResult.requestCount;
			}
			else if(fso.isFile()) {
				if(path.basename(relativeFilePath) === "index.yaml") {
					indexFolders.push(path.dirname(relativeFilePath));
					indexFileFound = true;
				}
				else
					requestFileCount++;
			}
		}
	} catch (err) {
		result.errorCount++;
		result.errors.push("Unable to observe directory structure at \"" + fsPath + "\", Error: " + err);
	}
	
	// check whether there are possible errors
	if(!indexFileFound && requestFileCount > 0) {
		result.warningCount++;
		result.warnings.push("Folder \"" + fsPath + "\" contains no index.yaml but has request files!")
	}
	else if(indexFileFound) {
		// load the index file (temporary) and check the number of request files and the number
		// of requests that are specified in the index.yaml
		var indexFile = yaml.safeLoad(fs.readFileSync(path.join(fsPath, 'index.yaml'), 'utf8'));
		var requestCount = (indexFile.requests !== null) ? indexFile.requests.length : 0;
		if(requestFileCount != requestCount) {
			result.warningCount++;
			result.warnings.push("Folder \"" + fsPath + "\" contains " + requestFileCount + " request file(s) but " + requestCount + " specified in corresponding index.yaml!");
		}
		
		result.requestCount += requestCount;
	}
	
	return result;
}

function isValidFileName(filename) {
	var regex = /^[^\\/:\*\?"<>\|]+$/;
	return regex.test(filename);
}

function drawInfo() {
	if(!customFileName) {
		console.log("---------------------------------------------------------------------------------");
		console.log("INFO: you can specify the file name of the JSON file as optional argument");
		console.log(scriptUsage);
	}
}

function drawError(title, err) {
	console.log("#################################################################################");
	console.log("# ERROR: " + title);
	if(err !== null && err.message)
		console.log("# > " + err.message);
	console.log("#################################################################################");
}