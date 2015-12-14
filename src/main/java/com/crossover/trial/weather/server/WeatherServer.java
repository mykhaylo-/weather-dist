package com.crossover.trial.weather.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.crossover.trial.weather.client.WeatherClient;
import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.server.data.DummyRepository;
import com.crossover.trial.weather.server.data.Repository;
import com.crossover.trial.weather.ws.RestAirportsEndpoint;

/**
 * A main method used to test the Weather Application locally -- live deployment
 * is to a tomcat container.
 *
 */
public class WeatherServer {

	// TODO : configure logger formatting
	public static final Logger log = Logger.getLogger(WeatherClient.class.getName());

	private static final String BASE_URL = "http://localhost:8080/";

	private static final Repository repository = DummyRepository.getInstance();

	public static void main(String[] args) throws Exception {
		log.info("Starting Weather App local testing server: " + BASE_URL);
		log.info("Not for production use");

		init();

		final ResourceConfig resourceConfig = new ResourceConfig(RestAirportsEndpoint.class, LoggingFilter.class);
	
		final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
				URI.create(BASE_URL), resourceConfig, false);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				server.shutdownNow();
			}
		}));

		server.start();

		log.info("Weather Server started at " + BASE_URL);
		// Thread.currentThread().join();
	}

	/**
	 * A dummy init method that loads hard coded data
	 */
	private static void init() {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("airports.dat");

		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
			buffer.lines().forEach(line -> repository.saveAirport(buildAirportFromCSV(line)));
		} catch(IOException e) {
			log.log(Level.WARNING, "Failed to load dummy data", e);
		}
	}

	private static Airport buildAirportFromCSV(String line) {
		String[] parts = line.split(",");
		return new Airport.Builder().withIataCode(parts[0])
				.withLatitude(Double.parseDouble(parts[1]))
				.withLongitude(Double.parseDouble(parts[2]))
				.build();
	}
}
