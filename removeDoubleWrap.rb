require 'find'

Find.find(".") do
  |f|
  next unless f.endsWith(".java")
  
  sourceCode = File.read(f);
  
  
end