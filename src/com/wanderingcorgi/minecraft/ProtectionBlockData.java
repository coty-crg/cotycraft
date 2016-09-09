package com.wanderingcorgi.minecraft;

import java.io.Serializable;

public class ProtectionBlockData implements Serializable {
	
	public String BoardName; 
	public LocationSerializable Location;
	public Integer ProtectionLevel = 0; 
	
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
		result = prime * result + ((ProtectionLevel == null) ? 0 : ProtectionLevel.hashCode());
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
		if (ProtectionLevel == null) {
			if (other.ProtectionLevel != null)
				return false;
		} else if (!ProtectionLevel.equals(other.ProtectionLevel))
			return false;
		return true;
	}
}