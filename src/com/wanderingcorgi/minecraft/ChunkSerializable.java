package com.wanderingcorgi.minecraft;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class ChunkSerializable implements Serializable {
	
	private static final long serialVersionUID = -4300163907888087907L;

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
		ChunkSerializable other = (ChunkSerializable) obj;
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

	
	public UUID WorldId; 
	public int X, Y, Z; 
	
	public static int ChunkSize = 32; 
	
	public ChunkSerializable(World world, double x, double y, double z){
		WorldId = world.getUID();  
		Location location = new Location(world, x, y, z);
		Chunk chunk = location.getChunk(); 
		X = chunk.getX(); 
		Y = (int) Math.round(y / ChunkSize) * ChunkSize;
		Z = chunk.getZ(); 
	}
	
	public ChunkSerializable(Location location){
		WorldId = location.getWorld().getUID(); 
		Chunk chunk = location.getChunk(); 
		X = chunk.getX(); 
		Y = (int) Math.round(location.getY() / ChunkSize) * ChunkSize;
		Z = chunk.getZ(); 
	}
	
	public ChunkSerializable(Block block){
		Location location = block.getLocation(); 
		WorldId = location.getWorld().getUID(); 
		Chunk chunk = location.getChunk(); 
		X = chunk.getX(); 
		Y = (int) Math.round(location.getY() / ChunkSize) * ChunkSize;
		Z = chunk.getZ(); 
	}
}
