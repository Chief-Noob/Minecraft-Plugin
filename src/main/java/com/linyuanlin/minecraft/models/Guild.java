package com.linyuanlin.minecraft.models;

import java.util.*;

import org.bukkit.entity.Player;
import static java.util.Map.entry;

public class Guild {
	private Player president;
	private List<Player> memberList, lordList, vicePresidentList;
	private int Capacity = 10;
	private boolean freeJoin = true;
	private int level = 1;

	private static class LevelAttributes {
		private int cost, memberNums, lordNums, vicePresidentNums;

		public LevelAttributes(int cost, int memberNums, int lordNums, int vicePresidentNums) {
			this.cost = cost;
			this.memberNums = memberNums;
			this.lordNums = lordNums;
			this.vicePresidentNums = vicePresidentNums;
		}

		public int cost() {
			return this.cost;
		}

		public int memberNums() {
			return this.memberNums;
		}

		public int lordNums() {
			return this.lordNums;
		}

		public int vicePresidentNums() {
			return this.vicePresidentNums;
		}
	}

	private final static LevelAttributes level_1 = new LevelAttributes(0, 20, 5, 2);
	private final static LevelAttributes level_2 = new LevelAttributes(2000, 25, 6, 3);
	private final static LevelAttributes level_3 = new LevelAttributes(5000, 30, 7, 4);
	private final static LevelAttributes level_4 = new LevelAttributes(10000, 40, 8, 4);
	private final static LevelAttributes level_5 = new LevelAttributes(20000, 50, 10, 5);

	private final static Map<Integer, LevelAttributes> levelMap = Map.ofEntries(entry(1, level_1),
			entry(2, level_2), entry(3, level_3), entry(4, level_4), entry(5, level_5));

	public boolean isMemberFull() {
		return this.memberList.size() > getLevelAttributes(this.level, "memberNums");
	}

	public boolean isLordFull() {
		return this.lordList.size() > getLevelAttributes(this.level, "lordNums");
	}

	public boolean isVicePresidentFull() {
		return this.vicePresidentList.size() > getLevelAttributes(this.level, "vicePresidentNums");
	}

	public Player president() {
		return this.president;
	}

	public boolean isMember(PlayerData p) {
		return this.memberList.contains((Object) p.player());
	}

	public boolean isVicePresident(PlayerData p) {
		return this.vicePresidentList.contains((Object) p.player());
	}

	public boolean isLord(PlayerData p) {
		return this.lordList.contains((Object) p.player());
	}

	public int capacity() {
		return this.Capacity;
	}

	public int level() {
		return this.level;
	}

	public void setFreeJoin(boolean b) {
		this.freeJoin = b;
	}

	public void upgrade() {
		this.level++;
	}

	public boolean isFreeJoin() {
		return freeJoin;
	}

	public void newPresident(PlayerData p) {
		president = p.player();
	}

	public boolean addMember(PlayerData p) {
		if (this.isMemberFull()) {
			return false;
		}

		return memberList.add(p.player());
	}

	public boolean addLord(PlayerData p) {
		if (this.isLordFull()) {
			return false;
		}

		return lordList.add(p.player());
	}

	public boolean addVicePresident(PlayerData p) {
		if (this.isVicePresidentFull()) {
			return false;
		}

		return vicePresidentList.add(p.player());
	}

	public boolean deleteMember(PlayerData p) {
		return memberList.remove((Object) p.player());
	}

	public boolean deleteLord(PlayerData p) {
		return lordList.remove((Object) p.player());
	}

	public boolean deleteVicePresident(PlayerData p) {
		return vicePresidentList.remove((Object) p.player());
	}

	public int getLevelAttributes(int level, String key) {
		switch (key) {
			case "cost":
				return Guild.levelMap.get(level).cost();
			case "memberNums":
				return Guild.levelMap.get(level).memberNums();
			case "lordNums":
				return Guild.levelMap.get(level).lordNums();
			case "vicePresidentNums":
				return Guild.levelMap.get(level).vicePresidentNums();
			default:
				return -1;
		}
	}
}
