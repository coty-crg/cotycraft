package com.wanderingcorgi.minecraft;

public class RelationColor {

	public static String Neutral = "§f";
	public static String Enemy = "§c";
	public static String Ally = "§d"; 
	public static String Truce = "§e";

	public static String War = "§4";
	public static String Peaceful = "§6";
	public static String Self = "§a"; 
	
	public static String getRelColor(String relation){

		if(relation.equals(Relation.Self)) return Self; 
		if(relation.equals(Relation.Neutral)) return Neutral; 
		if(relation.equals(Relation.Enemy)) return Enemy; 
		if(relation.equals(Relation.Ally)) return Ally; 
		if(relation.equals(Relation.Truce)) return Truce; 
		if(relation.equals(Relation.War)) return War; 
		if(relation.equals(Relation.Peaceful)) return Peaceful; 
		
		return Neutral; 
	}
	
}