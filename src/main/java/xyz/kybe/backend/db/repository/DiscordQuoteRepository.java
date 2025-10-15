package xyz.kybe.backend.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.kybe.backend.db.model.DiscordQuote;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiscordQuoteRepository extends JpaRepository<DiscordQuote, UUID> {
	List<DiscordQuote> findBySenderId(String senderId);
	List<DiscordQuote> findByMessageId(String messageId);
}
