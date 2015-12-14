package com.crossover.trial.weather.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Airport {

	private String iataCode = "";

	private String city;

	private String country;

	private String icaoCode = "";

	private double latitude;

	private double longitude;

	private double altitude;

	// So far we don't use this field so don't need any conversion to be done. 
	// Just String for now
	private String utcOffset;

	private DST dst;

	private Airport(Builder builder) {
		this.iataCode = builder.iataCode;
		this.icaoCode = builder.icaoCode;
		this.country = builder.country;
		this.city = builder.city;
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;
		this.altitude = builder.altitude;
		this.utcOffset = builder.utcOffset;
		this.dst = builder.dst;
	}

	public String getIataCode() {
		return iataCode;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getIcaoCode() {
		return icaoCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public String getUtcOffset() {
		return utcOffset;
	}

	public DST getDst() {
		return dst;
	}

	public static class Builder {

		private String iataCode = "";
		private String city;
		private String country;
		private String icaoCode = "";
		private double latitude;
		private double longitude;
		private double altitude;
		private String utcOffset = "0";
		private DST dst = DST.U;

		public Builder() {
		}

		public Builder withIataCode(String iataCode) {
			this.iataCode = iataCode;
			return this;
		}

		public Builder withCity(String city) {
			this.city = city;
			return this;
		}

		public Builder withCountry(String country) {
			this.country = country;
			return this;
		}

		public Builder withIcaoCode(String icaoCode) {
			this.icaoCode = icaoCode;
			return this;
		}

		public Builder withLatitude(double latitude) {
			this.latitude = latitude;
			return this;
		}

		public Builder withLongitude(double longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder withAltitude(double altitude) {
			this.altitude = altitude;
			return this;
		}

		public Builder withUtcOffset(String utcOffset) {
			this.utcOffset = utcOffset;
			return this;
		}

		public Builder withDst(DST dst) {
			this.dst = dst;
			return this;
		}

		public Airport build() {
			return new Airport(this);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
	}
}
