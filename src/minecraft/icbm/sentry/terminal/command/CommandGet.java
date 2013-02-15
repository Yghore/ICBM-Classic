package icbm.sentry.terminal.command;

import icbm.sentry.gui.GuiTerminal;
import icbm.sentry.platform.TileEntityTurretPlatform;
import icbm.sentry.terminal.AccessLevel;
import icbm.sentry.terminal.ISpecialAccess;
import icbm.sentry.terminal.TerminalCommand;
import icbm.sentry.terminal.UserAccess;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

public class CommandGet extends TerminalCommand
{
	@Override
	public String getCommandPrefix()
	{
		return "get";
	}

	@Override
	public boolean processCommand(EntityPlayer player, ISpecialAccess TE, GuiTerminal gui, String[] args)
	{
		if (args[0].equalsIgnoreCase("get") && args.length > 1 && args[1] != null && TE instanceof TileEntityTurretPlatform)
		{
			TileEntityTurretPlatform turret = (TileEntityTurretPlatform) TE;
			if (args[1].equalsIgnoreCase("owner"))
			{
				List<UserAccess> userList = turret.getUsersWithAcess(AccessLevel.OWNER);

				if (userList.size() > 0)
				{
					for (UserAccess access : userList)
					{
						gui.addToConsole("" + access.username);
					}
				}
				else
				{
					gui.addToConsole("No owners");
				}
				return true;
			}
			else if (args[1].equalsIgnoreCase("position"))
			{
				gui.addToConsole("position: " + turret.xCoord + "x " + turret.yCoord + "y " + turret.zCoord + "z ");
				return true;
			}
			else if (args[1].equalsIgnoreCase("kills"))
			{
				// TODO track
				gui.addToConsole("Not yet useable");
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canPlayerUse(EntityPlayer var1, ISpecialAccess mm)
	{
		return true;
	}

	@Override
	public boolean showOnHelp(EntityPlayer player, ISpecialAccess mm)
	{
		return true;
	}

	@Override
	public List<String> getCmdUses(EntityPlayer player, ISpecialAccess mm)
	{
		List<String> cmds = new ArrayList<String>();
		cmds.add("get owner");
		cmds.add("get position");
		// cmds.add("get kills");
		// cmds.add("get ammo");
		// cmds.add("get ammoTypes");
		// cmds.add("get attackTypes");
		// cmds.add("get terminal/root");
		return cmds;
	}

	@Override
	public boolean canMachineUse(ISpecialAccess mm)
	{
		return mm instanceof TileEntityTurretPlatform;
	}

}
