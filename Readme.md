Technical Assessment Illumio:

Overview

Given flow logs file (A flow log record represents a network flow in your VPC) of 10MB and a lookup file, parse log file and generate count based on tag and port_protocol

Solution:

Initial thoughts:

After checking input size of both input files, I came to a conclusion that there is no need to use streams, dividing the file in chunks, accessing the file randomly
Since files are relatively smaller and assuming the file content is strictly same according to the format mentioned, preprocessing of data is skipped

Lookup table is in the form of file , converting that to a hashmap provides faster lookup O(1)

Flow log file can be split based on space and get the required protocol,dst_port based on the index by following the order mentioned in https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html

Traversing each record in a file is done via Path and Files using Java NIO package for the following benefits:

1.Bulk Operations for reading all lines
2.Memory mapped I/O for large files
3.Checked exceptions for robustness
4.It also provides Stream support. Note: I read all lines at once as input size is smaller and I did not want to introduce extra overhead of streamin,filtering,mapping and counting

Approach:

As both files has text content, I read each file with a sample file handler from Java NIO
Populated lookup_file.txt with content from the email. Parsed and populated into a hashmap
Copied given logs into flow_logs.txt,parsed and generated mapping for tag and protocol&portnumber
From the generated mappings, output is written to output.txt

Testing:
Manually verified if the output count and mappings generated from the logs file are same/equal
Populated the flow_logs.txt file for a size of 10MB and calculated response time to see how fast/slow the algorithm.
Introduced new empty line in flow logs and checked if program process efficiently without failing
Any wrong formatted (appending two lines into single line) considers only one/first line

Notes:
I thought of writing Junit unit tests but the requirement was to submit a executable program without needing to download dependencies.Hence I used very basic programming to accomplish this task
Program processes only log files with version = 2
In the program files.readAllLines do not reuire explicit file manangement and hence try with resources and finally block are not used

Screenshots:
