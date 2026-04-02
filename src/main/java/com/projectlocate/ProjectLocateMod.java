package com.projectlocate;

import com.projectlocate.command.ClocateCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectLocateMod implements ModInitializer {
	public static final String MOD_ID = "projectlocate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Project Locate initialized");
		CommandRegistrationCallback.EVENT.register(ClocateCommand::register);
	}
}
