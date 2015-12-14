package com.crossover.trial.weather.client;

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.filter.LoggingFilter;

import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;

/**
 * A reference implementation for the weather client. Consumers of the REST API
 * can look at WeatherClient to understand API semantics. This existing client
 * populates the REST endpoint with dummy data useful for testing.
 *
 * @author code test administrator
 */
public class WeatherClient {

	public static final Logger log = Logger.getLogger(WeatherClient.class.getName());

	private static final String BASE_URI = "http://localhost:8080";

	private final WebTarget airportsEndpoint;

	public WeatherClient() {
		Client client = ClientBuilder.newClient();
		client.register(new LoggingFilter(Logger.getLogger(LoggingFilter.class.getName()), true));

		airportsEndpoint = client.target(BASE_URI).path("/airports");
	}

	public void pingAirportsEndpoint() {
		airportsEndpoint.request().head();
	}

	public void populate() {
		WebTarget path = airportsEndpoint.path("/BOS/weather");
		DataPoint dp = new DataPoint.Builder(DataPointType.WIND).withFirst(0).withLast(10).withSecond(8).withMean(4).withCount(10).build();
		path.request().put(Entity.entity(dp, "application/json"));
	}

	public void queryWeather() {
		WebTarget path = airportsEndpoint.path("/BOS/weather/0");
		path.request().get();
	}
	
	public void queryAirports() {
		airportsEndpoint.request().get();
	}
	
	public void querySingleAirport() {
		airportsEndpoint.path("/JFK").request().get();
	}

	public static void main(String[] args) {
		WeatherClient wc = new WeatherClient();
		wc.pingAirportsEndpoint();
		wc.populate();
		wc.queryWeather();
		wc.queryAirports();
		wc.querySingleAirport();
	}
}
