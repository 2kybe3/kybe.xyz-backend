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
import java.util.Random;

@Component
public class LinkShortenerCommand extends ListenerAdapter {
	private static final int[][] EMOJI_RANGES = {
		{0x1F600, 0x1F64F}, // Emoticons
		{0x1F300, 0x1F5FF}, // Misc Symbols & Pictographs
		{0x1F680, 0x1F6FF}, // Transport & Map Symbols
		{0x1F900, 0x1F9FF}, // Supplemental Symbols & Pictographs
		{0x2600, 0x26FF}    // Misc symbols
	};
	private final DiscordBot discordBot;
	private final ShortLinkRepository shortLinkRepository;
	private final String prefix;
	private final Random random = new Random();

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

		String mode = event.getOption("mode") != null ? Objects.requireNonNull(event.getOption("mode")).getAsString().toLowerCase() : "full";

		String code;
		do {
			code = switch (mode) {
				case "emoji" -> generateEmojiCode(8);
				case "chinese" -> generateChineseCode(8);
				case "full" -> generateFullUnicodeCode(2);
				case "ascii" -> generateAsciiCode(8);
				default -> generateFullUnicodeCode(2);
			};
		} while (shortLinkRepository.existsById(code));

		ShortLink shortLink = new ShortLink();
		shortLink.setCode(code);
		shortLink.setRedirectUrl(url);
		shortLinkRepository.save(shortLink);

		event.reply(this.prefix + code).queue();
	}

	private String generateFullUnicodeCode(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int codePoint;
			while (true) {
				codePoint = random.nextInt(Character.MAX_CODE_POINT + 1);
				if (Character.isSurrogate((char) codePoint)) continue;
				if (Character.isWhitespace(codePoint)) continue;
				if ("\"#%/<>?\\^`{|}".indexOf(codePoint) >= 0) continue;
				break;
			}
			sb.appendCodePoint(codePoint);
		}
		return sb.toString();
	}

	private String generateAsciiCode(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

	private String generateEmojiCode(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int[] range = EMOJI_RANGES[random.nextInt(EMOJI_RANGES.length)];
			int codePoint = range[0] + random.nextInt(range[1] - range[0] + 1);
			sb.append(Character.toChars(codePoint));
		}
		return sb.toString();
	}

	private String generateChineseCode(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int codePoint = 0x4E00 + random.nextInt(0x9FFF - 0x4E00 + 1);
			sb.append((char) codePoint);
		}
		return sb.toString();
	}
}