package com.wanderingcorgi.minecraft;

import java.io.Serializable;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum Rank {
		Normie, Mod, Admin
	}
	
	public LocationSerializable Home; 
	public UUID Id; 
	public String BoardName; 
	public Rank BoardRank; 
	
	public User(UUID id){
		this.Id = id;
		BoardName = null; 
		BoardRank = Rank.Normie; 
		Home = null; 
	}
	
	public Player getPlayer(){
		return Bukkit.getPlayer(Id); 
	}
	
	public boolean hasHome(){
		return (Home != null); 
	}
	
	public boolean HasBoard(){
		return (BoardName != null); 
	}
	
	public static User FromUUID(UUID id){
		return Memory.Users.get(id); 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((BoardName == null) ? 0 : BoardName.hashCode());
		result = prime * result + ((BoardRank == null) ? 0 : BoardRank.hashCode());
		result = prime * result + ((Home == null) ? 0 : Home.hashCode());
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
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
		User other = (User) obj;
		if (BoardName == null) {
			if (other.BoardName != null)
				return false;
		} else if (!BoardName.equals(other.BoardName))
			return false;
		if (BoardRank != other.BoardRank)
			return false;
		if (Home == null) {
			if (other.Home != null)
				return false;
		} else if (!Home.equals(other.Home))
			return false;
		if (Id == null) {
			if (other.Id != null)
				return false;
		} else if (!Id.equals(other.Id))
			return false;
		return true;
	}
}

