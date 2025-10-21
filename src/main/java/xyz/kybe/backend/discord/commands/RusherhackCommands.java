package xyz.kybe.backend.discord.commands;

import jakarta.validation.constraints.NotNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;
import xyz.kybe.backend.service.RusherhackScrapperDTO;
import xyz.kybe.backend.service.RusherhackScrapperService;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class RusherhackCommands extends ListenerAdapter {
	private final RusherhackScrapperService scrapper;

	public RusherhackCommands(RusherhackScrapperService scrapper) {
		this.scrapper = scrapper;
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if (!event.getName().equals("rusher-info")) return;

		long start = System.nanoTime();

		String query = Objects.requireNonNull(event.getOption("search")).getAsString().trim();

		try {
			RusherhackScrapperDTO dto = scrapper.fetchClientJson();

			RusherhackScrapperDTO.Module module = dto.getModules().get(query);
			RusherhackScrapperDTO.Command command = dto.getCommands().get(query);
			RusherhackScrapperDTO.HUDElement hud = dto.getHudElements().get(query);

			if (module == null && command == null && hud == null) {
				event.reply("No module, command, or HUD element found named `" + query + "`.").setEphemeral(true).queue();
				return;
			}

			long durationMs = (System.nanoTime() - start) / 1_000_000;

			EmbedBuilder embed = new EmbedBuilder()
				.setTitle("RusherHack Info: " + query)
				.setColor(Color.CYAN)
				.setFooter("Generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " | " + query + " | " + durationMs + "ms");

			if (module != null) {
				embedModule(embed, module);
			} else if (command != null) {
				embedCommand(embed, command);
			} else {
				embedHUDElement(embed, hud);
			}

			event.replyEmbeds(embed.build()).queue();
		} catch (Exception e) {
			event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
		}
	}

	private void embedModule(EmbedBuilder embed, RusherhackScrapperDTO.Module module) {
		embed.addField("Type", "Module", false);
		embed.addField("Category", module.getCategory(), false);
		embed.addField("Description", orDefault(module.getDescription()), false);

		if (module.getSettings() != null && !module.getSettings().isEmpty()) {
			StringBuilder settingsText = new StringBuilder("```");
			List<Map<String, RusherhackScrapperDTO.Setting>> settings = module.getSettings();

			for (int i = 0; i < settings.size(); i++) {
				Map<String, RusherhackScrapperDTO.Setting> map = settings.get(i);
				int mapIndex = 0;
				for (var entry : map.entrySet()) {
					boolean isLast = (i == settings.size() - 1) && (mapIndex == map.size() - 1);
					settingsText.append(formatSettingTree(entry.getKey(), entry.getValue(), "", isLast));
					mapIndex++;
				}
			}

			settingsText.append("```");
			embed.addField("Settings", settingsText.toString(), false);
		}
	}

	private void embedCommand(EmbedBuilder embed, RusherhackScrapperDTO.Command command) {
		embed.addField("Type", "Command", false);
		embed.addField("Description", orDefault(command.getDescription()), false);
		if (command.getSyntax() != null && !command.getSyntax().isEmpty())
			embed.addField("Syntax", String.join("\n", command.getSyntax()), false);
		if (command.getAliases() != null && !command.getAliases().isBlank())
			embed.addField("Aliases", command.getAliases(), false);
	}

	private void embedHUDElement(EmbedBuilder embed, RusherhackScrapperDTO.HUDElement hud) {
		embed.addField("Type", "HUD Element", false);
		embed.addField("Description", orDefault(hud.getDescription()), false);

		if (hud.getSettings() != null && !hud.getSettings().isEmpty()) {
			StringBuilder settingsText = new StringBuilder("```");
			List<Map<String, RusherhackScrapperDTO.Setting>> settings = hud.getSettings();

			for (int i = 0; i < settings.size(); i++) {
				Map<String, RusherhackScrapperDTO.Setting> map = settings.get(i);
				int mapIndex = 0;
				for (var entry : map.entrySet()) {
					boolean isLast = (i == settings.size() - 1) && (mapIndex == map.size() - 1);
					settingsText.append(formatSettingTree(entry.getKey(), entry.getValue(), "", isLast));
					mapIndex++;
				}
			}

			settingsText.append("```");
			embed.addField("Settings", settingsText.toString(), false);
		}
	}

	private String formatSettingTree(String name, RusherhackScrapperDTO.Setting setting, String prefix, boolean isLast) {
		StringBuilder sb = new StringBuilder();
		String branch = isLast ? "└─ " : "├─ ";
		sb.append(prefix).append(branch).append(name);

		if (setting.getEnumOptions() != null && !setting.getEnumOptions().isEmpty()) {
			List<String> modeNames = setting.getEnumOptions().stream()
				.flatMap(map -> map.keySet().stream())
				.collect(java.util.stream.Collectors.toList());

			if (!modeNames.isEmpty()) {
				sb.append(" (").append(String.join(", ", modeNames)).append(")");
			} else {
				sb.append(" (").append(setting.getType()).append(")");
			}
		} else if (setting.getType() != null) {
			sb.append(" (").append(setting.getType()).append(")");
		}
		sb.append("\n");

		if (setting.getSubSettings() != null && !setting.getSubSettings().isEmpty()) {
			List<Map<String, RusherhackScrapperDTO.Setting>> subs = setting.getSubSettings();
			for (int i = 0; i < subs.size(); i++) {
				Map<String, RusherhackScrapperDTO.Setting> subMap = subs.get(i);
				int j = 0;
				for (var subEntry : subMap.entrySet()) {
					boolean last = (i == subs.size() - 1) && (j == subMap.size() - 1);
					String newPrefix = prefix + (isLast ? "    " : "│   ");
					sb.append(formatSettingTree(subEntry.getKey(), subEntry.getValue(), newPrefix, last));
					j++;
				}
			}
		}

		return sb.toString();
	}

	@Override
	public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
		if (!event.getName().equals("rusher-info") || !event.getFocusedOption().getName().equals("search")) return;

		try {
			RusherhackScrapperDTO dto = scrapper.fetchClientJson();
			List<String> names = new ArrayList<>();
			names.addAll(dto.getModules().keySet());
			names.addAll(dto.getCommands().keySet());
			names.addAll(dto.getHudElements().keySet());

			List<Command.Choice> matches = names.stream()
				.filter(n -> n.toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()))
				.limit(25)
				.map(n -> new Command.Choice(n, n))
				.toList();

			event.replyChoices(matches).queue();
		} catch (Exception e) {
			event.replyChoices(List.of()).queue();
		}
	}

	private String orDefault(String s) {
		return s == null || s.isBlank() ? "No description provided." : s;
	}
}