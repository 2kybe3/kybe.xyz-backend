package xyz.kybe.backend.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.kybe.backend.db.model.ShortLink;

import java.util.List;

public interface ShortLinkRepository extends JpaRepository<ShortLink, String> {
	List<ShortLink> getByCode(String code);

	List<ShortLink> getByRedirectUrl(String redirectUrl);
}
