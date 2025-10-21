package xyz.kybe.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Service
public class RusherhackScrapperService {

	private final String URL = "https://rusherhack.org/assets/client.json";
	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public RusherhackScrapperService() {
		objectMapper.registerModule(new Jdk8Module());
	}

	@Cacheable("clientJson")
	public RusherhackScrapperDTO fetchClientJson() {
		log.info("Fetching JSON from remote URL...");
		String jsonString = restTemplate.getForObject(URL, String.class);

		try {
			RusherhackScrapperDTO dto = objectMapper.readValue(jsonString, RusherhackScrapperDTO.class);

			log.info("Parsed version: {}", dto.getVersion());
			log.info("Number of modules: {}", dto.getModules() != null ? dto.getModules().size() : 0);

			return dto;
		} catch (IOException e) {
			log.error("Error parsing JSON", e);
			throw new RuntimeException("Failed to parse client JSON", e);
		}
	}

	public String convertDtoToJson(RusherhackScrapperDTO dto) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);
		} catch (IOException e) {
			log.error("Error converting DTO to JSON", e);
			throw new RuntimeException("Failed to convert DTO to JSON", e);
		}
	}

	@CacheEvict("clientJson")
	public void resetCache() {
	}
}
