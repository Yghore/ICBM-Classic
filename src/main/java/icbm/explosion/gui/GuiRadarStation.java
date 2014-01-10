package icbm.explosion.gui;

import icbm.core.ICBMCore;
import icbm.explosion.ICBMExplosion;
import icbm.explosion.machines.BlockICBMMachine;
import icbm.explosion.machines.TileEntityRadarStation;
import icbm.explosion.missile.missile.EntityMissile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import universalelectricity.api.energy.UnitDisplay;
import universalelectricity.api.vector.Vector2;
import calclavia.lib.utility.LanguageUtility;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiRadarStation extends GuiBase
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(ICBMCore.DOMAIN, ICBMCore.GUI_PATH + "gui_radar.png");
    public static final ResourceLocation TEXTURE_RED_DOT = new ResourceLocation(ICBMCore.DOMAIN, ICBMCore.GUI_PATH + "reddot.png");
    public static final ResourceLocation TEXTURE_YELLOW_DOT = new ResourceLocation(ICBMCore.DOMAIN, ICBMCore.GUI_PATH + "yellowdot.png");
    public static final ResourceLocation TEXTURE_WHITE_DOT = new ResourceLocation(ICBMCore.DOMAIN, ICBMCore.GUI_PATH + "whitedot.png");
    private TileEntityRadarStation tileEntity;

    private int containerPosX;
    private int containerPosY;

    private GuiTextField textFieldAlarmRange;
    private GuiTextField textFieldSafetyZone;
    private GuiTextField textFieldFrequency;

    private List<Vector2> missileCoords = new ArrayList<Vector2>();

    private Vector2 mouseOverCoords = new Vector2();
    private Vector2 mousePosition = new Vector2();

    // Radar Map
    private Vector2 radarCenter;
    private float radarMapRadius;

    private String info = "";

    private String info2;

    public GuiRadarStation(TileEntityRadarStation tileEntity)
    {
        this.tileEntity = tileEntity;
        mouseOverCoords = new Vector2(this.tileEntity.xCoord, this.tileEntity.zCoord);
        this.xSize = 256;
        radarCenter = new Vector2(this.containerPosX + this.xSize / 3 - 14, this.containerPosY + this.ySize / 2 + 4);
        radarMapRadius = TileEntityRadarStation.MAX_DETECTION_RANGE / 63.8F;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.textFieldSafetyZone = new GuiTextField(fontRenderer, 210, 67, 30, 12);
        this.textFieldSafetyZone.setMaxStringLength(3);
        this.textFieldSafetyZone.setText(this.tileEntity.safetyRange + "");

        this.textFieldAlarmRange = new GuiTextField(fontRenderer, 210, 82, 30, 12);
        this.textFieldAlarmRange.setMaxStringLength(3);
        this.textFieldAlarmRange.setText(this.tileEntity.alarmRange + "");

        this.textFieldFrequency = new GuiTextField(fontRenderer, 155, 112, 50, 12);
        this.textFieldFrequency.setMaxStringLength(6);
        this.textFieldFrequency.setText(this.tileEntity.getFrequency() + "");

        PacketDispatcher.sendPacketToServer(PacketManager.getPacket("ICBM", this.tileEntity, -1, true));
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        PacketDispatcher.sendPacketToServer(PacketManager.getPacket("ICBM", this.tileEntity, -1, false));
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of the items) */
    @Override
    protected void drawForegroundLayer(int var2, int var3, float var1)
    {
        this.fontRenderer.drawString("\u00a77" + LanguageUtility.getLocal("icbm.machine.9.name"), this.xSize / 2 - 30, 6, 4210752);

        this.fontRenderer.drawString("Coordinates:", 155, 18, 4210752);
        this.fontRenderer.drawString("X: " + (int) Math.round(mouseOverCoords.x) + " Z: " + (int) Math.round(mouseOverCoords.y), 155, 30, 4210752);

        this.fontRenderer.drawString("\u00a76" + this.info, 155, 42, 4210752);
        this.fontRenderer.drawString("\u00a74" + this.info2, 155, 54, 4210752);

        this.fontRenderer.drawString("Safe Zone:", 152, 70, 4210752);
        this.textFieldSafetyZone.drawTextBox();
        this.fontRenderer.drawString("Alarm Zone:", 150, 85, 4210752);
        this.textFieldAlarmRange.drawTextBox();

        this.fontRenderer.drawString("Frequency:", 155, 100, 4210752);
        this.textFieldFrequency.drawTextBox();

        this.fontRenderer.drawString(UnitDisplay.getDisplay(TileEntityRadarStation.WATTS, Unit.WATT), 155, 128, 4210752);

        this.fontRenderer.drawString(UnitDisplay.getDisplay(this.tileEntity.getVoltage(), Unit.VOLTAGE), 155, 138, 4210752);

        // Shows the status of the radar
        String color = "\u00a74";
        String status = "Idle";

        if (this.tileEntity.getEnergyStored() >= this.tileEntity.getRequest(null))
        {
            color = "\u00a72";
            status = "Radar On!";
        }
        else
        {
            status = "No Electricity!";
        }

        this.fontRenderer.drawString(color + status, 155, 150, 4210752);
    }

    /** Call this method from you GuiScreen to process the keys into textbox. */
    @Override
    public void keyTyped(char par1, int par2)
    {
        super.keyTyped(par1, par2);
        this.textFieldSafetyZone.textboxKeyTyped(par1, par2);
        this.textFieldAlarmRange.textboxKeyTyped(par1, par2);
        this.textFieldFrequency.textboxKeyTyped(par1, par2);

        try
        {
            int newSafetyRadius = Math.min(TileEntityRadarStation.MAX_DETECTION_RANGE, Math.max(0, Integer.parseInt(this.textFieldSafetyZone.getText())));
            this.tileEntity.safetyRange = newSafetyRadius;
            PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ICBMExplosion.CHANNEL, this.tileEntity, 2, this.tileEntity.safetyRange));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            int newAlarmRadius = Math.min(TileEntityRadarStation.MAX_DETECTION_RANGE, Math.max(0, Integer.parseInt(this.textFieldAlarmRange.getText())));
            this.tileEntity.alarmRange = newAlarmRadius;
            PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ICBMExplosion.CHANNEL, this.tileEntity, 3, this.tileEntity.alarmRange));
        }
        catch (NumberFormatException e)
        {
        }

        try
        {
            this.tileEntity.setFrequency(Integer.parseInt(this.textFieldFrequency.getText()));
            PacketDispatcher.sendPacketToServer(PacketManager.getPacket(ICBMExplosion.CHANNEL, this.tileEntity, 4, this.tileEntity.getFrequency()));
        }
        catch (NumberFormatException e)
        {
        }

    }

    /** Args: x, y, buttonClicked */
    @Override
    public void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);
        this.textFieldAlarmRange.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
        this.textFieldSafetyZone.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
        this.textFieldFrequency.mouseClicked(par1 - containerPosX, par2 - containerPosY, par3);
    }

    /** Draw the background layer for the GuiContainer (everything behind the items) */
    @Override
    protected void drawBackgroundLayer(int var2, int var3, float var1)
    {
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.containerPosX = (this.width - this.xSize) / 2;
        this.containerPosY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(containerPosX, containerPosY, 0, 0, this.xSize, this.ySize);

        this.radarCenter = new Vector2(this.containerPosX + this.xSize / 3 - 10, this.containerPosY + this.ySize / 2 + 4);
        this.radarMapRadius = TileEntityRadarStation.MAX_DETECTION_RANGE / 71f;

        this.info = "";
        this.info2 = "";

        if (this.tileEntity.getEnergyStored() >= this.tileEntity.getRequest(null))
        {
            int range = 4;

            for (Entity entity : this.tileEntity.detectedEntities)
            {
                Vector2 position = new Vector2(radarCenter.x + (entity.posX - this.tileEntity.xCoord) / this.radarMapRadius, radarCenter.y - (entity.posZ - this.tileEntity.zCoord) / this.radarMapRadius);

                if (entity instanceof EntityMissile)
                {
                    if (this.tileEntity.isWeiXianDaoDan((EntityMissile) entity))
                    {
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_RED_DOT);
                    }
                    else
                    {
                        FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_YELLOW_DOT);
                    }
                }
                else
                {
                    FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_YELLOW_DOT);
                }

                this.drawTexturedModalRect(position.intX(), position.intY(), 0, 0, 2, 2);

                // Hover Detection
                Vector2 minPosition = position.clone();
                minPosition.add(-range);
                Vector2 maxPosition = position.clone();
                maxPosition.add(range);

                if (new Region2(minPosition, maxPosition).isIn(this.mousePosition))
                {
                    this.info = entity.getEntityName();

                    if (entity instanceof EntityPlayer)
                    {
                        this.info = "\u00a71" + this.info;
                    }

                    if (entity instanceof EntityMissile)
                    {
                        if (((EntityMissile) entity).targetVector != null)
                        {
                            this.info2 = "(" + ((EntityMissile) entity).targetVector.intX() + ", " + ((EntityMissile) entity).targetVector.intZ() + ")";
                        }
                    }
                }
            }

            range = 2;

            for (TileEntity jiQi : this.tileEntity.detectedTiles)
            {
                Vector2 position = new Vector2(this.radarCenter.x + (jiQi.xCoord - this.tileEntity.xCoord) / this.radarMapRadius, this.radarCenter.y - (jiQi.zCoord - this.tileEntity.zCoord) / this.radarMapRadius);
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TEXTURE_WHITE_DOT);

                this.drawTexturedModalRect(position.intX(), position.intY(), 0, 0, 2, 2);

                Vector2 minPosition = position.clone();
                minPosition.add(-range);
                Vector2 maxPosition = position.clone();
                maxPosition.add(range);

                if (new Region2(minPosition, maxPosition).isIn(this.mousePosition))
                {
                    if (jiQi.getBlockType() != null)
                    {
                        if (jiQi.getBlockType() instanceof BlockICBMMachine)
                        {
                            this.info = BlockICBMMachine.getJiQiMing(jiQi);
                        }
                        else
                        {
                            this.info = jiQi.getBlockType().getLocalizedName();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();

        if (Mouse.isInsideWindow())
        {
            if (Mouse.getEventButton() == -1)
            {
                this.mousePosition = new Vector2(Mouse.getEventX() * this.width / this.mc.displayWidth, this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1);

                float difference = TileEntityRadarStation.MAX_DETECTION_RANGE / this.radarMapRadius;

                if (this.mousePosition.x > this.radarCenter.x - difference && this.mousePosition.x < this.radarCenter.x + difference && this.mousePosition.y > this.radarCenter.y - difference && this.mousePosition.y < this.radarCenter.y + difference)
                {
                    // Calculate from the mouse position the relative position
                    // on the grid
                    int xDifference = (int) (this.mousePosition.x - this.radarCenter.x);
                    int yDifference = (int) (this.mousePosition.y - this.radarCenter.y);
                    int xBlockDistance = (int) (xDifference * this.radarMapRadius);
                    int yBlockDistance = (int) (yDifference * this.radarMapRadius);

                    this.mouseOverCoords = new Vector2(this.tileEntity.xCoord + xBlockDistance, this.tileEntity.zCoord - yBlockDistance);
                }
            }
        }

        if (!this.textFieldSafetyZone.isFocused())
            this.textFieldSafetyZone.setText(this.tileEntity.safetyRange + "");
        if (!this.textFieldAlarmRange.isFocused())
            this.textFieldAlarmRange.setText(this.tileEntity.alarmRange + "");
    }
}