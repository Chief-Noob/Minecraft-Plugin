package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.models.PlayerData;
import com.linyuanlin.minecraft.models.Team;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class TeamManager implements CommandExecutor {
	private App app;

	public TeamManager(App app) {
		this.app = app;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		try {
			PlayerData p = app.allPlayers.get(((Player) sender).getUniqueId());

			if (p == null) {
				return false;
			}

			switch (args[0]) {
				case "invite":
					return this.invite(p, args);
				case "join":
					return this.join(p, args);
				case "list":
					return this.list(p);
				case "leave":
					return this.leave(p);
				default:
					return this.help(p);
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			app.discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
			return false;
		}
	}

	private boolean invite(PlayerData senderPlayer, String[] args) {
		if (args.length != 2) {
			senderPlayer.player.sendMessage(ChatColor.RED + "邀請指令錯誤");
			this.help(senderPlayer);
			return false;
		}

		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			senderPlayer.player.sendMessage(
					"你邀請的玩家 " + ChatColor.GOLD + args[1] + ChatColor.WHITE + " 不存在或是不在線上！");
			return false;
		}

		PlayerData receiverPlayer = app.allPlayers.get(p.getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player.sendMessage("你邀請的玩家 " + ChatColor.GOLD + args[1] + ChatColor.WHITE
					+ " 不存在於allPlayers中,請聯繫開發人員");
			return false;
		}

		Optional<Team> team = app.allPlayers.get(receiverPlayer.player.getUniqueId()).team;

		if (receiverPlayer == senderPlayer) {
			senderPlayer.player.sendMessage("不能邀請自己");
			return false;
		}

		if (team.isPresent()) {
			senderPlayer.player.sendMessage(
					ChatColor.GOLD + receiverPlayer.player.getName() + ChatColor.WHITE + "已經有隊伍了！");
			return false;
		}

		if (senderPlayer.isInviteCooling(receiverPlayer)) {
			senderPlayer.player.sendMessage("邀請" + ChatColor.GOLD + receiverPlayer.player.getName()
					+ ChatColor.WHITE + "冷卻中" + ChatColor.RED + "(1分鐘)");
			return false;
		}

		TextComponent msg;
		msg = new TextComponent(
				"[確認 " + ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE + " 的組隊邀請]");
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("點擊接受 " + ChatColor.GOLD
				+ senderPlayer.player.getName() + ChatColor.WHITE + " 的組隊邀請")));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/team join " + senderPlayer.player.getName()));
		receiverPlayer.player.spigot().sendMessage(msg);

		senderPlayer.player.sendMessage("已發送邀請給 " + ChatColor.GOLD + receiverPlayer.player.getName());
		senderPlayer.recordInvite(receiverPlayer);

		return true;
	}

	private boolean join(PlayerData senderPlayer, String[] args) {
		if (args.length != 2) {
			senderPlayer.player.sendMessage("組隊指令錯誤");
			this.help(senderPlayer);
			return false;
		}

		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			senderPlayer.player.sendMessage(
					"你要加入的隊伍的邀請人 " + ChatColor.GOLD + args[1] + ChatColor.WHITE + " 不存在或是不在線上！");
			return false;
		}
		PlayerData receiverPlayer = app.allPlayers.get(p.getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player.sendMessage("你要加入的隊伍的邀請人" + ChatColor.GOLD + args[1] + ChatColor.WHITE
					+ "不存在於allPlayers中, 請聯繫開發人員");
			return false;
		}

		if (senderPlayer.team.isPresent()) {
			senderPlayer.player.sendMessage("你已有隊伍");
			return false;
		}

		if (!senderPlayer.isInvitedBy(receiverPlayer)) {
			senderPlayer.player.sendMessage("你並沒有被邀請至 " + ChatColor.GOLD + receiverPlayer.player.getName()
					+ ChatColor.WHITE + " 的隊伍");
			return false;
		}

		Optional<Team> team = receiverPlayer.team;

		TextComponent msg;
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

			msg = new TextComponent(ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE + "已加入"
					+ senderPlayer.teamCapacityStatus());

			receiverPlayer.destroyInvitedRecord();
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

		receiverPlayer.team.ifPresent(t -> t.sendMessageToAll(msg));

		senderPlayer.destroyInvitedRecord();

		return true;
	}

	private boolean list(PlayerData senderPlayer) {
		Optional<Team> team = senderPlayer.team;
		if (!team.isPresent()) {
			senderPlayer.player.sendMessage("你不在任何隊伍裡");

			return false;
		}

		senderPlayer.player.spigot()
				.sendMessage(new TextComponent("隊伍成員：" + team.get().allTeamMemberString()
						+ ChatColor.GRAY + " | " + ChatColor.WHITE + "隊長:" + ChatColor.GOLD
						+ team.get().leader().player.getName()));

		return true;
	}

	private boolean leave(PlayerData senderPlayer) {
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
				team.get().leader().team = Optional.empty();
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
				senderPlayer.team.ifPresent(t -> t.sendMessageToAll(new TextComponent("隊長 "
						+ ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE
						+ " 離開了隊伍, 新隊長為" + ChatColor.GOLD + team.get().leader().player.getName()
						+ senderPlayer.teamCapacityStatus())));
			} else {
				try {
					team.get().delete(senderPlayer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				senderPlayer.team.ifPresent(t -> t.sendMessageToAll(new TextComponent(
						"隊員 " + ChatColor.GOLD + senderPlayer.player.getName() + ChatColor.WHITE
								+ " 離開了隊伍" + senderPlayer.teamCapacityStatus())));
			}
		} else {
			return false;
		}

		senderPlayer.team = Optional.empty();
		senderPlayer.player.spigot().sendMessage(new TextComponent("你離開了隊伍"));

		return true;
	}

	private boolean help(PlayerData senderPlayer) {
		senderPlayer.player.sendMessage(ChatColor.RED + "沒有這個指令");
		senderPlayer.player.sendMessage(ChatColor.AQUA + "/team 的使用方式：");

		senderPlayer.player.sendMessage("/team invite <對象>" + ChatColor.GRAY +" - 邀請其他玩家加入隊伍");
		senderPlayer.player.sendMessage("/team join <對象>" + ChatColor.GRAY + "  - 加入其他玩家的隊伍");
		senderPlayer.player.sendMessage("/team list" + ChatColor.GRAY + "        - 顯示隊伍狀態");

		senderPlayer.player.sendMessage(ChatColor.GRAY + "/team help" + "        - 取得幫助");
		return false;
	}
}
