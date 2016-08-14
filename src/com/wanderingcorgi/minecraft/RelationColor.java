package com.wanderingcorgi.minecraft;

import com.wanderingcorgi.minecraft.User.Relation;

public class RelationColor {

	// public static String Truce = "§e"; 
	// public static String War = "§4";
	// public static String Peaceful = "§6";
	public static String Neutral = "§f";
	public static String Faction = "§a"; 
	public static String Ally = "§d"; 
	public static String Enemy = "§c";
	
	public static String FromRelation(Relation relation){
		
		if(relation == Relation.Faction) return Faction; 
		if(relation == Relation.Ally) return Ally; 
		if(relation == Relation.Enemy) return Enemy; 
		
		return Neutral; 
	}
	
}