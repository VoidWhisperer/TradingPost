package edu.rit.chrisbitler.ritcraft.tradingpost;

import edu.rit.chrisbitler.ritcraft.tradingpost.cmd.TradePost;
import edu.rit.chrisbitler.ritcraft.tradingpost.data.Sale;
import edu.rit.chrisbitler.ritcraft.tradingpost.data.Sales;
import edu.rit.chrisbitler.ritcraft.tradingpost.web.Index;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by Chris on 11/1/2015.
 */
public class TradingPost extends JavaPlugin {
    private Connection conn;
    private FileConfiguration config;
    public static TradingPost instance;
    public Economy economy;
    public void onEnable() {
        instance = this;

        //Load configuration
        config = getConfig();
        String mysqlUsername,mysqlPassword,mysqlHost,mysqlDb;
        String ip;
        int port;

        if(getConfig().getString("mysql_username") != null && !getConfig().getString("mysql_username").equalsIgnoreCase("username")) {
            mysqlUsername = getConfig().getString("mysql_username");
            mysqlPassword = getConfig().getString("mysql_password");
            mysqlHost = getConfig().getString("mysql_host");
            mysqlDb = getConfig().getString("mysql_db");
            ip = getConfig().getString("website_ip");
            port = Integer.parseInt(getConfig().getString("website_port"));
        }else{
            if(getConfig().getString("mysql_username") == null) {
                getConfig().addDefault("mysql_username", "username");
                getConfig().addDefault("mysql_password", "password");
                getConfig().addDefault("mysql_host", "host");
                getConfig().addDefault("mysql_db", "tp_db");
                getConfig().addDefault("website_ip","0.0.0.0");
                getConfig().addDefault("website_port","33333");
                getConfig().options().copyDefaults(true);
                saveConfig();
            }
            System.out.println("Please configure the mysql and other information for this plugin before using it.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        //Attempt to connect to mysql
        try {
            conn = DriverManager.getConnection("jdbc:mysql://"+mysqlHost+"/"+mysqlDb+"?user="+mysqlUsername+"&password="+mysqlPassword);
        } catch (SQLException e) {
            System.out.println("Error connecting to mysql! Check your connection info - " + e.getMessage());
        }

        //Load sales data
        System.out.println("Loading offers..");
        try {
            Statement stmt = getMYSQL().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `listings`");
            while (rs.next()) {
                Sale sale = new Sale(rs.getString("owner"), rs.getInt("price"), rs.getInt("id"), rs.getString("item"));
                Sales.sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Attempt to start the spark site
        Index.register(ip,port);
        //Register commands
        getCommand("tradepost").setExecutor(new TradePost());

        setupEconomy();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public Connection getMYSQL() {
        return conn;
    }
}
