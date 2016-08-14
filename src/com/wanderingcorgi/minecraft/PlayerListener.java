package com.wanderingcorgi.minecraft;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.wanderingcorgi.minecraft.User.Chat;
import com.wanderingcorgi.minecraft.User.Rank;


public class PlayerListener implements Listener {

	private final Main plugin;
	
	public PlayerListener(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
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
	    
	    if(Memory.Users.containsKey(playerId)) 
	    	return; 

	    User user = new User(playerId);
	    Memory.Users.put(playerId, user); 
	}

	private String BoardChatPrefix = String.format("%s(board)", RelationColor.Self); 
	private String AdminPrefix = "**";
	private String ModPrefix = "*"; 
	private String NormiePrefix = ""; 
	private String ChatFormat = "%s%s%s/%s/ (%s): %s"; 
	
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
			
			// faction chat? 
			displayFactionPrefix = false; 
			if(user.HasBoard() && user.ChatMode == Chat.Board){
				if(!listenUser.HasBoard() || !user.BoardName.equals(listenUser.BoardName))
					continue; 
				
				displayFactionPrefix = true; 
			}
			
			String message = String.format(ChatFormat, 
							user.BoardRank == Rank.Admin ? AdminPrefix : NormiePrefix,
							user.BoardRank == Rank.Mod ? ModPrefix : NormiePrefix, 
							displayFactionPrefix ? BoardChatPrefix : NormiePrefix,
							board.Name, 
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
