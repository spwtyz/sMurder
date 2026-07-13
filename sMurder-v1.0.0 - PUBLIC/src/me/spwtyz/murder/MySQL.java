package me.spwtyz.murder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class MySQL {

    private String user;
    private String database;
    private String password;
    private String port;
    private String hostname;
    private Main plugin;
    private Connection connection;

    public MySQL(Main plugin, String hostname, String port, String database, String username, String password) {
        this.plugin = plugin;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
        this.connection = null;
        openConnection();
    }

    public synchronized boolean isConnected() {
        return checkConnection();
    }

    public synchronized boolean checkConnection() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized Connection openConnection() {
        try {
            if (checkConnection()) {
                return connection;
            }

            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database
                    + "?autoReconnect=true&useSSL=false&characterEncoding=utf8";

            connection = DriverManager.getConnection(url, this.user, this.password);
            Bukkit.getConsoleSender().sendMessage("§a[sMurder] MySQL conectado/reconectado com sucesso.");
            createTable();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not connect to MySQL server! because: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "JDBC Driver not found!");
        }

        return connection;
    }

    public synchronized Connection getConnection() {
        if (!checkConnection()) {
            return openConnection();
        }
        return connection;
    }

    /**
     * Nao feche a conexao global depois de cada query.
     * Em Paper/Spigot 1.8 varias tasks async usam o mesmo objeto MySQL;
     * fechar aqui causa: No operations allowed after connection closed.
     */
    public void closeConnection() {
        // Mantido por compatibilidade com chamadas antigas. Nao faz nada.
    }

    /** Use apenas no onDisable. */
    public synchronized void closeConnectionInstantly() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error closing the MySQL Connection!");
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }

    public ResultSet Query(String query) {
        Connection c = getConnection();
        if (c == null) {
            return null;
        }

        try {
            Statement s = c.createStatement();
            return s.executeQuery(query);
        } catch (SQLException e) {
            // tenta uma reconexao rapida e executa mais uma vez
            try {
                synchronized (this) {
                    if (connection != null) {
                        try { connection.close(); } catch (Exception ignored) {}
                    }
                    connection = null;
                }
                c = getConnection();
                if (c != null) {
                    Statement s = c.createStatement();
                    return s.executeQuery(query);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return null;
    }

    public void update(final String update) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection c = getConnection();
                if (c == null) {
                    return;
                }

                Statement s = null;
                try {
                    s = c.createStatement();
                    s.executeUpdate(update);
                } catch (SQLException e) {
                    // reconecta e tenta novamente uma vez
                    try {
                        synchronized (MySQL.this) {
                            if (connection != null) {
                                try { connection.close(); } catch (Exception ignored) {}
                            }
                            connection = null;
                        }
                        c = getConnection();
                        if (c != null) {
                            s = c.createStatement();
                            s.executeUpdate(update);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                } finally {
                    if (s != null) {
                        try { s.close(); } catch (SQLException ignored) {}
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public synchronized void createTable() {
        Connection c = connection;
        if (c == null) {
            return;
        }

        Statement statement = null;
        try {
            statement = c.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderData (playername VARCHAR(16), wins INT(10), loses INT(10), deaths INT(10), kills INT(10), coins INT(10), score INT(10));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderKnifeData (uuid VARCHAR(36) NOT NULL, selected_skin VARCHAR(64), selected_trail VARCHAR(64), owned_skins TEXT, owned_trails TEXT, PRIMARY KEY (uuid));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderKnifeUpgrades (uuid VARCHAR(36) NOT NULL, skin_id VARCHAR(64) NOT NULL, attr VARCHAR(32) NOT NULL, value INT(10), PRIMARY KEY (uuid, skin_id, attr));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderLevelData (uuid VARCHAR(36) NOT NULL, name VARCHAR(16), level INT(10), xp INT(10), total_xp INT(10), PRIMARY KEY (uuid));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderTags (uuid VARCHAR(36) NOT NULL, tag VARCHAR(96), PRIMARY KEY (uuid));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderCosmetics (uuid VARCHAR(36) NOT NULL, category VARCHAR(32) NOT NULL, owned TEXT, selected VARCHAR(64), PRIMARY KEY(uuid, category));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderBattlePass (uuid VARCHAR(36) NOT NULL, xp INT(10), premium BOOLEAN, claimed TEXT, boxes INT(10), PRIMARY KEY (uuid));");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MurderLeaderboards (type VARCHAR(64) NOT NULL, world VARCHAR(64) NOT NULL, x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, yaw FLOAT NOT NULL, pitch FLOAT NOT NULL, updated_at BIGINT NOT NULL, PRIMARY KEY (type));");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try { statement.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
