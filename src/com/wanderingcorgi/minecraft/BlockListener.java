package com.wanderingcorgi.minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

	private final Main plugin;
	
	public BlockListener(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event){
		List<Block> blocks = event.getBlocks();
		for(Block block : blocks){
			boolean isTNT = block.getType() == Material.TNT; 
			boolean isRedstoneTorch = block.getType() == Material.REDSTONE_TORCH_OFF || block.getType() == Material.REDSTONE_TORCH_ON;
			boolean isEmeraldBlock = block.getType() == Material.EMERALD_BLOCK; 
			boolean isEnforced = Memory.GetDurability(block) > 0; 
			
			if(isTNT || isEnforced || isRedstoneTorch || isEmeraldBlock){
				event.setCancelled(true);
				return; 
			}
		}
	}
	
	private static final List<Material> Tools = Arrays.asList( Material.AIR,
			Material.WOOD_AXE, Material.WOOD_HOE, Material.WOOD_SPADE, Material.WOOD_SWORD, Material.WOOD_PICKAXE,
			Material.STONE_AXE, Material.STONE_HOE, Material.STONE_SPADE, Material.STONE_SWORD, Material.STONE_PICKAXE,
			Material.GOLD_AXE, Material.GOLD_HOE, Material.GOLD_SPADE, Material.GOLD_SWORD, Material.GOLD_PICKAXE,
			Material.IRON_AXE, Material.IRON_HOE, Material.IRON_SPADE, Material.IRON_SWORD, Material.IRON_PICKAXE,
			Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_SPADE, Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE); 
	
	public static final List<Material> Doors = Arrays.asList( 
			Material.WOOD_DOOR, Material.ACACIA_DOOR, Material.ACACIA_DOOR_ITEM, Material.BIRCH_DOOR, 
			Material.DARK_OAK_DOOR, Material.IRON_DOOR, Material.IRON_DOOR_BLOCK, Material.JUNGLE_DOOR,
			Material.SPRUCE_DOOR, Material.SPRUCE_DOOR, Material.TRAP_DOOR, Material.WOODEN_DOOR); 
	
	public static boolean IsDoor(Material input){
		for(Material mat : Doors){
			if(mat == input)
				return true; 
		}
		
		return false; 
	}
	
	@EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {

    	Player player = event.getPlayer(); 
        Action action = event.getAction();
        Block block = event.getClickedBlock(); 
        
        if (action == Action.LEFT_CLICK_BLOCK) {
        	ItemStack itemInHand = player.getItemInHand();
        	if(itemInHand == null || Tools.contains(itemInHand.getType()))
        		return; 
        	
        	int worth = Memory.MaterialValues.getOrDefault(itemInHand.getType(), 0); 
        	if(worth == 0)
        		return; 
        	
        	int oldAmount = itemInHand.getAmount();
        	int newAmount = oldAmount - 1; 
        	itemInHand.setAmount(newAmount);
        	
        	if(newAmount <= 0){
        		player.setItemInHand(null);
        	}
        	
        	Memory.IncreaseDurability(block, worth);
        	player.sendMessage(String.format("§7[durability: %s (+%s)]", Memory.GetDurability(block), worth));
        	event.setCancelled(true);
        }
        
        if(action == Action.RIGHT_CLICK_BLOCK){
        	if(!IsDoor(block.getType()))
        		return;
        	
        	LocationSerializable ls = new LocationSerializable(block.getLocation()); 
        	String doorBoardOwner = Memory.Doors.get(ls);
        	
        	if(doorBoardOwner == null){
        		ls = new LocationSerializable(block.getLocation().getWorld(), block.getLocation().getX(), block.getLocation().getY() - 1, block.getLocation().getZ() ); 
            	doorBoardOwner = Memory.Doors.get(ls);
            	
            	if(doorBoardOwner == null)
            		return; 
        	}
        		
        	User user = User.FromUUID(player.getUniqueId()); 
        	if(user.BoardName != doorBoardOwner){
        		player.sendMessage(String.format("/%s/ owns this door! You must destroy it to pass.", doorBoardOwner));
        		event.setCancelled(true);
        		return; 
        	}
        }
    }
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event){
		Block block = event.getBlock();
		
		int durabilityLeft = Memory.GetDurability(block);
		
		if(durabilityLeft > Memory.MaxDurabilityUntilExplosionsRequired){
			int tntOnlyDurabilityRemaining = durabilityLeft - Memory.MaxDurabilityUntilExplosionsRequired; 
			event.getPlayer().sendMessage(String.format("§c[Explosions required for another %s durability]", tntOnlyDurabilityRemaining));
			event.setCancelled(true);
			return; 
		}

		if(durabilityLeft > 1)
			event.getPlayer().sendMessage(String.format("§7[durability]: %s", durabilityLeft));
		
		boolean actuallyBreak = Memory.BlockBroken(block, 1);
		if(actuallyBreak) 
			return; 
		
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockExplodeEvent(BlockExplodeEvent event){
		List<Block> originalList = new ArrayList<Block>(event.blockList()); 
		for(Block block : originalList){

			boolean actuallyBreak = Memory.BlockBroken(block, 10);
			if(actuallyBreak)
				continue; 
			
			event.blockList().remove(block); 
		}
	}
	
	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent event){
		boolean isTNT = event.getEntityType() == EntityType.PRIMED_TNT || event.getEntityType() == EntityType.MINECART_TNT; 
		int damage = isTNT ? 10 : 5; 
		List<Block> originalList = new ArrayList<Block>(event.blockList()); 
		for(Block block : originalList){

			boolean actuallyBreak = Memory.BlockBroken(block, damage);
			if(actuallyBreak)
				continue; 
			
			event.blockList().remove(block); 
		}
	}
	
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent event){
		Block block = event.getBlock(); 

		ChunkSerializable thisChunk = new ChunkSerializable(block.getLocation()); 
		if(Memory.ProtectorBlocks.containsKey(thisChunk)){
			Date date =  Memory.ProtectorBlocks.get(thisChunk); 
			event.setCancelled(true);
			
			Player player = event.getPlayer();
			if(player != null)
				player.sendMessage(String.format("Cannot place blocks in this chunk until protector is destroyed! This chunk has been protected since: %s", date.toString()));
			
			return; 
		}
		
		// handle claims 
		if(block.getType() == Material.REDSTONE_TORCH_ON && block.getType() == Material.REDSTONE_TORCH_OFF){
			Block relative = block.getRelative(BlockFace.DOWN); 
			if(relative.getType() != Material.EMERALD_BLOCK)
				return; 
			
			ChunkSerializable ls = new ChunkSerializable(relative.getLocation()); 
			Date date = new Date(System.currentTimeMillis()); 
			Memory.ProtectorBlocks.put(ls, date);
			return; 
		} 
		
		// handle door claims 
		if(IsDoor(block.getType())){
			Player player = event.getPlayer(); 
			User user = User.FromUUID(player.getUniqueId()); 
			if(!user.HasBoard()) return; 
			LocationSerializable ls = new LocationSerializable(block.getLocation()); 
			Memory.Doors.put(ls, user.BoardName); 
		}
		
	}
}
