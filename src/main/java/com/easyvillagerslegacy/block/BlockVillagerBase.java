package com.easyvillagerslegacy.block;

import com.easyvillagerslegacy.EasyVillagersLegacy;
import com.easyvillagerslegacy.item.ItemVillager;
import com.easyvillagerslegacy.tileentity.TileEntityVillagerBase;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class for all villager machine blocks.
 * Handles common logic: placing/removing villagers, rotation, dropping inventory,
 * and the custom display case rendering with per-block accent textures.
 */
public abstract class BlockVillagerBase extends BlockContainer {

    // Set from client-side registration (BlockRendererVillager constructor)
    public static int renderId = 0;

    // Shared textures (registered once, used by all blocks)
    @SideOnly(Side.CLIENT)
    protected static IIcon iconFrame;   // farm.png - teal frame
    @SideOnly(Side.CLIENT)
    protected static IIcon iconGlass;   // vanilla glass
    @SideOnly(Side.CLIENT)
    protected static IIcon iconIron;    // vanilla iron_block

    // Per-block accent textures (front and back floor halves)
    @SideOnly(Side.CLIENT)
    protected IIcon iconAccentFront;
    @SideOnly(Side.CLIENT)
    protected IIcon iconAccentBack;

    /** The vanilla texture name for the front accent (e.g., "iron_block", "dirt") */
    protected final String accentFrontName;
    /** The vanilla texture name for the back accent */
    protected final String accentBackName;

    protected BlockVillagerBase(String name, String accentFront, String accentBack) {
        super(Material.wood);
        setBlockName(EasyVillagersLegacy.MOD_ID + "." + name);
        setBlockTextureName(EasyVillagersLegacy.MOD_ID + ":" + name);
        setCreativeTab(CreativeTabs.tabRedstone);
        setHardness(2.5f);
        setResistance(5.0f);
        setStepSound(soundTypeWood);
        setLightOpacity(0);
        this.accentFrontName = accentFront;
        this.accentBackName = accentBack;
    }

    // ========================
    // Rendering
    // ========================

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderBlockPass() {
        return 0; // Cutout pass (same as glass)
    }

    @Override
    public boolean canRenderInPass(int pass) {
        return pass == 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        // Shared textures - re-register every time so icons stay valid across atlas rebuilds
        iconFrame = reg.registerIcon(EasyVillagersLegacy.MOD_ID + ":farm");
        iconGlass = reg.registerIcon("minecraft:glass");
        iconIron = reg.registerIcon("minecraft:iron_block");

        // Per-block accent textures
        iconAccentFront = reg.registerIcon(accentFrontName);
        iconAccentBack = reg.registerIcon(accentBackName);
    }

    // Accessors for the renderer
    @SideOnly(Side.CLIENT)
    public IIcon getFrameIcon() { return iconFrame; }
    @SideOnly(Side.CLIENT)
    public IIcon getGlassIcon() { return iconGlass; }
    @SideOnly(Side.CLIENT)
    public IIcon getIronIcon() { return iconIron; }
    @SideOnly(Side.CLIENT)
    public IIcon getAccentFrontIcon() { return iconAccentFront; }
    @SideOnly(Side.CLIENT)
    public IIcon getAccentBackIcon() { return iconAccentBack; }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Fallback for particles and item rendering contexts
        return iconIron != null ? iconIron : super.getIcon(side, meta);
    }

    // ========================
    // Block interactions
    // ========================

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                     EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityVillagerBase)) return false;
        TileEntityVillagerBase tile = (TileEntityVillagerBase) te;

        ItemStack held = player.getHeldItem();

        // Sneak + right-click with empty hand: remove villager
        if (player.isSneaking() && held == null) {
            return handleRemoveVillager(world, x, y, z, player, tile);
        }

        // Right-click with villager item: place villager, or open GUI if slot is full
        if (ItemVillager.isVillagerItem(held)) {
            if (handlePlaceVillager(world, x, y, z, player, tile, held)) {
                return true;
            }
        }

        // Delegate to subclass for block-specific behavior (opens GUI as fallback)
        return handleBlockActivated(world, x, y, z, player, tile, held);
    }

    protected boolean handlePlaceVillager(World world, int x, int y, int z,
                                           EntityPlayer player, TileEntityVillagerBase tile, ItemStack held) {
        if (tile.hasVillager(0)) {
            return false;
        }

        tile.setVillagerFromItem(0, held);

        if (!player.capabilities.isCreativeMode) {
            held.stackSize--;
            if (held.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        }

        tile.markDirty();
        tile.syncToClient();
        return true;
    }

    protected boolean handleRemoveVillager(World world, int x, int y, int z,
                                            EntityPlayer player, TileEntityVillagerBase tile) {
        if (!tile.hasVillager(0)) {
            return false;
        }

        // Create item with full villager data (preserves profession, trades, etc.)
        ItemStack villagerItem = tile.createVillagerItem(0);

        tile.removeVillager(0);
        tile.markDirty();
        tile.syncToClient();

        if (!player.inventory.addItemStackToInventory(villagerItem)) {
            EntityItem entityItem = new EntityItem(world, x + 0.5, y + 1.0, z + 0.5, villagerItem);
            world.spawnEntityInWorld(entityItem);
        }

        return true;
    }

    protected abstract boolean handleBlockActivated(World world, int x, int y, int z,
                                                     EntityPlayer player, TileEntityVillagerBase tile,
                                                     ItemStack heldItem);

    /**
     * Returns an offset added to the facing metadata on placement.
     * Override in subclasses to adjust default orientation.
     * 0 = no offset, 1 = 90° CW, 2 = 180°, 3 = 270°.
     */
    protected int getFacingOffset() {
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        facing = (facing + getFacingOffset()) & 3;
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityVillagerBase) {
            TileEntityVillagerBase tile = (TileEntityVillagerBase) te;

            for (int i = 0; i < tile.getVillagerSlotCount(); i++) {
                if (tile.hasVillager(i)) {
                    ItemStack villagerItem = tile.createVillagerItem(i);
                    if (villagerItem != null) {
                        spawnItemDrop(world, x, y, z, villagerItem);
                    }
                }
            }

            for (int i = 0; i < tile.getSizeInventory(); i++) {
                ItemStack stack = tile.getStackInSlot(i);
                if (stack != null) {
                    spawnItemDrop(world, x, y, z, stack);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    private void spawnItemDrop(World world, int x, int y, int z, ItemStack stack) {
        EntityItem entity = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
        entity.motionX = world.rand.nextGaussian() * 0.05;
        entity.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
        entity.motionZ = world.rand.nextGaussian() * 0.05;
        world.spawnEntityInWorld(entity);
    }
}
