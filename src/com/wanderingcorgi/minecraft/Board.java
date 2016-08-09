package com.wanderingcorgi.minecraft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Board implements Serializable { 

	private static final long serialVersionUID = 1L;
	
	public String Name;
	public LocationSerializable Home; 
	public List<UUID> Members; 
	public List<UUID> Admins; 
	public List<UUID> Mods; 
	public boolean Open; 
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Admins == null) ? 0 : Admins.hashCode());
		result = prime * result + ((Home == null) ? 0 : Home.hashCode());
		result = prime * result + ((Members == null) ? 0 : Members.hashCode());
		result = prime * result + ((Mods == null) ? 0 : Mods.hashCode());
		result = prime * result + ((Name == null) ? 0 : Name.hashCode());
		result = prime * result + (Open ? 1231 : 1237);
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
		Board other = (Board) obj;
		if (Admins == null) {
			if (other.Admins != null)
				return false;
		} else if (!Admins.equals(other.Admins))
			return false;
		if (Home == null) {
			if (other.Home != null)
				return false;
		} else if (!Home.equals(other.Home))
			return false;
		if (Members == null) {
			if (other.Members != null)
				return false;
		} else if (!Members.equals(other.Members))
			return false;
		if (Mods == null) {
			if (other.Mods != null)
				return false;
		} else if (!Mods.equals(other.Mods))
			return false;
		if (Name == null) {
			if (other.Name != null)
				return false;
		} else if (!Name.equals(other.Name))
			return false;
		if (Open != other.Open)
			return false;
		return true;
	}

	public Board(String name, UUID owner){
		Name = name; 
		Home = null; 
		Members = new ArrayList<UUID>(); 
		Admins = new ArrayList<UUID>(); 
		Mods = new ArrayList<UUID>(); 
		
		Members.add(owner); 
		Admins.add(owner); 
		
		Open = false; 
	}
	
	public static Board FromName(String name){
		return Memory.Boards.get(name); 
	}
	
	public static Board AddBoard(String name, UUID owner){
		Board board = new Board(name, owner); 
		Memory.Boards.put(name, board);
		return board; 
	}
}
