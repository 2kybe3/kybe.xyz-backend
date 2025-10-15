package xyz.kybe.backend.discord;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DirectMessageCommand extends ListenerAdapter {
	private final Logger logger = LoggerFactory.getLogger(DirectMessageCommand.class);

	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if (!event.getName().equals("Direct Message User")) {
			return;
		}

		User targetUser = event.getTarget();

		TextInput messageInput = TextInput.create("dm_message", "Message", TextInputStyle.PARAGRAPH)
			.setPlaceholder("Enter the message you want to send...")
			.setRequired(true)
			.setMaxLength(2000)
			.build();

		Modal modal = Modal.create("send_dm:" + targetUser.getId(), "Send DM to " + targetUser.getName())
			.addActionRow(messageInput)
			.build();

		event.replyModal(modal).queue();
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		if (!event.getModalId().startsWith("send_dm:")) {
			return;
		}

		String targetUserId = event.getModalId().split(":", 2)[1];
		String message = event.getValue("dm_message").getAsString();

		event.deferReply(true).queue();

		event.getJDA().retrieveUserById(targetUserId).queue(
			targetUser -> {
				targetUser.openPrivateChannel()
					.flatMap(channel -> channel.sendMessage(message))
					.queue(
						success -> {
							event.getHook().editOriginal("Message sent to " + targetUser.getAsMention())
								.queue();
							logger.info("DM sent from {} to {}", event.getUser().getName(), targetUser.getName());
						},
						error -> {
							event.getHook().editOriginal("Failed to send message. The user may have DMs disabled or has blocked you.")
								.queue();
							logger.error("Failed to send DM from {} to {}", event.getUser().getName(), targetUser.getName());
						}
					);
			},
			error -> {
				event.getHook().editOriginal("Failed to retrieve user information.")
					.queue();
				logger.error("Failed to retrieve user by ID: {}", targetUserId, error);
			}
		);
	}
}
