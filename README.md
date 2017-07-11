# x-assoc

## This project is Objectivity/Thingspan specific.

Evaluating various types of association data structure and their performance impact during ingest and iteration.

Using Thingspan 15.4.1

To run the test:
* create the FD by executing "recreatefd.sh" in the data directory
* in the main project directoy execute the following tasks.
  * gradle setupObjy (only run this once after creating a new fd)
  * gradle testXassoc

 
