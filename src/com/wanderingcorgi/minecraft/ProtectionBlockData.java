package com.wanderingcorgi.minecraft;

import java.io.Serializable;

public class ProtectionBlockData implements Serializable {
	public String BoardName; 
	public LocationSerializable Location; 
	
	public ProtectionBlockData(String boardName, LocationSerializable location){
		this.BoardName = boardName; 
		this.Location = location; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((BoardName == null) ? 0 : BoardName.hashCode());
		result = prime * result + ((Location == null) ? 0 : Location.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProtectionBlockData other = (ProtectionBlockData) obj;
		if (BoardName == null) {
			if (other.BoardName != null)
				return false;
		} else if (!BoardName.equals(other.BoardName))
			return false;
		if (Location == null) {
			if (other.Location != null)
				return false;
		} else if (!Location.equals(other.Location))
			return false;
		return true;
	}
}