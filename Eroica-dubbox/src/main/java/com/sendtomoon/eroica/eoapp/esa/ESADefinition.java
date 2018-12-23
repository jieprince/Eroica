package com.sendtomoon.eroica.eoapp.esa;

import java.util.Properties;

public class ESADefinition {

	public static final String WEB_MAPPING_PATH = "mappingPath";

	private Properties properties;

	private String esaName;

	public ESADefinition(String esaName) {
		this.esaName = esaName;
	}

	public ESADefinition(String esaName, Properties properties) {
		this.esaName = esaName;
		this.properties = properties;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String getEsaName() {
		return esaName;
	}

	public void setEsaName(String esaName) {
		this.esaName = esaName;
	}

	@Override
	public int hashCode() {
		return esaName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ESADefinition) {
			return esaName != null && esaName.equals(((ESADefinition) obj).getEsaName());
		}
		return false;
	}

	@Override
	public String toString() {
		return esaName;
	}

	public boolean isLocal() {
		Object isLocal = (properties == null ? null : properties.get("local"));
		return properties != null && isLocal != null && isLocal.toString().equalsIgnoreCase("true");
	}

}
