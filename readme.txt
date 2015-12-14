Assumptions:
Since there are dependent systems as mentioned in task, I'm not sure whether it is allowed
to change endpoint addresses during refactoring. So in real scenario this needs to be clarified
but here to demonstrate skills I'm going to refactor the application also 
by changing REST endpoints to make them more straightforward and according REST convention.
New endpoint addresses:
	  HEAD 	/airports - replacement for GET /collect/ping - just to check that the system alive. 
	   GET 	/airports - replacement for GET /collect/airports - returns list of airports
	   GET 	/airports/stats - replacement for GET /query/ping - returns statistical information
	  POST 	/airports - replaces  POST /collect/airport/{iata}/{lat}/{long} - adds new airport
	DELETE 	/airports/{iata} - replaces DELETE /collect/airport/{iata} - removes an airport 
	   PUT 	/airports/{iata}/weather - replaces POST /collect/weather/{iata}/{pointType} - 
	   GET 	/airports/{iata}/weather/{radius} - gets weather within given radius around specified airport
The main idea behind this is that we don't need these "collect" and "query" because
their meaning is specified by corresponding HTTP method used (GET for querying 
and PUT (not POST) for updating data). 
