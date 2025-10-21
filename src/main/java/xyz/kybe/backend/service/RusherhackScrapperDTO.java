package xyz.kybe.backend.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class RusherhackScrapperDTO {
	private String version;
	private String date;
	private Map<String, Module> modules;
	private Map<String, Command> commands;
	private Map<String, HUDElement> hudElements;

	public static enum SettingType {
		FLOAT,
		COLOR,
		KEYBOARDKEY,
		STRING,
		DOUBLE,
		NULLKEY,
		BOOLEAN,
		MOUSEKEY,
		INTEGER,
		LONG;

		@JsonCreator
		public static SettingType fromString(String key) {
			return key == null ? null : SettingType.valueOf(key.toUpperCase());
		}
	}

	@Getter
	@Setter
	public static class Module {
		private String name;
		private String description;
		private String category;
		private List<Map<String, Setting>> settings;
		private List<String> commands;
	}

	@Getter
	@Setter
	public static class Setting {
		private String name;
		private SettingType type;
		private String description;
		private List<Map<String, EnumOptions>> enumOptions;
		private List<Map<String, Setting>> subSettings;
	}

	@Getter
	@Setter
	public static class EnumOptions {
		private String name;
	}

	@Getter
	@Setter
	public static class Command {
		private String name;
		private String aliases;
		private String description;
		private List<String> syntax;
	}

	@Getter
	@Setter
	public static class HUDElement {
		private String name;
		private String description;
		private List<Map<String, Setting>> settings;
	}
}
