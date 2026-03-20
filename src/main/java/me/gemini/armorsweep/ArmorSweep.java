package me.gemini.armorsweep;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class ArmorSweep extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ArmorSweep активирован! Теперь разящий клинок работает на стойках.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorStandHit(EntityDamageByEntityEvent event) {
        // Проверяем, что ударили именно стойку для брони
        if (!(event.getEntity() instanceof ArmorStand)) return;
        
        // Проверяем, что ударил игрок
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Проверяем наличие чар "Разящий клинок" (Sweeping Edge)
        if (item.hasItemMeta() && item.getEnchantmentLevel(Enchantment.SWEEPING_EDGE) > 0) {
            int level = item.getEnchantmentLevel(Enchantment.SWEEPING_EDGE);
            double damage = event.getDamage();
            
            // Рассчитываем урон размаха по формуле: 
            // (урон_удара * (уровень_чар / (уровень_чар + 1))) + 1
            double sweepDamage = (damage * ((double) level / (level + 1))) + 1;

            spawnSweepEffect(event.getEntity().getLocation(), player);
            applySweepDamage(event.getEntity(), player, sweepDamage);
        }
    }

    private void applySweepDamage(Entity target, Player attacker, double damage) {
        // Ищем сущностей в радиусе 2.5 блоков (стандарт для размаха)
        Collection<Entity> nearby = target.getWorld().getNearbyEntities(target.getLocation(), 2.5, 1.0, 2.5);
        
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity && entity != target && entity != attacker) {
                // Наносим урон ближайшим живым существам
                ((LivingEntity) entity).damage(damage, attacker);
            }
        }
    }

    private void spawnSweepEffect(Location loc, Player player) {
        // Визуальный эффект размаха
        player.getWorld().spawnParticle(org.bukkit.Particle.SWEEP_ATTACK, loc.add(0, 1, 0), 1);
    }
}
