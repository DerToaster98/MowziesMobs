package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.creativetab.CreativeTabHandler;
import com.bobmowzie.mowziesmobs.server.entity.effects.EntityIceBreath;
import com.bobmowzie.mowziesmobs.server.property.MowziePlayerProperties;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Josh on 6/6/2017.
 */
public class ItemIceCrystal extends Item {
    public ItemIceCrystal() {
        setCreativeTab(CreativeTabHandler.INSTANCE.creativeTab);
        setTranslationKey("iceCrystal");
        setRegistryName("ice_crystal");
        setMaxDamage(ConfigHandler.TOOLS_AND_ABILITIES.ICE_CRYSTAL.durability);
        setMaxStackSize(1);
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 40;
//    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn.getHeldItemOffhand().getItem() != Items.SHIELD) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            MowziePlayerProperties property = EntityPropertiesHandler.INSTANCE.getProperties(playerIn, MowziePlayerProperties.class);
            if (stack.getItemDamage() + 20 < stack.getMaxDamage() || ConfigHandler.TOOLS_AND_ABILITIES.ICE_CRYSTAL.breakable) {
                if (!property.usingIceBreath) {
                    property.icebreath = new EntityIceBreath(worldIn, playerIn);
                    property.icebreath.setPositionAndRotation(playerIn.posX, playerIn.posY + playerIn.eyeHeight - 0.5f, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
                    if (!worldIn.isRemote) worldIn.spawnEntity(property.icebreath);
                    property.usingIceBreath = true;
                }
                stack.damageItem(20, playerIn);
                showDurabilityBar(playerIn.getHeldItem(handIn));
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
            } else {
                property.icebreath.setDead();
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (ConfigHandler.TOOLS_AND_ABILITIES.ICE_CRYSTAL.breakable) tooltip.remove(0);
        ItemHandler.addItemText(this, tooltip);
    }
}
