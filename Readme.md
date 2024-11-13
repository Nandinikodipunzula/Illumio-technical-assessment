# Technical Assessment Illumio:

## Overview

Given a flow logs file (A flow log record represents a network flow in your VPC) of 10MB and a lookup file, parse the log file and generate a count based on tag and port_protocol


## Solution:

### Initial thoughts:

1. Input size of both input files is relatively smaller hence skipped using streams, dividing the file in chunks, accessing the file randomly
2. Assuming the file content is strictly the same according to the format mentioned, preprocessing of data is skipped
3. Lookup table is a text file of comma-separated values, converting it into a hashmap provides faster lookup O(1)
4. The flow log file can be split based on whitespace character to get the required protocol,dst_port based on the index by following the order mentioned in https://docs.aws.amazon.com/vpc/latest/userguide/flow-log-records.html
5. Traversing each record in a file is done via Path and Files using Java NIO package for the following benefits:

        Bulk Operations for reading all lines
        
        Memory mapped I/O for large files
        
        Checked exceptions for robustness
        
        It also provides Stream support
        
        Note: I read all lines at once as the input size is smaller
              I did not want to introduce the extra overhead of streaming, filtering, mapping, and counting

### Execution steps:

   Check `Execution.md` file for execution instructions

### Approach:

1. As both files have text content, I read each file with a sample file handler from Java NIO
2. Populate lookup_file.txt with content from the email. Parsed and populated into a hashmap
3. Copied given logs from the email into flow_logs.txt, parsed and generated mappings for tag and protocol&portnumber
4. From the generated mappings, output is written to output.txt

### Time & Space complexity
      Time : O(m+n)
      Space: O(m+n)
      m = number of records in the lookup table
      n = number of log lines in flow_log.txt
      Space n represents the  memory used to create dst_port and protocol count along with tag count

### Testing:
1. Manually verified if the output count and mappings generated from the logs file are the same/equal
2. Populated the flow_logs.txt file for a size of 10MB and calculated response time to see how fast/slow the algorithm
3. Introduced new empty line in flow logs and checked if the program process efficiently without failing
4. Any wrong formatted (appending two lines into a single line) considers only one/first line

#### Output Screenshots: 

#### Outfile.txt
<img width="1091" alt="Screenshot 2024-11-12 at 9 28 42 PM" src="https://github.com/user-attachments/assets/3255f9f5-0532-40c9-a772-ee28d8ef1660">

#### Response time calculated after populating file with 10MB data
<img width="1144" alt="Screenshot 2024-11-12 at 9 22 47 PM" src="https://github.com/user-attachments/assets/d513e755-2a1d-4975-bb08-222078e7a128">


### Notes:
1. Junit unit tests are skipped for now as the requirement was to submit an executable program without needing to download dependencies.
2. Program processes only log files with version = 2
3. In the program files.readAllLines do not require explicit file management and hence try with resources and finally block are not used


