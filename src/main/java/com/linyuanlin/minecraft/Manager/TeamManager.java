package com.linyuanlin.minecraft.manager;

import com.linyuanlin.minecraft.App;
import com.linyuanlin.minecraft.models.*;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class TeamManager implements CommandExecutor {
	/*
	 * constants
	 */
	public final static int INVITE_COOLING_MINS = 1;

	public static App getPlugin() {
		return JavaPlugin.getPlugin(App.class);
	}

	public TeamManager() {
		Objects.requireNonNull(getPlugin().getCommand("team")).setExecutor(this);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		try {
			PlayerData p = getPlugin().allPlayers.get(((Player) sender).getUniqueId());
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
			getPlugin().discordBotManager.sendMessage("TEST", "Project-Minecraft", sw.toString());
			return false;
		}
	}

	/*
	 * sender invite receiver to his team
	 *
	 * Behavior:
	 *
	 * 1. send a invitation message to receiver's chat box
	 *
	 * 2. record the invitation record to inviteTimeMap and invitedTimeMap
	 *
	 * 3. set the inviting cooling for sender
	 *
	 * Exception:
	 *
	 * 1. receiver doesn't exist(off-line or not exist)
	 *
	 * 2. receiver is sender himself
	 *
	 * 3. receiver already has team
	 *
	 * 4. sender already invite receiver(cooling)
	 *
	 */
	private boolean invite(PlayerData senderPlayer, String[] args) {
		if (args.length != 2) {
			senderPlayer.player().sendMessage(ChatColor.RED + "??????????????????");
			this.help(senderPlayer);
			return false;
		}

		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			senderPlayer.player().sendMessage(
					"?????????????????? " + ChatColor.GOLD + args[1] + ChatColor.WHITE + " ??????????????????????????????");
			return false;
		}

		PlayerData receiverPlayer = getPlugin().allPlayers.get(p.getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player().sendMessage("?????????????????? " + ChatColor.GOLD + args[1] + ChatColor.WHITE
					+ " ????????????allPlayers???,?????????????????????");
			return false;
		}

		Optional<Team> team = getPlugin().allPlayers.get(receiverPlayer.player().getUniqueId()).team();

		if (receiverPlayer == senderPlayer) {
			senderPlayer.player().sendMessage(ChatColor.RED + "??????????????????");
			return false;
		}

		if (team.isPresent()) {
			senderPlayer.player().sendMessage(ChatColor.GOLD + receiverPlayer.player().getName()
					+ ChatColor.WHITE + "?????????????????????");
			return false;
		}

		if (senderPlayer.isInviteCooling(receiverPlayer)) {
			senderPlayer.player().sendMessage("??????" + ChatColor.GOLD + receiverPlayer.player().getName()
					+ ChatColor.WHITE + "?????????" + ChatColor.RED + "(1??????)");
			return false;
		}

		TextComponent msg;
		msg = new TextComponent("[?????? " + ChatColor.GOLD + senderPlayer.player().getName() + ChatColor.WHITE
				+ " ???????????????]");
		msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("???????????? " + ChatColor.GOLD
				+ senderPlayer.player().getName() + ChatColor.WHITE + " ???????????????")));
		msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				"/team join " + senderPlayer.player().getName()));
		receiverPlayer.player().spigot().sendMessage(msg);

		senderPlayer.player().sendMessage("?????????????????? " + ChatColor.GOLD + receiverPlayer.player().getName());
		senderPlayer.recordInvite(receiverPlayer);

		return true;
	}

	/*
	 * sender joins receiver's team
	 *
	 * Behavior:
	 *
	 * 1. if receiver doesn't have a team initially, create a team with receiver as
	 * the leader and add both receiver and sender to this team
	 *
	 * 2. else, add sender to receiver's team as a team member
	 *
	 * Exception:
	 *
	 * 1. receiver doesn't exist(off-line or not exist)
	 *
	 * 2. sender already has team
	 *
	 * 3. sender is not invited by receiver
	 *
	 * 4. receiver's team is full
	 *
	 */
	private boolean join(PlayerData senderPlayer, String[] args) throws Exception {
		if (args.length != 2) {
			senderPlayer.player().sendMessage(ChatColor.RED + "??????????????????");
			this.help(senderPlayer);
			return false;
		}

		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			senderPlayer.player().sendMessage(
					"????????????????????????????????? " + ChatColor.GOLD + args[1] + ChatColor.WHITE + " ??????????????????????????????");
			return false;
		}
		PlayerData receiverPlayer = getPlugin().allPlayers.get(p.getUniqueId());
		if (receiverPlayer == null) {
			senderPlayer.player().sendMessage("?????????????????????????????????" + ChatColor.GOLD + args[1] + ChatColor.WHITE
					+ "????????????allPlayers???, ?????????????????????");
			return false;
		}

		if (senderPlayer.team().isPresent()) {
			senderPlayer.player().sendMessage(ChatColor.RED + "???????????????");
			return false;
		}

		if (!senderPlayer.isInvitedBy(receiverPlayer)) {
			senderPlayer.player().sendMessage("???????????????????????? " + ChatColor.GOLD
					+ receiverPlayer.player().getName() + ChatColor.WHITE + " ?????????");
			return false;
		}

		Optional<Team> team = receiverPlayer.team();

		TextComponent msg;
		if (!team.isPresent()) {
			Team newTeam = new Team();
			try {
				newTeam.add(receiverPlayer);
				newTeam.add(senderPlayer);
			} catch (Exception e) {
				e.printStackTrace();
			}

			receiverPlayer.replaceTeam(Optional.of(newTeam));
			senderPlayer.replaceTeam(Optional.of(newTeam));

			msg = new TextComponent(ChatColor.GOLD + senderPlayer.player().getName() + ChatColor.WHITE
					+ "?????????" + senderPlayer.teamCapacityStatus());

			receiverPlayer.destroyInvitedRecord();
		} else if (!team.get().isFull()) {
			try {
				team.get().add(senderPlayer);
			} catch (Exception e) {
				e.printStackTrace();
			}

			senderPlayer.replaceTeam(team);

			msg = new TextComponent(ChatColor.GOLD + senderPlayer.player().getName() + ChatColor.WHITE
					+ "?????????" + senderPlayer.teamCapacityStatus());
		} else {
			senderPlayer.player().sendMessage(ChatColor.RED + "????????????");

			return false;
		}

		receiverPlayer.team().ifPresent(t -> t.sendMessageToAll(msg));

		senderPlayer.destroyInvitedRecord();

		return true;
	}

	/*
	 * List the player's team status
	 *
	 * Behavior:
	 *
	 * 1. send a team list to player's chat box
	 *
	 * Exception:
	 *
	 * 1. player doesn't have a team
	 *
	 */
	private boolean list(PlayerData senderPlayer) {
		Optional<Team> team = senderPlayer.team();
		if (!team.isPresent()) {
			senderPlayer.player().sendMessage(ChatColor.RED + "????????????????????????");

			return false;
		}

		senderPlayer.player().spigot()
				.sendMessage(new TextComponent("???????????????" + team.get().allTeamMemberString()
						+ ChatColor.GRAY + " | " + ChatColor.WHITE + "??????:" + ChatColor.GOLD
						+ team.get().leader().player().getName()));

		return true;
	}

	/*
	 * Player leaves his team
	 *
	 * Behavior:
	 *
	 * 1. if sender's team has more than 2 people(>=3), the team will be kept; else
	 * the team will be destroy.
	 *
	 * 2. then if sender is the leader, a new leader will be assigned
	 *
	 * Exception:
	 *
	 * 1. player doesn't have team
	 *
	 */
	private boolean leave(PlayerData senderPlayer) {
		Optional<Team> team = senderPlayer.team();
		if (!team.isPresent()) {
			senderPlayer.player().sendMessage(ChatColor.RED + "???????????????");
			return false;
		}

		if (team.get().size() == 2) {
			team.get().sendMessageToAll(new TextComponent(ChatColor.GOLD + senderPlayer.player().getName()
					+ ChatColor.WHITE + " ???????????????, ?????????????????????????????????"));
			try {
				team.get().delete(senderPlayer);
				team.get().delete(team.get().leader());
				team.get().leader().replaceTeam(Optional.empty());
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
				senderPlayer.team()
						.ifPresent(t -> t.sendMessageToAll(new TextComponent("?????? "
								+ ChatColor.GOLD + senderPlayer.player().getName()
								+ ChatColor.WHITE + " ???????????????, ????????????" + ChatColor.GOLD
								+ team.get().leader().player().getName()
								+ senderPlayer.teamCapacityStatus())));
			} else {
				try {
					team.get().delete(senderPlayer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				senderPlayer.team().ifPresent(t -> t.sendMessageToAll(new TextComponent("?????? "
						+ ChatColor.GOLD + senderPlayer.player().getName() + ChatColor.WHITE
						+ " ???????????????" + senderPlayer.teamCapacityStatus())));
			}
		} else {
			return false;
		}

		senderPlayer.replaceTeam(Optional.empty());
		senderPlayer.player().sendMessage("??????????????????");

		return true;
	}

	/*
	 * List the helping command for /team
	 *
	 * Behavior:
	 *
	 * send all command messages related to \team to sender's chat box
	 *
	 * Exception:
	 *
	 * Nan
	 *
	 */
	private boolean help(PlayerData senderPlayer) {
		senderPlayer.player().sendMessage(ChatColor.RED + "??????????????????");
		senderPlayer.player().sendMessage(ChatColor.AQUA + "/team ??????????????????");

		senderPlayer.player().sendMessage("/team invite <??????>" + ChatColor.GRAY + " - ??????????????????????????????");
		senderPlayer.player().sendMessage("/team join <??????>" + ChatColor.GRAY + " - ???????????????????????????");
		senderPlayer.player().sendMessage("/team list" + ChatColor.GRAY + " - ??????????????????");

		senderPlayer.player().sendMessage(ChatColor.GRAY + "/team help - ????????????");
		return false;
	}
}
