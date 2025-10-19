package xyz.kybe.backend.discord;

import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.IntegrationType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.kybe.backend.discord.commands.DecompressLogCommand;
import xyz.kybe.backend.discord.commands.DirectMessageCommand;
import xyz.kybe.backend.discord.commands.LinkShortenerCommand;

@Component
public class CommandRegister {
	private final Logger logger = LoggerFactory.getLogger(CommandRegister.class);

	private final DiscordBot discordBot;
	private final DirectMessageCommand directMessageCommand;
	private final DecompressLogCommand decompressLogCommand;
	private final LinkShortenerCommand linkShortenerCommand;

	public CommandRegister(
		DiscordBot discordBot,
		DirectMessageCommand directMessageCommand,
		DecompressLogCommand decompressLogCommand,
		LinkShortenerCommand linkShortenerCommand
	) {
		this.discordBot = discordBot;
		this.directMessageCommand = directMessageCommand;
		this.decompressLogCommand = decompressLogCommand;
		this.linkShortenerCommand = linkShortenerCommand;
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
						.addOption(OptionType.STRING, "url", "The URL to shorten")
				).queue();

			jda.addEventListener(directMessageCommand);
			jda.addEventListener(decompressLogCommand);
			jda.addEventListener(linkShortenerCommand);
			logger.info("All command handlers initialized successfully");
		} catch (Exception e) {
			logger.error("Failed to initialize command handlers", e);
		}
	}
}
