import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class team {
    int id;
    int score;

    team(int n_id) {
        id = n_id;
        score = 0;
    }
}

class group {
    int id;
    team[] teams;

    group(int n_id, int size) {
        id = n_id;
        teams = new team[size];
    }
}

class league {
    public static final int TEAMS_NUM = 8;
    private static final int GROUP_SIZE = 4;
    private static final int GROUPS_NUM = TEAMS_NUM / GROUP_SIZE;
    private static final int LOG_LVL_DEBUG = 2;
    private static final int LOG_LVL_TEST = 1;
    private static final int SCORE_FOR_WINNING = 3;
    private static int debug_level;

    private static void LOG(final int lvl, final String in, final Object... args) {
        if (lvl <= debug_level) {
            System.out.println(String.format(in, args));
        }
    }

    public static void LOGD(final String in, final Object... args) {
        LOG(LOG_LVL_DEBUG, in, args);
    }

    private static void LOGT(final String in, final Object... args) {
        LOG(LOG_LVL_TEST, in, args);
    }

    group[] groups;
    Boolean[][] strength_graph;
    Boolean[][] positive_manipulators;
    int manipulated_team_id;

    league() {
        groups = new group[GROUPS_NUM];
        strength_graph = new Boolean[TEAMS_NUM][TEAMS_NUM];
        positive_manipulators = new Boolean[TEAMS_NUM][TEAMS_NUM];
    }

    public int algorithm_init(int logLevel, int teamWantedToBeWinner, List<Integer> coalitionList) {
        int g, t1, t2;
        for (g = 0; g < GROUPS_NUM; g++) {
            groups[g] = new group(g + 1, GROUP_SIZE);
            for (t1 = 0; t1 < GROUP_SIZE; t1++) {
                groups[g].teams[t1] = new team(g * GROUP_SIZE + t1);
            }
        }
        for (t1 = 0; t1 < TEAMS_NUM; t1++) {
            for (t2 = 0; t2 < TEAMS_NUM; t2++) {
                if (t2 >= t1 + 1) {
                    strength_graph[t1][t2] = true;// t1 beats t2
                } else {
                    strength_graph[t1][t2] = false;
                }
                positive_manipulators[t1][t2] = false;
            }
        }

        // debug level: 0- don't show any logs, 1- testing mode 2- show all logs
        debug_level = logLevel;

        // the id of the team that we want to check if won using manipulations
        manipulated_team_id = teamWantedToBeWinner;
        // all the rest arguments are ids of the coalition teams
        for (Integer coalitionTeamId : coalitionList) {
            positive_manipulators[coalitionTeamId][manipulated_team_id] = true;
            LOGD("team %d will manipulate for win of team %d\n", coalitionTeamId, manipulated_team_id);
        }
        return 0;
    }

    private Boolean game_winner_first_win(int tm1, int tm2) {
        if (positive_manipulators[tm1][tm2] == true) {
            LOGD("%d wins %d due to manipulation\n", tm2, tm1);
            return false;
        }
        if (positive_manipulators[tm2][tm1] == true) {
            LOGD("%d wins %d due to manipulation\n", tm1, tm2);
            return true;
        }
        //TODO:  what about scores ???
        //TODO: no consistent rule for 3 with same score that have circle by this func: 1 before 2, 2 before 3, 3 before 1!

        if (strength_graph[tm1][tm2] == true) {
            LOGD("%d wins %d due to strength\n", tm1, tm2);
            return true;
        }
        LOGD("%d wins %d due to strength\n", tm2, tm1);
        return false;
    }

    private Boolean first_team_best_score(team tm1, team tm2) {
        if (tm1.score > tm2.score) {
            LOGD("%d before %d due to score\n", tm1.id, tm2.id);
            return true;
        }
        if (tm2.score > tm1.score) {
            LOGD("%d before %d due to score\n", tm2.id, tm1.id);
            return false;
        }
        if (positive_manipulators[tm1.id][tm2.id] == true) {
            LOGD("%d before %d due to manipulation\n", tm2.id, tm1.id);
            return false;
        }
        if (positive_manipulators[tm2.id][tm1.id] == true) {
            LOGD("%d before %d due to manipulation\n", tm1.id, tm2.id);
            return true;
        }

        // it's TEKO and no team manipulate for the other one
        if (strength_graph[tm1.id][tm2.id] == true) {
            // so the more strength team tm1 will be above due to it's ability to score more goals
            LOGD("%d before %d due to strength\n", tm1.id, tm2.id);
            return true;
        }
        LOGD("%d before %d due to strength\n", tm2.id, tm1.id);
        // so the more strength team tm2 will be above due to it's ability to score more goals
        return false;
    }

    private int calculate_groups_scores() {
        int g, t1, t2, t3;
        team temp;
        for (g = 0; g < GROUPS_NUM; g++) {
            LOGD("GROUP %d - calculate winners:\n", g);
            for (t1 = 0; t1 < GROUP_SIZE; t1++) {
                for (t2 = t1 + 1; t2 < GROUP_SIZE; t2++) {
                    if (game_winner_first_win(groups[g].teams[t1].id, groups[g].teams[t2].id) == true) {
                        groups[g].teams[t1].score += SCORE_FOR_WINNING;
                    } else {
                        groups[g].teams[t2].score += SCORE_FOR_WINNING;
                    }
                }
            }
            for (g = 0; g < GROUPS_NUM; g++) {
                for (t1 = 0; t1 < GROUP_SIZE; t1++) {
                    for (t2 = t1 + 1; t2 < GROUP_SIZE; t2++) {
                        for (t3 = t2 + 1; t3 < GROUP_SIZE; t3++) {
                            if ((game_winner_first_win(groups[g].teams[t1].id, groups[g].teams[t2].id)) &&
                            (game_winner_first_win(groups[g].teams[t2].id, groups[g].teams[t3].id)) &&
                                    (game_winner_first_win(groups[g].teams[t3].id, groups[g].teams[t1].id)))
                            {
                                LOGT("Error");
                            }

                            if ((game_winner_first_win(groups[g].teams[t1].id, groups[g].teams[t3].id)) &&
                                    (game_winner_first_win(groups[g].teams[t3].id, groups[g].teams[t2].id)) &&
                                    (game_winner_first_win(groups[g].teams[t2].id, groups[g].teams[t1].id)))
                            {
                                LOGT("Error");
                            }
                        }
                    }
                }
            }

            LOGD("\n");
            LOGD("calculate table order:\n");
            for (t1 = 0; t1 < GROUP_SIZE; t1++) {
                for (t2 = t1 + 1; t2 < GROUP_SIZE; t2++) {
                    if (first_team_best_score(groups[g].teams[t2], groups[g].teams[t1])) {
                        temp = groups[g].teams[t1];
                        groups[g].teams[t1] = groups[g].teams[t2];
                        groups[g].teams[t2] = temp;
                    }
                }
            }
            LOGD("\n");
            LOGD("final table:\n");
            for (t1 = 0; t1 < GROUP_SIZE; t1++) {
                LOGD("TEAM %d: score=%d\n", groups[g].teams[t1].id, groups[g].teams[t1].score);
            }
            LOGD("\n");
        }
        return 0;
    }

    private Boolean calculate_tree() {
        team[] tree_teams = new team[GROUPS_NUM * 2];
        int g, t = 0;
        for (g = 0; g < GROUPS_NUM; g++, t++) {
            if (g % 2 == 0) {
                tree_teams[t] = groups[g].teams[0];
                tree_teams[GROUPS_NUM * 2 - t - 1] = groups[g].teams[1];
            } else {
                tree_teams[t] = groups[g].teams[1];
                tree_teams[GROUPS_NUM * 2 - t - 1] = groups[g].teams[0];
            }
        }
        int left_teams;
        for (left_teams = GROUPS_NUM * 2; left_teams > 1; left_teams /= 2) {
            for (t = 0; t < left_teams / 2; t++) {
                if (game_winner_first_win(tree_teams[2 * t].id, tree_teams[2 * t + 1].id) == true) {
                    tree_teams[t] = tree_teams[2 * t];
                } else {
                    tree_teams[t] = tree_teams[2 * t + 1];
                }
            }
            LOGD("\n");
        }
        LOGD("%d\n", tree_teams[0].id);
        if (tree_teams[0].id == manipulated_team_id) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean try_swap_two_firsts_on_tm_group() {
        return false;
    }

    public boolean algorithm_execute() {
        calculate_groups_scores();
        if (calculate_tree()) {
            LOGT("yes\n");
            return true;
        }
        if (try_swap_two_firsts_on_tm_group()) {
            if (calculate_tree()) {
                LOGT("yes\n");
                return true;
            }
        }
        LOGT("no\n");
        return false;
    }
}

class sportsManipulation {
    public static void main(String[] argv) {
        List<String> dataFromFile = readFile("test_plan.txt");
        int passedTestCases = 0;
        int testCases = 0;
        boolean isBugFound = false;
        ArrayList<ArrayList<ArrayList<Integer>>> minimalCoalitions = new ArrayList<>();
        for (int index = 1; index <= league.TEAMS_NUM; index++) {
            minimalCoalitions.add(new ArrayList<>());
        }
        if (dataFromFile != null && !dataFromFile.isEmpty()) {
            for (String rowData : dataFromFile) {
                if (rowData.startsWith("#")) {
                    continue;
                }
                Scanner scanner = new Scanner(rowData);
                List<Integer> list = new ArrayList<>();
                while (scanner.hasNextInt()) {
                    list.add(scanner.nextInt());
                }

                int teamToBeWinner = list.get(0);
                ArrayList<Integer> coalition = new ArrayList<>();
                for (int i = 1; i < list.size(); i++) {
                    coalition.add(list.get(i));
                }
                minimalCoalitions.get(teamToBeWinner).add(coalition);
            }
            for (int index = 0; index < league.TEAMS_NUM; index++) {
                for (ArrayList<Integer> coltn : optionalCoallitions(index)) {
                    testCases++;
                    if (expectedLeagueResult(index, coltn, minimalCoalitions) == run(0, index, coltn)) {
                        passedTestCases++;
                    } else {
                        if (!isBugFound) {
                            isBugFound = true;
                            run(2, index, coltn);
                        }
                    }
                }
            }
            System.out.println("passed " + passedTestCases + " tests out of " + testCases);
        }
    }

    private static boolean tryIncreaseArray(int teamToBeWinner, boolean[] teams) {
        for (int i = 0; i < teams.length; i++) {
            if (i != teamToBeWinner) {
                if (!teams[i]) {
                    teams[i] = true;
                    return true;
                }
                teams[i] = false;
            }
        }
        return false;
    }

    private static ArrayList<Integer> calculateCoalition(int teamToBeWinner, boolean[] teams) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int i = 0; i < league.TEAMS_NUM; i++) {
            if (i != teamToBeWinner && teams[i]) {
                res.add(i);
            }
        }
        return res;
    }

    private static ArrayList<ArrayList<Integer>> optionalCoallitions(int teamToBeWinner) {
        boolean[] teams = new boolean[league.TEAMS_NUM];
        for (int i = 0; i < league.TEAMS_NUM; i++) {
            teams[i] = false;
        }
        ArrayList<ArrayList<Integer>> res = new ArrayList<>();
        do {
            res.add(calculateCoalition(teamToBeWinner, teams));
        } while (tryIncreaseArray(teamToBeWinner, teams));
        return res;
    }

    private static boolean coalitionContainsMinimal(List<Integer> tested, List<Integer> minimal) {
        for (Integer i : minimal) {
            if (!tested.contains(i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean expectedLeagueResult(int teamToBeWinner, List<Integer> coalition, ArrayList<ArrayList<ArrayList<Integer>>> minimalCoalitions) {
        for (ArrayList<Integer> coltn : minimalCoalitions.get(teamToBeWinner)) {
            if (coalitionContainsMinimal(coalition, coltn)) {
                return true;
            }
        }
        return false;
    }

    private static boolean run(int logLevel, int teamWantedToBeWinner, List<Integer> coalitionList) {
        league.LOGD("team wanted to be winner is %d, ", teamWantedToBeWinner);

        league lg = new league();
        lg.algorithm_init(logLevel, teamWantedToBeWinner, coalitionList);
        return lg.algorithm_execute();
    }

    private static List<String> readFile(String filename) {
        List<String> records = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(line);
            }
            reader.close();
            return records;
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }
}