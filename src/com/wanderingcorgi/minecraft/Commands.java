package com.wanderingcorgi.minecraft;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kitteh.tag.TagAPI;

import com.wanderingcorgi.minecraft.User.Chat;
import com.wanderingcorgi.minecraft.User.Rank;
import com.wanderingcorgi.minecraft.User.Relation;

interface MyCommand {
    void run(CommandSender sender, Player player, String[] arguments);
}

public class Commands implements CommandExecutor  {

	private final Main plugin;

	HashMap<String, MyCommand> commands = new HashMap<String, MyCommand>();

	List<BoardCommand> CommandList = new ArrayList<BoardCommand>(); 
	
	public class BoardCommand {
		
		public MyCommand Command; 
		public String Description; 
		public String Arguments; 
		public String Name; 
		
		public BoardCommand(String name, String arguments, String description, MyCommand cmd){
			Name = name; 
			Arguments = arguments; 
			Description = description; 
			Command = cmd; 
		}
		
	}
	
	public Commands(Main plugin) {
		this.plugin = plugin;
		initialize();
	}
	
	/**
	 * All commands will go here, until I figure out a better system for this.
	 */
	public void initialize(){

		CommandList.add(new BoardCommand("allowtextformatting", "user", "[Admin] Enables text formatting for a user.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
				if(!player.isOp()){
					sender.sendMessage("§cYou are not an admin!");
				}
				
				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a user! Example: /b allowtextformatting user");
            		return; 
            	}
				
            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
				
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	otherUser.TextFormatting = true;
            	sender.sendMessage(String.format("%s can now use text formatting", otherPlayer.getDisplayName()));
            	otherPlayer.sendMessage(String.format("You can now format text by prefixing a message with &code, like &cExample would print §cExample! Go crazy!", otherPlayer.getDisplayName()));
			}
		}));
		
		CommandList.add(new BoardCommand("create", "name", "Creates a new board. Name limit of 5 characters.", new MyCommand() {
			@Override
            public void run(CommandSender sender, Player player, String[] arguments) {
            	if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a board name");
            		return; 
            	}
            	
            	String name = arguments[1]; 
            	boolean exists = Board.FromName(name) != null;
            	
            	if(name.length() > 5){
            		sender.sendMessage("§cThat board name is too long! 5 characters max!");
            		return; 
            	}
            	
            	if(exists){
            		sender.sendMessage(String.format("§cThe board /%s/ already exists!", name));
            		return; 
            	}

            	User user = User.FromUUID(player.getUniqueId()); 
            	if(user.HasBoard()){
            		sender.sendMessage(String.format("§cYou must leave /%s/ before creating a new board!", user.BoardName));
            		return; 
            	}
            	
            	// make board 
            	Board newBoard = Board.AddBoard(name, player.getUniqueId()); 
            	sender.sendMessage(String.format("Successfully created /%s/!", newBoard.Name));
            	
            	// setup user 
            	user.BoardName = newBoard.Name; 
            	user.BoardRank = Rank.Admin; 
            	sender.sendMessage(String.format("You are now leader of /%s/!", newBoard.Name));
            	TagAPI.refreshPlayer(player);
            }
        }));

		CommandList.add(new BoardCommand("info", "board", "Returns the information on a specified board. If no board is given, returns information on your board.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				

            	String otherFactionName = null; 
				if(arguments.length > 1){
					otherFactionName = arguments[1]; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(otherFactionName == null)
            		otherFactionName = board.Name; 
            	
            	Board otherBoard = Board.FromName(otherFactionName); 
            	
            	if(otherBoard == null){
            		sender.sendMessage("§cThis board does not exist!");
            		return; 
            	}
            	
        		Relation relationship = board.GetRelation(otherBoard);
        		String relationColor = RelationColor.FromRelation(relationship); 
        		
        		StringBuilder memberList = new StringBuilder();
        		for(UUID playerId : otherBoard.Members){
        			Player playerMember = Bukkit.getPlayer(playerId); 
        			if(playerMember == null) continue; 
        			memberList.append(playerMember.getDisplayName()); 
        			memberList.append(','); 
        		}
        		
        		StringBuilder enemyList = new StringBuilder();
        		StringBuilder allyList = new StringBuilder();
        		for(String listBoardName : Memory.Boards.keySet()){
        			Board listBoard = Board.FromName(listBoardName); 
        			Relation listRelation = board.GetRelation(listBoard); 
        			
        			if(listRelation == Relation.Ally)
        				allyList.append(String.format("%s, ", listBoardName));
        			
        			if(listRelation == Relation.Enemy)
        				enemyList.append(String.format("%s, ", listBoardName));
        		}
        		
        		String message = String.format("%s/%s/ - %s members§f \n§dAlliances: %s §c\nEnemies: %s \n\n§7Online: %s", 
        				relationColor,
        				otherBoard.Name,
        				otherBoard.Members.size(),
        				allyList.toString(),
        				enemyList.toString(),
        				memberList.toString()); 
        		
        		sender.sendMessage(message);
			}
		})); 

		CommandList.add(new BoardCommand("enemy", "board", "Declares war against the specified board.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a board! Example: /b enemy a");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("§cYou are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}
            	
            	String otherFactionName = arguments[1]; 
            	Board otherBoard = Board.FromName(otherFactionName); 
            	
            	if(otherBoard == null){
            		sender.sendMessage("§cThat faction doesn't exist!");
            		return; 
            	}
            	
            	boolean alreadyEnemies = board.GetRelation(otherBoard) == Relation.Enemy; 
            	if(alreadyEnemies){
            		sender.sendMessage(String.format("§cYou are already enemies with /%s/!", otherFactionName));
            		return; 
            	}
            	
            	if(user.BoardName.equals(otherFactionName)){
            		sender.sendMessage(String.format("§cYou cannot be in a relationship with yourself!"));
            		return; 
            	}
            	
            	board.Enemies.add(otherFactionName); 
            	board.Allies.remove(otherFactionName);
            	board.Truce.remove(otherFactionName); 
            	otherBoard.Enemies.add(board.Name); 
            	otherBoard.Allies.remove(board.Name); 
            	otherBoard.Truce.remove(board.Name); 
            	
            	board.MessageMembers(String.format("§c%s has notified /%s/ that you are now enemies!", player.getDisplayName(), otherFactionName));
            	otherBoard.MessageMembers(String.format("§c/%s/ has declared war! They are now your enemy.", board.Name));
            	
            	// refresh names for players 
            	for(UUID memberId : board.Members){
            		Player member = Bukkit.getPlayer(memberId);
            		if(member == null) continue;             		
            		TagAPI.refreshPlayer(member);
            	}
            	
            	for(UUID memberId : otherBoard.Members){
            		Player member = Bukkit.getPlayer(memberId);
            		if(member == null) continue;             		
            		TagAPI.refreshPlayer(member);
            	}
			}
		}));
		
		CommandList.add(new BoardCommand("ally", "board", "Requests or confirms an alliance with the specified board.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a board! Example: /b ally tg");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("§cYou are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherFactionName = arguments[1]; 
            	Board otherBoard = Board.FromName(otherFactionName); 
            	
            	if(otherBoard == null){
            		sender.sendMessage("§cThat faction doesn't exist!");
            		return; 
            	}
            	
            	boolean alreadyAllies = board.GetRelation(otherBoard) == Relation.Ally; 
            	if(alreadyAllies){
                	sender.sendMessage(String.format("§dYour faction is already in an alliance with /%s/", otherFactionName));
            		return; 
            	}
            	
            	boolean alreadyRequestedAwaitingResponse = board.Allies.contains(otherFactionName); 
            	if(alreadyRequestedAwaitingResponse){
            		sender.sendMessage("§dYour faction is still awaiting a response!");
            		return; 
            	}
            	
            	if(user.BoardName.equals(otherFactionName)){
            		sender.sendMessage(String.format("§cYou cannot be in a relationship with yourself!"));
            		return; 
            	}
            	
            	board.Allies.add(otherFactionName); 
            	board.Enemies.remove(otherFactionName); 
            	board.Truce.remove(otherFactionName); 

            	boolean acceptingAllyRequest = board.GetRelation(otherBoard) == Relation.Ally;  
            	if(acceptingAllyRequest){
                	board.MessageMembers(String.format("§d%s has accepted /%s/'s alliance request!", player.getDisplayName(), otherFactionName));
                	otherBoard.MessageMembers(String.format("§d/%s/ has accepted your alliance request!", board.Name));
            		return; 
            	}
            	
            	board.MessageMembers(String.format("§d%s has sent an ally request to /%s/!", player.getDisplayName(), otherFactionName));
            	otherBoard.MessageMembers(String.format("§d/%s/ has sent an ally request! Do /b ally %s to accept!", board.Name, board.Name));
            	
            	// refresh names for players 
            	for(UUID memberId : board.Members){
            		Player member = Bukkit.getPlayer(memberId);
            		if(member == null) continue;             		
            		TagAPI.refreshPlayer(member);
            	}
            	
            	for(UUID memberId : otherBoard.Members){
            		Player member = Bukkit.getPlayer(memberId);
            		if(member == null) continue;             		
            		TagAPI.refreshPlayer(member);
            	}
			}
		}));
		
		CommandList.add(new BoardCommand("truce", "board", "Requests or confirms a truce with the specified board.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a board! Example: /b truce tg");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("§cYou are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherFactionName = arguments[1]; 
            	Board otherBoard = Board.FromName(otherFactionName); 
            	
            	if(otherBoard == null){
            		sender.sendMessage("§cThat faction doesn't exist!");
            		return; 
            	}
            	
            	boolean alreadyAllies = board.GetRelation(otherBoard) == Relation.Truce; 
            	if(alreadyAllies){
                	sender.sendMessage(String.format("§dYour faction is already in a truce with /%s/", otherFactionName));
            		return; 
            	}
            	
            	boolean alreadyRequestedAwaitingResponse = board.Truce.contains(otherFactionName); 
            	if(alreadyRequestedAwaitingResponse){
            		sender.sendMessage("§dYour faction is still awaiting a response!");
            		return; 
            	}
            	
            	if(user.BoardName.equals(otherFactionName)){
            		sender.sendMessage(String.format("§cYou cannot be in a relationship with yourself!"));
            		return; 
            	}
            	
            	board.Truce.add(otherFactionName); 
            	board.Allies.remove(otherFactionName);
            	board.Enemies.remove(otherFactionName); 

            	boolean acceptingTruceRequest = board.GetRelation(otherBoard) == Relation.Truce;  
            	if(acceptingTruceRequest){
                	board.MessageMembers(String.format("§d%s has accepted /%s/'s truce request!", player.getDisplayName(), otherFactionName));
                	otherBoard.MessageMembers(String.format("§d/%s/ has accepted your truce request!", board.Name));
            		return; 
            	}
            	
            	board.MessageMembers(String.format("§d%s has sent an truce request to /%s/!", player.getDisplayName(), otherFactionName));
            	otherBoard.MessageMembers(String.format("§d/%s/ has sent an truce request! Do /b truce %s to accept!", board.Name, board.Name));
            	
            	// refresh names for players 
            	for(UUID memberId : board.Members){
            		Player member = Bukkit.getPlayer(memberId);
            		if(member == null) continue;             		
            		TagAPI.refreshPlayer(member);
            	}
            	
            	for(UUID memberId : otherBoard.Members){
            		Player member = Bukkit.getPlayer(memberId);
            		if(member == null) continue;             		
            		TagAPI.refreshPlayer(member);
            	}
			}
		}));
		
		CommandList.add(new BoardCommand("sethome", "", "While looking at a home rune's bottom gold block, this command sets your board's home. If your board's home rune is destroyed or moved, the home will be lost. A Home rune is constructed (top to bottom) by a Gold Block -> Air -> Air -> Air -> Gold Block.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
        		
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage("§cYou are not the owner or a mod!");
            		return; 
            	}
            	
            	Set<Material> IgnoreBlocks = null; 
            	Block homeBottom = player.getTargetBlock(IgnoreBlocks, 10);
            	Block airBottom = homeBottom.getRelative(BlockFace.UP);
            	Block airMid = airBottom.getRelative(BlockFace.UP);
            	Block airTop = airMid.getRelative(BlockFace.UP);
            	Block homeTop = airTop.getRelative(BlockFace.UP);
            	
            	if(homeBottom.getType() != Memory.HomeBlock
        			||airBottom.getType() != Material.AIR 
        			|| airMid.getType() != Material.AIR 
        			|| airTop.getType() != Material.AIR 
        			|| homeTop.getType() != Memory.HomeBlock){
            		sender.sendMessage(String.format("§cThis is not a §bHome Rune§c! (From top to bottom: Gold, Air, Air, Gold; use this command on the bottom Gold Block)"));
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	board.Home = new LocationSerializable(homeBottom.getLocation()); 
        		sender.sendMessage("§aThis bed is now your board's home!");
			}
		}));
		
		// disabled by design 
		/*CommandList.add(new BoardCommand("bed", "", "Sleeping in a bed will set your personal home. Using this command will teleport you to your bed. Note: this is different than the board's home bed.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	
				User user = User.FromUUID(player.getUniqueId()); 
				
				long secondsSinceLastTp = System.currentTimeMillis() / 1000 - user.LastTeleportMS / 1000; 
				long secondsRemaining = Memory.BedTeleportCooldownSeconds - secondsSinceLastTp; 
				if(secondsRemaining > 0){
            		sender.sendMessage(String.format("You must wait %s seconds before you can teleport to your bed!", secondsRemaining));
            		return; 
				}
				
				Location bedSpawn = player.getBedSpawnLocation(); 
            	if(bedSpawn == null){
            		sender.sendMessage("§cYou do not have a bed! Was it destroyed?");
            		return; 
            	}
            	
            	Location aboveBed1 = new Location(bedSpawn.getWorld(), bedSpawn.getX(), bedSpawn.getY() + 1, bedSpawn.getZ()); 
            	Location aboveBed2 = new Location(bedSpawn.getWorld(), bedSpawn.getX(), bedSpawn.getY() + 2, bedSpawn.getZ()); 
            	
            	if(aboveBed1.getBlock().getType().isSolid() || aboveBed2.getBlock().getType().isSolid()){
            		sender.sendMessage("§cYour home is blocked!");
            		return; 
            	}
            	
            	player.teleport(aboveBed1); 
        		sender.sendMessage("§aTeleported to your bed!");
        		user.LastTeleportMS = System.currentTimeMillis(); 
			}
		}));*/
		
		CommandList.add(new BoardCommand("home", "", "Teleports you to your board's home bed. Note: this is different than your personal bed.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	long secondsSinceLastTp = System.currentTimeMillis() / 1000 - user.LastTeleportMS / 1000; 
				long secondsRemaining = Memory.HomeTeleportCooldownSeconds - secondsSinceLastTp; 
				if(secondsRemaining > 0){
            		sender.sendMessage(String.format("You must wait %s seconds before you can teleport to your board's home!", secondsRemaining));
            		return; 
				}
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board.Home == null){
            		sender.sendMessage("§cYour board doesn't have a home!");
            		return; 
            	}
            	
            	Block block = player.getWorld().getBlockAt((int) board.Home.X, (int) board.Home.Y, (int) board.Home.Z); 
            	
            	if(block.getType() != Memory.HomeBlock){
            		sender.sendMessage("§cYour board's home has been destroyed!!");
            		return; 
            	}
            	
            	Location bed = LocationSerializable.FromLoctionSerializable(board.Home); 
            	Location aboveHome1 = new Location(bed.getWorld(), bed.getX(), bed.getY() + 1, bed.getZ()); 
            	Location aboveHome2 = new Location(bed.getWorld(), bed.getX(), bed.getY() + 2, bed.getZ()); 
            	
            	if(aboveHome1.getBlock().getType().isSolid() || aboveHome2.getBlock().getType().isSolid()){
            		sender.sendMessage("§cYour board's home is blocked!");
            		return; 
            	}
            	
            	player.teleport(aboveHome1); 
        		sender.sendMessage("§aTeleported to your board's home!");
        		user.LastTeleportMS = System.currentTimeMillis(); 
			}
		}));

		CommandList.add(new BoardCommand("chat", "", "Toggles between [Global] and [Board] chat. While in [Board] chat, only your own board members can hear what you say.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		user.ChatMode = Chat.Global; 
            		return; 
            	}
        		
            	if(arguments.length > 1){
            		String desiredChat = arguments[1]; 
            		switch(desiredChat){
	            		case "g":
	            		case "all":
	            		case "global":
	            			user.ChatMode = Chat.Global; 
	                		sender.sendMessage("§fNow in Global chat.");
	                		return; 

	            		case "a":
	            		case "ally":
	                		user.ChatMode = Chat.Ally; 
	                		sender.sendMessage("§dNow in Ally chat.");
	                		return; 

	            		case "t":
	            		case "truce":
	                		user.ChatMode = Chat.Truce; 
	                		sender.sendMessage("§eNow in Truce chat.");
	                		return; 
	                		
	            		case "f":
	            		case "faction":
	            		case "b":
	            		case "board":
	                		user.ChatMode = Chat.Board; 
	                		sender.sendMessage("§aNow in board-only chat.");
	                		return; 

	            		case "e":
	            		case "enemy":
	                		user.ChatMode = Chat.Enemy; 
	                		sender.sendMessage("§cNow in Enemy chat.");
	                		return; 
	                		
	            		case "l":
	            		case "local":
	                		user.ChatMode = Chat.Local; 
	                		sender.sendMessage("§7Now in Local chat. Only those close to you can hear you!");
	                		return; 
            		}
            	}
            	
            	if(user.ChatMode == Chat.Global){
            		user.ChatMode = Chat.Board; 
            		sender.sendMessage("§aNow in board-only chat.");
            		return; 
            	}
            	
            	if(user.ChatMode == Chat.Board){
            		user.ChatMode = Chat.Ally; 
            		sender.sendMessage("§dNow in Ally chat.");
            		return; 
            	}

            	if(user.ChatMode == Chat.Ally){
            		user.ChatMode = Chat.Truce; 
            		sender.sendMessage("§eNow in Truce chat.");
            		return; 
            	}
            	
            	if(user.ChatMode == Chat.Truce){
            		user.ChatMode = Chat.Enemy; 
            		sender.sendMessage("§fNow in Enemy chat.");
            		return; 
            	}
            	
            	if(user.ChatMode == Chat.Enemy){
            		user.ChatMode = Chat.Local; 
            		sender.sendMessage("§fNow in Local chat.");
            		return; 
            	}
            	
            	if(user.ChatMode == Chat.Local){
            		user.ChatMode = Chat.Global; 
            		sender.sendMessage("§fNow in Global chat.");
            		return; 
            	}
			}
		}));
		
		CommandList.add(new BoardCommand("reinforce", "", "Toggles reinforcement via left click on and off. The level of reinforcement applied to a block depends on the item in your hand.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	User user = User.FromUUID(player.getUniqueId()); 
            	user.ReinforceMode = !user.ReinforceMode; 
            	
            	if(user.ReinforceMode)
            		sender.sendMessage("You can now reinforce blocks by left clicking them!");
            	else
            		sender.sendMessage("Reinforcement via left click is now temporarily disabled.");
			}
		}));
		
		CommandList.add(new BoardCommand("admin", "player", "Gives ownership of the board to the specified player.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage(String.format("§cYou are not admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("§cYou cannot admin yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("§cYou are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("§cYou cannot admin the board owner!");
            		return; 
            	}

            	otherUser.BoardRank = Rank.Admin;
            	board.Mods.remove(otherUser.Id); 
            	board.Admins.add(otherUser.Id);

            	user.BoardRank = Rank.Mod; 
            	board.Mods.add(user.Id); 
            	board.Admins.remove(user.Id);

        		sender.sendMessage(String.format("§aSuccessfully passed admin to %s! You are now a mod.", otherPlayerName));
            	otherPlayer.sendMessage(String.format("§aYou've been promoted to admin of /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		}));
		
		CommandList.add(new BoardCommand("demote", "player", "Removes mod rank from specified player.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage(String.format("§cYou are not admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("§cYou cannot demote yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("§cYou are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("§cYou cannot demote the board owner!");
            		return; 
            	}
            	
            	board.Mods.remove(otherUser.Id);
            	otherUser.BoardRank = Rank.Normie; 

        		sender.sendMessage(String.format("§aSuccessfully demoted %s!", otherPlayerName));
            	otherPlayer.sendMessage(String.format("§aYou've been demoted to normie of /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		}));
		
		CommandList.add(new BoardCommand("promote", "player", "Grants mod rank to the specified player.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage(String.format("§cYou are not admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("§cYou cannot promote yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("§cYou are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("§cYou cannot promote the board owner!");
            		return; 
            	}
            	
            	board.Mods.add(otherUser.Id);
            	otherUser.BoardRank = Rank.Mod; 

        		sender.sendMessage(String.format("§aSuccessfully promoted %s!", otherPlayerName));
            	otherPlayer.sendMessage(String.format("§aYou've been promoted to mod of /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		}));
		
		CommandList.add(new BoardCommand("kick", "player", "Removes the specified player from your board.", new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("§cYou are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("§cYou cannot invite yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("§cYou are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("§cYou cannot kick the board owner!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Mod && user.BoardRank == Rank.Mod){
            		sender.sendMessage("§cOnly the board owner can remove other mods!");
            		return; 
            	}
            	
            	board.Members.remove(otherUser.Id); 
            	board.Admins.remove(otherUser.Id); 
            	board.Mods.remove(otherUser.Id);
            	
            	otherUser.BoardName = null; 
            	otherUser.BoardRank = Rank.Normie; 

        		sender.sendMessage(String.format("§aSuccessfully kicked %s!", otherPlayerName));
            	otherPlayer.sendMessage(String.format("§cYou've been kicked from /%s/ by %s!", user.BoardName, player.getDisplayName()));
            	
            	TagAPI.refreshPlayer(otherPlayer);
			}
		}));
		
		CommandList.add(new BoardCommand("leave", "", "Leaves your current board. If you are the owner of the board, you can only leave if no other members are in the board or if you give ownership via the /b admin command.", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank == Rank.Admin && board.Admins.size() == 1 && board.Members.size() > 1){
            		sender.sendMessage(String.format("§c/%s/ cannot be left until a new admin is assigned!", board.Name));
            		return; 
            	}

            	board.Members.remove(user.Id); 
            	board.Admins.remove(user.Id); 
            	board.Mods.remove(user.Id);
            	
            	// if empty, remove 
            	if(board.Members.size() == 0){
            		Memory.Boards.remove(board.Name); 
            		sender.sendMessage(String.format("§a/%s/ disbanded!", board.Name));
            	}
            	
            	user.BoardName = null; 
            	user.BoardRank = Rank.Normie; 
        		sender.sendMessage(String.format("§aYou have left /%s/!", board.Name));

            	TagAPI.refreshPlayer(player);
			}
		}));

		CommandList.add(new BoardCommand("open", "", "Toggles your board between open and closed states. While open, anyone can join without an invite.", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage("§cYou are not a board admin!");
            		return; 
            	}
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	board.Open = !board.Open; 
            	
            	String message = String.format("§aThe board is now %s!", (board.Open ? "open" : "closed"));
            	sender.sendMessage(message); 
			}
		}));
		
		CommandList.add(new BoardCommand("invite", "player", "Invites the specified player to your board.", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	
				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a player!");
            		return; 
            	}
            	
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage("§cYou are not a board admin or mod!");
            		return; 
            	}
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("§cYou are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	String otherPlayerName = arguments[1]; 
            	
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("§c/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("§cYou cannot invite yourself!");
            		return; 
            	}
            	
            	board.InvitedMembers.add(otherPlayer.getUniqueId());

        		sender.sendMessage(String.format("§aInvited %s to your /%s/", otherPlayerName, user.BoardName));
        		otherPlayer.sendMessage(String.format("§aYou've been invited to /%s/ by %s! Do /b join %s to accept!", user.BoardName, player.getDisplayName(), user.BoardName));
			}
		}));
		
		CommandList.add(new BoardCommand("join", "board", "Joins the specified board. Note: You must have been invited or the board must be open for you to join it.", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				if(arguments.length == 1){
            		sender.sendMessage("§cPlease specify a board!");
            		return; 
            	}
            	
				UUID playerId = player.getUniqueId(); 
            	User user = User.FromUUID(playerId); 
            	
            	if(user.HasBoard()){
            		sender.sendMessage("§cLeave your current board with /b leave first!");
            		return; 
            	}
            	
            	String boardName = arguments[1]; 
            	Board board = Board.FromName(boardName);
            	
            	if(board == null){
            		sender.sendMessage("§cBoard not found?!");
            		return; 
            	}
            	
            	if(!board.Open && !board.InvitedMembers.contains(playerId)){
            		sender.sendMessage("§cYou have not been invited to this board!");
            		return;
            	}
            	
            	board.InvitedMembers.remove(playerId); 
            	board.Members.add(playerId); 
            	
            	user.BoardName = board.Name; 
            	user.BoardRank = Rank.Normie; 
            	
        		sender.sendMessage(String.format("§aSuccessfully joined /%s/!", user.BoardName));
            	TagAPI.refreshPlayer(player);
			}
		}));
		
		CommandList.add(new BoardCommand("help", "", "Prints out a copy of every command.", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				StringBuilder builder = new StringBuilder(); 
				
				for(BoardCommand cmd : CommandList){
					builder.append(String.format("§b/b %s §a%s §7- %s\n\n", cmd.Name, cmd.Arguments, cmd.Description));
				}
				
				sender.sendMessage(builder.toString());
			}
		}));
		
		for(BoardCommand cmd : CommandList){
			commands.put(cmd.Name, cmd.Command); 
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		
		if(arguments.length == 0){
			sender.sendMessage("No command entered.");
			return false; 
		}
		
		if(!(sender instanceof Player)){
			sender.sendMessage("Only players can do commands!");
			return true;
		}

		Player player = (Player) sender;
		
		String cmd = arguments[0].toLowerCase();

		if(!commands.containsKey(cmd)){
			sender.sendMessage("Command not found!");
			return false; 
		}
		
		commands.get(cmd).run(sender, player, arguments);

		return true; 
	}
}