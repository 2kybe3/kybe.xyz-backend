package xyz.kybe.backend.discord.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xyz.kybe.backend.db.model.DiscordQuote;
import xyz.kybe.backend.db.repository.DiscordQuoteRepository;

@Component
public class QuoteMessageCommand extends ListenerAdapter {
	private final Logger logger = LoggerFactory.getLogger(QuoteMessageCommand.class);

	private final DiscordQuoteRepository quoteRepository;

	public QuoteMessageCommand(DiscordQuoteRepository quoteRepository) {
		this.quoteRepository = quoteRepository;
	}

	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if (!event.getName().equals("Get User Quotes")) {
			return;
		}

		try {
			User targetUser = event.getTarget();
			var quotes = quoteRepository.findBySenderId(targetUser.getId());

			if (quotes.isEmpty()) {
				event.reply("No quotes found for user " + targetUser.getName()).queue();
				return;
			}

			StringBuilder response = new StringBuilder("Quotes by " + targetUser.getName() + ":\n");
			for (DiscordQuote quote : quotes) {
				response
					.append("- ")
					.append(quote.getMessageContent())
					.append(" <t:")
					.append(quote.getOriginalMessageTimestamp().getEpochSecond())
					.append(":F>\n");
			}

			event.reply(response.toString()).queue();

			logger.info("Provided {} quotes for user {}", quotes.size(), targetUser.getName());
		} catch (Exception e) {
			logger.error("Failed to retrieve quotes", e);
			event.reply("Failed to retrieve quotes.")
				.queue();
		}
	}

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (!event.getName().equals("Save Quote")) {
			return;
		}

		try {
			Message targetMessage = event.getTarget();
			User sender = targetMessage.getAuthor();

			DiscordQuote quote = new DiscordQuote();
			quote.setSenderId(sender.getId());
			quote.setSenderUsername(sender.getName());
			quote.setSenderAvatarUrl(sender.getAvatarUrl());
			quote.setMessageContent(targetMessage.getContentRaw());
			quote.setMessageId(targetMessage.getId());
			quote.setOriginalMessageTimestamp(targetMessage.getTimeCreated().toInstant());

			quoteRepository.save(quote);

			event.reply("Quote saved").queue();

			logger.info("Quote saved: {} by {}", quote.getId(), sender.getName());
		} catch (Exception e) {
			logger.error("Failed to save quote", e);
			event.reply("Failed to save quote.")
				.setEphemeral(true)
				.queue();
		}
	}
}
