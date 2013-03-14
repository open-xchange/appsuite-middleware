require 'find'
require 'pp'


def is_ox_java_file?(filename)
  filename =~ /(com\/openexchange\/.*)\.java/
end


def get_class_name(content)
	content =~ /public\s+(class|enum)\s+(\w+)/
	return $2
end

def error(msg)
  $errors.push msg
end

def get_package(filename)
  file = File.open(filename, "rb")
  content = file.read
  return unless content =~ /public\s+enum\s+(.+?)\s+implements.*?OXExceptionCode/
  enumName = $1
  @enums.push enumName
  error("Package not found in #{file}") && return unless content =~ /^package\s(.+?);/
  package = $1
  @imports.push(package + '.' + enumName)
end


@enums = []
@errors = []
@imports = []
dirname = ARGV[0]

abort("Error: Need path to search") unless dirname

Find.find(dirname) do |file|
  next unless is_ox_java_file?(file)
  get_package(file)
end

@imports.reject!{|elem| !elem}

importStatements = @imports.map{|elem| "import #{elem};\n"}.join("")
addStatements = @enums.map{|elem| "    exceptions.addAll(Arrays.asList(#{elem}.values()));\n"}.join("")

if @errors.size > 0
  print "\n\n#{$debugging.size} ERRORS\n"
  pp $errors
else
  print <<-eos
import com.openexchange.exception.OXExceptionCode;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

#{importStatements}

public class ErrorListGenerator {

  public static void main(String[] args) {

    List<OXExceptionCode> exceptions = new LinkedList<OXExceptionCode>();
    #{addStatements}

    Collections.sort(exceptions, new Comparator<OXExceptionCode>() {
      @Override
      public int compare(OXExceptionCode o1, OXExceptionCode o2) {
        if (o1.getPrefix().compareTo(o2.getPrefix()) != 0)
          return o1.getPrefix().compareTo(o2.getPrefix());
        if (o1.getNumber() != o2.getNumber())
          return o1.getNumber() - o2.getNumber();
        return 1;
      }
    });

    for (OXExceptionCode ex: exceptions) {
      System.out.println(ex.getPrefix() + ex.getNumber() + " , " + ex.getMessage());
    }

  }

}

eos
end
