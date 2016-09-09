package com.wanderingcorgi.minecraft;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

import com.wanderingcorgi.minecraft.User.Chat;
import com.wanderingcorgi.minecraft.User.Rank;
import com.wanderingcorgi.minecraft.User.Relation;


public class PlayerListener implements Listener {

	private final Main plugin;
	
	public PlayerListener(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void OnPlayerBucketEvent(PlayerBucketEmptyEvent event){
		Block block = event.getBlockClicked();
		if(event.getBucket() == Material.BUCKET) return; // don't block empty buckets from picking up water
		
		ChunkSerializable thisChunk = new ChunkSerializable(block.getLocation()); 
		if(Memory.ProtectorBlocks.containsKey(thisChunk)){
			ProtectionBlockData protectionBlockData =  Memory.ProtectorBlocks.get(thisChunk); 
			String ownerBoardName = protectionBlockData.BoardName; 
			
			Player player = event.getPlayer();
			
			if(player == null)
				return; 
			
			User user = User.FromUUID(player.getUniqueId()); 
			if(user.BoardName == null || !user.BoardName.equals(ownerBoardName)){
				String relationColor = RelationColor.FromRelation(user.GetRelation(ownerBoardName)); 
				player.sendMessage(String.format("Cannot place blocks in this chunk until protector is destroyed! Chunk protected by: %s/%s/", relationColor, ownerBoardName));
				event.setCancelled(true);
			}
			
			return; 
		}
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
	    
	    if(!Memory.Users.containsKey(playerId)){
	    	User user = new User(playerId);
	    	Memory.Users.put(playerId, user); 	    
	    	return;
	    } 

	    User user = User.FromUUID(playerId); 
	    if(user.HasBoard())
	    	player.setPlayerListName( String.format("/%s/%s", user.BoardName, player.getDisplayName()) );
	}

	private final String BoardChatPrefix = String.format("%s[board]", RelationColor.Faction); 
	private final String AllyChatPrefix = String.format("%s[ally]", RelationColor.Ally); 
	private final String TruceChatPrefix = String.format("%s[truce]", RelationColor.Truce); 
	private final String EnemyChatPrefix = String.format("%s[enemy]", RelationColor.Enemy); 
	private final String LocalChatPrefix = String.format("%s[local]", RelationColor.Local); 
	private final String AdminPrefix = "**";
	private final String ModPrefix = "*"; 
	private final String NormiePrefix = ""; 
	
	@EventHandler
	public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
		Player player = event.getPlayer(); 
		Location playerLocation = player.getLocation(); 
		
		UUID playerId = player.getUniqueId(); 
		String playerName = event.getPlayer().getName(); 
		
		User user = User.FromUUID(playerId); 
		Board board = Board.FromName(user.BoardName); 
		
		String Message = event.getMessage().replace(">", "§a>");
		if(user.TextFormatting)
			Message = event.getMessage().replaceAll("&", "§");

		Set<Player> listeners = event.getRecipients();
		
		if(board == null) {
			for(Player listener : listeners){
				String finalMessage = String.format("§b(§f%s§b)§f: %s", playerName, Message); 
				listener.sendMessage(finalMessage);
			}

			Bukkit.getConsoleSender().sendMessage(String.format("(%s): %s", player.getDisplayName(), Message));
			event.setCancelled(true);
			return; 
		}
		
		//loop through everyone on the server, to set appropriate colors and decide whether or not they can hear it
		for(Player listener : listeners){
			User listenUser = User.FromUUID(listener.getUniqueId()); 
			
			Relation relation = listenUser.GetRelation(user);  
			String relationColor = RelationColor.FromRelation(relation); 
			String prefix = NormiePrefix; 
			
			if(user.HasBoard()){
				if(user.ChatMode == Chat.Board){
					if(relation != Relation.Faction)
						continue; 
					
					prefix = BoardChatPrefix; 
				}
				
				if(user.ChatMode == Chat.Ally){
					if(relation != Relation.Ally && relation != Relation.Faction)
						continue; 
					
					prefix = AllyChatPrefix; 
				}

				if(user.ChatMode == Chat.Truce){
					if(relation != Relation.Truce && relation != Relation.Faction)
						continue; 
					
					prefix = TruceChatPrefix; 
				}
				
				if(user.ChatMode == Chat.Enemy){
					if(relation != Relation.Enemy && relation != Relation.Faction)
						continue;
					
					prefix = EnemyChatPrefix; 
				}
				
				if(user.ChatMode == Chat.Local){
					Location listenerLocation = listener.getLocation(); // uses a sqrt, so its slow 
					if(!playerLocation.getWorld().equals(listenerLocation))
						continue;
					
					if(playerLocation.distance(listenerLocation) > 80)
						continue; 
					
					prefix = LocalChatPrefix; 
				}
			}
			
			// color [rank stars] /board/ (color name): message 
			String message = String.format("%s %s%s%s/%s/ §b(%s%s§b)§f: %s",
							prefix,
							relationColor,
							user.BoardRank == Rank.Admin ? AdminPrefix : NormiePrefix,
							user.BoardRank == Rank.Mod ? ModPrefix : NormiePrefix, 
							board.Name, 
							relationColor,
							playerName, 
							Message);
			
			listener.sendMessage(message);
		}
		
		Bukkit.getConsoleSender().sendMessage(String.format("[%s] /%s/ (%s): %s", user.ChatMode, user.BoardName, player.getDisplayName(), Message));
		event.setCancelled(true);
	}
	
	@EventHandler
	public void OnEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if( !(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))  
			return; 
		
		UUID playerId = event.getEntity().getUniqueId(); 
		UUID otherPlayerId = event.getDamager().getUniqueId(); 
		if(playerId == null || otherPlayerId == null)
			return; 
		
		User user = User.FromUUID(playerId);
		User otherUser = User.FromUUID(otherPlayerId); 
		
		if(user == null || otherUser == null || user.BoardName == null || otherUser.BoardName == null)
			return; 
		
		if(user.BoardName.equals(otherUser.BoardName)){
			Player player = Bukkit.getPlayer(playerId);
			Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
			otherPlayer.sendMessage(String.format("[§7%s is in your board!]", player.getDisplayName()));
			event.setCancelled(true);
			return; 
		}
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
	
	@EventHandler
	public void onNameTag(AsyncPlayerReceiveNameTagEvent event) {
		Player namedPlayer = event.getNamedPlayer(); 
		User namedUser = User.FromUUID(namedPlayer.getUniqueId()); 

		if(!namedUser.HasBoard())
			return;
		
		Player player = event.getPlayer();
		User user = User.FromUUID(player.getUniqueId()); 
		Relation relation = user.GetRelation(namedUser);
		String color = RelationColor.FromRelation(relation); 
		String newTag = String.format("%s/%s/%s", color, namedUser.BoardName, namedPlayer.getDisplayName()); 
		
		event.setTag(newTag);
	}
}
