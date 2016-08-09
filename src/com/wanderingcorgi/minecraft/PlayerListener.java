package com.wanderingcorgi.minecraft;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;


public class PlayerListener implements Listener {

	private final Main plugin;
	
	public PlayerListener(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
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
		for(Player listener : listeners){
			String message = String.format("/%s/ (%s): %s", board.Name, playerName, event.getMessage()); // "(" + user.getPlayer().getName() + "): " + event.getMessage();
			listener.sendMessage(message);
		}

		event.setCancelled(true);
	}
}
