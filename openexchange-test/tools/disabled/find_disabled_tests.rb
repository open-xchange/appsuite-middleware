def iterate_all_files(directory)
  
end


def read_file(filename)
  
end



disabled_tests = Array.new

iterate_all_files(Dir.new("../..")) do |file|
  read_file(file) do |line|
    if(line =~ /public\s+void\s+(\w+?)test(\w+?)\(\)/ )
      disabled_tests << "#{file}##{$1}test#{$2}"
    end
  end
end











