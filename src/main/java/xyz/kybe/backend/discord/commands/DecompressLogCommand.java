package xyz.kybe.backend.discord.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

@Component
public class DecompressLogCommand extends ListenerAdapter {
	private final Logger logger = LoggerFactory.getLogger(DecompressLogCommand.class);

	@Override
	public void onMessageContextInteraction(MessageContextInteractionEvent event) {
		if (!event.getName().equals("Decompress Log")) {
			return;
		}

		event.deferReply().queue();

		try {
			Message targetMessage = event.getTarget();

			if (targetMessage.getAttachments().isEmpty()) {
				event.getHook().editOriginal("No attachments found in this message.").queue();
				return;
			}

			Message.Attachment gzAttachment = targetMessage.getAttachments().stream()
				.filter(att -> att.getFileName().endsWith(".log.gz") || att.getFileName().endsWith(".gz"))
				.findFirst()
				.orElse(null);

			if (gzAttachment == null) {
				event.getHook().editOriginal("No .log.gz or .gz file found in this message.").queue();
				return;
			}

			URL url = new URL(gzAttachment.getUrl());
			try (InputStream inputStream = url.openStream();
				 GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
				 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

				byte[] buffer = new byte[8192];
				int bytesRead;
				while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				byte[] decompressedData = outputStream.toByteArray();

				String originalName = gzAttachment.getFileName();
				String newName = originalName.replace(".log.gz", "");
				newName = newName.replace(".gz", "");

				event.getHook().editOriginal("Decompressed " + originalName + " (" + gzAttachment.getSize() + " bytes -> " + decompressedData.length + " bytes)")
					.setFiles(FileUpload.fromData(decompressedData, newName))
					.queue();

				logger.info("Decompressed {} ({} bytes -> {} bytes) for user {}", originalName, gzAttachment.getSize(), decompressedData.length, event.getUser().getName());
			}

		} catch (Exception e) {
			logger.error("Failed to decompress log file", e);
			event.getHook().editOriginal("Failed to decompress file: " + e.getMessage()).queue();
		}
	}
}
