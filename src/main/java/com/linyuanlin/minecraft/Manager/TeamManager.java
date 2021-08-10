package com.linyuanlin.minecraft.Manager;

import com.linyuanlin.minecraft.models.PlayerData;
import com.linyuanlin.minecraft.models.Team;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.linyuanlin.minecraft.App;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class TeamManager {
	private HashMap<UUID, PlayerData> allPlayers;

	public TeamManager(App app) {
		this.allPlayers = app.allPlayers;
	}

	public boolean onCommandTeam(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		switch (args[0]) {
			case "invite":
				return this.invite(sender, cmd, cmdlable, args, senderPlayer);
			case "join":
				return this.join(sender, cmd, cmdlable, args, senderPlayer);
			case "list":
				return this.list(sender, cmd, cmdlable, args, senderPlayer);
			case "leave":
				return this.leave(sender, cmd, cmdlable, args, senderPlayer);
			default:
				return this.help(sender, cmd, cmdlable, args, senderPlayer);
		}
	}

	private boolean invite(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		PlayerData receiverPlayer = allPlayers.get((Object) Bukkit.getPlayer(args[1]).getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player.sendMessage("你邀請的玩家" + ChatColor.GOLD + args[1] + ChatColor.WHITE + "不存在");
			return false;
		}

		TextComponent msg = new TextComponent();
		Optional<Team> team = allPlayers.get(receiverPlayer.player.getUniqueId()).team;

		if (team.isPresent()) {
			msg = new TextComponent(
					ChatColor.GOLD + receiverPlayer.player.getName() + ChatColor.WHITE + "已經有隊伍了！");
			senderPlayer.player.spigot().sendMessage(msg);
			return false;
		}

		if (senderPlayer.isInviteCooling(receiverPlayer)) {
			sender.sendMessage("邀請" + ChatColor.GOLD + receiverPlayer.player.getName() + ChatColor.WHITE
					+ "冷卻中" + ChatColor.RED + "(1分鐘)");
			return false;
		}

		msg = new TextComponent(
				"[確認 " + ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE + " 的組隊邀請]");
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊接受 " + ChatColor.GOLD
				+ senderPlayer.player.getName() + ChatColor.WHITE + " 的組隊邀請")));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/team join " + senderPlayer.player.getName()));
		receiverPlayer.player.spigot().sendMessage(msg);

		senderPlayer.player.spigot().sendMessage(
				new TextComponent("已發送邀請給 " + ChatColor.GOLD + receiverPlayer.player.getName()));
		senderPlayer.recordInvite(receiverPlayer);

		return true;
	}

	private boolean join(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		PlayerData receiverPlayer = allPlayers.get((Object) Bukkit.getPlayer(args[1]).getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player.sendMessage(
					"你要加入的隊伍的邀請人" + ChatColor.GOLD + args[1] + ChatColor.WHITE + "不存在");

			return false;
		}

		if (senderPlayer.team.isPresent()) {
			senderPlayer.player.sendMessage("你已有隊伍");

			return false;
		}

		if (!senderPlayer.isInvitedBy(receiverPlayer)) {
			sender.sendMessage("你並沒有被邀請至 " + ChatColor.GOLD + receiverPlayer.player.getName()
					+ ChatColor.WHITE + " 的隊伍");

			return false;
		}

		TextComponent msg = new TextComponent("");
		Optional<Team> team = receiverPlayer.team;

		if (!team.isPresent()) {
			Team newTeam = new Team();
			try {
				newTeam.add(receiverPlayer);
				newTeam.add(senderPlayer);
			} catch (Exception e) {
				e.printStackTrace();
			}

			receiverPlayer.team = Optional.of(newTeam);
			senderPlayer.team = Optional.of(newTeam);

			newTeam.newLeader();

			msg = new TextComponent(ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE + "已加入"
					+ senderPlayer.teamCapacityStatus());
		} else if (!team.get().isFull()) {
			try {
				team.get().add(senderPlayer);
			} catch (Exception e) {
				e.printStackTrace();
			}

			senderPlayer.team = team;

			msg = new TextComponent(ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE + "已加入"
					+ senderPlayer.teamCapacityStatus());
		} else {
			senderPlayer.player.sendMessage("隊伍已滿");

			return false;
		}

		receiverPlayer.team.get().sendMessageToAll(msg);

		receiverPlayer.destroyInviteRecord(senderPlayer);

		return true;
	}

	private boolean list(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		Optional<Team> team = senderPlayer.team;

		if (!team.isPresent()) {
			senderPlayer.player.sendMessage("你不在任何隊伍裡");

			return false;
		}

		senderPlayer.player.spigot().sendMessage(new TextComponent(ChatColor.GREEN + "隊伍成員："
				+ team.get().allTeamMemberString() + ChatColor.BLACK + " | " + ChatColor.GREEN + "隊長:"
				+ ChatColor.GOLD + team.get().leader().player.getName()));

		return true;
	}

	private boolean leave(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		Optional<Team> team = senderPlayer.team;

		if (!team.isPresent()) {
			senderPlayer.player.spigot().sendMessage(new TextComponent("你沒有隊伍"));
			return false;
		}

		if (team.get().size() == 2) {
			team.get().sendMessageToAll(new TextComponent(ChatColor.GOLD + senderPlayer.player.getName()
					+ ChatColor.WHITE + " 離開了隊伍, 隊伍人數不足，自動解散"));
			try {
				team.get().delete(senderPlayer);
				team.get().delete(team.get().leader());

				// team = Optional.empty()?
				// team.get().leader().team = Optional.empty();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (team.get().size() > 2) {
			if (team.get().leader() == senderPlayer) {
				try {
					team.get().delete(senderPlayer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				senderPlayer.team.get().sendMessageToAll(new TextComponent("隊長 " + ChatColor.GOLD
						+ senderPlayer.player.getName() + ChatColor.WHITE + " 離開了隊伍, 新隊長為"
						+ ChatColor.GOLD + team.get().leader().player.getName()
						+ senderPlayer.teamCapacityStatus()));
			} else {
				try {
					team.get().delete(senderPlayer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				senderPlayer.team.get()
						.sendMessageToAll(new TextComponent("隊員 " + ChatColor.GOLD
								+ senderPlayer.player.getName() + ChatColor.WHITE
								+ " 離開了隊伍" + senderPlayer.teamCapacityStatus()));
			}
		} else {
			return false;
		}

		team = Optional.empty();
		// senderPlayer.team = Optional.empty();
		senderPlayer.player.spigot().sendMessage(new TextComponent("你離開了隊伍"));

		return true;
	}

	private boolean help(CommandSender sender, Command cmd, String cmdlable, String[] args,
			PlayerData senderPlayer) {
		senderPlayer.player.sendMessage("沒有這個指令");
		senderPlayer.player.sendMessage(ChatColor.BLACK + "/team 的使用方式：");
		senderPlayer.player.sendMessage(ChatColor.GRAY + "/team invite <對象>");
		senderPlayer.player.sendMessage(ChatColor.GRAY + "/team join <對象>");
		senderPlayer.player.sendMessage(ChatColor.GRAY + "/team list");
		senderPlayer.player.sendMessage(ChatColor.GRAY + "/team help <對象>");

		return false;
	}
}
