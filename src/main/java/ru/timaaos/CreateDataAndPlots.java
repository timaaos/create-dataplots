package ru.timaaos;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllDisplayTargets;
import com.simibubi.create.Create;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.timaaos.blocks.*;
import ru.timaaos.network.DashboardPackets;

import java.util.function.Supplier;


public class CreateDataAndPlots implements ModInitializer {
	public static final String ID = "dataplots";
    public static final Logger LOGGER = LoggerFactory.getLogger("dataplots");
	public static final Block DASHBOARD_BLOCK = new DashboardBlock(Block.Settings.create().strength(4.0f));
	public static final BlockEntityType<DashboardBlockEntity> DASHBOARD_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			new Identifier(ID, "dashboard"),
			FabricBlockEntityTypeBuilder.create(DashboardBlockEntity::new, DASHBOARD_BLOCK).build()
	);
	public static final Item DASHBOARD_BLOCK_ITEM = new BlockItem(DASHBOARD_BLOCK, new FabricItemSettings());
	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);
	public static final BlockEntry<PortablePlayerInterfaceBlock> PORTABLE_PLAYER_INTERFACE = REGISTRATE
			.block("portable_player_interface", PortablePlayerInterfaceBlock::new)
			.initialProperties(SharedProperties::softMetal)
			.simpleItem()
			.blockEntity(PortablePlayerInterfaceBlockEntity::new)
			.build()
			.register();
	public static final RegistryEntry<DashboardBlockTarget> DASHBOARD_TARGET = REGISTRATE.displayTarget("dashboard", DashboardBlockTarget::new)
		.associate(DASHBOARD_BLOCK_ENTITY)
		.register();

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier(ID, "dashboard"), DASHBOARD_BLOCK);
		Registry.register(Registries.ITEM, new Identifier(ID, "dashboard"), DASHBOARD_BLOCK_ITEM);
		REGISTRATE.register();
		DashboardPackets.registerServer();

		//AllDisplayTargets.register(new DashboardBlockTarget(), "dashboard").accept(DASHBOARD_BLOCK);
		ItemGroupEvents.modifyEntriesEvent(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()).register(content -> {
			content.add(DASHBOARD_BLOCK_ITEM);
			content.add(PORTABLE_PLAYER_INTERFACE);
		});

		LOGGER.info("Hello from Create: Data & Plots!");
	}
}