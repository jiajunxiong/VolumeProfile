###Requirement
####1. Definitions

Assume a volume profile for a given symbol that has following 4 attributes.

Start Time
End Time
Percent of Day’s Volume in the Bucket
Bucket Type (Open Auction, Close Auction, Continuous Trading etc.)
Using the above definition, create a file that specifies a well-formed (reasonable) volume profile for a symbol.

 

####2. Problem

 

Write a production quality Java program to do the following :

Load the volume profile from the aforementioned file. This piece of code should validate the curve being loaded for quality of data. In the code comments please provide some commentary on what are your thoughts on some of the validations that should be added.
Provide a function that can return cumulative volume profile elapsed between two time points.
Provide a function that, given a time, start time, end time provide normalized target percent for that time.
