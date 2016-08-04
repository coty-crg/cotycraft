package com.wanderingcorgi.minecraft;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Board implements Serializable { 

	private static final long serialVersionUID = -8233979960252438515L;
	
	public String Name;
	public Location Home; 
	public List<Player> Members; 
	public boolean Open; 
	
	public Board(){
		Name = ""; 
		Home = null; 
		Members = new ArrayList<Player>(); 
		Open = false; 
	}
	
	public Board(String name){
		Name = name; 
		Home = null; 
		Members = new ArrayList<Player>(); 
		Open = false; 
	}
	
	
}
