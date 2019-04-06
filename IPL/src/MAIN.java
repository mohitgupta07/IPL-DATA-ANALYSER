import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.HashMap;
/*
 * Code Written By:- Mohit Gupta.
 * Purpose :- FRAGMA DATA - CODING PROBLEM
 * Last Modified :- 05/03/2019
 * Datasets Link:- https://drive.google.com/file/d/1I_2eX55sliYXfwn4Tl4nUsQBBG1etmb1/view
 * I have verified Results from official website:- https://www.iplt20.com/stats/2017 
 * Notice that points table results are shown before group stage rounds i.e., all matches except qualifiers/eliminators/semis/finals.
 * 
 * */

public class MAIN {
	/*
	 * This Class is the main Class where query executions are performed. This Class
	 * contains Code for : 1- Running all the Tasks/queries. 2- Code for all tasks.
	 * 3- Sorting a HashMap efficiently.
	 */

	// 1- Running All tasks/Queries
	public static void main(String args[]) throws Exception {
		// LOADING matches.csv
		// List<List<String>> ==var
		var<List<String>> matches = READ_CSV.loadData(MAIN.class.getResource("matches.csv").getFile());

		// LOADING deliveries.csv
		var deliveries = READ_CSV.loadData(MAIN.class.getResource("deliveries.csv").getFile());
		System.out.println("------TASK 1 starts----");
		System.out.println("YEAR TEAM COUNT");
		doTASK1(matches, 4, new int[] { 2016, 2017 });// DUMMY EXAMPLE. Code is made modular. Just you have to provide
														// Top K teams and years list.
		System.out.println("------TASK 1 ends------\n");
		// System.out.println(deliveries.get(0));

		System.out.println("------TASK 2 starts----");
		System.out.println("YEAR, TEAM_NAME, FOURS_COUNT, SIXES_COUNT, TOTAL_SCORE");
		// [MATCH_ID, INNING, BATTING_TEAM, BOWLING_TEAM, OVER, BALL, BATSMAN, BOWLER,
		// WIDE_RUNS, BYE_RUNS, LEGBYE_RUNS, NOBALL_RUNS, PENALTY_RUNS, BATSMAN_RUNS,
		// EXTRA_RUNS, TOTAL_RUNS]

		doTASK2(deliveries, matches);
		System.out.println("------TASK 2 ends------\n");

		System.out.println("------TASK 3 starts----");
		System.out.println("YEAR, PLAYER, ECONOMY");

		doTASK3(deliveries, matches, 10);
		System.out.println("------TASK 3 ends------\n");

		System.out.println("------TASK 4 starts----");
		System.out.println("YEAR, Team");
		// Net Run Rate = (Total Runs Scored / Total Overs Faced) – (Total Runs Conceded
		// /Total Overs Bowled)
		doTASK4(deliveries, matches, 1);
		System.out.println("------TASK 4 ends------");

	}

	// 2- Code for all tasks.
	
	/*Find the team name which has Highest Net Run Rate with respect to year.*/ 
	
	private static void doTASK4(List<List<String>> deliveries, List<List<String>> matches, int topK) {
		// Creating a Mapping from Match_ID(many) to Season/year(one)--Mapper. Match_ID
		// to Season is Many-One relationship.
		// Thus Key is Match-ID(According to Entity relationship rules of DBMS).
		//
		// Part 1- Creating a Mapping from Match_ID to Season
		HashMap<String, String> matchID_yr = new HashMap<>();
		var year_index = matches.get(0).indexOf("SEASON");
		var matchID_index = matches.get(0).indexOf("MATCH_ID");
		var len = matches.size();
		// System.out.println(len);
		for (int i = 1; i < len; i++) {
			var match = matches.get(i);
			// System.out.println(match);
			var tyr = match.get(year_index);
			var mi = match.get(matchID_index);
			// System.out.println(mi+" "+tyr);
			if (!matchID_yr.containsKey(mi)) {
				matchID_yr.put(mi, tyr);// Key: Match_ID,Value: Year/Season
			}
		}
		//
		// Part 2- Counting Per season, Which Team Scored Runs/Overs faced and conceded runs/balls delievered.
		HashMap<String, HashMap> year = new HashMap<>();// Technically year should be Integer but Assuming it as String
		var len2 = deliveries.size();
		matchID_index = deliveries.get(0).indexOf("MATCH_ID");
		var BATTING_TEAM_index = deliveries.get(0).indexOf("BATTING_TEAM");

		var BOWLING_TEAM_index = deliveries.get(0).indexOf("BOWLING_TEAM");

		var batsmanRuns_index = deliveries.get(0).indexOf("BATSMAN_RUNS");
		var totalRuns_index = deliveries.get(0).indexOf("TOTAL_RUNS");
		var WIDE_RUNS_index = deliveries.get(0).indexOf("WIDE_RUNS");
		var NOBALL_RUNS_index = deliveries.get(0).indexOf("NOBALL_RUNS");
		for (int i = 1; i < len2; i++) {
			var delivery = deliveries.get(i);
			String tyr = matchID_yr.get(delivery.get(matchID_index));// Using shortforms for naming convention of Local Variables.
			HashMap<String, short[][]> X;
			// HashMap<String, short[]> X;
			if (!year.containsKey(tyr)) {
				HashMap<String, short[][]> year_entry = new HashMap<>();
				year.put(tyr, year_entry);
				// X = year_entry;
			}
			X = year.get(tyr);
			var tbt = delivery.get(BOWLING_TEAM_index);

			if (!X.containsKey(tbt)) {

				X.put(tbt, new short[][] { { 0, 0 }, { 0, 0 } });// runs and valid_deliveries - Batting -0 , Bowling -1
				// Short datatype is enough as no team cannot make more than 30000runs in an year,
				// As it Practically impossible.
			}
			var tbatTeam = delivery.get(BATTING_TEAM_index);

			if (!X.containsKey(tbatTeam)) {

				X.put(tbatTeam, new short[][] { { 0, 0 }, { 0, 0 } });// runs and valid_deliveries - Batting -0 , Bowling -1
			}

			/*
			 * if not (row['WIDE_RUNS'] or row['NOBALL_RUNS']): outer[ mtch2season[
			 * row['MATCH_ID'] ] ] [ row['BOWLER'] ][1] += 1 outer[ mtch2season[
			 * row['MATCH_ID'] ] ] [ row['BOWLER']
			 * ][0]+=(row['TOTAL_RUNS']-(row['LEGBYE_RUNS']+row['BYE_RUNS']))
			 */
			var valBowl = X.get(tbt);
			var valBat = X.get(tbatTeam);
			var twr = Short.parseShort(delivery.get(WIDE_RUNS_index));
			var tnr = Short.parseShort(delivery.get(NOBALL_RUNS_index));
			if (twr == 0 && tnr == 0)// No wide ball as well as no noball.
			{

				valBat[0][1] += 1; // Balls Faced
				valBowl[1][1] += 1; // Balls Delivered
			}

			var ttr = Short.parseShort(delivery.get(totalRuns_index));

			var tbr = Short.parseShort(delivery.get(batsmanRuns_index));
			valBat[0][0] += ttr;
			valBowl[1][0] += ttr;
			X.put(tbt, valBowl);
			X.put(tbatTeam, valBat);
		}
		//Part 3- Sorting The Result based on Years(chronological order) and printing results based on best Net-run-rate.
		var keys = year.keySet();
		List<String> keyL = new ArrayList<>(keys);
		Collections.sort(keyL);
		// System.out.println(keyL);
		/*
		 * For sorting we can use basic sorting. But for very large n, its better to go
		 * for a simple insertion sort or HashMap/Trees. This is a good way as when
		 * entries are very large then you are not supposed to have full data in
		 * main-memory but just a subset of it. Although cost for making the top-k list
		 * is O(n) per insert-query which means for large n, you cannot scale based on
		 * algo but still you can scale based on H/w. That is, allotting sorting tasks
		 * to multi-processors. We can also go for n-way merge sort algorithm.
		 */
		for (String yr : keyL)// Printing year-wise in ascending order
		{
			HashMap<String, short[][]> teamsData = year.get(yr);
			HashMap<String, Float> tmptD = new HashMap<>();
			for (Entry<String, short[][]> tx : teamsData.entrySet()) {
				// System.out.println(tx.getKey()+" "+((float) tx.getValue()[0][0]) + " "+
				// ((float) tx.getValue()[0][1]) +" "+ ((float) tx.getValue()[1][0]) + " "+
				// ((float) tx.getValue()[1][1]));
				tmptD.put(tx.getKey(), 6f * (((float) tx.getValue()[0][0]) / ((float) tx.getValue()[0][1])
						- ((float) tx.getValue()[1][0]) / ((float) tx.getValue()[1][1])));

			}
			// System.out.println(tmptD);
			List<Entry<String, Float>> list = sortList(tmptD, 1);
			// List<Entry<String,Float>> best=new ArrayList<>(topK);
			int count = topK;
			for (Entry<String, Float> entry : list) {
				if (count-- == 0)
					break;
				System.out.println(yr + ", " + entry.getKey());

			}

		}

	}

	/*	
	 * Task 3:-Top 10 best economy rate bowler with respect to year who bowled at least 10 
	 * overs (LEGBYE_RUNS and BYE_RUNS should not be considered for Total Runs Given
	 * by a bowler)
 	 */
	private static void doTASK3(List<List<String>> deliveries, List<List<String>> matches, int topK) {
		// Creating a Mapping from Match_ID(many) to Season/year(one)--Mapper. Match_ID
		// to Season is Many-One relationship.
		// Thus Key is Match-ID(According to Entity relationship rules of DBMS).
		//
		// Part 1- Creating a Mapping from Match_ID to Season
		HashMap<String, String> matchID_yr = new HashMap<>();
		var year_index = matches.get(0).indexOf("SEASON");
		var matchID_index = matches.get(0).indexOf("MATCH_ID");
		var len = matches.size();
		// System.out.println(len);
		// Creating a Mapping from Match_ID to Season/year
		for (int i = 1; i < len; i++) {
			var match = matches.get(i);
			// System.out.println(match);
			var tyr = match.get(year_index);
			var mi = match.get(matchID_index);
			// System.out.println(mi+" "+tyr);
			if (!matchID_yr.containsKey(mi)) {
				matchID_yr.put(mi, tyr);
			}
		}
		
		//Part 2- Getting per season stat for a player - runs given and balls delievered.
		HashMap<String, HashMap> year = new HashMap<>();// Technically year should be Integer but Assuming it as String
		var len2 = deliveries.size();
		matchID_index = deliveries.get(0).indexOf("MATCH_ID");
		var BOWLER_index = deliveries.get(0).indexOf("BOWLER");
		var batsmanRuns_index = deliveries.get(0).indexOf("BATSMAN_RUNS");
		var totalRuns_index = deliveries.get(0).indexOf("TOTAL_RUNS");
		var WIDE_RUNS_index = deliveries.get(0).indexOf("WIDE_RUNS");
		var NOBALL_RUNS_index = deliveries.get(0).indexOf("NOBALL_RUNS");
		for (int i = 1; i < len2; i++) {
			var delivery = deliveries.get(i);
			String tyr = matchID_yr.get(delivery.get(matchID_index));

			HashMap<String, short[]> X;
			// HashMap<String, short[]> X;
			if (!year.containsKey(tyr)) {
				HashMap<String, short[]> year_entry = new HashMap<>();
				year.put(tyr, year_entry);
				// X = year_entry;
			}
			X = year.get(tyr);
			var tbt = delivery.get(BOWLER_index);

			if (!X.containsKey(tbt)) {

				X.put(tbt, new short[] { 0, 0 });// runs and valid_deliveries
			}

			/*
			 * if not (row['WIDE_RUNS'] or row['NOBALL_RUNS']): outer[ mtch2season[
			 * row['MATCH_ID'] ] ] [ row['BOWLER'] ][1] += 1 outer[ mtch2season[
			 * row['MATCH_ID'] ] ] [ row['BOWLER']
			 * ][0]+=(row['TOTAL_RUNS']-(row['LEGBYE_RUNS']+row['BYE_RUNS']))
			 */
			var val = X.get(tbt);

			var twr = Short.parseShort(delivery.get(WIDE_RUNS_index));
			var tnr = Short.parseShort(delivery.get(NOBALL_RUNS_index));
			if (twr == 0 && tnr == 0)// No wide ball as well as no noball.
			{

				val[1] += 1;
			}

			var tbr = Short.parseShort(delivery.get(batsmanRuns_index));
			var ttr = Short.parseShort(delivery.get(totalRuns_index));
			var tlb = Short.parseShort(delivery.get(deliveries.get(0).indexOf("LEGBYE_RUNS")));
			var tby = Short.parseShort(delivery.get(deliveries.get(0).indexOf("BYE_RUNS")));

			// val[0]+=twr+tnr+tbr;
			val[0] += ttr - (tlb + tby);
			X.put(tbt, val);
		}
		//Part 3:- Sorting in chronological order and printing results.
		var keys = year.keySet();
		List<String> keyL = new ArrayList<>(keys);
		Collections.sort(keyL);
		// System.out.println(keyL);
		/*
		 * For sorting we can use basic sorting. But for very large n, its better to go
		 * for a simple insertion sort or HashMap/Trees. This is a good way as when
		 * entries are very large then you are not supposed to have full data in
		 * main-memory but just a subset of it. Although cost for making the top-k list
		 * is O(n) per insert-query which means for large n, you cannot scale based on
		 * algo but still you can scale based on H/w. That is, allotting sorting tasks
		 * to multi-processors. We can also go for n-way merge sort algorithm.
		 */
		for (String yr : keyL)// Printing year-wise in ascending order
		{
			HashMap<String, short[]> teamsData = year.get(yr);
			HashMap<String, Float> tmptD = new HashMap<>();
			for (Entry<String, short[]> tx : teamsData.entrySet()) {
				if (tx.getValue()[1] >= 10 * 6) {
					tmptD.put(tx.getKey(), ((float) tx.getValue()[0]) / ((float) tx.getValue()[1]) * 6);
				}
			}
			List<Entry<String, Float>> list = sortList(tmptD, -1);// Min economy first so order=-1
			// List<Entry<String,Float>> best=new ArrayList<>(topK);
			int count = topK;
			for (Entry<String, Float> entry : list) {
				if (count-- == 0)
					break;
				System.out.println(yr + ", " + entry.getKey() + ", " + entry.getValue());

			}

		}

	}

	/*Task 2:- List total number of fours, sixes, total score with respect to team and year.*/ 
	private static void doTASK2(List<List<String>> deliveries, List<List<String>> matches) {
		// Creating a Mapping from Match_ID(many) to Season/year(one)--Mapper. Match_ID
		// to Season is Many-One relationship.
		// Thus Key is Match-ID(According to Entity relationship rules of DBMS).
		//
		// Part 1- Creating a Mapping from Match_ID to Season
		HashMap<String, String> matchID_yr = new HashMap<>();
		var year_index = matches.get(0).indexOf("SEASON");
		var matchID_index = matches.get(0).indexOf("MATCH_ID");
		var len = matches.size();
		for (int i = 1; i < len; i++) {
			var match = matches.get(i);
			// System.out.println(match);
			var tyr = match.get(year_index);
			var mi = match.get(matchID_index);
			// System.out.println(mi+" "+tyr);
			if (!matchID_yr.containsKey(mi)) {
				matchID_yr.put(mi, tyr);
			}
		}
		//Part 2:- Counting fours/sixes/total score per team per Season.
		/*Note that, Method for counting fours is not described and thus I am assuming to count a four only if batsman hit it and not by byes/legbyes.*/
		HashMap<String, HashMap> year = new HashMap<>();// Technically year should be Integer but Assuming it as String
		var len2 = deliveries.size();
		matchID_index = deliveries.get(0).indexOf("MATCH_ID");
		var battingTeam_index = deliveries.get(0).indexOf("BATTING_TEAM");
		var batsmanRuns_index = deliveries.get(0).indexOf("BATSMAN_RUNS");
		var totalRuns_index = deliveries.get(0).indexOf("TOTAL_RUNS");

		for (int i = 1; i < len2; i++) {
			var delivery = deliveries.get(i);
			String tyr = matchID_yr.get(delivery.get(matchID_index));

			HashMap<String, short[]> X;
			// HashMap<String, short[]> X;
			if (!year.containsKey(tyr)) {
				HashMap<String, short[]> year_entry = new HashMap<>();
				year.put(tyr, year_entry);
				// X = year_entry;
			}
			X = year.get(tyr);
			var tbt = delivery.get(battingTeam_index);

			if (!X.containsKey(tbt)) {

				X.put(tbt, new short[] { 0, 0, 0 });//Fours_count / Sixes_count / Total_Score
			}

			var tbr = Short.parseShort(delivery.get(batsmanRuns_index));
			var ttr = Short.parseShort(delivery.get(totalRuns_index));

			var val = X.get(tbt);
			if (tbr == 4)
				val[0] += 1;
			else if (tbr == 6)
				val[1] += 1;
			val[2] += ttr;
			X.put(tbt, val);
		}
		//Part 3:- Printing Results.
		var keys = year.keySet();
		List keyL = new ArrayList<>(keys);
		Collections.sort(keyL);
		// System.out.println(keyL);
		for (Object key : keyL) {
			HashMap teamsData = year.get(key);
			var keysTD = teamsData.keySet();
			for (Object keyTD : keysTD) {

				System.out.print(key + ", " + keyTD);
				short[] xxx = (short[]) teamsData.get(keyTD);
				for (short xxxx : xxx) {
					System.out.print(", " + xxxx);
				}
				System.out.println();

			}
		}

	}
	
	/*Task 1:- Top 4 teams which elected to field first after winning toss in the year 2016 and
	2017. */
	private static void doTASK1(List<List<String>> matches, int topK, int yearsRes[]) {
		HashMap<String, HashMap> year = new HashMap<>();// Technically year should be Integer but Assuming it as String is better.
		var year_index = matches.get(0).indexOf("SEASON");
		var tossWinner_index = matches.get(0).indexOf("TOSS_WINNER");
		var tossDecision_index = matches.get(0).indexOf("TOSS_DECISION");
		var len = matches.size();
		for (int i = 1; i < len; i++) {
			var match = matches.get(i);
			// System.out.println(match);
			var tyr = match.get(year_index);
			var ttw = match.get(tossWinner_index);
			HashMap<String, Float> X;

			if (!year.containsKey(tyr)) {
				HashMap<String, Float> year_entry = new HashMap<>();
				year.put(tyr, year_entry);
				// X = year_entry;
			}
			X = year.get(tyr);
			if (!X.containsKey(ttw)) {
				X.put(ttw, 0f);
			}

			var ttd = match.get(tossDecision_index);
			if (ttd.equalsIgnoreCase("field")) {
				var val = X.get(ttw);
				val += 1;
				X.put(ttw, val);
			}


		}

		for (int yr : yearsRes) {
			List<Entry<String, Float>> list = sortList(year.get(yr + ""), 1);
			// "" used for making sure that type is string<TYPE_CASTING>. As yr is int type.
			int count = topK;
			for (Entry<String, Float> entry : list) {
				if (count-- == 0)
					break;
				System.out.println(yr + ", " + entry.getKey() + ", " + entry.getValue().intValue());

			}

		}

	}

	// 3- Sorting a HashMap efficiently.
	private static List<Entry<String, Float>> sortList(HashMap<String, Float> X, int order) {
		// IF order=1 then Descending Order, if Order=-1 the Ascending Order.
		/*
		 * I am using the Logic based on the results I got from the below link. I am
		 * using priority queue as it is already available in standard java library. I
		 * have come up with various solutions of my own but that will take complexity
		 * very similar to priority queue and thus I am opting to use the standard
		 * library.
		 * 
		 * https://www.michaelpollmeier.com/selecting-top-k-items-from-a-list-
		 * efficiently-in-java-groovy
		 * https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
		 */
		/*
		 * For sorting we can use basic sorting. But for very large n, its better to go
		 * for a simple insertion sort or HashMap/Trees. This is a good way as when
		 * entries are very large then you are not supposed to have full data in
		 * main-memory but just a subset of it. Although cost for making the top-k list
		 * is O(n) per insert-query which means for large n, you cannot scale based on
		 * algo but still you can scale based on H/w. That is, allotting sorting tasks
		 * to multi-processors. We can also go for n-way merge sort algorithm.
		 */
		Set<Entry<String, Float>> set = X.entrySet();
		List<Entry<String, Float>> list = new ArrayList<Entry<String, Float>>(set);
		/*
		 * PriorityQueue<Entry> pq=new PriorityQueue<>(list.size());
		 * pq.add(list.get(0)); System.out.println(pq);
		 */
		Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
				return order * o2.getValue().compareTo(o1.getValue());
			}
		});
		return list;
	}

}
