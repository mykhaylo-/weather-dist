package com.crossover.trial.weather.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.crossover.trial.weather.WeatherException;
import com.crossover.trial.weather.model.Airport;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.server.data.DummyRepository;
import com.crossover.trial.weather.server.data.Repository;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport weather collection
 * sites via secure VPN.
 *
 * @author code test administrator
 */
@Path("/airports")
public class RestAirportsEndpoint implements AirportsEndpoint {
    
	public final static Logger log = Logger.getLogger(RestAirportsEndpoint.class.getName());

	public static final double EARTH_RADIUS_KM = 6372.8;
	
	private final Repository repository;
	
    public final static Gson gson = new Gson();

    public RestAirportsEndpoint() {
    	// TODO : Jersey requires noarg constructor.
    	this(DummyRepository.getInstance());
    }
    
    public RestAirportsEndpoint(Repository repository) {
    	this.repository = repository;
	}
    
    @HEAD
    @Override
    public Response ping() {
    	return Response.status(Response.Status.OK).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirports() {
    	List<String> iataCodes = repository.getAirports()
    			.stream().map(sc -> sc.getIataCode()).collect(Collectors.toList());
        return Response.status(Response.Status.OK).entity(gson.toJson(iataCodes)).build();
    }

    @GET
    @Path("/{iata}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response getAirport(@PathParam("iata") String iata) {
        Airport airport = repository.getAirport(iata);

        if(airport == null) {
        	return Response.status(Status.NOT_FOUND).build();
        } else {
        	return Response.status(Response.Status.OK).entity(gson.toJson(airport)).build();
        }
    }

    @POST
    @Override
    public Response createAirport(String airportJson) {
    	Airport airport = gson.fromJson(airportJson, Airport.class);
		if (repository.airportExists(airport.getIataCode())) {
			return Response.status(Response.Status.CONFLICT).build();
		} else {
			repository.saveAirport(airport);
			return Response.status(Response.Status.CREATED).build();
		}
    }
    
    // Implemented it for compatibility with existing systems which depend on this application
	@POST
	@Path("/{iata}/{lat}/{long}")
	@Override
	public Response addAirport(@PathParam("iata") String iata, @PathParam("lat") String latString, @PathParam("long") String longString) {
		if (repository.airportExists(iata)) {
			return Response.status(Response.Status.CONFLICT).build();
		} else {
			Airport airport = new Airport.Builder().withIataCode(iata).withLatitude(Double.parseDouble(latString))
					.withLongitude(Double.parseDouble(longString)).build();
			repository.saveAirport(airport);
			return Response.status(Response.Status.CREATED).build();
		}
	}

    @DELETE
    @Path("/{iata}")
    @Override
    public Response deleteAirport(@PathParam("iata") String iata) {
    	if (repository.airportExists(iata)) {
    		repository.deleteAirport(iata);
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
    }
    
    
	/**
	 * Given a query in json format {'iata': CODE, 'radius': km} extracts the
	 * requested airport information and return a list of matching atmosphere
	 * information.
	 *
	 * @param iata
	 *            the iataCode
	 * @param radiusString
	 *            the radius in km
	 *
	 * @return a list of atmospheric information
	 */
	@GET
	@Path("/{iata}/weather/{radius}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getWeatherAround(@PathParam("iata") String iata, @PathParam("radius") String radiusString) {
		double radius = radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);
	
		repository.updateRequestCount(iata, repository.getRequestCount(iata) + 1);
		repository.updateRadiusCount(radius, repository.getRadiusCount(radius) +1);
	
		List<AtmosphericInformation> responseEntity = new ArrayList<>();
		if (radius == 0) {
			responseEntity.add(repository.getAtmosphericInformationByIataCode(iata));
		} else {
			Airport requestedAirport = repository.getAirport(iata);
			for (Airport airport : repository.getAirports()) {
				if (calculateDistance(requestedAirport, airport) <= radius) {
					AtmosphericInformation ai = repository.getAtmosphericInformationByIataCode(airport.getIataCode());
					if (isAtmosphericInformationPopulated(ai)) {
						responseEntity.add(ai);
					}
				}
			}
		}
		
		return Response.status(Response.Status.OK).entity(gson.toJson(responseEntity)).build();
	}

	@PUT
	@Path("/{iata}/weather")
	@Override
	public Response updateWeather(@PathParam("iata") String iataCode, String datapointJson) {
		try {
			AtmosphericInformation ai = repository.getAtmosphericInformationByIataCode(iataCode);
			updateAtmosphericInformation(ai, gson.fromJson(datapointJson, DataPoint.class));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Supplied data is not valid", e);
			return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		
		return Response.status(Response.Status.OK).build();
	}

	@GET
	@Path("/stats")
	public Response stats() {
		Map<String, Object> responseEntity = new HashMap<>();

		// we only count recent readings
		// updated in the last day
		long dataSize = repository.getAllAtmosphericInformation().stream().filter(ai -> isAtmosphericInformationPopulated(ai) &&
				ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000).count();
		
		responseEntity.put("datasize", dataSize);

		Map<String, Integer> requestCounts = repository.getRequestCounts();

		int totalRequestsCount = requestCounts.values().stream().mapToInt(Number::intValue).sum();

		Map<String, Double> freq = new HashMap<>();
		// fraction of queries
		for (Airport airport : repository.getAirports()) {
			double frac = (double) requestCounts.getOrDefault(airport.getIataCode(), 0) / totalRequestsCount;
			freq.put(airport.getIataCode(), frac);
		}

		responseEntity.put("iata_freq", freq);
		
		Map<Double, Integer> radiusFreq = repository.getRadiusCounts();

		int m = radiusFreq.keySet().stream().max(Double::compare).orElse(1000.0).intValue() + 1;

		int[] hist = new int[m];
		for (Map.Entry<Double, Integer> e : radiusFreq.entrySet()) {
			int i = e.getKey().intValue() % 10;
			hist[i] += e.getValue();
		}
		responseEntity.put("radius_freq", hist);

		return Response.status(Response.Status.OK).entity(gson.toJson(responseEntity)).build();

	}

	/**
	 * update atmospheric information with the given data point for the given
	 * point type
	 *
	 * @param ai
	 *            the atmospheric information object to update
	 * @param pointType
	 *            the data point type as a string
	 * @param dp
	 *            the actual data point
	 */
	private void updateAtmosphericInformation(AtmosphericInformation ai, DataPoint dp) throws WeatherException {
		// TODO : avoid this switch
		switch (dp.getType()) {
		case WIND:
			ai.setWind(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
			break;
		case TEMPERATURE:
			ai.setTemperature(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
			break;
		case HUMIDITY:
			ai.setHumidity(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
			break;
		case PRESSURE:
			ai.setLastUpdateTime(System.currentTimeMillis());
			ai.setPressure(dp);
			break;
		case CLOUDCOVER:
			ai.setLastUpdateTime(System.currentTimeMillis());
			ai.setCloudCover(dp);
			break;
		case PRECIPITATION:
			ai.setPrecipitation(dp);
			ai.setLastUpdateTime(System.currentTimeMillis());
			break;
		default:
			throw new WeatherException("Data in request is not valid");
		}
	}

	/**
	 * Haversine distance between two airports.
	 *
	 * @param ad1
	 *            airport 1
	 * @param ad2
	 *            airport 2
	 * @return the distance in KM
	 */
	private double calculateDistance(Airport ad1, Airport ad2) {
		double deltaLat = Math.toRadians(ad2.getLatitude() - ad1.getLatitude());
		double deltaLon = Math.toRadians(ad2.getLongitude() - ad1.getLongitude());
		double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2) * Math.cos(ad1.getLatitude())
				* Math.cos(ad2.getLatitude());
		double c = 2 * Math.asin(Math.sqrt(a));
		return EARTH_RADIUS_KM * c;
	}

	private boolean isAtmosphericInformationPopulated(AtmosphericInformation ai) {
		return ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPrecipitation() != null || ai.getPressure() != null
				|| ai.getTemperature() != null || ai.getWind() != null;
	}
}
