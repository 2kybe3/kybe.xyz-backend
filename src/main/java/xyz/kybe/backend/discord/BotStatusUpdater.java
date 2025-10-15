package xyz.kybe.backend.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class BotStatusUpdater {
	private static final Logger logger = LoggerFactory.getLogger(BotStatusUpdater.class);

	private final DiscordBot discordBot;

	public BotStatusUpdater(DiscordBot discordBot) {
		this.discordBot = discordBot;
	}

	@Scheduled(fixedRateString = "#{60 * 1000}")
	public void updateStatus() {
		try {
			JDA jda = discordBot.getJda();
			if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
				logger.debug("Skipping status update - JDA is not connected");
				return;
			}

			Duration uptime = Duration.between(discordBot.getStartTime(), Instant.now());
			String statusMessage = formatUptime(uptime);

			jda.getPresence().setActivity(Activity.customStatus(statusMessage));

			logger.trace("Updated bot status: {}", statusMessage);
		} catch (Exception e) {
			logger.error("Failed to update bot status", e);
		}
	}

	private String formatUptime(Duration duration) {
		long days = duration.toDays();
		long hours = duration.toHoursPart();
		long minutes = duration.toMinutesPart();

		if (days > 0) {
			return String.format("%dd %dh %dm", days, hours, minutes);
		} else if (hours > 0) {
			return String.format("%dh %dm", hours, minutes);
		} else {
			return String.format("%dm", minutes);
		}
	}
}
