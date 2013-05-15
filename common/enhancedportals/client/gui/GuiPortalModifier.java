package enhancedportals.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import enhancedportals.container.ContainerPortalModifier;
import enhancedportals.lib.BlockIds;
import enhancedportals.lib.GuiIds;
import enhancedportals.lib.Localization;
import enhancedportals.lib.Reference;
import enhancedportals.network.packet.PacketGui;
import enhancedportals.network.packet.PacketTEUpdate;
import enhancedportals.portal.PortalTexture;
import enhancedportals.portal.Upgrade;
import enhancedportals.tileentity.TileEntityPortalModifier;

public class GuiPortalModifier extends GuiContainer
{
    TileEntityPortalModifier portalModifier;
    public boolean hasInteractedWith = false, isActive = false;
    GuiButton okayButton;
    List<GuiItemStackButton> elementList;

    Upgrade upgrade;

    public GuiPortalModifier(InventoryPlayer player, TileEntityPortalModifier modifier)
    {
        super(new ContainerPortalModifier(player, modifier));
        portalModifier = modifier;
        isActive = portalModifier.isActive();
        upgrade = new Upgrade(this, modifier);
        elementList = new ArrayList<GuiItemStackButton>();

        List<String> strList = new ArrayList<String>();
        strList.add(Localization.localizeString("gui.redstoneControl.title"));
        strList.add(EnumChatFormatting.GRAY + Localization.localizeString("gui.redstoneControl.normal"));
        elementList.add(new GuiItemStackButton(xSize + 4, 4, new ItemStack(Block.torchRedstoneActive), strList, "redstoneHigh", this));
        elementList.get(0).active = portalModifier.redstoneSetting == 0;

        strList = new ArrayList<String>();
        strList.add(Localization.localizeString("gui.redstoneControl.title"));
        strList.add(EnumChatFormatting.GRAY + Localization.localizeString("gui.redstoneControl.inverted"));
        elementList.add(new GuiItemStackButton(xSize + 4, 24, new ItemStack(Block.torchRedstoneIdle), strList, "redstoneLow", this));
        elementList.get(1).active = portalModifier.redstoneSetting == 1;
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 100)
        {
            mc.thePlayer.closeScreen();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(Reference.GUI_LOCATION + "portalModifier.png");
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2 - 3;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        int padding = 5, width = 10;

        if (portalModifier.thickness == 1)
        {
            padding = 4;
            width = 8;
        }
        else if (portalModifier.thickness == 2)
        {
            padding = 2;
            width = 4;
        }
        else if (portalModifier.thickness == 3)
        {
            padding = 0;
            width = 0;
        }

        drawTexturedModalRect(0, 0, 0, 0, 0, 0);
        drawRect(guiLeft + 134 + padding, guiTop + 15, guiLeft + 16 - width + 134 + padding, guiTop + 15 + 16, 0xFF555555);
        ItemStack itemstack = null;

        if (portalModifier.texture.blockID != -1)
        {
            itemstack = new ItemStack(Block.blocksList[portalModifier.texture.blockID], 1, portalModifier.texture.metaData);
        }
        else if (portalModifier.texture.colour != null)
        {
            itemstack = new ItemStack(Block.blocksList[BlockIds.DummyPortal], 1, portalModifier.texture.colour.ordinal());
        }

        if (itemstack != null)
        {
            itemRenderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, guiLeft + xSize - 24, guiTop + 15);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        fontRenderer.drawString(Localization.localizeString("tile.portalModifier.name"), xSize / 2 - fontRenderer.getStringWidth(Localization.localizeString("tile.portalModifier.name")) / 2, -15, 0xFFFFFF);
        fontRenderer.drawString(Localization.localizeString("gui.upgrades.title"), 8, 3, 0xFF444444);
        fontRenderer.drawString(Localization.localizeString("gui.modifications.title"), xSize - 8 - fontRenderer.getStringWidth(Localization.localizeString("gui.modifications.title")), 3, 0xFF444444);
        fontRenderer.drawString(Localization.localizeString("gui.network.title"), 8, 35, 0xFF444444);
    }

    @Override
    public void drawScreen(int x, int y, float par3)
    {
        if (!isActive)
        {
            super.drawScreen(x, y, par3);

            if (!portalModifier.network.equals(""))
            {
                String[] split = portalModifier.network.split(Reference.glyphSeperator);

                for (int i = 0; i < split.length; i++)
                {
                    for (int j = 0; j < Reference.glyphValues.size(); j++)
                    {
                        if (Reference.glyphValues.get(j).equalsIgnoreCase(split[i]))
                        {
                            itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, Reference.glyphItems.get(j), guiLeft + 8 + i * 18, guiTop + 47);
                        }
                    }
                }
            }
            else
            {
                fontRenderer.drawStringWithShadow(Localization.localizeString("gui.network.info"), guiLeft + xSize / 2 - fontRenderer.getStringWidth(Localization.localizeString("gui.network.info")) / 2, guiTop + 51, 0xFF00FF00);
            }

            for (int i = 0; i < elementList.size(); i++)
            {
                elementList.get(i).drawElement(guiLeft, guiTop, x, y, fontRenderer, itemRenderer, mc.renderEngine);
            }

            upgrade.drawElements(x, y, fontRenderer, itemRenderer, mc.renderEngine);

            if (isPointInRegion(134, 15, 16, 16, x, y))
            {
                String thickness = "";

                switch (portalModifier.thickness)
                // TODO LANGUAGE
                {
                    case 0:
                        thickness = Localization.localizeString("gui.thickness.normal");
                        break;

                    case 1:
                        thickness = Localization.localizeString("gui.thickness.thick");
                        break;

                    case 2:
                        thickness = Localization.localizeString("gui.thickness.thicker");
                        break;

                    case 3:
                        thickness = Localization.localizeString("gui.thickness.fullblock");
                        break;
                }

                List<String> list = new ArrayList<String>();
                list.add(Localization.localizeString("gui.thickness.title"));
                list.add(EnumChatFormatting.GRAY + thickness);
                drawText(list, x, y);
            }
            else if (isPointInRegion(152, 15, 16, 16, x, y))
            {
                String txt = "";

                if (portalModifier.texture.blockID == -1)
                {
                    txt = Localization.localizeString("gui.portalColour." + ItemDye.dyeColorNames[PortalTexture.swapColours(portalModifier.texture.colour.ordinal())]);
                }
                else
                {
                    txt = Localization.localizeString(Block.blocksList[portalModifier.texture.blockID].getUnlocalizedName() + ".name");

                    if (portalModifier.texture.blockID == 9 || portalModifier.texture.blockID == 11)
                    {
                        txt = "Still " + txt;
                    }
                    else if (portalModifier.texture.blockID == 8 || portalModifier.texture.blockID == 10)
                    {
                        txt = "Flowing " + txt;
                    }
                }

                List<String> list = new ArrayList<String>();
                list.add(Localization.localizeString("gui.facade.title"));
                list.add(EnumChatFormatting.GRAY + txt);
                drawText(list, x, y);
            }
            else if (isPointInRegion(7, 46, 162, 18, x, y))
            {
                List<String> str = new ArrayList<String>();
                str.add(Localization.localizeString("gui.network.title"));
                str.add(EnumChatFormatting.GRAY + Localization.localizeString("gui.network.info"));
                drawText(str, x, y);
            }
        }
        else
        {
            String txt = Localization.localizeString("gui.activeModifier"); // TODO LANGUAGE

            drawDefaultBackground();
            fontRenderer.drawString(txt, width / 2 - fontRenderer.getStringWidth(txt) / 2, guiTop, 0xFFFFFF);
            okayButton.drawButton(mc, 0, 0);
        }
    }

    public void drawText(List<String> list, int x2, int y2)
    {
        super.drawHoveringText(list, x2, y2, fontRenderer);
    }

    public void drawTexturedRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float f = 0.062F;
        float f1 = 0.062F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(par1 + 0, par2 + par6, zLevel, (par3 + 0) * f, (par4 + par6) * f1);
        tessellator.addVertexWithUV(par1 + par5, par2 + par6, zLevel, (par3 + par5) * f, (par4 + par6) * f1);
        tessellator.addVertexWithUV(par1 + par5, par2 + 0, zLevel, (par3 + par5) * f, (par4 + 0) * f1);
        tessellator.addVertexWithUV(par1 + 0, par2 + 0, zLevel, (par3 + 0) * f, (par4 + 0) * f1);
        tessellator.draw();
    }

    public void elementClicked(GuiItemStackButton itemStackButton, int button)
    {
        if (itemStackButton.value.equalsIgnoreCase("redstoneHigh"))
        {
            portalModifier.redstoneSetting = 0;
            itemStackButton.active = true;
            elementList.get(1).active = false;
        }
        else if (itemStackButton.value.equalsIgnoreCase("redstoneLow"))
        {
            portalModifier.redstoneSetting = 1;
            itemStackButton.active = true;
            elementList.get(0).active = false;
        }

        hasInteractedWith = true;
    }

    public int getGuiLeft()
    {
        return guiLeft;
    }

    public int getGuiTop()
    {
        return guiTop;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui()
    {
        super.initGui();

        okayButton = new GuiButton(100, width / 2 - 50, height / 2 - 10, 100, 20, "Close");
        buttonList.add(okayButton);
        okayButton.drawButton = isActive;
    }

    @Override
    protected void mouseClicked(int x, int y, int buttonClicked)
    {
        super.mouseClicked(x, y, buttonClicked);

        for (int i = 0; i < elementList.size(); i++)
        {
            elementList.get(i).handleMouseClick(guiLeft, guiTop, x, y, buttonClicked, i);
        }

        upgrade.mouseClicked(x, y, buttonClicked);

        if (isPointInRegion(134, 15, 16, 16, x, y))
        {
            portalModifier.thickness++;

            if (portalModifier.thickness >= 4)
            {
                portalModifier.thickness = 0;
            }

            hasInteractedWith = true;
        }
        else if (isPointInRegion(7, 46, 162, 18, x, y))
        {
            PacketDispatcher.sendPacketToServer(new PacketGui(true, false, GuiIds.PortalModifierNetwork, portalModifier).getPacket());
        }
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (hasInteractedWith)
        {
            PacketDispatcher.sendPacketToServer(new PacketTEUpdate(portalModifier).getPacket());
        }
    }
    
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        upgrade.updateUpgradesList();
    }
}
