package xyz.kybe.backend.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.kybe.backend.db.model.ShortLink;
import xyz.kybe.backend.db.repository.ShortLinkRepository;
import xyz.kybe.backend.discord.DiscordBot;

import java.util.Objects;
import java.util.UUID;

@Component
public class LinkShortenerCommand extends ListenerAdapter {
	private final DiscordBot discordBot;
	private final ShortLinkRepository shortLinkRepository;

	private final String prefix;

	public LinkShortenerCommand(
		DiscordBot discordBot,
		ShortLinkRepository shortLinkRepository,
		@Value("${shortlink.prefix:https://s.kybe.xyz/}") String prefix
	) {
		this.discordBot = discordBot;
		this.shortLinkRepository = shortLinkRepository;
		this.prefix = prefix;
	}

	@Override
	@Transactional
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (!event.getName().equals("shorten-link")) return;

		if (!event.getUser().getId().equals(discordBot.getAdminUserId())) {
			event.reply("You do not have permission to use this command.").queue();
			return;
		}

		String url = event.getOption("url") != null ? Objects.requireNonNull(event.getOption("url")).getAsString() : null;
		if (url == null || url.isBlank()) {
			event.reply("Please provide a valid URL.").queue();
			return;
		}

		var existing = shortLinkRepository.getByRedirectUrl(url);
		if (!existing.isEmpty()) {
			event.reply(this.prefix+existing.getFirst().getCode()).queue();
			return;
		}

		String code = UUID.randomUUID().toString().substring(0, 8);
		ShortLink shortLink = new ShortLink();
		shortLink.setCode(code);
		shortLink.setRedirectUrl(url);
		shortLinkRepository.save(shortLink);

		event.reply(this.prefix+code).queue();
	}
}
