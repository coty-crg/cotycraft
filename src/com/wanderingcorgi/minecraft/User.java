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
	
	public enum Chat {
		Global, Board, Ally, Truce, Enemy, Local
	}
	
	public enum Relation {
		Neutral, 
		Faction,
		Ally, 
		Enemy,
		Truce
	}
	
	public LocationSerializable Home; 
	public UUID Id; 
	public String BoardName; 
	public Rank BoardRank; 
	public Chat ChatMode; 
	public boolean ReinforceMode = false; 
	public long LastTeleportMS = 0L; 
	public long LastDurabilityDecreasedMS = 0L; 
	
	public boolean TextFormatting = false; 
	public boolean DropPlayerHeads = false; 
	public boolean LightningOnSpawn = false; 
	
	public User(UUID id){
		this.Id = id;
		ChatMode = Chat.Global; 
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

	public Relation GetRelation(User otherUser){
		
		if(BoardName == null || BoardName.equals("") || otherUser.BoardName == null || otherUser.BoardName.equals(""))
			return Relation.Neutral;
		
		if(BoardName.equals(otherUser.BoardName))
			return Relation.Faction; 
		
		Board ourBoard = Board.FromName(BoardName); 
		Board otherBoard = Board.FromName(otherUser.BoardName); 

		if(ourBoard.Allies.contains(otherUser.BoardName) && otherBoard.Allies.contains(ourBoard.Name))
			return Relation.Ally; 
		
		if(ourBoard.Truce.contains(otherUser.BoardName) && otherBoard.Truce.contains(ourBoard.Name))
			return Relation.Truce; 
		
		if(ourBoard.Enemies.contains(otherUser.BoardName) || otherBoard.Enemies.contains(ourBoard.Name))
			return Relation.Enemy; 
		
		return Relation.Neutral; 
	}
	
	public Relation GetRelation(String otherBoardName){
		
		if(BoardName == null || BoardName.equals(""))
			return Relation.Neutral;
		
		if(BoardName.equals(otherBoardName))
			return Relation.Faction; 
		
		Board ourBoard = Board.FromName(BoardName); 
		Board otherBoard = Board.FromName(otherBoardName); 
		
		if(ourBoard.Allies.contains(otherBoardName) && otherBoard.Allies.contains(ourBoard.Name))
			return Relation.Ally; 
		
		if(ourBoard.Truce.contains(otherBoardName) && otherBoard.Truce.contains(ourBoard.Name))
			return Relation.Truce; 
		
		if(ourBoard.Enemies.contains(otherBoardName) || otherBoard.Enemies.contains(ourBoard.Name))
			return Relation.Enemy; 
		
		return Relation.Neutral; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((BoardName == null) ? 0 : BoardName.hashCode());
		result = prime * result + ((BoardRank == null) ? 0 : BoardRank.hashCode());
		result = prime * result + ((ChatMode == null) ? 0 : ChatMode.hashCode());
		result = prime * result + (DropPlayerHeads ? 1231 : 1237);
		result = prime * result + ((Home == null) ? 0 : Home.hashCode());
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		result = prime * result + (int) (LastTeleportMS ^ (LastTeleportMS >>> 32));
		result = prime * result + (LightningOnSpawn ? 1231 : 1237);
		result = prime * result + (ReinforceMode ? 1231 : 1237);
		result = prime * result + (TextFormatting ? 1231 : 1237);
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
		if (ChatMode != other.ChatMode)
			return false;
		if (DropPlayerHeads != other.DropPlayerHeads)
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
		if (LastTeleportMS != other.LastTeleportMS)
			return false;
		if (LightningOnSpawn != other.LightningOnSpawn)
			return false;
		if (ReinforceMode != other.ReinforceMode)
			return false;
		if (TextFormatting != other.TextFormatting)
			return false;
		return true;
	}
	

}

