package xyz.kybe.backend.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DiscordBot {
	private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);

	@Getter private final Instant startTime;

	private final String token;

	@Getter private final String adminUserId;
	@Getter private final String logChannelId;

	@Getter private JDA jda;
	@Getter private TextChannel logChannel;

	public DiscordBot(
		@Value("${discord.token}") String token,
		@Value("${discord.admin-id}") String adminUserId,
		@Value("${discord.log-channel}") String logChannelId
	) {
		this.startTime = Instant.now();
		this.token = token;
		this.adminUserId = adminUserId;
		this.logChannelId = logChannelId;
	}

	@PostConstruct
	public void start() {
		try {
			logger.info("Starting Discord bot...");

			jda = JDABuilder.createDefault(token)
				.enableIntents(
					GatewayIntent.MESSAGE_CONTENT
				)
				.disableCache(
					CacheFlag.VOICE_STATE,
					CacheFlag.EMOJI,
					CacheFlag.STICKER,
					CacheFlag.SCHEDULED_EVENTS
				)
				.build();

			jda.awaitReady();
			logger.info("Discord bot connected successfully in {} ms", Instant.now().toEpochMilli() - startTime.toEpochMilli());

			logChannel = initializeChannel(logChannelId, "log");

			logger.info("Discord bot started successfully in {} ms", Instant.now().toEpochMilli() - startTime.toEpochMilli());

			logChannel.sendMessage("Discord bot started successfully in " + (Instant.now().toEpochMilli() - startTime.toEpochMilli()) + " ms").queue();
		} catch (Exception e) {
			logger.error("Failed to start Discord bot", e);
			throw new IllegalStateException("Failed to start Discord bot", e);
		}
	}

	@PreDestroy
	public void shutdown() {
		if (jda == null) return;
		logger.info("Shutting down Discord bot...");
		jda.shutdown();
		logger.info("Discord bot shut down successfully");
	}

	public boolean isAdmin(Member member) {
		if (member == null) {
			return false;
		}
		return member.getId().equals(adminUserId);
	}

	private TextChannel initializeChannel(String channelId, String channelName) {
		if (jda == null) {
			logger.error("Cannot initialize {} channel: JDA is not ready", channelName);
			StackTraceElement[] st = Thread.currentThread().getStackTrace();
			logger.error("Stack trace: ");
			for (StackTraceElement element : st) {
				logger.error("	at {}", element.toString());
			}
			return null;
		}

		TextChannel channel = jda.getTextChannelById(channelId);

		if (channel != null) {
			logger.info("Successfully initialized {} channel: {} (ID: {})", channelName, channel.getName(), channelId);
		} else {
			logger.error("Failed to find {} channel with ID: {}. Please verify the channel ID in configuration.", channelName, channelId);
		}

		return channel;
	}
}
