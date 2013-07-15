package icbm.gangshao.turret.sentries;

import icbm.api.sentry.IAATarget;
import icbm.core.ZhuYaoICBM;
import icbm.gangshao.ProjectileType;
import icbm.gangshao.ZhuYaoGangShao;
import icbm.gangshao.damage.TileDamageSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import calclavia.lib.CalculationHelper;

public class TLeiShe extends TPaoTaiZiDong
{
	/**
	 * Laser turret spins its barrels every shot.
	 */
	public float barrelRotation;
	public float barrelRotationVelocity;

	public TLeiShe()
	{
		this.targetPlayers = true;
		this.targetHostile = true;

		this.baseTargetRange = 20;
		this.maxTargetRange = 35;

		this.rotationSpeed = 3;

		this.baseFiringDelay = 12;
		this.minFiringDelay = 5;

		this.projectileType = ProjectileType.UNKNOWN;
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.worldObj.isRemote)
		{
			this.barrelRotation = MathHelper.wrapAngleTo180_float(this.barrelRotation + this.barrelRotationVelocity);
			this.barrelRotationVelocity = Math.max(this.barrelRotationVelocity - 0.1f, 0);
		}
	}

	@Override
	public float getFiringRequest()
	{
		return 20000;
	}

	@Override
	public float getVoltage()
	{
		return 480;
	}

	@Override
	public int getMaxHealth()
	{
		return 130;
	}

	@Override
	public void playFiringSound()
	{
		this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, ZhuYaoICBM.PREFIX + "lasershot", 5F, 1F - (this.worldObj.rand.nextFloat() * 0.2f));
	}

	@Override
	public void renderShot(Vector3 target)
	{
		Vector3 center = this.getCenter();
		ZhuYaoGangShao.proxy.renderBeam(this.worldObj, Vector3.add(center, CalculationHelper.getDeltaPositionFromRotation(this.currentRotationYaw - 6, this.currentRotationPitch * 1.4f).multiply(1.2)), target, 1, 0.4f, 0.4f, 5);
		ZhuYaoGangShao.proxy.renderBeam(this.worldObj, Vector3.add(center, CalculationHelper.getDeltaPositionFromRotation(this.currentRotationYaw + 6, this.currentRotationPitch * 1.4f).multiply(1.2)), target, 1, 0.4f, 0.4f, 5);
		this.barrelRotationVelocity += 1;
	}

	@Override
	protected boolean onFire()
	{
		if (!this.worldObj.isRemote)
		{
			if (this.getPlatform() != null)
			{
				if (this.target instanceof EntityLiving)
				{
					this.getPlatform().provideElectricity(ForgeDirection.UP, ElectricityPack.getFromWatts(this.getFiringRequest(), this.getVoltage()), true);
					this.target.attackEntityFrom(TileDamageSource.doLaserDamage(this), 2);
					this.target.setFire(3);
					return true;
				}
				else if (this.target instanceof IAATarget)
				{
					if (this.worldObj.rand.nextFloat() > 0.2)
					{
						int damage = ((IAATarget) this.target).doDamage(10);

						if (damage == -1 && this.worldObj.rand.nextFloat() > 0.7)
						{
							((IAATarget) this.target).destroyCraft();
						}
						else if (damage <= 0)
						{
							((IAATarget) this.target).destroyCraft();
						}
					}

					return true;
				}
			}
		}

		return false;
	}
}
