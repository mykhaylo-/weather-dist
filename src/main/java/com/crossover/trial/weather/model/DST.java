package com.crossover.trial.weather.model;

public enum DST {
	E("Europe"), A("US/Canada"), S("South America"), 
	O("Australia"), Z("New Zeland"), N("None"), U("Unknown");

	private String dstRegion;

	private DST(String dstRegion) {
		this.dstRegion = dstRegion;
	}

	public String getDstRegion() {
		return dstRegion;
	}
}
