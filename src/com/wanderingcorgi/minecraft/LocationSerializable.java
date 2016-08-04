package com.wanderingcorgi.minecraft;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class LocationSerializable implements Serializable  {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((WorldId == null) ? 0 : WorldId.hashCode());
		long temp;
		temp = Double.doubleToLongBits(X);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(Y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(Z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		LocationSerializable other = (LocationSerializable) obj;
		if (WorldId == null) {
			if (other.WorldId != null)
				return false;
		} else if (!WorldId.equals(other.WorldId))
			return false;
		if (Double.doubleToLongBits(X) != Double.doubleToLongBits(other.X))
			return false;
		if (Double.doubleToLongBits(Y) != Double.doubleToLongBits(other.Y))
			return false;
		if (Double.doubleToLongBits(Z) != Double.doubleToLongBits(other.Z))
			return false;
		return true;
	}

	private static final long serialVersionUID = -5307006774464330281L;
	
	public UUID WorldId; 
	public double X, Y, Z; 
	
	public LocationSerializable(World world, double x, double y, double z){
		WorldId = world.getUID();  
		X = x; 
		Y = y; 
		Z = z; 
	}
	
	public LocationSerializable(Location location){
		WorldId = location.getWorld().getUID(); 
		X = location.getX();  
		Y = location.getY();  
		Z = location.getZ(); 
	}
	
	public LocationSerializable(Block block){
		Location location = block.getLocation(); 
		WorldId = location.getWorld().getUID(); 
		X = location.getX();  
		Y = location.getY();  
		Z = location.getZ(); 
	}
}