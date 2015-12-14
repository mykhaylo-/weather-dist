package com.crossover.trial.weather.ws;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.crossover.trial.weather.model.Airport;

/**
 * Airports web-service endpoint.
 */
public interface AirportsEndpoint {

	/**
	 * A liveliness check for the collection endpoint.
	 * 
	 * @return Response with 200 OK if alive.
	 */
	Response ping();

	/**
	 * Returns a list of known airports 
	 *
	 * @return HTTP Response 
	 */
	Response getAirports();

	/**
	 * Retrieve airport data, including latitude and longitude for a particular
	 * airport
	 *
	 * @param iata
	 *            the 3 letter airport code
	 * @return an HTTP Response with {@link Airport}
	 */
	Response getAirport(@PathParam("iata") String iata);

	/**
	 * Add a new airport to the known airport list.
	 *
	 * @param iata
	 *            the 3 letter airport code of the new airport
	 * @param latString
	 *            the airport's latitude in degrees as a string [-90, 90]
	 * @param longString
	 *            the airport's longitude in degrees as a string [-180, 180]
	 * @return HTTP Response code for the add operation
	 * @deprecated  use {@link #createAirport(String)} instead.  
	 */
	@Deprecated
	Response addAirport(@PathParam("iata") String iata, @PathParam("lat") String latString, @PathParam("long") String longString);

	/**
	 * Add a new airport to the known airport list.
	 *
	 * @param airport
	 *            JSON representation of {@link Airport}
	 * @return HTTP 201 Created if Airport was added and HTTP 409 Conflict if Airport with specified iataCode already exists in system
	 */
	Response createAirport(String airport);
	
	/**
	 * Remove an airport from the known airport list
	 *
	 * @param iata
	 *            the 3 letter airport code
	 * @return HTTP Repsonse code for the delete operation
	 */
	Response deleteAirport(@PathParam("iata") String iata);
	
	/**
	 * Retrieve health and status information for the the query api. Returns
	 * information about how the number of datapoints currently held in memory,
	 * the frequency of requests for each IATA code and the frequency of
	 * requests for each radius.
	 *
	 * @return a Response with health information.
	 */
	Response stats();
	
	/**
	 * Retrieve the most up to date atmospheric information from the given
	 * airport and other airports in the given radius.
	 *
	 * @param iata
	 *            the three letter airport code
	 * @param radiusString
	 *            the radius, in km, from which to collect weather data
	 *
	 * @return an HTTP Response and a list of {@link AtmosphericInformation}
	 *         from the requested airport and airports in the given radius
	 */
	Response getWeatherAround(String iata, String radiusString);
	
	/**
	 * Update the airports atmospheric information for a particular pointType
	 * with json formatted data point information.
	 *
	 * @param iataCode
	 *            the 3 letter airport code
	 * @param pointType
	 *            the point type, {@link DataPointType} for a complete list
	 * @param datapointJson
	 *            a json dict containing mean, first, second, thrid and count
	 *            keys
	 *
	 * @return HTTP Response code
	 */
	Response updateWeather(String iataCode, String dataPoint);
}
