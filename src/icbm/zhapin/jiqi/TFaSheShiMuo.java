package icbm.zhapin.jiqi;

import icbm.api.IMissile;
import icbm.api.ITier;
import icbm.api.LauncherType;
import icbm.core.ZhuYaoICBM;
import icbm.zhapin.ZhuYaoZhaPin;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.ElectricityHandler;
import universalelectricity.prefab.tile.IRotatable;
import calclavia.lib.multiblock.IBlockActivate;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * This tile entity is for the screen of the missile launcher
 * 
 * @author Calclavia
 * 
 */
public class TFaSheShiMuo extends TFaSheQi implements IBlockActivate, IPacketReceiver, ITier, IRotatable
{
	// Is the block powered by redstone?
	private boolean isPowered = false;

	// The rotation of this missile component
	private byte fangXiang = 3;

	// The tier of this screen
	private int tier = 0;

	// The missile launcher base in which this
	// screen is connected with
	public TFaSheDi faSheDi = null;

	public short gaoDu = 3;

	private final Set<EntityPlayer> yongZhe = new HashSet<EntityPlayer>();

	public TFaSheShiMuo()
	{
		super();
		this.electricityHandler = new ElectricityHandler(this, this.getMaxEnergyStored());
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.faSheDi == null)
		{
			for (byte i = 2; i < 6; i++)
			{
				Vector3 position = new Vector3(this.xCoord, this.yCoord, this.zCoord);
				position.modifyPositionFromSide(ForgeDirection.getOrientation(i));

				TileEntity tileEntity = this.worldObj.getBlockTileEntity(position.intX(), position.intY(), position.intZ());

				if (tileEntity != null)
				{
					if (tileEntity instanceof TFaSheDi)
					{
						this.faSheDi = (TFaSheDi) tileEntity;
						this.fangXiang = i;
					}
				}
			}
		}
		else
		{
			if (this.faSheDi.isInvalid())
			{
				this.faSheDi = null;
			}
		}

		if (isPowered)
		{
			isPowered = false;
			this.launch();
		}

		if (!this.worldObj.isRemote)
		{
			if (this.ticks % 3 == 0)
			{
				if (this.muBiao == null)
				{
					this.muBiao = new Vector3(this.xCoord, 0, this.zCoord);
				}

				for (EntityPlayer wanJia : this.yongZhe)
				{
					PacketDispatcher.sendPacketToPlayer(this.getDescriptionPacket2(), (Player) wanJia);
				}
			}

			if (this.ticks % 600 == 0)
			{
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketManager.getPacket(ZhuYaoZhaPin.CHANNEL, this, 0, this.fangXiang, this.tier, this.getFrequency(), this.gaoDu);
	}

	public Packet getDescriptionPacket2()
	{
		return PacketManager.getPacket(ZhuYaoZhaPin.CHANNEL, this, 3, this.getEnergyStored(), this.muBiao.x, this.muBiao.y, this.muBiao.z);
	}

	@Override
	public void placeMissile(ItemStack itemStack)
	{
		if (this.faSheDi != null)
		{
			if (!this.faSheDi.isInvalid())
			{
				this.faSheDi.setInventorySlotContents(0, itemStack);
			}
		}
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			final int ID = dataStream.readInt();

			if (ID == -1)
			{
				if (dataStream.readBoolean())
				{
					this.yongZhe.add(player);
					PacketManager.sendPacketToClients(this.getDescriptionPacket());
				}
				else
				{
					this.yongZhe.remove(player);
				}
			}
			else if (ID == 0)
			{
				this.fangXiang = dataStream.readByte();
				this.tier = dataStream.readInt();
				this.setFrequency(dataStream.readInt());
				this.gaoDu = dataStream.readShort();
			}
			else if (!this.worldObj.isRemote)
			{
				if (ID == 1)
				{
					this.setFrequency(dataStream.readInt());
				}
				else if (ID == 2)
				{
					this.muBiao = new Vector3(dataStream.readDouble(), dataStream.readDouble(), dataStream.readDouble());

					if (this.getTier() < 2)
					{
						this.muBiao.y = 0;
					}
				}
				else if (ID == 3)
				{
					this.gaoDu = (short) Math.max(Math.min(dataStream.readShort(), Short.MAX_VALUE), 3);
				}
			}
			else if (ID == 3)
			{
				if (this.worldObj.isRemote)
				{
					this.setEnergyStored(dataStream.readFloat());
					this.muBiao = new Vector3(dataStream.readDouble(), dataStream.readDouble(), dataStream.readDouble());
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// Checks if the missile is launchable
	@Override
	public boolean canLaunch()
	{
		if (this.faSheDi != null)
		{
			if (this.faSheDi.daoDan != null)
			{
				if (this.getEnergyStored() >= this.getMaxEnergyStored())
				{
					if (this.faSheDi.isInRange(this.muBiao))
					{
						return true;
					}

				}
			}
		}

		return false;
	}

	/**
	 * Calls the missile launcher base to launch it's missile towards a targeted location
	 */
	@Override
	public void launch()
	{
		if (this.canLaunch())
		{
			this.setEnergyStored(0);
			this.faSheDi.launchMissile(this.muBiao.clone(), this.gaoDu);
		}
	}

	/**
	 * Gets the display status of the missile launcher
	 * 
	 * @return The string to be displayed
	 */
	@Override
	public String getStatus()
	{
		String color = "\u00a74";
		String status = "Idle";

		if (this.faSheDi == null)
		{
			status = "Not connected!";
		}
		else if (this.getEnergyStored() < this.getMaxEnergyStored())
		{
			status = "Insufficient electricity!";
		}
		else if (this.faSheDi.daoDan == null)
		{
			status = "Missile silo is empty!";
		}
		else if (this.muBiao == null)
		{
			status = "Target is invalid!";
		}
		else if (this.faSheDi.shiTaiJin(this.muBiao))
		{
			status = "Target too close!";
		}
		else if (this.faSheDi.shiTaiYuan(this.muBiao))
		{
			status = "Target too far!";
		}
		else
		{
			color = "\u00a72";
			status = "Ready to launch!";
		}

		return color + status;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);

		this.tier = par1NBTTagCompound.getInteger("tier");
		this.fangXiang = par1NBTTagCompound.getByte("facingDirection");
		this.gaoDu = par1NBTTagCompound.getShort("gaoDu");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setInteger("tier", this.tier);
		par1NBTTagCompound.setByte("facingDirection", this.fangXiang);
		par1NBTTagCompound.setShort("gaoDu", this.gaoDu);
	}

	@Override
	public float getVoltage()
	{
		switch (this.getTier())
		{
			default:
				return 120;
			case 1:
				return 240;
			case 2:
				return 480;
		}
	}

	@Override
	public void onPowerOn()
	{
		this.isPowered = true;
	}

	@Override
	public void onPowerOff()
	{
		this.isPowered = false;
	}

	@Override
	public int getTier()
	{
		return this.tier;
	}

	@Override
	public void setTier(int tier)
	{
		this.tier = tier;
	}

	@Override
	public ForgeDirection getDirection()
	{
		return ForgeDirection.getOrientation(this.fangXiang);
	}

	@Override
	public void setDirection(ForgeDirection facingDirection)
	{
		this.fangXiang = (byte) facingDirection.ordinal();
	}

	@Override
	public float getMaxEnergyStored()
	{
		switch (this.getTier())
		{
			case 0:
				return 400000;
			case 1:
				return 60000;
		}

		return 800000;
	}

	@Override
	public boolean onActivated(EntityPlayer entityPlayer)
	{
		entityPlayer.openGui(ZhuYaoZhaPin.instance, ZhuYaoICBM.GUI_FA_SHE_SHI_MUO, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		return true;
	}

	@Override
	public LauncherType getLauncherType()
	{
		return LauncherType.TRADITIONAL;
	}

	@Override
	public IMissile getMissile()
	{
		if (this.faSheDi != null)
		{
			return this.faSheDi.getContainingMissile();
		}

		return null;
	}
}
