package rearthserver.net.rookieprot;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RookieProt extends JavaPlugin implements Listener {

    private HashMap<UUID, Long> cooldowns;
    private long cooldownMillis;
    private String cooldownMessage;
    private String protectionStartMessage;
    private String protectionEndMessage;

    @Override
    public void onEnable() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        cooldownMillis = config.getLong("prot-minutes", 10) * 60 * 1000;
        cooldownMessage = config.getString("prot");
        protectionStartMessage = config.getString("prot-start");
        protectionEndMessage = config.getString("prot-end");

        cooldowns = new HashMap<>();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (cooldowns.containsKey(playerId)) {
            return;
        }
        cooldowns.put(
                playerId,
                System.currentTimeMillis() + cooldownMillis
        );
        player.sendMessage(ChatColor.YELLOW + protectionStartMessage);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            UUID playerId = player.getUniqueId();
            if (
                    cooldowns.containsKey(playerId) &&
                            cooldowns.get(playerId) > System.currentTimeMillis()
            ) {
                event.setCancelled(true);
                Player attacker = null;
                if (event.getDamager() instanceof Player) {
                    attacker = (Player) event.getDamager();
                    attacker.sendMessage(ChatColor.RED + cooldownMessage);
                }
            } else {
                cooldowns.put(
                        playerId,
                        System.currentTimeMillis() + cooldownMillis
                );
                player.sendMessage(ChatColor.YELLOW + protectionEndMessage);
            }
        }
    }
}

