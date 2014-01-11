package icbm.sentry.turret;

import icbm.Reference;
import icbm.core.CreativeTabICBM;
import icbm.core.prefab.BlockICBM;
import icbm.sentry.ICBMSentry;
import icbm.sentry.damage.EntityTileDamagable;
import icbm.sentry.render.BlockRenderingHandler;
import icbm.sentry.turret.mount.TileRailGun;
import icbm.sentry.turret.sentries.TileEntityAAGun;
import icbm.sentry.turret.sentries.TileEntityGunTurret;
import icbm.sentry.turret.sentries.TileEntityLaserGun;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import universalelectricity.api.UniversalElectricity;
import calclavia.lib.multiblock.link.IBlockActivate;
import calclavia.lib.prefab.block.BlockAdvanced;
import calclavia.lib.prefab.tile.IRedstoneReceptor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block turret is a class used by all turrets. Each type of turret will have a different tile
 * entity.
 * 
 * @author Calclavia
 */
public class BlockTurret extends BlockICBM
{
	public enum TurretType
	{
		GUN(TileEntityGunTurret.class), RAILGUN(TileRailGun.class), AA(TileEntityAAGun.class),
		LASER(TileEntityLaserGun.class);

		public Class<? extends TileEntity> tileEntity;

		private TurretType(Class<? extends TileEntity> tile)
		{
			this.tileEntity = tile;
		}
	}

	public BlockTurret(int par1)
	{
		super(par1, "turret", UniversalElectricity.machine);
		this.setCreativeTab(CreativeTabICBM.INSTANCE);
		this.setHardness(100f);
		this.setResistance(50f);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		TileEntity ent = world.getBlockTileEntity(x, y, z);
		if (ent instanceof TileTurret)
		{
			EntityTileDamagable dEnt = ((TileTurret) ent).getDamageEntity();
			if (dEnt != null)
			{
				this.setBlockBounds(.2f, 0, .2f, .8f, .4f, .8f);
			}
			else
			{
				this.setBlockBounds(.2f, 0, .2f, .8f, .8f, .8f);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(Reference.PREFIX + "machine");
	}

	@Override
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		TileEntity ent = world.getBlockTileEntity(x, y, z);

		if (ent instanceof TileTurret)
		{
			Random random = new Random();
			((TileTurret) ent).setHealth(5 + random.nextInt(7), true);
			return true;
		}

		return false;
	}

	@Override
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX, float hitY, float hitZ)
	{
		/**
		 * Checks the TileEntity if it can activate. If not, then try to activate the turret
		 * platform below it.
		 */
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof IBlockActivate)
		{
			return ((IBlockActivate) tileEntity).onActivated(entityPlayer);
		}

		int id = world.getBlockId(x, y - 1, z);
		Block block = Block.blocksList[id];

		if (block instanceof BlockAdvanced)
		{
			return ((BlockAdvanced) block).onMachineActivated(world, x, y - 1, z, entityPlayer, side, hitX, hitY, hitZ);
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int side)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity instanceof TileTurret)
		{
			if (this.canBlockStay(world, x, y, z))
			{
				if (tileEntity instanceof IRedstoneReceptor)
				{
					if (world.isBlockIndirectlyGettingPowered(x, y, z))
					{
						((IRedstoneReceptor) tileEntity).onPowerOn();
					}
					else
					{
						((IRedstoneReceptor) tileEntity).onPowerOff();
					}
				}
			}
			else
			{
				if (tileEntity != null)
				{
					((TileTurret) tileEntity).destroy(false);
				}
			}
		}
	}

	@Override
	public TileEntity createTileEntity(World world, int meta)
	{
		if (meta < TurretType.values().length)
		{
			try
			{
				return TurretType.values()[meta].tileEntity.newInstance();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
	{
		return super.canPlaceBlockAt(world, x, y, z) && this.canBlockStay(world, x, y, z);
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z)
	{
		return world.getBlockId(x, y - 1, z) == ICBMSentry.blockPlatform.blockID;
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List list)
	{
		for (int i = 0; i < TurretType.values().length; i++)
		{
			list.add(new ItemStack(par1, 1, i));
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getRenderType()
	{
		return BlockRenderingHandler.ID;
	}
}
