package com.megatrex4;

import com.megatrex4.api.v1.InventoryWeightEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuckPermsInventoryWeight implements ModInitializer {

	public static final String MOD_ID = "luckperms_inventoryweight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	private static final String META_ADD = "inventoryweight.maxweight.add";
	private static final String META_PERCENT = "inventoryweight.maxweight.percent";
	private static final String META_MULTIPLIER = "inventoryweight.maxweight.multiplier";

	@Override
	public void onInitialize() {
		InventoryWeightEvents.MODIFY_MAX_WEIGHT.register(LuckPermsInventoryWeight::modifyMaxWeight);

		LOGGER.info(
				"LuckPerms Inventory Weight initialized. LuckPerms loaded: {}",
				FabricLoader.getInstance().isModLoaded("luckperms")
		);
	}

	private static float modifyMaxWeight(ServerPlayer player, float currentMaxWeight) {
		LuckPerms luckPerms = getLuckPerms();

		if (luckPerms == null) {
			return currentMaxWeight;
		}

		User user = luckPerms.getUserManager().getUser(player.getUUID());

		if (user == null) {
			return currentMaxWeight;
		}

		float additive = getMetaFloat(user, META_ADD, 0.0f);
		float percent = getMetaFloat(user, META_PERCENT, 0.0f);
		float multiplier = getMetaFloat(user, META_MULTIPLIER, 1.0f);

		additive = Math.max(0.0f, additive);
		percent = Math.max(0.0f, percent);
		multiplier = Math.max(0.0f, multiplier);

		float percentMultiplier = 1.0f + percent / 100.0f;

		float result = (currentMaxWeight + additive) * percentMultiplier * multiplier;

		return Math.max(1.0f, result);
	}

	private static LuckPerms getLuckPerms() {
		try {
			return LuckPermsProvider.get();
		} catch (IllegalStateException exception) {
			return null;
		}
	}

	private static float getMetaFloat(User user, String key, float defaultValue) {
		String rawValue = user
				.getCachedData()
				.getMetaData()
				.getMetaValue(key);

		Float parsed = parseFloat(rawValue);

		return parsed == null ? defaultValue : parsed;
	}

	private static Float parseFloat(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return null;
		}

		try {
			float value = Float.parseFloat(rawValue.trim());

			if (!Float.isFinite(value)) {
				return null;
			}

			return value;
		} catch (NumberFormatException ignored) {
			return null;
		}
	}
}