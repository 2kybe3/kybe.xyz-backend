package xyz.kybe.backend.db.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
	name = "discord_quotes"
)
public class DiscordQuote {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String senderId;

	@Column(nullable = false)
	private String senderUsername;

	@Column(nullable = false)
	private String senderAvatarUrl;

	@Column(nullable = false, length = 2000)
	private String messageContent;

	@Column(nullable = false)
	private String messageId;

	@Column(nullable = false)
	private Instant quotedAt;

	@Column
	private Instant originalMessageTimestamp;

	@PrePersist
	protected void onCreate() {
		quotedAt = Instant.now();
	}
}
