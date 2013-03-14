require 'find'
require 'pp'


def is_ox_java_file?(filename)
  filename =~ /(com\/openexchange\/.*)\.java/
end


def get_class_name(content)
	content =~ /public\s+(class|enum)\s+(\w+)/
	return $2
end


def get_prefix(content)
	content =~ /PREFIX\s*=\s*"(\w+)"/
	return $1
end


def parse_error_file(filename)
  errs = []
  file = File.open(filename, "rb")
  content = file.read
  return [] unless content =~ /public\s+enum\s.+?implements\s.*?OXExceptionCode/

  enumName = get_class_name(content)
  prefix = get_prefix(content)

  index = 0
  content.scan(/\n\s+(\w+)\((.+?)\)/) do |match|
  	index += 1
  	errorname = $1
  	params = $2

  	errornum = -1
  	if params =~ /[\(,]\s*(\d+)/
  		errornum = /[\(,]\s*(\d+)/.match(params)[1]
  	else
  		errornum = index
  	end

  	texts = []
  	params.scan(/(\w+\.\w+)/) do |match|
  		texts.push $1
  	end
  	$debugging.push "[Error:#{enumName}] errorname=#{errorname}, errornum=#{errornum}, position=#{index}, no message?" if texts.size == 0

  	errs.push({"class" => enumName, "name" => errorname, "messageRef" => texts.last}) if enumName && errorname && texts.size > 0
  end
  return errs
end


def parse_message_file(filename)
  msgs = {}
  file = File.open(filename, "rb")
  content = file.read
  return {} unless content =~ /public\s+class\s.+?implements\s.*?LocalizableStrings/

  className = get_class_name(content)
  content.scan(/\n\s+public\s+final\s+static\s+String\s+(\w+)\s*=\s*"(.+?)"/) do |match|
  	if $1 && $2
  	  msgs["#{className}.#{$1}"] = $2
  	else
  	  $debugging.push "[Message:#{className}] $1=#{$1}, $2=#{$2}"
  	end
  end
  return msgs
end


def merge(errs, msgs)
  errs.each do |err|
		err['message'] = msgs[err['messageRef']] if msgs[err['messageRef']]
	end
end



errors = []
messages = {}
$debugging = []
dirname = ARGV[0]

abort("Error: Need path to search") unless dirname

Find.find(dirname) do |file|
  next unless is_ox_java_file?(file)
  errors.concat(parse_error_file(file))
  messages.merge!(parse_message_file(file))
end
errors = merge(errors, messages)

#pp messages
pp errors

if $debugging.size > 0
  print "\n\n#{$debugging.size} ERRORS\n"
  #pp $debugging
end
