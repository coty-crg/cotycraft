package com.wanderingcorgi.minecraft;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.wanderingcorgi.minecraft.User.Chat;
import com.wanderingcorgi.minecraft.User.Rank;

interface MyCommand {
    void run(CommandSender sender, Player player, String[] arguments);
}

public class Commands implements CommandExecutor  {

	private final Main plugin;

	HashMap<String, MyCommand> commands = new HashMap<String, MyCommand>();

	public Commands(Main plugin) {
		this.plugin = plugin;
		initialize();
	}
	
	/**
	 * All commands will go here, until I figure out a better system for this.
	 */
	public void initialize(){

		commands.put("create", new MyCommand() {
			@Override
            public void run(CommandSender sender, Player player, String[] arguments) {
            	if(arguments.length == 1){
            		sender.sendMessage("Please specify a board name");
            		return; 
            	}
            	
            	String name = arguments[1]; 
            	boolean exists = Board.FromName(name) != null;
            	
            	if(exists){
            		sender.sendMessage(String.format("The board /%s/ already exists!", name));
            		return; 
            	}

            	User user = User.FromUUID(player.getUniqueId()); 
            	if(user.HasBoard()){
            		sender.sendMessage(String.format("You must leave /%s/ before creating a new board!", user.BoardName));
            		return; 
            	}
            	
            	// make board 
            	Board newBoard = Board.AddBoard(name, player.getUniqueId()); 
            	sender.sendMessage(String.format("Successfully created /%s/!", newBoard.Name));
            	
            	// setup user 
            	user.BoardName = newBoard.Name; 
            	user.BoardRank = Rank.Admin; 
            	sender.sendMessage(String.format("You are now leader of /%s/!", newBoard.Name));
            }
        });
		
		commands.put("enemy",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
				if(arguments.length == 1){
            		sender.sendMessage("Please specify a board! Don't include //s");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("You are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}
            	
            	String otherFactionName = arguments[1]; 
            	Board otherBoard = Board.FromName(otherFactionName); 
            	
            	if(otherBoard == null){
            		sender.sendMessage("That faction doesn't exist!");
            		return; 
            	}
            	
            	boolean alreadyEnemies = otherBoard.Enemies.contains(board.Name); 
            	if(alreadyEnemies){
            		sender.sendMessage(String.format("You are already enemies with /%s/!", otherFactionName));
            		return; 
            	}
            	
            	if(user.BoardName.equals(otherFactionName)){
            		sender.sendMessage(String.format("You cannot be in a relationship with yourself!"));
            		return; 
            	}
            	
            	board.Enemies.add(otherFactionName); 
            	board.Allies.remove(otherFactionName); 
            	otherBoard.Enemies.add(board.Name); 
            	otherBoard.Allies.remove(board.Name); 
            	
            	board.MessageMembers(String.format("%s has notified /%s/ that you are now enemies!", player.getDisplayName(), otherFactionName));
            	otherBoard.MessageMembers(String.format("/%s/ has declared war! They are now your enemy.", board.Name));
			}
		});
		
		commands.put("ally",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
				if(arguments.length == 1){
            		sender.sendMessage("Please specify a board! Don't include //s");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("You are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherFactionName = arguments[1]; 
            	Board otherBoard = Board.FromName(otherFactionName); 
            	
            	if(otherBoard == null){
            		sender.sendMessage("That faction doesn't exist!");
            		return; 
            	}
            	
            	boolean alreadyAllies = otherBoard.Allies.contains(board.Name); 
            	if(alreadyAllies){
                	sender.sendMessage(String.format("Your faction is already in an alliance with /%s/", otherFactionName));
            		return; 
            	}
            	
            	if(user.BoardName.equals(otherFactionName)){
            		sender.sendMessage(String.format("You cannot be in a relationship with yourself!"));
            		return; 
            	}
            	
            	board.Allies.add(otherFactionName); 
            	board.Enemies.remove(otherFactionName); 

            	boolean acceptingAllyRequest = otherBoard.Allies.contains(board.Name); 
            	if(acceptingAllyRequest){
                	board.MessageMembers(String.format("%s has accepted /%s/'s alliance request!", player.getDisplayName(), otherFactionName));
                	otherBoard.MessageMembers(String.format("/%s/ has accepted your alliance request!", board.Name));
            		return; 
            	}
            	
            	board.MessageMembers(String.format("%s has sent an ally request to /%s/!", player.getDisplayName(), otherFactionName));
            	otherBoard.MessageMembers(String.format("/%s/ has sent an ally request! Do /b ally to accept!", board.Name));
			}
		});
		
		commands.put("sethome",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
        		
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage("You are not the owner or a mod!");
            		return; 
            	}
            	
            	Set<Material> IgnoreBlocks = null; 
            	Block targetBlock = player.getTargetBlock(IgnoreBlocks, 10);
            	
            	if(targetBlock.getType() != Material.BED_BLOCK){
            		sender.sendMessage("This is not a bed!!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	board.Home = new LocationSerializable(targetBlock.getLocation()); 
        		sender.sendMessage("This bed is now your board's home!");
			}
		});
		
		commands.put("bed",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	Location bedSpawn = player.getBedSpawnLocation(); 
            	if(bedSpawn == null){
            		sender.sendMessage("You do not have a bed! Was it destroyed?");
            		return; 
            	}
            	
            	player.teleport(bedSpawn); 
        		sender.sendMessage("Teleported to your bed!");
			}
		});
		
		commands.put("home",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board.Home == null){
            		sender.sendMessage("Your board doesn't have a home!");
            		return; 
            	}
            	
            	Block block = player.getWorld().getBlockAt((int) board.Home.X, (int) board.Home.Y, (int) board.Home.Z); 
            	
            	if(block.getType() != Material.BED_BLOCK){
            		sender.sendMessage("Your board's home has been destroyed!!");
            		return; 
            	}
            	
            	player.teleport(LocationSerializable.FromLoctionSerializable(board.Home)); 
        		sender.sendMessage("Teleported to your board's home!");
			}
		});
		
		commands.put("chat",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		user.ChatMode = Chat.Global; 
            		return; 
            	}
        		
            	if(user.ChatMode == Chat.Global){
            		user.ChatMode = Chat.Board; 
            		sender.sendMessage("Now in board-only chat.");
            		return; 
            	}
            	
            	if(user.ChatMode == Chat.Board){

            		user.ChatMode = Chat.Global; 
            		sender.sendMessage("Now in Global chat.");
            		return; 
            	}

			}
		});
		
		commands.put("admin",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("Please specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage(String.format("You are not admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("You cannot admin yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("You are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("You cannot admin the board owner!");
            		return; 
            	}

            	otherUser.BoardRank = Rank.Admin;
            	board.Mods.remove(otherUser.Id); 
            	board.Admins.add(otherUser.Id);

            	user.BoardRank = Rank.Mod; 
            	board.Mods.add(user.Id); 
            	board.Admins.remove(user.Id);

        		sender.sendMessage(String.format("Successfully passed admin to %s! You are now a mod.", otherPlayerName));
            	otherPlayer.sendMessage(String.format("You've been promoted to admin of /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		});
		
		commands.put("demote",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("Please specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage(String.format("You are not admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("You cannot promote yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("You are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("You cannot demote the board owner!");
            		return; 
            	}
            	
            	board.Mods.remove(otherUser.Id);
            	otherUser.BoardRank = Rank.Normie; 

        		sender.sendMessage(String.format("Successfully demoted %s!", otherPlayerName));
            	otherPlayer.sendMessage(String.format("You've been demoted to normie of /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		});
		
		commands.put("promote",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("Please specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage(String.format("You are not admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("You cannot promote yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("You are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("You cannot promote the board owner!");
            		return; 
            	}
            	
            	board.Mods.add(otherUser.Id);
            	otherUser.BoardRank = Rank.Mod; 

        		sender.sendMessage(String.format("Successfully promoted %s!", otherPlayerName));
            	otherPlayer.sendMessage(String.format("You've been promoted to mod of /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		});
		
		commands.put("kick",  new MyCommand() {
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {

				if(arguments.length == 1){
            		sender.sendMessage("Please specify a player!");
            		return; 
            	}
				
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
        		Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage(String.format("You are not a mod or admin of /%s/!", board.Name));
            		return; 
            	}

            	String otherPlayerName = arguments[1]; 
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("You cannot invite yourself!");
            		return; 
            	}
            	
            	User otherUser = User.FromUUID(otherPlayer.getUniqueId()); 
            	
            	if(!otherUser.BoardName.equals(user.BoardName)){
            		sender.sendMessage("You are not in the same board!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Admin){
            		sender.sendMessage("You cannot kick the board owner!");
            		return; 
            	}
            	
            	if(otherUser.BoardRank == Rank.Mod && user.BoardRank == Rank.Mod){
            		sender.sendMessage("Only the board owner can remove other mods!");
            		return; 
            	}
            	
            	board.Members.remove(otherUser.Id); 
            	board.Admins.remove(otherUser.Id); 
            	board.Mods.remove(otherUser.Id);
            	
            	otherUser.BoardName = null; 
            	otherUser.BoardRank = Rank.Normie; 

        		sender.sendMessage(String.format("Successfully kicked %s!", otherPlayerName));
            	otherPlayer.sendMessage(String.format("You've been kicked from /%s/ by %s!", user.BoardName, player.getDisplayName()));
			}
		});
		
		commands.put("leave", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	if(user.BoardRank == Rank.Admin && board.Admins.size() == 1 && board.Members.size() > 1){
            		sender.sendMessage(String.format("/%s/ cannot be left until a new admin is assigned!", board.Name));
            		return; 
            	}

            	board.Members.remove(user.Id); 
            	board.Admins.remove(user.Id); 
            	board.Mods.remove(user.Id);
            	
            	// if empty, remove 
            	if(board.Members.size() == 0){
            		Memory.Boards.remove(board.Name); 
            		sender.sendMessage(String.format("/%s/ disbanded!", board.Name));
            	}
            	
            	user.BoardName = null; 
            	user.BoardRank = Rank.Normie; 
        		sender.sendMessage(String.format("You have left /%s/!", board.Name));
			}
		}); 

		commands.put("open", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(user.BoardRank != Rank.Admin){
            		sender.sendMessage("You are not a board admin!");
            		return; 
            	}
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	board.Open = !board.Open; 
            	
            	String message = String.format("The board is now %s!", (board.Open ? "open" : "closed"));
            	sender.sendMessage(message); 
			}
		}); 
		
		commands.put("list", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				Set<String> keys = Memory.Boards.keySet();
				StringBuilder message = new StringBuilder(); 
				
				for(String key : keys){
					message.append(String.format("%s\n", key) ); 	
				}
				
				String finalMessage = message.toString();
				sender.sendMessage(finalMessage);
			}
		}); 
		
		commands.put("invite", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
            	
				if(arguments.length == 1){
            		sender.sendMessage("Please specify a player!");
            		return; 
            	}
            	
            	User user = User.FromUUID(player.getUniqueId()); 
            	
            	if(user.BoardRank != Rank.Admin && user.BoardRank != Rank.Mod){
            		sender.sendMessage("You are not a board admin or mod!");
            		return; 
            	}
            	
            	if(!user.HasBoard()){
            		sender.sendMessage("You are not in a board!");
            		return; 
            	}
            	
            	Board board = Board.FromName(user.BoardName); 
            	
            	if(board == null){
            		sender.sendMessage(String.format("/%s/ not found?!", user.BoardName));
            		return; 
            	}
            	
            	String otherPlayerName = arguments[1]; 
            	
            	Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
            	
            	if(otherPlayer == null){
            		sender.sendMessage(String.format("/%s/ not found?!", otherPlayerName));
            		return; 
            	}
            	
            	if(otherPlayer.getUniqueId() == player.getUniqueId()){
            		sender.sendMessage("You cannot invite yourself!");
            		return; 
            	}
            	
            	board.InvitedMembers.add(otherPlayer.getUniqueId());

        		sender.sendMessage(String.format("Invited %s to your /%s/", otherPlayerName, user.BoardName));
        		otherPlayer.sendMessage(String.format("You've been invited to /%s/ by %s! Do /b join %s to accept!", user.BoardName, player.getDisplayName(), user.BoardName));
			}
		}); 
		
		commands.put("join", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				if(arguments.length == 1){
            		sender.sendMessage("Please specify a board!");
            		return; 
            	}
            	
				UUID playerId = player.getUniqueId(); 
            	User user = User.FromUUID(playerId); 
            	
            	if(user.HasBoard()){
            		sender.sendMessage("Leave your current board with /b leave first!");
            		return; 
            	}
            	
            	String boardName = arguments[1]; 
            	Board board = Board.FromName(boardName);
            	
            	if(board == null){
            		sender.sendMessage("Board not found?!");
            		return; 
            	}
            	
            	if(!board.Open && !board.InvitedMembers.contains(playerId)){
            		sender.sendMessage("You have not been invited to this board!");
            		return;
            	}
            	
            	board.InvitedMembers.remove(playerId); 
            	board.Members.add(playerId); 
            	
            	user.BoardName = board.Name; 
            	user.BoardRank = Rank.Normie; 
            	
        		sender.sendMessage(String.format("Successfully joined /%s/!", user.BoardName));
			}
		}); 
		
		commands.put("saveall", new MyCommand(){
			@Override
			public void run(CommandSender sender, Player player, String[] arguments) {
				try {
					Memory.SaveToDB();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}); 
		
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