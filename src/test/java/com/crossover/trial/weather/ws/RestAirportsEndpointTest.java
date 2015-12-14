package com.crossover.trial.weather.ws;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.server.data.Repository;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class RestAirportsEndpointTest {

	@Mock
	private Repository repository;

	@InjectMocks
	private RestAirportsEndpoint unit;

	private static final Gson gson = new Gson();

	@Test
	public void testPing() throws Exception {
		// When
		Response response = unit.ping();

		// Then
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	@Test
	public void testGetExistingAirport() {
		// Given
		Airport airport = new Airport.Builder().withCity("NY").build();
		when(repository.getAirport(anyString())).thenReturn(airport);

		// When
		Response response = unit.getAirport("DDD");

		// Then
		verify(repository).getAirport("DDD");
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		assertEquals(gson.toJson(airport), response.getEntity());
	}

	@Test
	public void testGetNonExistingAirport() {
		// Given
		when(repository.getAirport(anyString())).thenReturn(null);

		// When
		Response response = unit.getAirport("DDD");

		// Then
		verify(repository).getAirport("DDD");
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	public void testAddExistingAirport() {
		// Given
		when(repository.airportExists(anyString())).thenReturn(true);

		// When
		Response response = unit.addAirport("DDD", "1231.123", "-123.33");

		// Then
		verify(repository).airportExists("DDD");
		verify(repository, times(0)).saveAirport(anyObject());
		assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
	}

	@Test
	public void testAddNonExistingAirport() {
		// Given
		when(repository.airportExists(anyString())).thenReturn(false);

		// When
		Response response = unit.addAirport("DDD", "-62.13", "12.12312");

		// Then
		verify(repository).airportExists("DDD");
		
		Airport airportToBeSaved = new Airport.Builder()
				.withIataCode("DDD")
				.withLatitude(-62.13)
				.withLongitude(12.12312)
			.build();
		
		verify(repository, times(1)).saveAirport(airportToBeSaved);
		
		assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
	}
	
	@Test
	public void testGetAirports() throws Exception {
		// Given
		Airport airport1 = new Airport.Builder().withIataCode("JFK").build();
		Airport airport2 = new Airport.Builder().withIataCode("LHR").build();

		Collection<Airport> airports = Arrays.asList(airport1, airport2);

		when(repository.getAirports()).thenReturn(airports);

		// When
		Response response = unit.getAirports();

		// Then
		verify(repository).getAirports();
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
		Object expectedResponse = gson.toJson(Arrays.asList("JFK", "LHR"));
		assertEquals(expectedResponse, response.getEntity());
	}

	@Test
	public void testGetWeatherAroundWithNonZeroRadius() throws Exception {
		// Given

		Airport requestedAirport = new Airport.Builder().withIataCode("JFK").withLatitude(40.64).withLongitude(-71).build();
		Airport airport1InRadius = new Airport.Builder().withIataCode("MNL").withLatitude(40.65).withLongitude(-71.1).build();
		Airport airport2InRadius = new Airport.Builder().withIataCode("LHR").withLatitude(40.68).withLongitude(-70.9).build();

		Airport airport3OutsideRadius = new Airport.Builder().withIataCode("AMS").withLatitude(50.64).withLongitude(-70.9).build();

		Collection<Airport> airports = Arrays.asList(requestedAirport, airport1InRadius, airport2InRadius, airport3OutsideRadius);

		int currentRadiusCount = 6;
		int currentRequestCount = 12;

		when(repository.getRadiusCount(anyDouble())).thenReturn(currentRadiusCount);
		when(repository.getRequestCount(anyString())).thenReturn(currentRequestCount);
		when(repository.getAirport(anyString())).thenReturn(requestedAirport);
		when(repository.getAirports()).thenReturn(airports);

		AtmosphericInformation requestedAirportData = new AtmosphericInformation.Builder().withWind(
				new DataPoint.Builder(DataPointType.WIND).withMean(5).build()).build();
		AtmosphericInformation airport1Data = new AtmosphericInformation.Builder().withHumidity(
				new DataPoint.Builder(DataPointType.HUMIDITY).withMean(5).build()).build();
		AtmosphericInformation airport2Data = new AtmosphericInformation.Builder().withPrecipitation(
				new DataPoint.Builder(DataPointType.PRECIPITATION).withMean(5).build()).build();

		when(repository.getAtmosphericInformationByIataCode("JFK")).thenReturn(requestedAirportData);
		when(repository.getAtmosphericInformationByIataCode("MNL")).thenReturn(airport1Data);
		when(repository.getAtmosphericInformationByIataCode("LHR")).thenReturn(airport2Data);

		// When
		Response response = unit.getWeatherAround("JFK", "13.4");

		// Then
		verify(repository).getRadiusCount(13.4d);
		verify(repository).getRequestCount("JFK");
		verify(repository).updateRadiusCount(13.4d, currentRadiusCount + 1);
		verify(repository).updateRequestCount("JFK", currentRequestCount + 1);

		verify(repository).getAirport("JFK");

		verify(repository).getAirports();
		verify(repository).getAtmosphericInformationByIataCode("JFK");
		verify(repository).getAtmosphericInformationByIataCode("MNL");
		verify(repository).getAtmosphericInformationByIataCode("LHR");
		verify(repository, times(0)).getAtmosphericInformationByIataCode("AMS");

		assertEquals(Status.OK.getStatusCode(), response.getStatus());

		Object expectedResponse = gson.toJson(Arrays.asList(requestedAirportData, airport1Data, airport2Data));
		assertEquals(expectedResponse.toString(), response.getEntity().toString());
	}

	@Test
	public void testGetWeatherWithFilteringNotPopulatedAtmosphericInformation() throws Exception {
		// TODO : write test for filtering not populated AtmosphericInformation. Hope to do this in part 3
	}
	
	@Test
	public void testGetWeatherAroundWithZeroRadius() throws Exception {
		// TODO : write test with zero radius. Hope to do this in part 3
	}

	@Test
	public void testUpdateWeather() throws Exception {
		// Given
		DataPoint dataPoint = new DataPoint.Builder(DataPointType.PRESSURE).withFirst(4).withMean(720).build();
		String postData = gson.toJson(dataPoint);

		AtmosphericInformation atmosphericInformation = new AtmosphericInformation.Builder().build();
		
		when(repository.getAtmosphericInformationByIataCode(anyString())).thenReturn(atmosphericInformation);
		
		// When
		unit.updateWeather("JFK", postData);

		// Tnen
		verify(repository).getAtmosphericInformationByIataCode("JFK");
	}
	
	@Test
	public void testStats() throws Exception {
		// TODO : write test for stats. Hope to do this in part 3
		//		unit.stats();
	}
	
	
	@Test
	public void testDeleteExistingAirport() {
		// Given
		when(repository.airportExists(anyString())).thenReturn(true);

		// When
		Response response = unit.deleteAirport("DDD");

		// Then
		verify(repository).airportExists("DDD");
		verify(repository).deleteAirport("DDD");
		assertEquals(Status.OK.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDeleteNonExistingAirport() {
		// Given
		when(repository.airportExists(anyString())).thenReturn(false);

		// When
		Response response = unit.deleteAirport("DDD");

		// Then
		verify(repository).airportExists("DDD");
		verify(repository, times(0)).deleteAirport("DDD");
		
		assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}
}
