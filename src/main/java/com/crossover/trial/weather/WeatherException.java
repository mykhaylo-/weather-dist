package com.crossover.trial.weather;

/**
 * An internal exception marker
 */
public class WeatherException extends Exception {
	
	private static final long serialVersionUID = -3915153143490977128L;

	public WeatherException(String msg) {
		super(msg);
	}
}
