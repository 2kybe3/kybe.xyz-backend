package xyz.kybe.backend.db.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
	name = "short_links",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"code", "redirect_url"})
	}
)
public class ShortLink {
	@Id
	@Column(nullable = false, unique = true)
	private String code;

	@Column(name ="redirect_url", nullable = false)
	private String redirectUrl;
}
