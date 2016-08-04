package com.wanderingcorgi.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

	private final Main plugin;
	
	public BlockListener(Main plugin){
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
        	
        	Player player = event.getPlayer(); 
        	
        	ItemStack itemInHand = player.getItemInHand();
        	if(itemInHand == null || itemInHand.getType() == Material.AIR)
        		return; 
        	
        	int oldAmount = itemInHand.getAmount();
        	int newAmount = oldAmount - 1; 
        	itemInHand.setAmount(newAmount);
        	
        	if(newAmount <= 0){
        		player.setItemInHand(null);
        	}

        	Block block = event.getClickedBlock(); 
        	Memory.IncreaseDurability(block, 1);
        }
    }
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event){
		Block block = event.getBlock();
		
		event.getPlayer().sendMessage("durability: " + Memory.GetDurability(block));
		
		boolean actuallyBreak = Memory.BlockBroken(block, 1);
		if(actuallyBreak) return; 
		
		event.setCancelled(true);
	}
	
}
