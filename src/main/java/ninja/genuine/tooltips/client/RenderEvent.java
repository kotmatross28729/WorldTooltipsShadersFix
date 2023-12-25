package ninja.genuine.tooltips.client;

import java.util.List;
import java.util.Objects;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import ninja.genuine.tooltips.WorldTooltips;
import ninja.genuine.tooltips.system.Tooltip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RenderEvent {
//TODO fix World Tooltips.max Distance is not used, which is why it is impossible to change the render distance for tooltips, the current value seems normal, but still
	private static Minecraft mc;
    //public static final Logger logger = LogManager.getLogger();
	private Tooltip cache;
    double distance;
	public RenderEvent() {}
	public void post() {
		mc = Minecraft.getMinecraft();
	}

	public void syncColors() {
		if (!Objects.isNull(cache))
			cache.syncSettings();
	}

	@SubscribeEvent
	public void render(final RenderWorldLastEvent event) {
        if (mc != null && mc.theWorld != null && mc.thePlayer != null && mc.objectMouseOver != null) {

        if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            distance = mc.thePlayer.getDistance(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
        } else if (mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            distance = mc.thePlayer.getDistanceToEntity(mc.objectMouseOver.entityHit);
        }
        EntityItem entity = getEntityItem(distance, mc.thePlayer, event.partialTicks);
        if (!Objects.isNull(entity)) {
            if (mc.objectMouseOver != null) {
                if (Objects.isNull(cache) || cache.getEntity() != entity)
                    cache = new Tooltip(Minecraft.getMinecraft().thePlayer, entity);
                cache.renderTooltip3D(mc, event.partialTicks);
            }
        }
    }
}

	@SubscribeEvent
	public void render(final RenderGameOverlayEvent.Post event) {
		// TODO Let's make it a choice to do 2D or 3D tooltips.
		//      Just need to make a nice anchoring gui first.
		// renderer.renderTooltip2D(mc, item, generateTooltip(mc, mc.player, item.getEntityItem()), event.getPartialTicks());
	}

    public static EntityItem getEntityItem(EntityPlayer player, Vec3 vec31, Vec3 vec3) {
        float f1 = 1.0F;
        double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
        List list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.boundingBox.addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f1, (double)f1, (double)f1));

        Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
        double d1 = d0;

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            if (mc.objectMouseOver != null) d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
        }

        double d2 = d1;
        for (int i = 0; i < list.size(); ++i) {
            Entity entity = (Entity)list.get(i);
            if (entity instanceof EntityItem) {
                float f2 = entity.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity.boundingBox.expand((double)f2, (double)f2, (double)f2);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (0.0D < d2 || d2 == 0.0D) return (EntityItem) entity;
                }
                else if (movingobjectposition != null) return (EntityItem) entity;
            }
        }
        return null;
    }
/**   public static EntityItem getEntityItem(double distance, EntityPlayer player, float partialTicks) {
        //Vec3 vec31 = player.getLook(1.0F);
        //Vec3 vec3 = player.getPosition(1.0F);
        Vec3 vec31 = player.getLook(partialTicks);
        Vec3 vec3 = player.getPosition(partialTicks);
        EntityItem item = getEntityItem(player, vec31, vec3);

        if (item != null && player.getDistanceToEntity(item) < distance) return item;

        return null;

    }*/
public static EntityItem getEntityItem(double distance, EntityPlayer player, float partialTicks) {
    //Vec3 vec31 = player.getLook(1.0F);
    //Vec3 vec3 = player.getPosition(1.0F);
    Vec3 vec31 = player.getLook(partialTicks);
    Vec3 vec3 = player.getPosition(partialTicks);
    EntityItem item = getEntityItem(player, vec31, vec3);

    if (item != null && player.getDistanceToEntity(item) < distance) return item;

    return null;

}
	@SuppressWarnings("unchecked")
	public static EntityItem getMouseOver(Minecraft mc, float partialTicks) {
		EntityLivingBase viewer = mc.renderViewEntity;
		mc.mcProfiler.startSection("world-tooltips");
		double distanceLook = WorldTooltips.maxDistance;
		Vec3 eyes = viewer.getPosition(partialTicks);
		Vec3 look = viewer.getLook(partialTicks);
		Vec3 eyesLook = eyes.addVector(look.xCoord * distanceLook, look.yCoord * distanceLook, look.zCoord * distanceLook);
		float distanceMax = 1;
		List<EntityItem> entityList = mc.theWorld.getEntitiesWithinAABB(EntityItem.class,
				viewer.boundingBox.addCoord(look.xCoord * distanceLook, look.yCoord * distanceLook, look.zCoord * distanceLook).expand(distanceMax, distanceMax, distanceMax));
		double difference = 0;
		EntityItem target = null;
		for (int i = 0; i < entityList.size(); i++) {
			EntityItem entity = entityList.get(i);
			if (Objects.isNull(entity) || Objects.isNull(entity.boundingBox))
				continue;
			float boundSize = 0.15F;
			AxisAlignedBB aabb1 = entity.boundingBox;
			AxisAlignedBB aabb2 = AxisAlignedBB.getBoundingBox(aabb1.minX, aabb1.minY, aabb1.minZ, aabb1.maxX, aabb1.maxY, aabb1.maxZ);
			AxisAlignedBB expandedAABB = aabb2.offset(0, 0.25, 0).expand(0.15, 0.1, 0.15).expand(boundSize, boundSize, boundSize);
			MovingObjectPosition objectInVector = expandedAABB.calculateIntercept(eyes, eyesLook);
			if (expandedAABB.isVecInside(eyes)) {
				if (0.0D <= difference) {
					target = entity;
					difference = 0;
				}
			} else if (objectInVector != null) {
				final double distance = eyes.distanceTo(objectInVector.hitVec);
				if (distance < difference || difference == 0.0D) {
					target = entity;
					difference = distance;
				}
			}
		}
		mc.mcProfiler.endSection();
		return target;
	}
}
