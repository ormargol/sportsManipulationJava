class sportsManipulation {
  private static final int TEAMS_NUM = 8;
  private static final int GROUP_SIZE = 4;
  private static final int GROUPS_NUM = TEAMS_NUM / GROUP_SIZE;
  private static final int LOG_LVL_DEBUG = 2;
  private static final int LOG_LVL_TEST = 1;
  private static int debug_level;

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
    team[] teams[GROUP_SIZE];

    group(int n_id) {
      id = n_id;
      teams = new team[GROUP_SIZE];
    }   
  }

  class league {
    group[] groups;
    bool[][] strength_graph;
    bool[][] positive_manipulators;
    int manipulated_team_id;

    league(int n_manipulated_team_id) {
      manipulated_team_id = n_manipulated_team_id;
      groups = new group[GROUPS_NUM];
      strength_graph = new bool[TEAMS_NUM][TEAMS_NUM];
      positive_manipulators = new bool[TEAMS_NUM][TEAMS_NUM];
    }
  }

  public static int algorithm_init(league lg, int argc, char[][] argv) {
      int g, t1, t2;
      for (g = 0; g < GROUPS_NUM; g++) {
          lg.groups[g] = malloc(sizeof(group));
          lg.groups[g].id = g + 1;
          for (t1 = 0; t1 < GROUP_SIZE; t1++) {
              lg.groups[g].teams[t1] = malloc(sizeof(team));
              lg.groups[g].teams[t1].id = g * GROUP_SIZE + t1;
          }
      }
      for (t1 = 0; t1 < TEAMS_NUM; t1++) {
          for (t2 = 0; t2 < TEAMS_NUM; t2++) {
              if (t2 >= t1 + 1) {
                  lg.strength_graph[t1][t2] = true;// t1 beats t2
              } else {
                  lg.strength_graph[t1][t2] = false;
              }
              lg.positive_manipulators[t1][t2] = false;
          }
      }
      if (argc > 1) {
          sscanf(argv[1], "%d", debug_level);
          if (argc > 2) {
              sscanf(argv[2], "%d", lg.manipulated_team_id);
              int i, j;
              for (i = 3; i < argc; i++) {
                  sscanf(argv[i], "%d", j);
                  lg.positive_manipulators[j][lg.manipulated_team_id] = true;
                  LOGD("team %d will manipulate for win of team %d\n", j, lg.manipulated_team_id);
              }
          }
      }
      return 0;
  }

  public static bool game_winner_first_win(league lg, int tm1, int tm2) {
      if (lg.positive_manipulators[tm1][tm2] == true) {
          LOGD("%d wins %d due to manipulation\n", tm2, tm1);
          return false;
      }
      if (lg.positive_manipulators[tm2][tm1] == true) {
          LOGD("%d wins %d due to manipulation\n", tm1, tm2);
          return true;
      }
      if (lg.strength_graph[tm1][tm2] == true) {
          LOGD("%d wins %d due to strength\n", tm1, tm2);
          return true;
      }
      LOGD("%d wins %d due to strength\n", tm2, tm1);
      return false;
  }

  public static bool first_team_best_score(league lg, team tm1, team tm2) {
      if (tm1.score > tm2.score) {
          LOGD("%d before %d due to score\n", tm1.id, tm2.id);
          return true;
      }
      if (tm2.score > tm1.score) {
          LOGD("%d before %d due to score\n", tm2.id, tm1.id);
          return false;
      }
      if (lg.positive_manipulators[tm1.id][tm2.id] == true) {
          LOGD("%d before %d due to manipulation\n", tm2.id, tm1.id);
          return false;
      }
      if (lg.positive_manipulators[tm2.id][tm1.id] == true) {
          LOGD("%d before %d due to manipulation\n", tm1.id, tm2.id);
          return true;
      }
      if (lg.strength_graph[tm1.id][tm2.id] == true) {
          LOGD("%d before %d due to strength\n", tm1.id, tm2.id);
          return true;
      }
      LOGD("%d before %d due to strength\n", tm2.id, tm1.id);
      return false;
  }

  public static int calculate_groups_scores(league lg) {
      int g, t1, t2;
      team temp;
      for (g = 0; g < GROUPS_NUM; g++) {
          LOGD("GROUP %d - calculate winners:\n", g);
          for (t1 = 0; t1 < GROUP_SIZE; t1++) {
              for (t2 = t1 + 1; t2 < GROUP_SIZE; t2++) {
                  if (game_winner_first_win(lg, lg.groups[g].teams[t1].id, lg.groups[g].teams[t2].id) == true) {
                      lg.groups[g].teams[t1].score += 3;
                  } else {
                      lg.groups[g].teams[t2].score += 3;
                  }
              }
          }
          LOGD("\n");
          LOGD("calculate table order:\n");
          for (t1 = 0; t1 < GROUP_SIZE; t1++) {
              for (t2 = t1 + 1; t2 < GROUP_SIZE; t2++) {
                  if (first_team_best_score(lg, lg.groups[g].teams[t2], lg.groups[g].teams[t1])) {
                      temp = lg.groups[g].teams[t1];
                      lg.groups[g].teams[t1] = lg.groups[g].teams[t2];
                      lg.groups[g].teams[t2] = temp;
                  }
              }
          }
          LOGD("\n");
          LOGD("final table:\n");
          for (t1 = 0; t1 < GROUP_SIZE; t1++) {
              LOGD("TEAM %d: score=%d\n", lg.groups[g].teams[t1].id, lg.groups[g].teams[t1].score);
          }
          LOGD("\n");
      }
      return 0;
  }

  public static bool calculate_tree(league lg) {
      team tree_teams[GROUPS_NUM * 2];
      int g, t = 0;
      for (g = 0; g < GROUPS_NUM; g++, t++) {
          if (g %2 == 0) {
              tree_teams[t] = lg.groups[g].teams[0];
              tree_teams[GROUPS_NUM * 2 - t - 1] = lg.groups[g].teams[1];
          } else {
              tree_teams[t] = lg.groups[g].teams[1];
              tree_teams[GROUPS_NUM * 2 - t - 1] = lg.groups[g].teams[0];
          }
      }
      int left_teams;
      for (left_teams = GROUPS_NUM * 2; left_teams > 1; left_teams /= 2) {
          for (t = 0; t < left_teams / 2; t++) {
              if (game_winner_first_win(lg, tree_teams[2 * t].id, tree_teams[2 * t + 1].id) == true) {
                  tree_teams[t] = tree_teams[2 * t];
              } else {
                  tree_teams[t] = tree_teams[2 * t + 1];
              }
          }
          LOGD("\n");
      }
      LOGD("%d\n", tree_teams[0].id);
      if (tree_teams[0].id == lg.manipulated_team_id) {
          return true;
      } else {
          return false;
      }
  }

  public static bool try_swap_two_firsts_on_tm_group(league lg) {
      return false;
  }

  public static int algorithm_execute(league lg) {
      calculate_groups_scores(lg);
      if (calculate_tree(lg)) {
          LOGT("yes\n");
          return 0;
      }
      if (try_swap_two_firsts_on_tm_group(lg)) {
          if (calculate_tree(lg)) {
              LOGT("yes\n");
              return 0;
          }
      }
      LOGT("no\n");
      return 0;
  }

  public static int main(int argc, char[][] argv) {
      league lg;
      algorithm_init(lg, argc, argv);
      algorithm_execute(lg);
      return 0;
  }
}
