package su.nightexpress.goldenenchants.manager.enchants.tool;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.goldenenchants.GoldenEnchants;
import su.nightexpress.goldenenchants.manager.enchants.IEnchantChanceTemplate;
import su.nightexpress.goldenenchants.manager.enchants.api.BlockEnchant;

public class EnchantSmelter extends IEnchantChanceTemplate implements BlockEnchant {
	
	private Map<Material, Material> smeltingTable;
	
	public EnchantSmelter(@NotNull GoldenEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		
		this.smeltingTable = new HashMap<>();
    	for (String sFrom : cfg.getSection("settings.smelting-table")) {
    		Material mFrom = Material.getMaterial(sFrom.toUpperCase());
    		if (mFrom == null) {
    			plugin.error("[Smelter] Invalid source material '" + sFrom + "' !");
    			continue;
    		}
    		String sTo = cfg.getString("settings.smelting-table." + sFrom, "");
    		Material mTo = Material.getMaterial(sTo.toUpperCase());
    		if (mTo == null) {
    			plugin.error("[Smelter] Invalid result material '" + sTo + "' !");
    			continue;
    		}
    		this.smeltingTable.put(mFrom, mTo);
    	}
	}
	
	@Override
	public boolean canEnchant(@NotNull ItemStack item) {
		Material mat = item.getType();
		return ITEM_PICKAXES.contains(mat) || ITEM_SHOVELS.contains(mat) || ITEM_AXES.contains(mat);
	}
	
	@Override
	public boolean conflictsWith(@Nullable Enchantment en) {
		return en == Enchantment.SILK_TOUCH;
	}

	@Override
	@NotNull
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.TOOL;
	}
	
	@Override
	public boolean isCursed() {
		return false;
	}
	
	@Override
	public boolean isTreasure() {
		return false;
	}

    public void smeltBlock(@NotNull Block b) {
    	Material result = this.smeltingTable.get(b.getType());
    	if (result == null) return;
    	
    	ItemStack item = new ItemStack(result);
    	for (ItemStack drop : b.getDrops()) {
    		if (drop.getType() == b.getType()) {
    			b.getDrops().remove(drop);
    			break;
    		}
    	}
    	b.getDrops().add(item);
    }
	
	@Override
	public void use(@NotNull ItemStack tool, @NotNull Player p, @NotNull BlockBreakEvent e,
			int lvl) {
		
		if (!this.checkTriggerChance(lvl)) return;
		
		Block b = e.getBlock();
		
		Material result = this.smeltingTable.get(b.getType());
    	if (result == null) return;
		
    	ItemStack item = new ItemStack(result);
    	
		e.setCancelled(true);
	    b.setType(Material.AIR);
	    
	    Location loc = LocUT.getCenter(b.getLocation(), false);
	    b.getWorld().dropItem(loc, item);
		b.getWorld().playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 0.7f, 0.7f);
		EffectUT.playEffect(loc, "FLAME", 0.2f, 0.2f, 0.2f, 0.03f, 30);
		
		p.getInventory().setItemInMainHand(plugin.getNMS().damageItem(tool, 1, p));
	}
}
