test_stories = <<HEREDOC
<HierarchicalRequirement rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/hierarchicalrequirement/5286716540" objectVersion="6" refObjectName="[user docu] Fix table in chapter 5.3.1 in User Guide" CreatedAt="Jan 26">
   <CreationDate>2012-01-26T07:29:46.091Z</CreationDate>
   <ObjectID>5286716540</ObjectID>
   <Subscription rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/subscription/282116333" refObjectName="Open-Xchange GmbH" type="Subscription" />
   <Workspace rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/workspace/282116338" refObjectName="Open-Xchange" type="Workspace" />
   <Changesets />
   <Description>Copy missing row from 'Panel' table to 'toolbar' table</Description>
   <Discussion />
   <FormattedID>US7184</FormattedID>
   <LastUpdateDate>2012-02-13T12:19:03.589Z</LastUpdateDate>
   <Name>[user docu] Fix table in chapter 5.3.1 in User Guide</Name>
   <Notes />
   <Owner rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/user/291092766" refObjectName="Harald Petry" type="User" />
   <Project rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/project/285155000" refObjectName="OX6" type="Project" />
   <RevisionHistory rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/revisionhistory/5286716541" type="RevisionHistory" />
   <Tags />
   <Attachments />
   <Package>Documentation</Package>
   <Blocked>false</Blocked>
   <Children />
   <DefectStatus>NONE</DefectStatus>
   <Defects />
   <InProgressDate>2012-02-06T07:27:16.159Z</InProgressDate>
   <Iteration rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/iteration/4731932227" refObjectName="v6.20.1 Patches-S1" type="Iteration" />
   <Parent rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/hierarchicalrequirement/346014762" refObjectName="i18N - Internationalisierung &amp; Documentation" type="HierarchicalRequirement" />
   <PlanEstimate>0.15</PlanEstimate>
   <Predecessors />
   <Rank>499998912512.000</Rank>
   <Recycled>false</Recycled>
   <Release rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/release/2441741614" refObjectName="v6.20.0 Patches" type="Release" />
   <ScheduleState>Completed</ScheduleState>
   <Successors />
   <TaskActualTotal>0.5</TaskActualTotal>
   <TaskEstimateTotal>1.0</TaskEstimateTotal>
   <TaskRemainingTotal>0.0</TaskRemainingTotal>
   <TaskStatus>COMPLETED</TaskStatus>
   <Tasks>
      <Task rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/task/5286717344" refObjectName="Fix table in English" type="Task" />
      <Task rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/task/5286717074" refObjectName="Fix table in german" type="Task" />
   </Tasks>
   <TestCaseStatus>NONE</TestCaseStatus>
   <TestCases />
   <Scope>Customer Facing</Scope>
</HierarchicalRequirement>
HEREDOC

test_tasks = <<HEREDOC
<Task rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/task/5286717344" objectVersion="2" refObjectName="Fix table in English" CreatedAt="Jan 26">
   <CreationDate>2012-01-26T07:30:46.824Z</CreationDate>
   <ObjectID>5286717344</ObjectID>
   <Subscription rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/subscription/282116333" refObjectName="Open-Xchange GmbH" type="Subscription" />
   <Workspace rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/workspace/282116338" refObjectName="Open-Xchange" type="Workspace" />
   <Changesets />
   <Description />
   <Discussion />
   <FormattedID>TA8090</FormattedID>
   <LastUpdateDate>2012-02-13T12:19:03.572Z</LastUpdateDate>
   <Name>Fix table in English</Name>
   <Notes />
   <Owner rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/user/342236670" refObjectName="Antje Faber" type="User" />
   <RevisionHistory rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/revisionhistory/5286717345" type="RevisionHistory" />
   <Tags />
   <Actuals>0.25</Actuals>
   <Attachments />
   <Blocked>false</Blocked>
   <Estimate>0.5</Estimate>
   <Iteration rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/iteration/4731932227" refObjectName="v6.20.1 Patches-S1" type="Iteration" />
   <Project rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/project/285155000" refObjectName="OX6" type="Project" />
   <Rank>499998912512.000</Rank>
   <Recycled>false</Recycled>
   <Release rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/release/2441741614" refObjectName="v6.20.0 Patches" type="Release" />
   <State>Completed</State>
   <TaskIndex>1</TaskIndex>
   <ToDo>0.0</ToDo>
   <WorkProduct rallyAPIMajor="1" rallyAPIMinor="31" ref="https://rally1.rallydev.com/slm/webservice/x/hierarchicalrequirement/5286716540" refObjectName="[user docu] Fix table in chapter 5.3.1 in User Guide" type="HierarchicalRequirement" />
   <Completedin>2012-02</Completedin>
</Task>
HEREDOC

require 'rubygems'
require "hpricot"
require 'nokogiri'
require "open-uri"

class PivotalStory
  attr_accessor :description, :name, :owned_by, :requested_by, :project_id, :story_type, :estimate
end


#puts "Usage: program-name userstory1 userstory2... userstoryN" and exit if ARGV.length < 1
#story_identifiers = ARGV
#stories = Nokogiri::XML(open('Stories.xml')).root
#tasks = Nokogiri::XML(open('Tasks.xml')).root

story_identifiers = ["https://rally1.rallydev.com/slm/webservice/x/hierarchicalrequirement/5286716540", "US7184", "5286716540"]
story_identifiers.map! { |elem| (elem =~ /(\d+)$/)  ? $1  : elem }

stories = Nokogiri::XML(test_stories).root
tasks = Nokogiri::XML(test_tasks).root

story_identifiers.each_with_index do |id, i|  
  puts "Round #{i}"
  search = id =~ /^US/ ? "FormattedID='#{id}'" : "ObjectID='#{id}'"
  myStory = stories.xpath("//HierarchicalRequirement[#{search}]").first
  puts "Could not find story titled '#{id}'" && next unless myStory
  myTasks = myStory.xpath("//Task");
  myTasks.each do | shortTask | 
    ref = shortTask.attribute("ref")
    puts tasks.xpath("//Task[@ref='#{ref}']")
  end
end
