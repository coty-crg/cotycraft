package com.wanderingcorgi.minecraft;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.wanderingcorgi.minecraft.User.Chat;
import com.wanderingcorgi.minecraft.User.Rank;
import com.wanderingcorgi.minecraft.User.Relation;


public class PlayerListener implements Listener {

	private final Main plugin;
	
	public PlayerListener(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/*@EventHandler
	public void OnBukkitEvent(PlayerBucketEvent event){
		Block block = event.getBlockClicked();
		ChunkSerializable thisChunk = new ChunkSerializable(block.getLocation()); 
		if(Memory.ProtectorBlocks.containsKey(thisChunk)){
			Date date =  Memory.ProtectorBlocks.get(thisChunk); 
			event.setCancelled(true);
			
			Player player = event.getPlayer();
			if(player != null)
				player.sendMessage(String.format("Cannot place blocks in this chunk until protector is destroyed! This chunk has been protected since: %s", date.toString()));
			
			return; 
		}
	}*/
	
	@EventHandler
	public void OnPlayerTeleportEvent(PlayerTeleportEvent event){
		boolean IsEnderpearlEvent = event.getCause() == TeleportCause.ENDER_PEARL; 
		
		if(!IsEnderpearlEvent)
			return; 
		
		Location starting = event.getFrom();
		Location ending = event.getTo(); 
		double distance = starting.distance(ending);
		
		if(distance < 5d){
			Player player = event.getPlayer();
			player.sendMessage(String.format("Cannot use enderpearl in small ranges. Blame /tg/ and their hacked clients."));
			event.setCancelled(true);
			return; 
		}
	}
	
	@EventHandler
	public void PlayerLoginEvent(PlayerLoginEvent event) {
	    Player player = event.getPlayer();
	    UUID playerId = player.getUniqueId();
	    
	    if(!Memory.Users.containsKey(playerId)){
	    	User user = new User(playerId);
	    	Memory.Users.put(playerId, user); 	    
	    	return;
	    } 

	    User user = User.FromUUID(playerId); 
	    if(user.HasBoard())
	    	player.setPlayerListName( String.format("/%s/%s", user.BoardName, player.getDisplayName()) );
	}

	private String BoardChatPrefix = String.format(" %s[board] ", RelationColor.Faction); 
	private String AdminPrefix = "**";
	private String ModPrefix = "*"; 
	private String NormiePrefix = ""; 
	
	@EventHandler
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		UUID playerId = event.getPlayer().getUniqueId(); 
		String playerName = event.getPlayer().getName(); 
		User user = User.FromUUID(playerId); 
		Board board = Board.FromName(user.BoardName); 
		
		if(board == null) 
			return; 
		
		//loop through everyone on the server, to set appropriate colors and decide whether or not they can hear it
		Set<Player> listeners = event.getRecipients();
		boolean displayFactionPrefix = false; 
		for(Player listener : listeners){
			User listenUser = User.FromUUID(listener.getUniqueId()); 
			
			Relation relation = listenUser.GetRelation(user);  
			String relationColor = RelationColor.FromRelation(relation); 
			
			// faction chat? 
			displayFactionPrefix = false; 
			if(user.HasBoard() && user.ChatMode == Chat.Board){
				if(!listenUser.HasBoard() || !user.BoardName.equals(listenUser.BoardName))
					continue; 
				
				displayFactionPrefix = true; 
			}
			
			// color [rank stars] /board/ (color name): message 
			String message = String.format("%s%s%s%s/%s/ §b(%s%s§b)§f: %s",
							displayFactionPrefix ? BoardChatPrefix : NormiePrefix,
							relationColor,
							user.BoardRank == Rank.Admin ? AdminPrefix : NormiePrefix,
							user.BoardRank == Rank.Mod ? ModPrefix : NormiePrefix, 
							board.Name, 
							relationColor,
							playerName, 
							event.getMessage());
			
			listener.sendMessage(message);
		}

		event.setCancelled(true);
	}
	
	@EventHandler
	public void OnBedEnterEvent(PlayerBedEnterEvent event){
		Location bedLocation = event.getBed().getLocation(); 
		LocationSerializable ls = new LocationSerializable(bedLocation); 
		UUID bedOwner = Memory.Beds.get(ls);
		
		Player player = event.getPlayer(); 
		UUID playerId = player.getUniqueId(); 
		
		if(bedOwner != null && bedOwner != playerId){
			player.sendMessage("This bed belongs to somebody else!");
			event.setCancelled(true);
			return;
		}
		
		if(bedOwner == null){
			Memory.Beds.put(ls, playerId); 
			player.sendMessage("Claimed bed!");
		}
	}
}
