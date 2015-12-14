package com.crossover.trial.weather.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.filter.LoggingFilter;

import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.DST;
import com.google.gson.Gson;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 *
 * TODO: Implement the Airport Loader
 * 
 * @author code test administrator
 */

public class AirportLoader {

	private static final String BASE_URL = "http://localhost:8080";

	public static final Logger log = Logger.getLogger(WeatherClient.class.getName());

    private WebTarget airportsEndpoint;

	private static final Gson gson = new Gson();

    public AirportLoader() {
        Client client = ClientBuilder.newClient();
		client.register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()), true));

        airportsEndpoint = client.target(BASE_URL + "/airports");
    }

    // CR: This method needs to be implemented
    public void upload(InputStream inputStream) throws IOException{
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
			 List<Airport> airports = buffer.lines().map(line -> buildAirportFromCSV(line))
					 .collect(Collectors.toList());
			 
			 airports.stream().filter(Objects::nonNull)
			 	.forEach(airport -> createAirport(airport));
		}
    }

    private void createAirport(Airport airport) {
		airportsEndpoint.request().post(Entity.<String>entity(gson.toJson(airport), MediaType.APPLICATION_JSON));
	}

	public static void main(String args[]) throws IOException{
    	
    	InputStream inputStream = AirportLoader.class.getResourceAsStream("/airports.txt");
    	
		AirportLoader loader = new AirportLoader();
		loader.upload(inputStream);
    }
    
	private static Airport buildAirportFromCSV(String line) {
		Airport airport = null;
		
		String[] parts = line.split(",");
		
		if(parts.length != 11) {
			log.warning("Skipped. This line doesn't follow required format : " + line);
		} else {
			List<String> airportProperties = Arrays.asList(parts).stream().map(item ->stripLeadingAndTrailingQuote(item)).collect(Collectors.toList());

			airport = new Airport.Builder()
				.withCity(airportProperties.get(2))
				.withCountry(airportProperties.get(3))
				.withIataCode(airportProperties.get(4))
				.withIcaoCode(airportProperties.get(5))
				.withLatitude(Double.parseDouble(airportProperties.get(6)))
				.withLongitude(Double.parseDouble(airportProperties.get(7)))
				.withAltitude(Double.parseDouble(airportProperties.get(8)))
				.withUtcOffset(airportProperties.get(9))
				.withDst(DST.valueOf(airportProperties.get(10)))
				.build();
		}
		return airport;
	}

	private static String stripLeadingAndTrailingQuote(String line) {
		String result = line;
		if (result.startsWith("\"") ) {
			result = result.substring(1, result.length());
		}
		if (result.endsWith("\"") ) {
			result = result.substring(0, result.length()-1);
		}
		return result;
	}
}
