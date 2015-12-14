package com.crossover.trial.weather.model;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * encapsulates sensor information for a particular location
 */
public class AtmosphericInformation {

	/** temperature in degrees celsius */
	private DataPoint temperature;

	/** wind speed in km/h */
	private DataPoint wind;

	/** humidity in percent */
	private DataPoint humidity;

	/** precipitation in cm */
	private DataPoint precipitation;

	/** pressure in mmHg */
	private DataPoint pressure;

	/** cloud cover percent from 0 - 100 (integer) */
	private DataPoint cloudCover;

	/** the last time this data was updated, in milliseconds since UTC epoch */
	private long lastUpdateTime;

	private AtmosphericInformation(Builder builder) {
		builder.temperature.ifPresent(value -> setTemperature(value));
		builder.wind.ifPresent(value -> setWind(value));
		builder.humidity.ifPresent(value -> setHumidity(value));
		builder.precipitation.ifPresent(value -> setPrecipitation(value));
		builder.pressure.ifPresent(value -> setPressure(value));
		builder.cloudCover.ifPresent(value -> setCloudCover(value));
	}

	public DataPoint getTemperature() {
		return temperature;
	}

	public void setTemperature(DataPoint temperature) {
		if (temperature.getMean() < DataPointType.TEMPERATURE.getMin() || temperature.getMean() > DataPointType.TEMPERATURE.getMax()) {
			throw new IllegalArgumentException("Temperature is out of allowed range");
		}
		this.temperature = temperature;
	}

	public DataPoint getWind() {
		return wind;
	}

	public void setWind(DataPoint wind) {
		if (wind.getMean() < DataPointType.WIND.getMin() || wind.getMean() > DataPointType.WIND.getMax()) {
			throw new IllegalArgumentException("Wind is out of allowed range");
		}
		this.wind = wind;
	}

	public DataPoint getHumidity() {
		return humidity;
	}

	public void setHumidity(DataPoint humidity) {
		if (humidity.getMean() < DataPointType.HUMIDITY.getMin() || humidity.getMean() > DataPointType.HUMIDITY.getMax()) {
			throw new IllegalArgumentException("Humidity is out of allowed range");
		}
		this.humidity = humidity;
	}

	public DataPoint getPrecipitation() {
		return precipitation;
	}

	public void setPrecipitation(DataPoint precipitation) {
		if (precipitation.getMean() < DataPointType.PRECIPITATION.getMin() || precipitation.getMean() > DataPointType.PRECIPITATION.getMax()) {
			throw new IllegalArgumentException("Precipitation is out of allowed range");
		}
		this.precipitation = precipitation;
	}

	public DataPoint getPressure() {
		return pressure;
	}

	public void setPressure(DataPoint pressure) {
		if (pressure.getMean() < DataPointType.PRESSURE.getMin() || pressure.getMean() > DataPointType.PRESSURE.getMax()) {
			throw new IllegalArgumentException("Pressure is out of allowed range");
		}
		this.pressure = pressure;
	}

	public DataPoint getCloudCover() {
		return cloudCover;
	}

	public void setCloudCover(DataPoint cloudCover) {
		if (cloudCover.getMean() < DataPointType.CLOUDCOVER.getMin() || cloudCover.getMean() > DataPointType.CLOUDCOVER.getMax()) {
			throw new IllegalArgumentException("Cloudcover is out of allowed range");
		}
		this.cloudCover = cloudCover;
	}

	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	@Override
	public boolean equals(Object that) {
		return EqualsBuilder.reflectionEquals(this, that, false);
	}

	public static class Builder {

		private Optional<DataPoint> temperature = Optional.empty();
		private Optional<DataPoint> wind = Optional.empty();
		private Optional<DataPoint> humidity = Optional.empty();
		private Optional<DataPoint> precipitation = Optional.empty();
		private Optional<DataPoint> pressure = Optional.empty();
		private Optional<DataPoint> cloudCover = Optional.empty();

		public Builder withTemperature(DataPoint temperature) {
			this.temperature = Optional.of(temperature);
			return this;
		}

		public Builder withWind(DataPoint wind) {
			this.wind = Optional.of(wind);
			return this;
		}

		public Builder withHumidity(DataPoint humidity) {
			this.humidity = Optional.of(humidity);
			return this;
		}

		public Builder withPrecipitation(DataPoint precipitation) {
			this.precipitation = Optional.of(precipitation);
			return this;
		}

		public Builder withPressure(DataPoint pressure) {
			this.pressure = Optional.of(pressure);
			return this;
		}

		public Builder withCloudCover(DataPoint cloudCover) {
			this.cloudCover = Optional.of(cloudCover);
			return this;
		}

		public AtmosphericInformation build() {
			return new AtmosphericInformation(this);
		}
	}
}
