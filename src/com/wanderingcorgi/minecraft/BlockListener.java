package com.wanderingcorgi.minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import ru.beykerykt.lightapi.LightAPI;

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
			boolean isEmeraldBlock = block.getType() == Memory.ProtectorBlock; 
			boolean isEnforced = Memory.GetDurability(block) > 0; 
			
			if(isTNT || isEnforced || isRedstoneTorch || isEmeraldBlock){
				event.setCancelled(true);
				return; 
			}
		}
		
		for(Block block : blocks){
			Memory.Universe.remove(block); 
		}
	}
	
	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event){
		List<Block> blocks = event.getBlocks();
		for(Block block : blocks){
			boolean isTNT = block.getType() == Material.TNT; 
			boolean isRedstoneTorch = block.getType() == Material.REDSTONE_TORCH_OFF || block.getType() == Material.REDSTONE_TORCH_ON;
			boolean isEmeraldBlock = block.getType() == Memory.ProtectorBlock; 
			boolean isEnforced = Memory.GetDurability(block) > 0; 
			
			if(isTNT || isEnforced || isRedstoneTorch || isEmeraldBlock){
				event.setCancelled(true);
				return; 
			}
		}
		
		for(Block block : blocks){
			Memory.Universe.remove(block); 
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
			Material.SPRUCE_DOOR, Material.SPRUCE_DOOR, Material.TRAP_DOOR, Material.WOODEN_DOOR, 
			Material.IRON_TRAPDOOR, Material.FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.BIRCH_FENCE_GATE,
			Material.DARK_OAK_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.SPRUCE_FENCE_GATE); 
	
	/*private static final List<Material> DurabilityBlacklist = Arrays.asList(Material.AIR,
			Material.ANVIL, Material.LEAVES, Material.LEAVES_2, Material.SAND,
			Material.VINE, Material.DOUBLE_PLANT, Material.GRASS, Material.LONG_GRASS, 
			Material.SOUL_SAND, Material.REDSTONE, Material.STONE_BUTTON, Material.STONE_BUTTON,
			Material.REDSTONE_COMPARATOR, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
			Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.REDSTONE_WIRE, 
			Material.FLOWER_POT, Material.FLOWER_POT_ITEM, Material.YELLOW_FLOWER, Material.TNT, 
			Material.PAINTING, Material.TORCH, Material.LADDER, Material.WATER_LILY, Material.SNOW,
			Material.SNOW_BLOCK, Material.BED, Material.BED_BLOCK, Material.RAILS, Material.ACTIVATOR_RAIL,
			Material.DETECTOR_RAIL, Material.POWERED_RAIL, Material.PORTAL, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
			Material.ARMOR_STAND, Material.CAKE, Material.CAKE_BLOCK, Material.CARROT, Material.WHEAT, Material.SUGAR, 
			Material.SUGAR_CANE, Material.SUGAR_CANE_BLOCK, Material.POTATO, Material.GRAVEL, 
			Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.RED_ROSE, Material.SAPLING, Material.DEAD_BUSH,
			Material.PUMPKIN_STEM, Material.MELON_STEM, Material.NETHER_WARTS);*/ 
	
	public static boolean IsDoor(Material input){
		for(Material mat : Doors){
			if(mat == input)
				return true; 
		}
		
		return false; 
	}

	@EventHandler
	public void OnBlockFromToEvent(BlockFromToEvent event){
		Block targetBlock = event.getToBlock();
		int durability = Memory.GetDurability(targetBlock); 
		if(durability > 0 || targetBlock.getType() == Material.REDSTONE_TORCH_OFF || targetBlock.getType() == Material.REDSTONE_TORCH_ON){
			event.setCancelled(true);
			return; 
		}
	}
	
	@EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {

        Player player = event.getPlayer(); 
        if(player == null)
        	return; 
        
    	User user = User.FromUUID(player.getUniqueId());
    	if(user == null)
    		return; 
    	
    	Block block = event.getClickedBlock();
    	Action action = event.getAction();
    	
        if (action == Action.LEFT_CLICK_BLOCK) {
        	ItemStack itemInHand = player.getItemInHand();
        	if(itemInHand == null || Tools.contains(itemInHand.getType()) || block.getType() == Material.AIR)
        		return; 
        	
        	int worth = Memory.MaterialValues.getOrDefault(itemInHand.getType(), 0); 
        	if(worth == 0)
        		return; 
        	
        	if(block.getType() == Material.PISTON_EXTENSION || block.getType() == Material.PISTON_MOVING_PIECE)
        		return;
        	
        	if(!user.ReinforceMode){
            	player.sendMessage(String.format("§7[durability: %s] (+%s) To reinforce further, use /b reinforce and left click this block with a special item in hand.", Memory.GetDurability(block), worth));
        		return; 
        	}
        	
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
        		
        	if(user == null || user.BoardName == null || !user.BoardName.equals(doorBoardOwner)){
        		player.sendMessage(String.format("/%s/ owns this door! You must destroy it to pass.", doorBoardOwner));
        		event.setCancelled(true);
        		return; 
        	}
        }
    }
	
	@EventHandler
	public void OnBlockPhysicsEvent(BlockPhysicsEvent event){
		Block block = event.getBlock();
		Memory.Universe.remove(block); 
	}
	
	@EventHandler
	public void onFireBurnEvent(BlockBurnEvent event){
		Block block = event.getBlock();
		
		int durability = Memory.GetDurability(block); 
		if(durability > Memory.MaxDurabilityUntilExplosionsRequired){
			event.setCancelled(true);
			return; 
		}
		
		boolean actuallyBreak = Memory.BlockBroken(block, 1);
		if(actuallyBreak) 
			return; 
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event){
		// Bukkit.getConsoleSender().sendMessage("Broke block at " + event.getBlock().getLocation().toString());
		
		Block block = event.getBlock();
		int durabilityLeft = Memory.GetDurability(block);
		
		if(durabilityLeft > Memory.MaxDurabilityUntilExplosionsRequired){
			int tntOnlyDurabilityRemaining = durabilityLeft - Memory.MaxDurabilityUntilExplosionsRequired; 
			event.getPlayer().sendMessage(String.format("§c[Explosions required for another %s durability]", tntOnlyDurabilityRemaining));
			event.setCancelled(true);
			return; 
		}

		if(durabilityLeft > 1)
			event.getPlayer().sendMessage(String.format("§7[durability: %s]", durabilityLeft - 1));
		
		boolean actuallyBreak = Memory.BlockBroken(block, 1);
		if(actuallyBreak) 
			return; 
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent event){
		boolean isTNT = event.getEntityType() == EntityType.PRIMED_TNT || event.getEntityType() == EntityType.MINECART_TNT; 
		int damage = isTNT ? 10 : 5; 
		
		Block blockCenter = event.getLocation().getWorld().getBlockAt(event.getLocation()); 
		Block bottom = blockCenter.getRelative(BlockFace.DOWN); 
		Block top = blockCenter.getRelative(BlockFace.UP); 
		List<Block> blockList = Arrays.asList(
				blockCenter,
				blockCenter.getRelative(BlockFace.UP),
				blockCenter.getRelative(BlockFace.DOWN),
				blockCenter.getRelative(BlockFace.EAST),
				blockCenter.getRelative(BlockFace.NORTH),
				blockCenter.getRelative(BlockFace.NORTH_EAST),
				blockCenter.getRelative(BlockFace.NORTH_WEST),
				blockCenter.getRelative(BlockFace.WEST),
				blockCenter.getRelative(BlockFace.SOUTH),
				blockCenter.getRelative(BlockFace.SOUTH_EAST),
				blockCenter.getRelative(BlockFace.SOUTH_WEST),
				
				bottom,
				bottom.getRelative(BlockFace.EAST),
				bottom.getRelative(BlockFace.NORTH),
				bottom.getRelative(BlockFace.NORTH_EAST),
				bottom.getRelative(BlockFace.NORTH_WEST),
				bottom.getRelative(BlockFace.WEST),
				bottom.getRelative(BlockFace.SOUTH),
				bottom.getRelative(BlockFace.SOUTH_EAST),
				bottom.getRelative(BlockFace.SOUTH_WEST),
				
				top,
				top.getRelative(BlockFace.EAST),
				top.getRelative(BlockFace.NORTH),
				top.getRelative(BlockFace.NORTH_EAST),
				top.getRelative(BlockFace.NORTH_WEST),
				top.getRelative(BlockFace.WEST),
				top.getRelative(BlockFace.SOUTH),
				top.getRelative(BlockFace.SOUTH_EAST),
				top.getRelative(BlockFace.SOUTH_WEST)
				);
		
		for(Block block : blockList){
			if(event.blockList().contains( block )) continue;
			Memory.BlockBroken(block, damage);
		}
		
		List<Block> originalList = new ArrayList<Block>(event.blockList()); 
		for(Block block : originalList){
			boolean actuallyBreak = Memory.BlockBroken(block, damage);
			if(actuallyBreak)
				continue; 
			
			event.blockList().remove(block); 
		}
	}
	
	@EventHandler
	public void OnRedstonePower(BlockRedstoneEvent event){
		Block block = event.getBlock(); 
		boolean isDoor = IsDoor(block.getType()); 
		if(isDoor){
			event.setNewCurrent(0);
			return; 
		}
	}
	
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent event){
		Block block = event.getBlock(); 
		Player player = event.getPlayer();
		if(player == null)
			return; 

		User user = User.FromUUID(player.getUniqueId()); 
		Memory.Universe.remove(new LocationSerializable(block.getLocation())); // revert dura to 0 
		
		ChunkSerializable thisChunk = new ChunkSerializable(block.getLocation()); 
		if(Memory.ProtectorBlocks.containsKey(thisChunk)){
			String ownerBoardName =  Memory.ProtectorBlocks.get(thisChunk); 
			if(user.BoardName == null || !user.BoardName.equals(ownerBoardName)){
				String relationColor = RelationColor.FromRelation(user.GetRelation(ownerBoardName)); 
				player.sendMessage(String.format("Cannot place blocks in this chunk until protector is destroyed! Chunk protected by: %s/%s/", relationColor, ownerBoardName));
				event.setCancelled(true);
				return; 
			}
		}
		
		// handle claims 
		if(block.getType() == Material.REDSTONE_TORCH_ON || block.getType() == Material.REDSTONE_TORCH_OFF){
			Block relative = block.getRelative(BlockFace.DOWN); 
			if(relative.getType() != Memory.ProtectorBlock)
				return; 
			
			if(!user.HasBoard()){
				player.sendMessage("You need to join a /board/ before you can create this");
				event.setCancelled(true);
				return; 
			}
			
			ChunkSerializable ls = new ChunkSerializable(relative.getLocation()); 
			
			boolean exists = Memory.ProtectorBlocks.containsKey(ls);
			if(exists){
				player.sendMessage("There is already a protector rune in this chunk!");
				event.setCancelled(true);
				return;
			}
			
			player.sendMessage("Protector rune has been activated!");
			Memory.ProtectorBlocks.put(ls, user.BoardName);
			LightAPI.createLight(relative.getLocation(), 15, true); 
			return; 
		} 
		
		// handle claims if emerald is placed under redstone torch 
		if(block.getType() == Memory.ProtectorBlock){
			Block relative = block.getRelative(BlockFace.UP); 
			if(relative.getType() != Material.REDSTONE_TORCH_ON && relative.getType() != Material.REDSTONE_TORCH_OFF)
				return;
			
			if(!user.HasBoard()){
				player.sendMessage("You need to join a /board/ before you can create this");
				event.setCancelled(true);
				return; 
			}
			
			ChunkSerializable ls = new ChunkSerializable(block.getLocation()); 
			
			boolean exists = Memory.ProtectorBlocks.containsKey(ls);
			if(exists){
				player.sendMessage("There is already a protector rune in this chunk!");
				event.setCancelled(true);
				return;
			}

			player.sendMessage("Protector rune has been activated!");
			Memory.ProtectorBlocks.put(ls, user.BoardName);
			LightAPI.createLight(relative.getLocation(), 15, true); 
			return; 
		}
		
		// handle door claims 
		if(IsDoor(block.getType())){
			if(!user.HasBoard()) return; 
			LocationSerializable ls = new LocationSerializable(block.getLocation()); 
			Memory.Doors.put(ls, user.BoardName); 
		}
		
	}
}
