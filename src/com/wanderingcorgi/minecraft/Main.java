package com.wanderingcorgi.minecraft;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

	public static String dataFolder; 
	public boolean AmSaving; 
	private DataBaseThread databaseThread; 
	
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage(RelationColor.Self + "[Starting 4craft plugin]");
		dataFolder = getDataFolder().toString(); 

		// setup event listener 
		BlockListener blockEvents = new BlockListener(this);
		
		// Load from DB 
		try{
			Memory.LoadFromDB(); 		
		} catch (IOException e){
			Bukkit.getConsoleSender().sendMessage(RelationColor.Enemy + "[4craft: Error loading from DB!]");	
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(RelationColor.Enemy + "[4craft: Error loading from DB!]");	
			e.printStackTrace();
		}
		
		// save database on loop / new thread 
		databaseThread = new DataBaseThread();
		databaseThread.start(); 
	}
	
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage(RelationColor.Self + "[Stopping 4craft plugin]");
		databaseThread.interrupt(); 
        try {
			Memory.SaveToDB();
			Bukkit.getConsoleSender().sendMessage(RelationColor.Self + "[4craft: Successfully saving DB!]");	
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(RelationColor.Enemy + "[4craft: Error saving DB!]");	
			e.printStackTrace();
		}
	}
	
	public class DataBaseThread extends Thread {
	    public void run() {
	        while(true) {
	            try {
					Thread.sleep(1000 * 5); // save once every two minutes! 
					AmSaving = true; 
			        Memory.SaveToDB();
			        AmSaving = false; 
				} catch (InterruptedException e) {
					Bukkit.getConsoleSender().sendMessage(RelationColor.Enemy + "[4craft: Error saving DB!]");	
					Bukkit.getConsoleSender().sendMessage(RelationColor.Self + "Shutdown autosave thread.. ");
					e.printStackTrace();
				} catch (Exception e){
					Bukkit.getConsoleSender().sendMessage(RelationColor.Enemy + "[4craft: Error saving DB!]");
					Bukkit.getConsoleSender().sendMessage(RelationColor.Self + "Uncaught exception saving database.. ");
					e.printStackTrace();
				}
	        }
	    }
	}
}
