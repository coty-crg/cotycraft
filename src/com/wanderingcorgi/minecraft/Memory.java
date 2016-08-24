package com.wanderingcorgi.minecraft;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.google.common.io.Files;

import ru.beykerykt.lightapi.LightAPI;

public class Memory {
	
	// <location of block, durability of that block> 
	protected static HashMap<LocationSerializable, Integer> Universe = new HashMap<LocationSerializable, Integer>(); 
	
	// <location of bed, owner of bed> 
	protected static HashMap<LocationSerializable, UUID> Beds = new HashMap<LocationSerializable, UUID>();
	
	// <location of door, faction name> 
	protected static HashMap<LocationSerializable, String> Doors = new HashMap<LocationSerializable, String>();
	
	// <chunk protected, date protected since> 
	protected static HashMap<ChunkSerializable, ProtectionBlockData> ProtectorBlocks = new HashMap<ChunkSerializable, ProtectionBlockData>();
	
	// <board name, board data> 
	protected static HashMap<String, Board> Boards = new HashMap<String, Board>();
	
	// <user id, user data> 
	protected static HashMap<UUID, User> Users = new HashMap<UUID, User>(); 
	
	// < block or item, durability boost >
	protected static HashMap<Material, Integer> MaterialValues = new HashMap<Material, Integer>(){
		private static final long serialVersionUID = -327214699939405147L;
		{
			put(Material.IRON_INGOT, 4); 
			put(Material.GOLD_INGOT, 5); 
			put(Material.IRON_BLOCK, 40); 
			put(Material.GOLD_BLOCK, 50); 
			put(Material.BRICK, 2); 
			put(Material.CAKE, 5);
			put(Material.BOOK, 1);
			put(Material.CARROT_ITEM, 1);
			put(Material.CLAY_BRICK, 2);
			put(Material.COBBLESTONE, 1);
			put(Material.DIAMOND, 8);
			put(Material.DIAMOND_BLOCK, 75);
			put(Material.DRAGON_EGG, 500);
			put(Material.EMERALD, 4);
			put(Material.EXP_BOTTLE, 10);
			put(Material.GRAVEL, 1);
			put(Material.GLOWSTONE, 2);
			put(Material.LAPIS_BLOCK, 30);
			put(Material.OBSIDIAN, 5);
			put(Material.QUARTZ, 3);
			put(Material.QUARTZ_BLOCK, 25);
			put(Material.SPONGE, 10);
			put(Material.WOOL, 2); 
		}
	}; 
	
	public static final Material ProtectorBlock = Material.IRON_BLOCK; 
	public static final int MaxDurability = 1280;
	public static final int MaxDurabilityUntilExplosionsRequired = 640; 
	
	public static int GetDurability(Block block){
		Block targetBlock = block; 
		Block blockBelow = block.getRelative(BlockFace.DOWN);
		if(BlockListener.IsDoor(targetBlock.getType()) && BlockListener.IsDoor(blockBelow.getType())){
			targetBlock = blockBelow; 
		}
		
		LocationSerializable ls = new LocationSerializable(targetBlock);
    	Integer durability = Memory.Universe.getOrDefault(ls, 0);
    	return durability.intValue(); 
	}
	
	public static void SetDurability(Block block, int value){
		Block targetBlock = block; 
		Block blockBelow = block.getRelative(BlockFace.DOWN);
		if(BlockListener.IsDoor(targetBlock.getType()) && BlockListener.IsDoor(blockBelow.getType())){
			targetBlock = blockBelow; 
		}
		
		LocationSerializable ls = new LocationSerializable(targetBlock); 
		Memory.Universe.put(ls, value); 
	}
	
	public static void IncreaseDurability(Block block, int amount){
		int durability = GetDurability(block) + amount;
		if(durability > MaxDurability) durability = MaxDurability; 
		SetDurability(block, durability); 
	}
	
	public static void DecreaseDurability(Block block, int amount){
		int durability = GetDurability(block) - amount;
		if(durability < 0) durability = 0; 
		SetDurability(block, durability); 
	}
	
	// returns true if we really want to destroy the block 
	public static boolean BlockBroken(Block block, int power, Player player){		
		DecreaseDurability(block, power); 
		int durability = GetDurability(block);
		
		if(durability > 0)
			return false; 
		
		Material blockType = block.getType(); 
		
		// beds 
		if(blockType == Material.BED_BLOCK){
			Location bedLocation = block.getLocation(); 
			LocationSerializable ls = new LocationSerializable(bedLocation); 
			Memory.Beds.remove(ls); 
			return true; 
		}
		
		// protector blocks 
		if(blockType == ProtectorBlock || blockType == Material.REDSTONE_TORCH_ON || blockType == Material.REDSTONE_TORCH_OFF){		
			boolean removeProtections = false;
			Block protectorBlock = null; // the iron block 
			
			
			boolean IsProtectorBlock = blockType == ProtectorBlock; 
			if(IsProtectorBlock){
				Block relative = block.getRelative(BlockFace.UP);

				boolean matchingLocation = false; 
				ChunkSerializable cs = new ChunkSerializable(block.getLocation());
				ProtectionBlockData pbd = Memory.ProtectorBlocks.get(cs);
				if(pbd != null){
					LocationSerializable ls = new LocationSerializable(block.getLocation()); 
					matchingLocation = pbd.Location.equals(ls); 
				}
				
				if(relative.getType() == Material.REDSTONE_TORCH_ON || relative.getType() == Material.REDSTONE_TORCH_OFF || matchingLocation){
					protectorBlock = block; 					
					removeProtections = true; 
				}
			}

			boolean IsRedstoneTorch = blockType == Material.REDSTONE_TORCH_ON || blockType == Material.REDSTONE_TORCH_OFF; 
			if(IsRedstoneTorch){
				Block relative = block.getRelative(BlockFace.DOWN);
				
				boolean matchingLocation = false; 
				ChunkSerializable cs = new ChunkSerializable(relative.getLocation());
				ProtectionBlockData pbd = Memory.ProtectorBlocks.get(cs);
				if(pbd != null){
					LocationSerializable ls = new LocationSerializable(relative.getLocation()); 
					matchingLocation = pbd.Location.equals(ls); 
				}
				
				if(relative.getType() == ProtectorBlock || matchingLocation){
					protectorBlock = relative; 					
					removeProtections = true; 
				}
			}
			
			if(removeProtections){
				ChunkSerializable cs = new ChunkSerializable(protectorBlock.getLocation()); 
				Memory.ProtectorBlocks.remove(cs); 
				LightAPI.deleteLight(protectorBlock.getLocation(), true); 
				if(player != null)
					player.sendMessage(String.format("Removed protection rune!"));
				return true; 
			}
		}
		
		// door blocks 
		if(BlockListener.IsDoor(blockType)){
			LocationSerializable ls = new LocationSerializable(block.getLocation()); 
			Memory.Doors.remove(ls);

			Location top = block.getLocation(); 
			Block bottomBlock = top.getWorld().getBlockAt((int) top.getX(),(int) top.getY() - 1,(int) top.getZ());
			if(BlockListener.IsDoor(bottomBlock.getType())){
				LocationSerializable ls2 = new LocationSerializable(bottomBlock.getLocation()); 
				Memory.Doors.remove(ls2);
			}
		}
		
		return true;
	}
	
	public static void LoadFromDB() throws IOException, ClassNotFoundException {
		createDirectories(); 
		LoadWorldData();
		LoadBoardsData(); 
		LoadUserData(); 
		LoadBedData(); 
		LoadProtectorData(); 
		LoadDoorData(); 
		// PrintOutDB(); 
	}

	public static void BackupData(){
		
		File file = new File(Main.dataFolder + "/saves"); 
        File[] files = file.listFiles(); 
        
        Date date = new Date(System.currentTimeMillis()); 
        
        String backupFolderName = String.format("%s/backups/%sM%sD-%sH%S", Main.dataFolder, date.getMonth(), date.getDate(), date.getHours(), date.getMinutes()); 
        File backupFolder = new File(backupFolderName); 
    	if(!backupFolder.exists()){
    		backupFolder.mkdir();
		}
        
		try {
			
			// save backup
			for(File saveFile : files){
				File backupFile = new File(String.format("%s/%s", backupFolderName, saveFile.getName()));
				copyFile(saveFile, backupFile); 
			}
			
			// delete out of date backups 
			File backupDir = new File(Main.dataFolder + "/saves"); 
	        File[] backupDirs = file.listFiles(); 
	        for(File backupFile : backupDirs){
	        	if(!backupFile.isDirectory()) continue;  
	        	long hoursOld = (System.currentTimeMillis() - backupFile.lastModified()) / 1000l / 60l / 60l;
	        	if(hoursOld > 4){
	        		File[] backupFolderFiles = backupFile.listFiles();
	        		for(File backupFileInFolder : backupFolderFiles){
	        			backupFileInFolder.delete(); 
	        		}
	        		
	        		backupFile.delete(); 
	        	}
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
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
	public static void LoadProtectorData() throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(Main.dataFolder + "/saves/ProtectorBlocks.sav");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        ProtectorBlocks = ( HashMap<ChunkSerializable, ProtectionBlockData> ) in.readObject();
        in.close();
        fileIn.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void LoadBedData() throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(Main.dataFolder + "/saves/Beds.sav");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Beds = ( HashMap<LocationSerializable, UUID> ) in.readObject();
        in.close();
        fileIn.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void LoadDoorData() throws IOException, ClassNotFoundException{
		FileInputStream fileIn = new FileInputStream(Main.dataFolder + "/saves/Doors.sav");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Doors = ( HashMap<LocationSerializable, String> ) in.readObject();
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
		BackupData(); 
		SaveWorldData();
		SaveBoardData(); 
		SaveUserData(); 
		SaveBedData(); 
		SaveProtectorData(); 
		SaveDoorData(); 
	}
	
	public static void SaveWorldData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/Universe.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(Universe);
        out.close();
        fileOut.close();
	}
	
	public static void SaveProtectorData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/ProtectorBlocks.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(ProtectorBlocks);
        out.close();
        fileOut.close();
	}
	
	public static void SaveBedData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/Beds.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(Beds);
        out.close();
        fileOut.close();
	}
	
	public static void SaveDoorData() throws IOException{
		FileOutputStream fileOut = new FileOutputStream(Main.dataFolder + "/saves/Doors.sav");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(Doors);
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
    	
    	File dir_b = new File(Main.dataFolder + "/backups");
    	if(!dir_b.exists()){
    		dir_b.mkdir();
		}
    }
	
}
