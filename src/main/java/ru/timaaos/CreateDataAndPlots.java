package ru.timaaos;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.timaaos.blocks.DashboardBlock;
import ru.timaaos.blocks.DashboardBlockEntity;
import ru.timaaos.blocks.DashboardBlockTarget;


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
	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, new Identifier(ID, "dashboard"), DASHBOARD_BLOCK);
		Registry.register(Registries.ITEM, new Identifier(ID, "dashboard"), DASHBOARD_BLOCK_ITEM);
		AllDisplayBehaviours.assignDataBehaviour(new DashboardBlockTarget(), "dashboard").accept(DASHBOARD_BLOCK);
		ItemGroupEvents.modifyEntriesEvent(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()).register(content -> {
			content.add(DASHBOARD_BLOCK_ITEM);
		});

		LOGGER.info("Hello from Create: Data & Plots!");
	}
}