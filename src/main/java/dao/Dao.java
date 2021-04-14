package dao;

import bot.Bot;
import models.Match;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dao implements Closeable {
    static Connection con;

    public Dao() {
        String url = Bot.properties.getProperty("DBurl");
        String userName = Bot.properties.getProperty("DBuserName");
        String password = Bot.properties.getProperty("DBpassword");
        try {
            con = DriverManager.getConnection(url, userName, password);
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean writeMatch(String link) {
        try {
            Statement stmt = con.createStatement();
            ResultSet select = stmt.executeQuery("SELECT * FROM hltvmatch;");
            while (select.next()) {
                if (link.equals(select.getString("link"))) {
                    stmt.close();
                    return true;
                }
            }
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            Statement stmt = con.createStatement();
            String sql = "INSERT INTO hltvmatch (link, status) " +
                    "VALUES ('" + link +"', 'future');";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public ArrayList<Match> getFutureMatches() {
        ArrayList<Match> futureMatches = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            ResultSet select = stmt.executeQuery("SELECT * FROM hltvmatch WHERE status = 'future';");
            while (select.next()) {
                String link = select.getString("link");
                futureMatches.add(new Match(link));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return futureMatches;
    }

    public ArrayList<Match> getLiveMatches() {
        ArrayList<Match> liveMatches = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            ResultSet select = stmt.executeQuery("SELECT * FROM hltvmatch WHERE status = 'live';");
            while (select.next()) {
                liveMatches.add(new Match(select.getString("link")));
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liveMatches;
    }

    public boolean changeMatchToLive(Match match) {
        try {
            Statement stmt = con.createStatement();
            int updateCounter = stmt.executeUpdate("UPDATE hltvmatch SET status = 'live' WHERE link = '" + match.getLink() + "';");
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    public boolean changeMatchToPast(Match match) {
        try {
            Statement stmt = con.createStatement();
            int updateCounter = stmt.executeUpdate("UPDATE hltvmatch SET status = 'past' WHERE link = '" + match.getLink() + "';");
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(Dao.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
