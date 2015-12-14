package com.crossover.trial.weather.server.data;

import java.util.Collection;
import java.util.Map;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AtmosphericInformation;

public interface Repository {

	void saveAirport(Airport airport);
	
	Airport getAirport(String iataCode);
	
	boolean airportExists(String iataCode);
	
	void deleteAirport(String iata);

	Collection<Airport> getAirports();

	Collection<AtmosphericInformation> getAllAtmosphericInformation();

	Map<String, Integer> getRequestCounts();

	Map<Double, Integer> getRadiusCounts();

	AtmosphericInformation getAtmosphericInformationByIataCode(String iataCode);

	int getRequestCount(String iataCode);

	void updateRequestCount(String iataCode, int count);

	void updateRadiusCount(Double radius, int count);

	int getRadiusCount(Double radius);
	
}
