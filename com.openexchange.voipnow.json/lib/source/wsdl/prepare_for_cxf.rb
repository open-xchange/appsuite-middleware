require 'nokogiri'
require 'pp'
require 'logger'

log = Logger.new(STDOUT)
log.level = Logger::INFO

usage = <<EOP
This tool helps to modify the WSDL files of 4psa's 
VoipNow to work with Apache CXF. CXF gets rather 
confused when Header and Body of a SOAP message
are defined with parts that share the same name, 
like "messagePart". So this tool renames the parts 
for the Header from messagePart to headerPart.

Parameters:
  file - wsdl file supposed to be modified.
EOP

newPartName = "headerPart"

if(ARGV.size == 0)
	print usage
	exit
end

ARGV.each do |filename| 
	unless File.exists?(filename)
		print "File not found: #{filename}"
		print usage
		exit
	end
end

ARGV.each do |filename|
	print "Working on file #{filename}..."

	#reading content
	file = File.open(filename)
	wsdl = Nokogiri::XML(file)
	file.close
	
	#make backup
	File.open(filename+".original", "w") {|fh| fh.write(wsdl.to_xml)} unless File.exists?(filename+".original")

	#finding elements to change
	messages = wsdl.xpath("//xmlns:message")
	operations = wsdl.xpath("//xmlns:operation/xmlns:input/soap:header") + wsdl.xpath("//xmlns:operation/xmlns:output/soap:header")

	#changing elements
	messages.each do |message| 
		if(message[:name] =~ /Header1$/)
			part = message.at_xpath("./xmlns:part")
			part[:name] = newPartName
		end
	end

	operations.each do |operation|
		operation[:part] = newPartName
	end

	#write  changes to file
	File.open(filename, "w") {|fh| fh.write(wsdl.to_xml)}

	print " done.\n"
end

