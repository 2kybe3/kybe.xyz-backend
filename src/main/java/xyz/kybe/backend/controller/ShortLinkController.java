package xyz.kybe.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.kybe.backend.db.model.ShortLink;
import xyz.kybe.backend.db.repository.ShortLinkRepository;

@RestController
@RequestMapping("/short-links")
public class ShortLinkController {
	private final ShortLinkRepository shortLinkRepository;

	private final boolean redirectNotFound;
	private final String notFoundUrl;

	public ShortLinkController(
		ShortLinkRepository shortLinkRepository,
		@Value("${shortlink.redirectNotFound:true}") boolean redirectNotFound,
		@Value("${shortlink.notFoundUrl:https://kybe.xyz/404.html}") String notFoundUrl
	) {
		this.shortLinkRepository = shortLinkRepository;
		this.redirectNotFound = redirectNotFound;
		this.notFoundUrl = notFoundUrl;
	}

	@GetMapping("/{code}")
	public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String code) {
		var shortLinkOpt = shortLinkRepository.findById(code);
		if (shortLinkOpt.isEmpty() && !redirectNotFound) return ResponseEntity.notFound().build();
		String redirectUrl = shortLinkOpt.map(ShortLink::getRedirectUrl).orElse(notFoundUrl);
		return ResponseEntity.status(302).header("Location", redirectUrl).build();
	}
}
