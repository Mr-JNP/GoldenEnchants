package su.nightexpress.goldenenchants.nms;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import net.minecraft.server.v1_15_R1.BlockFluids;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.IBlockData;
import net.minecraft.server.v1_15_R1.Material;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.VoxelShapeCollision;
import net.minecraft.server.v1_15_R1.World;

public class V1_15_R1 implements EnchantNMS {

	@Override
	public void handleFlameWalker(@NotNull LivingEntity entity1, @NotNull Location loc, int level) {
		EntityLiving entity = ((CraftLivingEntity)entity1).getHandle();
		BlockPosition pos = new BlockPosition(loc.getX(), loc.getY(), loc.getZ());
		World world = ((CraftWorld)entity1.getWorld()).getHandle();
		
		IBlockData bStone = Blocks.COBBLESTONE.getBlockData();
		float rad = Math.min(16, 2 + level);
		
		BlockPosition.MutableBlockPosition posMut = new BlockPosition.MutableBlockPosition();
        for (BlockPosition bNear : BlockPosition.a(pos.a(-rad, -1.0, -rad), pos.a(rad, -1.0, rad))) {
            
            if (!bNear.a(entity.getPositionVector(), rad)) continue;
            posMut.d(bNear.getX(), bNear.getY() + 1, bNear.getZ());
            
            IBlockData bLavaUp = world.getType(posMut);
            IBlockData bLava = world.getType(bNear);
            
            if (!bLavaUp.isAir()) continue;
            if (bLava.getMaterial() != Material.LAVA) continue;
            if (bLava.get(BlockFluids.LEVEL) != 0) continue;
            if (!bStone.canPlace(world, bNear)) continue;
            if (!world.a(bStone, bNear, VoxelShapeCollision.a())) continue;
            if (!CraftEventFactory.handleBlockFormEvent(world, bNear, bStone, entity)) continue;
            
            world.getBlockTickList().a(bNear, Blocks.COBBLESTONE, MathHelper.nextInt(entity.getRandom(), 60, 120));
        }
	}
}
