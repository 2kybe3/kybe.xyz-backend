package xyz.kybe.backend.discord;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.kybe.backend.discord.commands.DecompressLogCommand;
import xyz.kybe.backend.discord.commands.DirectMessageCommand;
import xyz.kybe.backend.discord.commands.LinkShortenerCommand;
import xyz.kybe.backend.discord.commands.RusherhackCommands;

@Component
public class CommandRegister {
	private final Logger logger = LoggerFactory.getLogger(CommandRegister.class);

	private final DiscordBot discordBot;
	private final DirectMessageCommand directMessageCommand;
	private final DecompressLogCommand decompressLogCommand;
	private final LinkShortenerCommand linkShortenerCommand;
	private final RusherhackCommands rusherhackCommands;

	public CommandRegister(
		DiscordBot discordBot,
		DirectMessageCommand directMessageCommand,
		DecompressLogCommand decompressLogCommand,
		LinkShortenerCommand linkShortenerCommand,
		RusherhackCommands rusherhackCommands
	) {
		this.discordBot = discordBot;
		this.directMessageCommand = directMessageCommand;
		this.decompressLogCommand = decompressLogCommand;
		this.linkShortenerCommand = linkShortenerCommand;
		this.rusherhackCommands = rusherhackCommands;
	}

	@PostConstruct
	public void initialize() {
		try {
			JDA jda = discordBot.getJda();
			if (jda == null) {
				logger.error("Cannot register Direct Message: JDA is not initialized");
				return;
			}

			jda.updateCommands()
				.addCommands(
					Commands.context(Command.Type.USER, "Direct Message User")
						.setIntegrationTypes(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL),
					Commands.message("Decompress Log")
						.setIntegrationTypes(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL),
					Commands.slash("shorten-link", "Shorten a given link")
						.setIntegrationTypes(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL)
						.addOption(OptionType.STRING, "url", "The URL to shorten", true)
						.addOption(OptionType.STRING, "mode", "The code generation mode: ascii, emoji, full or chinese", false),
					Commands.slash("rusher-info", "Dumps the rusherhack client json")
						.setIntegrationTypes(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL)
						.addOptions(new OptionData(OptionType.STRING, "search", "What to search for", true).setAutoComplete(true)),
					Commands.slash("test", "Current test command stuff")
						.setIntegrationTypes(IntegrationType.USER_INSTALL, IntegrationType.GUILD_INSTALL)
				).queue();

			jda.addEventListener(directMessageCommand);
			jda.addEventListener(decompressLogCommand);
			jda.addEventListener(linkShortenerCommand);
			jda.addEventListener(rusherhackCommands);
			logger.info("All command handlers initialized successfully");
		} catch (Exception e) {
			logger.error("Failed to initialize command handlers", e);
		}
	}
}
