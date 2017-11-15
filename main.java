import java.lang.*;

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

  static final int TEAMS_NUM = 8;
  static final int GROUP_SIZE = 4;
  static final int GROUPS_NUM = TEAMS_NUM / GROUP_SIZE;
  static final int LOG_LVL_DEBUG = 2;
  static final int LOG_LVL_TEST = 1;
  static int debug_level;

  public static void LOG(final int lvl, final String in, final Object... args) {
    if (lvl <= debug_level) {
      System.out.println(String.format(in, args));
    }
  }

  public static void LOGD(final String in, final Object... args) {
    LOG(LOG_LVL_DEBUG, in, args);
  }

  public static void LOGT(final String in, final Object... args) {
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

  public int algorithm_init(String[] argv) {
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
      if (argv.length > 1) {
          debug_level = Integer.parseInt(argv[1]);
          if (argv.length > 2) {
              manipulated_team_id = Integer.parseInt(argv[2]);
              int i, j;
              for (i = 3; i < argv.length; i++) {
                  j = Integer.parseInt(argv[i]);
                  positive_manipulators[j][manipulated_team_id] = true;
                  LOGD("team %d will manipulate for win of team %d\n", j, manipulated_team_id);
              }
          }
      }
      return 0;
  }

  public Boolean game_winner_first_win(int tm1, int tm2) {
      if (positive_manipulators[tm1][tm2] == true) {
          LOGD("%d wins %d due to manipulation\n", tm2, tm1);
          return false;
      }
      if (positive_manipulators[tm2][tm1] == true) {
          LOGD("%d wins %d due to manipulation\n", tm1, tm2);
          return true;
      }
      if (strength_graph[tm1][tm2] == true) {
          LOGD("%d wins %d due to strength\n", tm1, tm2);
          return true;
      }
      LOGD("%d wins %d due to strength\n", tm2, tm1);
      return false;
  }

  public Boolean first_team_best_score(team tm1, team tm2) {
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
      if (strength_graph[tm1.id][tm2.id] == true) {
          LOGD("%d before %d due to strength\n", tm1.id, tm2.id);
          return true;
      }
      LOGD("%d before %d due to strength\n", tm2.id, tm1.id);
      return false;
  }

  public int calculate_groups_scores() {
      int g, t1, t2;
      team temp;
      for (g = 0; g < GROUPS_NUM; g++) {
          LOGD("GROUP %d - calculate winners:\n", g);
          for (t1 = 0; t1 < GROUP_SIZE; t1++) {
              for (t2 = t1 + 1; t2 < GROUP_SIZE; t2++) {
                  if (game_winner_first_win(groups[g].teams[t1].id, groups[g].teams[t2].id) == true) {
                      groups[g].teams[t1].score += 3;
                  } else {
                      groups[g].teams[t2].score += 3;
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

  public Boolean calculate_tree() {
      team[] tree_teams = new team[GROUPS_NUM * 2];
      int g, t = 0;
      for (g = 0; g < GROUPS_NUM; g++, t++) {
          if (g %2 == 0) {
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

  public Boolean try_swap_two_firsts_on_tm_group() {
      return false;
  }

  public int algorithm_execute() {
      calculate_groups_scores();
      if (calculate_tree()) {
          LOGT("yes\n");
          return 0;
      }
      if (try_swap_two_firsts_on_tm_group()) {
          if (calculate_tree()) {
              LOGT("yes\n");
              return 0;
          }
      }
      LOGT("no\n");
      return 0;
  }
}

class sportsManipulation {
  public static void main(String[] argv) {
      league lg = new league();
      lg.algorithm_init(argv);
      lg.algorithm_execute();
  }
}
