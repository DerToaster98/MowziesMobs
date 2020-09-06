package com.bobmowzie.mowziesmobs.server.property;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.client.particle.MMParticle;
import com.bobmowzie.mowziesmobs.client.particle.ParticleFactory;
import com.bobmowzie.mowziesmobs.server.entity.frostmaw.EntityFrozenController;
import com.bobmowzie.mowziesmobs.server.message.MessageAddFreezeProgress;
import com.bobmowzie.mowziesmobs.server.potion.MowziePotion;
import com.bobmowzie.mowziesmobs.server.potion.PotionHandler;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.ilexiconn.llibrary.server.entity.EntityProperties;
import net.ilexiconn.llibrary.server.nbt.NBTHandler;
import net.ilexiconn.llibrary.server.nbt.NBTProperty;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;

public class MowzieLivingProperties extends EntityProperties<EntityLivingBase> {
    @NBTProperty
    public float freezeProgress = 0;
    @NBTProperty
    public float frozenYaw;
    @NBTProperty
    public float frozenPitch;
    @NBTProperty
    public float frozenYawHead;
    @NBTProperty
    public float frozenRenderYawOffset;
    @NBTProperty
    public float frozenSwingProgress;
    @NBTProperty
    public float frozenLimbSwingAmount;
    @NBTProperty
    public boolean prevHasAI;

    // After taking freeze progress, this timer needs to reach zero before freeze progress starts to fade
    @NBTProperty
    public int freezeDecayDelay;
    public static int MAX_FREEZE_DECAY_DELAY = 10;

    public boolean prevFrozen = false;
    public EntityFrozenController frozenController;

    public float lastDamage = 0;

    public void addFreezeProgress(EntityLivingBase entity, float amount) {
        if (!entity.world.isRemote && !entity.isPotionActive(PotionHandler.FROZEN)) {
            freezeProgress += amount;
            freezeDecayDelay = MAX_FREEZE_DECAY_DELAY;
            MowziesMobs.NETWORK_WRAPPER.sendToDimension(new MessageAddFreezeProgress(entity, amount), entity.dimension);
        }
    }

    public void onFreeze(EntityLivingBase entity) {
        if (entity != null) {
            frozenController = new EntityFrozenController(entity.world);
            frozenController.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
            entity.world.spawnEntity(frozenController);
            frozenController.setRenderYawOffset(entity.renderYawOffset);
            frozenYaw = entity.rotationYaw;
            frozenPitch = entity.rotationPitch;
            frozenYawHead = entity.rotationYawHead;
            frozenLimbSwingAmount = entity.limbSwingAmount;
            frozenRenderYawOffset = entity.renderYawOffset;
            frozenSwingProgress = entity.swingProgress;
            entity.startRiding(frozenController, true);

            if (entity instanceof EntityLiving) prevHasAI = !((EntityLiving)entity).isAIDisabled();
            if (entity instanceof EntityLiving) ((EntityLiving)entity).setNoAI(true);

            if (entity.world.isRemote) {
                int particleCount = (int) (10 + 1 * entity.height * entity.width * entity.width);
                for (int i = 0; i < particleCount; i++) {
                    double snowX = entity.posX + entity.width * entity.getRNG().nextFloat() - entity.width / 2;
                    double snowZ = entity.posZ + entity.width * entity.getRNG().nextFloat() - entity.width / 2;
                    double snowY = entity.posY + entity.height * entity.getRNG().nextFloat();
                    Vec3d motion = new Vec3d(snowX - entity.posX, snowY - (entity.posY + entity.height / 2), snowZ - entity.posZ).normalize();
                    MMParticle.SNOWFLAKE.spawn(entity.world, snowX, snowY, snowZ, ParticleFactory.ParticleArgs.get().withData(0.1d * motion.x, 0.1d * motion.y, 0.1d * motion.z));
                }
            }
            entity.playSound(MMSounds.ENTITY_FROSTMAW_FROZEN_CRASH, 1, 1);
        }
    }

    public void onUnfreeze(EntityLivingBase entity) {
        if (entity != null && frozenController != null) {
            entity.dismountEntity(frozenController);
            entity.setPosition(frozenController.posX, frozenController.posY, frozenController.posZ);
            frozenController.setDead();
            entity.playSound(MMSounds.ENTITY_FROSTMAW_FROZEN_CRASH, 1, 0.5f);

            if (entity.world.isRemote) {
                int particleCount = (int) (10 + 1 * entity.height * entity.width * entity.width);
                for (int i = 0; i < particleCount; i++) {
                    double particleX = entity.posX + entity.width * entity.getRNG().nextFloat() - entity.width / 2;
                    double particleZ = entity.posZ + entity.width * entity.getRNG().nextFloat() - entity.width / 2;
                    double particleY = entity.posY + entity.height * entity.getRNG().nextFloat() + 0.3f;
                    entity.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, particleX, particleY, particleZ, 0, 0, 0, Block.getStateId(Blocks.ICE.getDefaultState()));
                }
            }
            if (entity instanceof EntityLiving && ((EntityLiving) entity).isAIDisabled() && prevHasAI) {
                ((EntityLiving) entity).setNoAI(false);
            }
        }
    }

    @Override
    public void init() {

    }

    @Override
    public String getID() {
        return "mm:living";
    }

    @Override
    public Class<EntityLivingBase> getEntityClass() {
        return EntityLivingBase.class;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTHandler.INSTANCE.saveNBTData(this, compound);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTHandler.INSTANCE.loadNBTData(this, compound);
    }
}
