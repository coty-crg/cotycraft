package com.wanderingcorgi.minecraft;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Memory {

	// <location of block, durability of that block> 
	protected static HashMap<LocationSerializable, Integer> Universe = new HashMap<LocationSerializable, Integer>(); 
	protected static HashMap<String, Board> Boards = new HashMap<String, Board>(); 
	protected static HashMap<UUID, User> Users = new HashMap<UUID, User>(); 
	
	public static int GetDurability(Block block){
		LocationSerializable ls = new LocationSerializable(block); 
    	boolean exists = Memory.Universe.containsKey(ls); 
    	Integer durability = exists ? Memory.Universe.get(ls) : 0;
    	return durability.intValue(); 
	}
	
	public static void SetDurability(Block block, int value){
		LocationSerializable ls = new LocationSerializable(block.getLocation()); 
		Memory.Universe.put(ls, value); 
	}
	
	public static void IncreaseDurability(Block block, int amount){
		int durability = GetDurability(block) + amount;
		if(durability > 100) durability = 100; 
		SetDurability(block, durability); 
	}
	
	public static void DecreaseDurability(Block block, int amount){
		int durability = GetDurability(block) - amount;
		if(durability < 0) durability = 0; 
		SetDurability(block, durability); 
	}
	
	// returns true if we really want to destroy the block 
	public static boolean BlockBroken(Block block, int power){
		DecreaseDurability(block, power); 
		int durability = GetDurability(block); 
		return durability <= 0; 
	}
	
	public static void LoadFromDB() throws IOException, ClassNotFoundException {
		createDirectories(); 
		LoadWorldData();
		LoadBoardsData(); 
		LoadUserData(); 
		// PrintOutDB(); 
	}

	@SuppressWarnings("unchecked")
	public static void LoadWorldData() throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(Main.dataFolder + "/saves/Universe.sav");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Universe = ( HashMap<LocationSerializable, Integer> ) in.readObject();
        in.close();
        fileIn.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void LoadBoardsData() throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(Main.dataFolder + "/saves/Boards.sav");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Boards = ( HashMap<String, Board> ) in.readObject();
        in.close();
        fileIn.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void LoadUserData() throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(Main.dataFolder + "/saves/Users.sav");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Users = ( HashMap<UUID, User> ) in.readObject();
        in.close();
        fileIn.close();
	}
	
	public static void PrintOutDB(){
		Set<String> keys = Boards.keySet();
		keys.forEach((v) -> {
			Board i = Boards.get(v); 
			Bukkit.getConsoleSender().sendMessage( "(" + v + ")");
		});
	}
	
	public static void SaveToDB() throws IOException {
		createDirectories(); 
		SaveWorldData();
		SaveBoardData(); 
		SaveUserData(); 
	}
	
	public static void SaveWorldData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/Universe.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(Universe);
        out.close();
        fileOut.close();
	}
	
	public static void SaveBoardData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/Boards.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(Boards);
        out.close();
        fileOut.close();
	}
	
	public static void SaveUserData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/Users.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(Users);
        out.close();
        fileOut.close();
	}
	
    public static void createDirectories(){
    	File dir0 = new File(Main.dataFolder + "/");
    	if(!dir0.exists()){
    		dir0.mkdir();
		}
    	
    	File dir_fac = new File(Main.dataFolder + "/saves");
    	if(!dir_fac.exists()){
    		dir_fac.mkdir();
		}
    }
	
}
