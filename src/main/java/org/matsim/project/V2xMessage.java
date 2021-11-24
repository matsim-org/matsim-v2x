package org.matsim.project;

import java.util.Objects;

class V2xMessage{
	private final String message;
	V2xMessage( String message ) {
		this.message = message;
	}
	public String getMessage(){
		return message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		V2xMessage that = (V2xMessage) o;
		return Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message);
	}
}
