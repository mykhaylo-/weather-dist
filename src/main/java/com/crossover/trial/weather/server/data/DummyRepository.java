package com.crossover.trial.weather.server.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AtmosphericInformation;

public class DummyRepository implements Repository {

	// Just a singleton for simplicity
	private static final Repository instance = new DummyRepository();

	private Map<String, Airport> airports = Collections.synchronizedMap(new HashMap<>());

	private Map<String, AtmosphericInformation> atmosphericInformation = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Internal performance counter to better understand most requested
	 * information, this map can be improved but for now provides the basis for
	 * future performance optimizations. Due to the stateless deployment
	 * architecture we don't want to write this to disk, but will pull it off
	 * using a REST request and aggregate with other performance metrics
	 * {@link #ping()}
	 */
	private Map<String, Integer> requestCounts = Collections.synchronizedMap(new HashMap<>());

	private Map<Double, Integer> radiusCounts = new HashMap<Double, Integer>();

	private DummyRepository() {
	}

	public static Repository getInstance() {
		return instance;
	}

	@Override
	public void saveAirport(Airport airport) {
		airports.put(airport.getIataCode(), airport);
	}

	@Override
	public Airport getAirport(String iataCode) {
		return airports.get(iataCode);
	}

	@Override
	public Collection<Airport> getAirports() {
		return airports.values();
	}

	@Override
	public Collection<AtmosphericInformation> getAllAtmosphericInformation() {
		return Collections.unmodifiableCollection(atmosphericInformation.values());
	}

	@Override
	public Map<String, Integer> getRequestCounts() {
		return Collections.unmodifiableMap(requestCounts);
	}

	@Override
	public Map<Double, Integer> getRadiusCounts() {
		return Collections.unmodifiableMap(radiusCounts);
	}

	@Override
	public AtmosphericInformation getAtmosphericInformationByIataCode(String iataCode) {
		return atmosphericInformation.getOrDefault(iataCode, new AtmosphericInformation.Builder().build());
	}

	@Override
	public int getRequestCount(String iataCode) {
		return requestCounts.getOrDefault(iataCode, 0);
	}

	@Override
	public void updateRequestCount(String iataCode, int count) {
		requestCounts.put(iataCode, count);
	}

	@Override
	public void updateRadiusCount(Double radius, int count) {
		radiusCounts.put(radius, count);
	}

	@Override
	public int getRadiusCount(Double radius) {
		return radiusCounts.getOrDefault(radius, 0);
	}

	@Override
	public boolean airportExists(String iataCode) {
		return airports.containsKey(iataCode);
	}

	@Override
	public void deleteAirport(String iata) {
		airports.remove(iata);
	}
}
