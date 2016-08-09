package com.wanderingcorgi.minecraft;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        		otherPlayer.sendMessage(String.format("You've been invited to /%s/ by %s! Do /b join %s to accept!", user.BoardName, otherPlayerName, user.BoardName));
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