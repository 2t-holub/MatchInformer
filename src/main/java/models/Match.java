package models;

import java.io.File;

public class Match {
    private int matchId;
    private String team1 = "Team1";
    private String team2 = "Team2";
    private String link;
    private String broadcastLink = "NOLINK";
    private String format;
    private int stars;
    private String bestPlayerNickname;
    private File beforeMatchImage;
    private File resultMatchImage;
    private File playerStatsImage;
    private int score1;
    private int score2;

    public Match(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public String getTeam1() {
        return team1;
    }

    public void setTeam1(String team1) {
        team1 = replaceSpecSymbols(team1);
        this.team1 = team1;
    }

    public String getTeam2() {
        return team2;
    }

    public void setTeam2(String team2) {
        team2 = replaceSpecSymbols(team2);
        this.team2 = team2;
    }

    public void setBestPlayerNickname(String bestPlayerNickname) {
        this.bestPlayerNickname = bestPlayerNickname;
    }

    public void setScore1(int score1) {
        this.score1 = score1;
    }

    public void setScore2(int score2) {
        this.score2 = score2;
    }

    private String replaceSpecSymbols(String str){
        str = str.replaceAll("`", "^");
        str = str.replaceAll("'", "^");
        str = str.replaceAll("_", "-");
        return str;
    }

    @Override
    public String toString() {
        return "https://www.hltv.org/matches/" + link;
    }

    public String getResultText(){
        return "⚔️ " + team1 + " " + score1 + ":" + score2 + " " + team2;
    }

    public String getPlayerText(){
        return "\uD83D\uDC51 Player of the match: " + bestPlayerNickname;
    }
}
