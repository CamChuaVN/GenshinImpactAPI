package camchua.genshinimpactapi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.springframework.util.DigestUtils;

import camchua.genshinimpactapi.GenshinImpact;
import camchua.genshinimpactapi.data.user.model.Avatar;
import camchua.genshinimpactapi.data.user.model.DailyReward;
import camchua.genshinimpactapi.data.user.model.Player;
import camchua.genshinimpactapi.data.user.model.Stat;
import camchua.genshinimpactapi.data.user.model.dailynote.DailyNote;
import camchua.genshinimpactapi.data.user.model.dailynote.DailyNoteExpeditions;
import camchua.genshinimpactapi.data.user.model.dailynote.DailyNoteExpeditions.Expeditions;
import camchua.genshinimpactapi.data.user.model.dailynote.DailyNoteTransformer;
import camchua.genshinimpactapi.data.user.model.explorations.ExplorationsOfferings;
import camchua.genshinimpactapi.data.user.model.explorations.WorldExplorations;
import camchua.genshinimpactapi.data.user.model.explorations.WorldExplorations.Explorations;
import camchua.genshinimpactapi.data.user.model.spiralabyss.SpiralAbyss;
import camchua.genshinimpactapi.data.user.model.spiralabyss.SpiralAbyssAvatar;
import camchua.genshinimpactapi.data.user.model.spiralabyss.floor.SpiralAbyssFirstHalfBattle;
import camchua.genshinimpactapi.data.user.model.spiralabyss.floor.SpiralAbyssFloor;
import camchua.genshinimpactapi.data.user.model.spiralabyss.floor.SpiralAbyssFloor.Floor;
import camchua.genshinimpactapi.data.user.model.spiralabyss.floor.SpiralAbyssSecondHalfBattle;
import camchua.genshinimpactapi.data.user.model.spiralabyss.floor.SpiralAbyssStage;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssDamageRank;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssDefeatRank;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssEnergySkillRank;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssNormalSkillRank;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssRevealRank;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssRevealRank.RevealRank;
import camchua.genshinimpactapi.data.user.model.spiralabyss.rank.SpiralAbyssTakeDamageRank;
import camchua.genshinimpactapi.data.user.model.travelerdiary.TravelerDiary;
import camchua.genshinimpactapi.data.user.model.travelerdiary.TravelerDiaryDayData;
import camchua.genshinimpactapi.data.user.model.travelerdiary.TravelerDiaryMonthData;
import camchua.genshinimpactapi.data.user.model.travelerdiary.TravelerDiaryMonthDetail;
import camchua.genshinimpactapi.data.user.model.travelerdiary.TravelerDiaryMonthDetail.MonthDetail;
import camchua.genshinimpactapi.enums.ElementType;

public class Utils {
	
	private static String OS_DS_SALT = "6cqshh5dhw73bzxn20oexa9k516chk7s";
	private static String CN_DS_SALT = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs";

	public static String generateDS() {
		PythonInterpreter python = new PythonInterpreter();
		python.set("salt", OS_DS_SALT);
		python.set("r", "ABCDEF");
		python.exec("import time, hashlib");
		python.exec("t = int(time.time())");
		python.exec("e = 'salt=' + salt + '&t=' + str(t) + '&r=' + r");
		python.exec("h = hashlib.md5(e.encode()).hexdigest()");
		python.exec("result = str(t) + ',' + r + ',' + h");
		PyObject result = python.get("result");
		python.close();
		return result.asString();
	}
	
	public static String generateDS_CN() {
		String md5Version = "cx2y9z9a29tfqvr1qsq6c7yz99b5jsqt";
		String t = Long.toString(System.currentTimeMillis() / 1000);
		String r = random(6);
		String c = DigestUtils.md5DigestAsHex(("salt=" + md5Version + "&t=" + t + "&r=" + r).getBytes());
		return t + "," + r + "," + c;
	}

	public static String random(int len) {
    	Random rd = new Random();
        char[] x = "1234567890abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] str = new char[len];
        for (int i = 0; i < len; i++) {
        	str[i] = x[rd.nextInt(x.length)];
        }
        return new String(str);
	}

	public static String getServerByUid(String uid) {
		char first = uid.charAt(0);
		String server = null;
		switch (first) {
		case '1':
			server = "cn_gf01";
			break;
		case '2':
			server = "cn_gf01";
			break;
		case '5':
			server = "cn_qd01";
			break;
		case '6':
			server = "os_usa";
			break;
		case '7':
			server = "os_euro";
			break;
		case '8':
			server = "os_asia";
			break;
		case '9':
			server = "os_cht";
			break;
		default:
			System.out.println("Wrong UID: " + uid);
			throw new IllegalArgumentException("Wrong uid");
		}
		return server;
	}

	public static JSONObject getConnectionResult(String url, String method, String reqBody, boolean cn) {
		try {
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) new URL(url + reqBody).openConnection();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			connection.setRequestMethod(method.toUpperCase());
			connection.setRequestProperty("User-Agent", "miHoYoBBS/2.5.1");
			connection.setRequestProperty("x-rpc-language", "en-us");
			connection.setRequestProperty("x-rpc-app_version", "2.2.1");
			connection.setRequestProperty("x-rpc-client_type", "4");
			connection.setRequestProperty("x-requested-with", "com.mihoyo.hyperion");
			connection.setRequestProperty("referer", "https://webstatic.mihoyo.com/");
			connection.setRequestProperty("origin", "https://webstatic.mihoyo.com");
			connection.setRequestProperty("sec-fetch-site", "same-site");
			connection.setRequestProperty("sec-fetch-mode", "cors");
			connection.setRequestProperty("sec-fetch-dest", "empty");
			connection.setRequestProperty("ds", cn ? Utils.generateDS_CN() : Utils.generateDS());
			connection.setRequestProperty("cookie", GenshinImpact.inst().getCookie());
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			final String response = reader.lines().collect(Collectors.joining());
			return new JSONObject(response);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Player initPlayer(String uid, boolean cn) {
		String info_str = GenshinImpact.getAPI().getPlayerInfo(uid, cn);
		JSONObject info = new JSONObject(info_str);
		if (info.getInt("retcode") != 0) {
			System.out.println("Init Player error: " + info.getString("message"));
			return new Player("", new ArrayList<>(), null, null, null, null, null, null);
		}

		String avt_str = GenshinImpact.getAPI().getCharacterInfo(uid, cn);
		JSONObject avt = new JSONObject(avt_str);

		JSONObject stats = info.getJSONObject("data").getJSONObject("stats");

		String spiralAbyss_str = GenshinImpact.getAPI().getAbyssInfo(uid, 1, cn);
		JSONObject spiralAbyss = new JSONObject(spiralAbyss_str);

		List<Avatar> avt_list = new ArrayList<>();
		int avatarNumber = stats.getInt("avatar_number");
		for (int i = 0; i < avatarNumber; i++) {
			JSONObject avts = avt.getJSONObject("data").getJSONArray("avatars").getJSONObject(i);

			String avatarId = String.valueOf(avts.getInt("id"));
			String name = avts.getString("name");
			ElementType element = ElementType.valueOf(avts.getString("element").toUpperCase());
			int rarity = avts.getInt("rarity");
			int fetter = avts.getInt("fetter");
			int level = avts.getInt("level");

			Avatar avtss = new Avatar(uid, avatarId, name, element, rarity, fetter, level);
			avt_list.add(avtss);
		}

		Stat stat = null;
		if (true) {
			int activeDayNumber = stats.getInt("active_day_number");
			int achievementNumber = stats.getInt("achievement_number");
			int winRate = stats.getInt("win_rate");

			int anemoculusNumber = stats.getInt("anemoculus_number");
			int geoculusNumber = stats.getInt("geoculus_number");
			int electroculusNumber = stats.getInt("electroculus_number");

			int wayPointNumber = stats.getInt("way_point_number");
			int domainNumber = stats.getInt("domain_number");

			int preciousChestNumber = stats.getInt("precious_chest_number");
			int luxuriousChestNumber = stats.getInt("luxurious_chest_number");
			int exquisiteChestNumber = stats.getInt("exquisite_chest_number");
			int commonChestNumber = stats.getInt("common_chest_number");
			int magicChestNumber = stats.getInt("magic_chest_number");

			String spiralAbysss = stats.getString("spiral_abyss");

			stat = new Stat(uid, activeDayNumber, achievementNumber, winRate, anemoculusNumber, geoculusNumber,
					electroculusNumber, avatarNumber, wayPointNumber, domainNumber, preciousChestNumber,
					luxuriousChestNumber, exquisiteChestNumber, commonChestNumber, magicChestNumber, spiralAbysss);
		}

		SpiralAbyss sa = null;
		if (true) {
			JSONObject data = spiralAbyss.getJSONObject("data");

			SpiralAbyssDamageRank damageRank = new SpiralAbyssDamageRank(data.getJSONArray("damage_rank").getJSONObject(0).getInt("avatar_id"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("rarity"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("value"));
			SpiralAbyssDefeatRank defeatRank = new SpiralAbyssDefeatRank(data.getJSONArray("defeat_rank").getJSONObject(0).getInt("avatar_id"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("rarity"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("value"));
			SpiralAbyssEnergySkillRank energySkillRank = new SpiralAbyssEnergySkillRank(data.getJSONArray("energy_skill_rank").getJSONObject(0).getInt("avatar_id"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("rarity"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("value"));
			SpiralAbyssNormalSkillRank normalSkillRank = new SpiralAbyssNormalSkillRank(data.getJSONArray("normal_skill_rank").getJSONObject(0).getInt("avatar_id"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("rarity"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("value"));
			SpiralAbyssTakeDamageRank takeDamageRank = new SpiralAbyssTakeDamageRank(data.getJSONArray("take_damage_rank").getJSONObject(0).getInt("avatar_id"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("rarity"), data.getJSONArray("damage_rank").getJSONObject(0).getInt("value"));
			
			SpiralAbyssRevealRank revealRank = null;
			List<RevealRank> rank = new ArrayList<>();
			JSONArray reveal = data.getJSONArray("reveal_rank");
			for (int i = 0; i < 4; i++) {
				if (reveal.isNull(i)) continue;
				
				JSONObject value = reveal.getJSONObject(i);
				rank.add(new RevealRank(value.getInt("avatar_id"), value.getInt("rarity"), value.getInt("value")));
			}
			revealRank = new SpiralAbyssRevealRank(rank);
			
			SpiralAbyssFloor spiralAbyssFloor = null;
			List<Floor> floor = new ArrayList<>();
			JSONArray floors = data.getJSONArray("floors");
			for (int i = 0; i < floors.length(); i++) {
				int star = floors.getJSONObject(i).getInt("star");
				int maxStar = floors.getJSONObject(i).getInt("max_star");
				int f = floors.getJSONObject(i).getInt("index");
				
				SpiralAbyssStage stage1 = new SpiralAbyssStage(0, 3, 1, new SpiralAbyssFirstHalfBattle(new ArrayList<>()), new SpiralAbyssSecondHalfBattle(new ArrayList<>()));
				SpiralAbyssStage stage2 = new SpiralAbyssStage(0, 3, 2, new SpiralAbyssFirstHalfBattle(new ArrayList<>()), new SpiralAbyssSecondHalfBattle(new ArrayList<>()));
				SpiralAbyssStage stage3 = new SpiralAbyssStage(0, 3, 3, new SpiralAbyssFirstHalfBattle(new ArrayList<>()), new SpiralAbyssSecondHalfBattle(new ArrayList<>()));
				for (int stage = 0; stage < 3; stage++) {
					if (floors.getJSONObject(i).getJSONArray("levels").isNull(stage)) continue;
					
					int stageStar = floors.getJSONObject(i).getJSONArray("levels").getJSONObject(stage).getInt("star");
					int stageMaxStar = floors.getJSONObject(i).getJSONArray("levels").getJSONObject(stage).getInt("max_star");
					int s = floors.getJSONObject(i).getJSONArray("levels").getJSONObject(stage).getInt("index");
					
					JSONArray firstAvatar = floors.getJSONObject(i).getJSONArray("levels").getJSONObject(stage).getJSONArray("battles").getJSONObject(0).getJSONArray("avatars");
					List<SpiralAbyssAvatar> firstAvatars = new ArrayList<>();
					for (int a = 0; a < firstAvatar.length(); a++) {
						firstAvatars.add(new SpiralAbyssAvatar(firstAvatar.getJSONObject(a).getInt("id"), firstAvatar.getJSONObject(a).getInt("level"), firstAvatar.getJSONObject(a).getInt("rarity")));
					}
					SpiralAbyssFirstHalfBattle firstHalf = new SpiralAbyssFirstHalfBattle(firstAvatars);
					
					JSONArray secondAvatar = floors.getJSONObject(i).getJSONArray("levels").getJSONObject(stage).getJSONArray("battles").getJSONObject(1).getJSONArray("avatars");
					List<SpiralAbyssAvatar> secondAvatars = new ArrayList<>();
					for (int a = 0; a < secondAvatar.length(); a++) {
						secondAvatars.add(new SpiralAbyssAvatar(secondAvatar.getJSONObject(a).getInt("id"), secondAvatar.getJSONObject(a).getInt("level"), secondAvatar.getJSONObject(a).getInt("rarity")));
					}
					SpiralAbyssSecondHalfBattle secondHalf = new SpiralAbyssSecondHalfBattle(secondAvatars);
					
					if (stage == 0) stage1 = new SpiralAbyssStage(stageStar, stageMaxStar, s, firstHalf, secondHalf);
					if (stage == 1) stage2 = new SpiralAbyssStage(stageStar, stageMaxStar, s, firstHalf, secondHalf);
					if (stage == 2) stage3 = new SpiralAbyssStage(stageStar, stageMaxStar, s, firstHalf, secondHalf);
				}
				
				floor.add(new Floor(star, maxStar, f, stage1, stage2, stage3));
			}
			spiralAbyssFloor = new SpiralAbyssFloor(floor);
			
			int totalWinTimes = data.getInt("total_win_times");
			int totalStar = data.getInt("total_star");
			int totalBattleTimes = data.getInt("total_battle_times");
			String maxFloor = data.getString("max_floor");
			
			sa = new SpiralAbyss(totalWinTimes, totalStar, totalBattleTimes, maxFloor, 
					spiralAbyssFloor, 
					damageRank, defeatRank, energySkillRank, normalSkillRank, revealRank, takeDamageRank);
		}
		
		WorldExplorations we = null;
		List<Explorations> explorations = new ArrayList<>();
		if (true) {
			JSONArray e = info.getJSONObject("data").getJSONArray("world_explorations");
			for (int i = 0; i < e.length(); i++) {
				ExplorationsOfferings eo = null;
				if (e.getJSONObject(i).getJSONArray("offerings").isNull(0)) {
					eo = new ExplorationsOfferings("", 0);
				} else {
					String offeringName = e.getJSONObject(i).getJSONArray("offerings").getJSONObject(0).getString("name");
					int offeringLevel = e.getJSONObject(i).getJSONArray("offerings").getJSONObject(0).getInt("level");
					eo = new ExplorationsOfferings(offeringName, offeringLevel);
				}
				
				String name = e.getJSONObject(i).getString("name");
				String type = e.getJSONObject(i).getString("type");
				int level = e.getJSONObject(i).getInt("level");
				double explorationPercentage = Double.parseDouble(new DecimalFormat("#.#").format(Double.parseDouble(String.valueOf(e.getJSONObject(i).getInt("exploration_percentage"))) / 10D));
				int id = e.getJSONObject(i).getInt("id");
				int parentId = e.getJSONObject(i).getInt("parent_id");
				
				explorations.add(new Explorations(name, type, level, explorationPercentage, id, parentId, eo));
			}
			we = new WorldExplorations(explorations);
		}
		
		DailyReward dr = null;
		if (true) {
			String dailyRewardInfo_str = GenshinImpact.getAPI().getDailyRewardInfo(cn);
			JSONObject dailyRewardInfo = new JSONObject(dailyRewardInfo_str).getJSONObject("data");
			
			int totalSignDay = dailyRewardInfo.getInt("total_sign_day");
			boolean sign = dailyRewardInfo.getBoolean("is_sign");
			String region = dailyRewardInfo.getString("region");
			boolean sub = dailyRewardInfo.getBoolean("is_sub");
			boolean firstBind = dailyRewardInfo.getBoolean("first_bind");
			
			dr = new DailyReward(totalSignDay, sign, region, sub, firstBind);
		}
		
		TravelerDiary td = null;
		if (true) {
			String diary_str = GenshinImpact.getAPI().getTravelerDiaryInfo(uid, Calendar.getInstance().get(Calendar.MONTH) + 1, cn);
			JSONObject diary = new JSONObject(diary_str);
			
			int dayPrimogems = diary.getJSONObject("data").getJSONObject("day_data").getInt("current_primogems");
			int dayMora = diary.getJSONObject("data").getJSONObject("day_data").getInt("current_mora");
			TravelerDiaryDayData dayData = new TravelerDiaryDayData(dayPrimogems, dayMora);
			
			int monthPrimogems = diary.getJSONObject("data").getJSONObject("month_data").getInt("current_primogems");
			int monthMora = diary.getJSONObject("data").getJSONObject("month_data").getInt("current_mora");
			TravelerDiaryMonthData monthData = new TravelerDiaryMonthData(monthPrimogems, monthMora);
			
			JSONArray detail = diary.getJSONObject("data").getJSONObject("month_data").getJSONArray("group_by");
			List<MonthDetail> details = new ArrayList<>();
			for(int i = 0; i < detail.length(); i++) {
				int actionId = detail.getJSONObject(i).getInt("action_id");
				String action = detail.getJSONObject(i).getString("action");
				int primogems = detail.getJSONObject(i).getInt("num");
				int percent = detail.getJSONObject(i).getInt("percent");
				
				details.add(new MonthDetail(actionId, action, primogems, percent));
			}
			TravelerDiaryMonthDetail monthDetail = new TravelerDiaryMonthDetail(details);
			
			int dataMonth = diary.getJSONObject("data").getInt("data_month");
			String region = diary.getJSONObject("data").getString("region");
			td = new TravelerDiary(dataMonth, region, dayData, monthData, monthDetail);
		}
		
		DailyNote dn = null;
		if (true) {
			String note_str = GenshinImpact.getAPI().getDailyNoteInfo(uid, cn);
			JSONObject note = new JSONObject(note_str);
			
			boolean to = note.getJSONObject("data").getJSONObject("transformer").getBoolean("obtained");
			int trs = note.getJSONObject("data").getJSONObject("transformer").getJSONObject("recovery_time").getInt("Second");
			int trm = note.getJSONObject("data").getJSONObject("transformer").getJSONObject("recovery_time").getInt("Minute");
			int trh = note.getJSONObject("data").getJSONObject("transformer").getJSONObject("recovery_time").getInt("Hour");
			int trd = note.getJSONObject("data").getJSONObject("transformer").getJSONObject("recovery_time").getInt("Day");
			boolean trr = note.getJSONObject("data").getJSONObject("transformer").getJSONObject("recovery_time").getBoolean("reached");
			DailyNoteTransformer t = new DailyNoteTransformer(to, trs, trm, trh, trd, trr);
			
			JSONArray expeditions = note.getJSONObject("data").getJSONArray("expeditions");
			List<Expeditions> expedition = new ArrayList<>();
			for(int i = 0; i < expeditions.length(); i++) {
				String rt = expeditions.getJSONObject(i).getString("remained_time");
				String s = expeditions.getJSONObject(i).getString("status");
				expedition.add(new Expeditions(rt, s));
			}
			DailyNoteExpeditions e = new DailyNoteExpeditions(expedition);
			
			int men = note.getJSONObject("data").getInt("max_expedition_num");
			int rdnl = note.getJSONObject("data").getInt("resin_discount_num_limit");
			int rrdn = note.getJSONObject("data").getInt("remain_resin_discount_num");
			int ttn = note.getJSONObject("data").getInt("total_task_num");
			int cr = note.getJSONObject("data").getInt("current_resin");
			int mhc = note.getJSONObject("data").getInt("max_home_coin");
			int ftn = note.getJSONObject("data").getInt("finished_task_num");
			int cen = note.getJSONObject("data").getInt("current_expedition_num");
			int mr = note.getJSONObject("data").getInt("max_resin");
			int chc = note.getJSONObject("data").getInt("current_home_coin");
			String rrt = note.getJSONObject("data").getString("resin_recovery_time");
			String hcrt = note.getJSONObject("data").getString("home_coin_recovery_time");
			boolean etrr = note.getJSONObject("data").getBoolean("is_extra_task_reward_received");
			dn = new DailyNote(men, rdnl, rrdn, ttn, cr, mhc, ftn, cen, mr, chc, rrt, hcrt, etrr, e, t);
		}

		return new Player(uid, avt_list, stat, sa, we, dr, td, dn);
	}

}
