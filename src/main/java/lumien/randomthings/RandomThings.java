package lumien.randomthings;

import java.util.List;

import org.apache.logging.log4j.Logger;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.client.GuiHandler;
import lumien.randomthings.config.ModConfiguration;
import lumien.randomthings.entitys.ModEntitys;
import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.RTEventHandler;
import lumien.randomthings.handler.magicavoxel.ModelHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.RTCreativeTab;
import lumien.randomthings.lib.Reference;
import lumien.randomthings.network.PacketHandler;
import lumien.randomthings.potion.ModPotions;
import lumien.randomthings.recipes.ModRecipes;
import lumien.randomthings.tileentity.ModTileEntitys;
import lumien.randomthings.tileentity.TileEntityEnderAnchor;
import lumien.randomthings.worldgen.WorldGenCores;
import lumien.randomthings.worldgen.WorldGenPlants;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.Type;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class RandomThings implements LoadingCallback
{
	@Instance(Reference.MOD_ID)
	public static RandomThings instance;

	@SidedProxy(clientSide = "lumien.randomthings.client.ClientProxy", serverSide = "lumien.randomthings.CommonProxy")
	public static CommonProxy proxy;

	public RTCreativeTab creativeTab;

	public Logger logger;

	public ModConfiguration configuration;

	public ModelHandler modelHandler;

	ASMDataTable asmDataTable;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		asmDataTable = event.getAsmData();
		modelHandler = new ModelHandler(event.getSide());

		creativeTab = new RTCreativeTab();
		logger = event.getModLog();

		configuration = new ModConfiguration();
		configuration.preInit(event);

		ModBlocks.load(event);
		ModItems.load(event);
		ModTileEntitys.register();
		ModEntitys.init();
		ModPotions.preInit(event);
		proxy.registerModels();

		RTEventHandler eventHandler = new RTEventHandler();
		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		PacketHandler.init();

		ForgeChunkManager.setForcedChunkLoadingCallback(this, this);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		ModRecipes.register();
		ModDimensions.register();

		GameRegistry.registerWorldGenerator(new WorldGenPlants(), 1000);
		GameRegistry.registerWorldGenerator(new WorldGenCores(), 1000);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		modelHandler.load();
		proxy.registerRenderers();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		// event.registerServerCommand(new RTCommand()); DEBUG
	}

	public ASMDataTable getASMData()
	{
		return asmDataTable;
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world)
	{
		for (Ticket t : tickets)
		{
			NBTTagCompound compound = t.getModData();
			TileEntity te = world.getTileEntity(new BlockPos(compound.getInteger("posX"), compound.getInteger("posY"), compound.getInteger("posZ")));
			if (te != null && te instanceof TileEntityEnderAnchor)
			{
				TileEntityEnderAnchor anchor = (TileEntityEnderAnchor) te;
				anchor.setTicket(t);

				for (ChunkCoordIntPair pair : t.getChunkList())
				{
					ForgeChunkManager.forceChunk(t, pair);
				}
			}

		}
	}

	@EventHandler
	public void missingMapping(FMLMissingMappingsEvent event)
	{
		for (MissingMapping mapping : event.getAll())
		{
			if (mapping.name.equals("randomthings:redstoneInterface"))
			{
				System.out.println("Fixing Redstone Interface Mapping");
				if (mapping.type == Type.BLOCK)
				{
					mapping.remap(ModBlocks.basicRedstoneInterface);
				}
				else if (mapping.type == Type.ITEM)
				{
					mapping.remap(Item.getItemFromBlock(ModBlocks.basicRedstoneInterface));
				}
			}
		}
	}
}
