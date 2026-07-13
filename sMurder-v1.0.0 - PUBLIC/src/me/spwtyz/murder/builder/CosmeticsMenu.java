package me.spwtyz.murder.builder;

import java.util.*;
import java.lang.reflect.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Base64;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.spwtyz.murder.Main;
import me.spwtyz.murder.api.MurderAPI;
import me.spwtyz.murder.listeners.ItemBuilder;
import me.spwtyz.murder.knife.KnifeSkinManager;
import me.spwtyz.murder.hats.HatAbilityManager;
import me.spwtyz.murder.guns.GunSkinManager;

public class CosmeticsMenu implements Listener {

    private final Main plugin;

    private static final String MAIN_TITLE = "§6§lLoja";
    private static final String KNIFE_TITLE = "§8Loja › Facas";
    private static final String HAT_TITLE = "§8Loja › Chapéus";
    private static final String GUN_TITLE = "§8Loja › Armas";
    private static final String TRAIL_TITLE = "§8Loja › Efeitos de Faca";
    private static final String DEATH_TITLE = "§8Loja › Efeitos de Morte";
    private static final String VICTORY_TITLE = "§8Loja › Vitória";
    private static final String AURA_TITLE = "§8Loja › Auras";
    private static final String TITLE_TITLE = "§8Loja › Títulos";
    private static final String GADGET_TITLE = "§8Loja › Gadgets";
    private static final String EMOTE_TITLE = "§8Loja › Emotes";
    private static final String COMPANION_TITLE = "§8Loja › Companions";
    private static final String PROFILE_TITLE = "§8Loja › Perfil";
    private static final String EFFECTS_TITLE = "§8Loja › Efeitos";
    private static final String BALLOON_TITLE = "§8Loja › Balloons";
    private static final String ANIMATED_HAT_TITLE = "§8Loja › Chapéus Animados";
    private static final String WALK_TRAIL_TITLE = "§8Loja › Walk Trails";

    public CosmeticsMenu(Main plugin) {
        this.plugin = plugin;
    }

    public enum Rarity {
        COMUM("§7Comum"), RARO("§9Raro"), EPICO("§5Épico"), LENDARIO("§6Lendário");
        private final String display;
        Rarity(String display) { this.display = display; }
        public String getDisplay() { return display; }
    }

    public enum GenericCosmetic {
        // Death effects
        DEATH_LIGHTNING("death_lightning", "§eRaio Final", "death", Material.GOLD_AXE, 1000, 1, Rarity.RARO, "§7Um raio visual cai quando você morre."),
        DEATH_EXPLOSION("death_explosion", "§cExplosão", "death", Material.TNT, 1500, 3, Rarity.EPICO, "§7Explosão visual ao morrer."),
        DEATH_FUNERAL("death_funeral", "§8Funeral", "death", Material.COAL_BLOCK, 2500, 5, Rarity.LENDARIO, "§7Efeito sombrio estilo funeral."),
        DEATH_GHOST("death_ghost", "§fFantasma", "death", Material.GHAST_TEAR, 1800, 4, Rarity.EPICO, "§7Partículas de fantasma na sua morte."),

        // Victory effects
        VICTORY_FIREWORK("victory_firework", "§aFogos", "victory", Material.FIREWORK, 1200, 2, Rarity.RARO, "§7Fogos ao vencer a partida."),
        VICTORY_LIGHTNING("victory_lightning", "§eTempestade", "victory", Material.BLAZE_ROD, 2500, 8, Rarity.LENDARIO, "§7Raios visuais ao vencer."),
        VICTORY_MUSIC("victory_music", "§dMúsica Final", "victory", Material.NOTE_BLOCK, 1600, 5, Rarity.EPICO, "§7Som especial de vitória."),

        // Auras
        AURA_BLOOD("aura_blood", "§cAura Sangrenta", "aura", Material.REDSTONE, 2000, 7, Rarity.EPICO, "§7Aura vermelha para exibir no lobby."),
        AURA_DARK("aura_dark", "§8Aura Sombria", "aura", Material.COAL, 2200, 9, Rarity.EPICO, "§7Aura escura e misteriosa."),
        AURA_MAGIC("aura_magic", "§dAura Mágica", "aura", Material.ENDER_PEARL, 3000, 12, Rarity.LENDARIO, "§7Aura mágica com partículas."),

        // Titles
        TITLE_SHADOW("title_shadow", "§8✦ The Shadow", "title", Material.NAME_TAG, 900, 1, Rarity.COMUM, "§7Título acima da cabeça."),
        TITLE_BLOOD_REAPER("title_blood_reaper", "§c☠ Blood Reaper", "title", Material.NAME_TAG, 1800, 4, Rarity.RARO, "§7Título acima da cabeça."),
        TITLE_NIGHTMARE("title_nightmare", "§5☽ Nightmare", "title", Material.NAME_TAG, 2200, 6, Rarity.EPICO, "§7Título acima da cabeça."),
        TITLE_FROZEN_KING("title_frozen_king", "§b❄ Frozen King", "title", Material.NAME_TAG, 2600, 8, Rarity.EPICO, "§7Título acima da cabeça."),
        TITLE_STORM_BRINGER("title_storm", "§e⚡ Storm Bringer", "title", Material.NAME_TAG, 3000, 10, Rarity.EPICO, "§7Título acima da cabeça."),
        TITLE_TOXIC("title_toxic", "§a☣ Toxic", "title", Material.NAME_TAG, 3200, 12, Rarity.EPICO, "§7Título acima da cabeça."),
        TITLE_SERIAL("title_serial", "§cSerial Killer", "title", Material.NAME_TAG, 2500, 10, Rarity.EPICO, "§7Título acima da cabeça."),
        TITLE_LEGEND("title_legend", "§6✪ Lenda do Murder", "title", Material.NAME_TAG, 5000, 25, Rarity.LENDARIO, "§7Título acima da cabeça."),
        TITLE_DIVINE("title_divine", "§d✧ Divine", "title", Material.NAME_TAG, 7500, 35, Rarity.LENDARIO, "§7Título acima da cabeça."),
        TITLE_VOID("title_void", "§0✹ Void Walker", "title", Material.NAME_TAG, 9000, 45, Rarity.LENDARIO, "§7Título acima da cabeça."),

        // Gadgets
        GADGET_FIREWORK("gadget_firework", "§aFirework Gadget", "gadget", Material.FIREWORK, 1000, 1, Rarity.RARO, "§7Gadget visual para lobby."),
        GADGET_SMOKE("gadget_smoke", "§7Smoke Gadget", "gadget", Material.SULPHUR, 1500, 4, Rarity.RARO, "§7Solta fumaça no lobby."),
        GADGET_HEART("gadget_heart", "§dHeart Gadget", "gadget", Material.RED_ROSE, 1800, 6, Rarity.EPICO, "§7Partículas de coração no lobby."),
        GADGET_GRAPPLING("gadget_grappling", "§bGrappling Hook", "gadget", Material.FISHING_ROD, 4500, 8, Rarity.EPICO, "§7Puxa você para frente no lobby."),
        GADGET_TRAMPOLINE("gadget_trampoline", "§aTrampolim", "gadget", Material.SLIME_BLOCK, 5000, 10, Rarity.EPICO, "§7Cria um trampolim temporário."),
        GADGET_TNT_FAKE("gadget_tnt_fake", "§cTNT Fake", "gadget", Material.TNT, 5500, 12, Rarity.EPICO, "§7Explosão fake sem dano."),
        GADGET_PAINT("gadget_paint", "§dPaint Gun", "gadget", Material.INK_SACK, 6000, 15, Rarity.EPICO, "§7Pinta o chão temporariamente."),
        GADGET_CARPET("gadget_carpet", "§5Magic Carpet", "gadget", Material.CARPET, 9000, 20, Rarity.LENDARIO, "§7Voa por alguns segundos em um tapete."),

        // Emotes estilo GadgetsMenu: ficam ativos ate o jogador remover/desativar.
        EMOTE_SMILE("emote_smile", "§eEmote Feliz", "emote", Material.SKULL_ITEM, 1200, 1, Rarity.COMUM, "§7Carinha feliz animada acima da cabeça."),
        EMOTE_CRY("emote_cry", "§bEmote Chorando", "emote", Material.SKULL_ITEM, 1800, 3, Rarity.RARO, "§7Carinha chorando animada acima da cabeça."),
        EMOTE_RAGE("emote_rage", "§cEmote Bravo", "emote", Material.SKULL_ITEM, 2200, 5, Rarity.EPICO, "§7Carinha brava animada acima da cabeça."),
        EMOTE_COOL("emote_cool", "§9Emote Cool", "emote", Material.SKULL_ITEM, 2500, 7, Rarity.EPICO, "§7Carinha de óculos animada acima da cabeça."),
        EMOTE_HEART("emote_heart", "§dEmote Apaixonado", "emote", Material.SKULL_ITEM, 3500, 10, Rarity.LENDARIO, "§7Carinha apaixonada animada acima da cabeça."),

        // Companions estilo kCosmetics: ficam seguindo o jogador no lobby ate remover.
        COMPANION_MINI_MURDER("companion_mini_murder", "§cMini Murder", "companion", Material.SKULL_ITEM, 4500, 5, Rarity.EPICO, "§7Um mini assassino flutuando do seu lado."),
        COMPANION_MINI_DETECTIVE("companion_mini_detective", "§bMini Detective", "companion", Material.SKULL_ITEM, 4500, 5, Rarity.EPICO, "§7Um mini detetive companheiro."),
        COMPANION_GHOST("companion_ghost", "§fFantasma", "companion", Material.GHAST_TEAR, 5000, 7, Rarity.EPICO, "§7Um fantasma orbitando ao seu redor."),
        COMPANION_DRAGON_FIRE("companion_dragon_fire", "§6Dragão de Fogo", "companion", Material.BLAZE_POWDER, 8500, 15, Rarity.LENDARIO, "§7Dragão com partículas de fogo."),
        COMPANION_DRAGON_ICE("companion_dragon_ice", "§bDragão de Gelo", "companion", Material.SNOW_BALL, 8500, 15, Rarity.LENDARIO, "§7Dragão com partículas de gelo."),
        COMPANION_AMONG_US("companion_among_us", "§aMini Among Us", "companion", Material.SKULL_ITEM, 6500, 10, Rarity.EPICO, "§7Crewmate compacto para o lobby."),
        COMPANION_KOALA("companion_koala", "§7Koala", "companion", Material.SKULL_ITEM, 3500, 3, Rarity.RARO, "§7Companion fofo estilo kCosmetics."),
        COMPANION_PANDA("companion_panda", "§fPanda", "companion", Material.SKULL_ITEM, 4000, 4, Rarity.RARO, "§7Panda companheiro de lobby."),
        COMPANION_ROBOT("companion_robot", "§8R3D3 Robot", "companion", Material.REDSTONE_COMPARATOR, 7500, 12, Rarity.LENDARIO, "§7Robô pequeno com partículas elétricas."),

        // Balloons
        BALLOON_RED("balloon_red", "§cBalão Vermelho", "balloon", Material.WOOL, 1800, 2, Rarity.RARO, "§7Balão flutuante que acompanha você no lobby."),
        BALLOON_RAINBOW("balloon_rainbow", "§dBalão Arco-Íris", "balloon", Material.WOOL, 4200, 8, Rarity.EPICO, "§7Balão colorido com animação suave."),
        BALLOON_SKULL("balloon_skull", "§8Balão Caveira", "balloon", Material.SKULL_ITEM, 6500, 15, Rarity.LENDARIO, "§7Uma caveira flutuante presa ao jogador."),

        // Animated hats
        ANIMATED_HAT_RAINBOW("animated_hat_rainbow", "§dChapéu Arco-Íris", "animated_hat", Material.LEATHER_HELMET, 4500, 8, Rarity.EPICO, "§7Muda de cor continuamente no lobby."),
        ANIMATED_HAT_DISCO("animated_hat_disco", "§bChapéu Disco", "animated_hat", Material.GLOWSTONE, 6000, 12, Rarity.LENDARIO, "§7Alterna blocos luminosos e partículas."),
        ANIMATED_HAT_ELEMENTS("animated_hat_elements", "§6Chapéu Elemental", "animated_hat", Material.BLAZE_POWDER, 7500, 18, Rarity.LENDARIO, "§7Alterna entre fogo, gelo e magia."),

        // Walk trails
        WALK_TRAIL_FIRE("walk_trail_fire", "§6Pegadas de Fogo", "walk_trail", Material.BLAZE_POWDER, 2500, 4, Rarity.RARO, "§7Deixa partículas de fogo ao caminhar."),
        WALK_TRAIL_MAGIC("walk_trail_magic", "§dPegadas Mágicas", "walk_trail", Material.ENDER_PEARL, 4000, 8, Rarity.EPICO, "§7Deixa uma trilha mágica atrás de você."),
        WALK_TRAIL_HEART("walk_trail_heart", "§cPegadas de Coração", "walk_trail", Material.RED_ROSE, 5000, 12, Rarity.EPICO, "§7Corações aparecem enquanto você anda."),
        WALK_TRAIL_CLOUD("walk_trail_cloud", "§fPegadas de Nuvem", "walk_trail", Material.FEATHER, 6000, 16, Rarity.LENDARIO, "§7Nuvens leves acompanham seus passos.");

        private final String id;
        private final String name;
        private final String category;
        private final Material material;
        private final int price;
        private final int level;
        private final Rarity rarity;
        private final String description;

        GenericCosmetic(String id, String name, String category, Material material, int price, int level, Rarity rarity, String description) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.material = material;
            this.price = price;
            this.level = level;
            this.rarity = rarity;
            this.description = description;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public Material getMaterial() { return material; }
        public int getPrice() { return price; }
        public int getLevel() { return level; }
        public Rarity getRarity() { return rarity; }
        public String getDescription() { return description; }
    }


    public enum SeasonalCosmetic {
        DEATH("Death", "death", Material.SKULL_ITEM, 3500, 1, Rarity.EPICO, "§7Efeito de morte exclusivo do evento."),
        VICTORY("Victory", "victory", Material.FIREWORK, 4500, 1, Rarity.LENDARIO, "§7Efeito de vitória exclusivo do evento."),
        AURA("Aura", "aura", Material.REDSTONE, 3000, 1, Rarity.EPICO, "§7Aura exclusiva para o lobby."),
        GADGET("Gadget", "gadget", Material.CHEST, 2500, 1, Rarity.RARO, "§7Gadget exclusivo do evento.");

        private final String key;
        private final String category;
        private final Material material;
        private final int defaultPrice;
        private final int level;
        private final Rarity rarity;
        private final String description;

        SeasonalCosmetic(String key, String category, Material material, int defaultPrice, int level, Rarity rarity, String description) {
            this.key = key;
            this.category = category;
            this.material = material;
            this.defaultPrice = defaultPrice;
            this.level = level;
            this.rarity = rarity;
            this.description = description;
        }

        public String getKey() { return key; }
        public String getCategory() { return category; }
        public Material getMaterial() { return material; }
        public int getDefaultPrice() { return defaultPrice; }
        public int getLevel() { return level; }
        public Rarity getRarity() { return rarity; }
        public String getDescription() { return description; }
    }

    @EventHandler
    public void onInteractCosmeticos(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;

        ItemStack item = p.getItemInHand();
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String name = item.getItemMeta().getDisplayName();
        if (!isShopItemName(name)) return;

        e.setCancelled(true);
        openMainMenu(p);
    }

    private String seasonName(String normal) {
        return (plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive())
                ? plugin.seasonalEventManager.themedName(normal)
                : normal;
    }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_TITLE);

        ItemStack filler = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 15)
                .name(" ")
                .build();

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Coluna esquerda
        inv.setItem(10, category(Material.LEATHER_HELMET, seasonName("§6Chapéus"), "§7Cosméticos para cabeça.", "§eClique para abrir."));
        inv.setItem(19, category(Material.GLOWSTONE, seasonName("§eChapéus Animados"), "§7Chapéus que mudam e animam no lobby.", "§eClique para abrir."));
        inv.setItem(28, category(Material.LEASH, seasonName("§bBalloons"), "§7Balões cosméticos que acompanham você.", "§eClique para abrir."));
        inv.setItem(37, category(Material.FEATHER, seasonName("§fWalk Trails"), "§7Pegadas e partículas ao caminhar.", "§eClique para abrir."));

        // Meio
        inv.setItem(14, category(Material.DIAMOND_AXE, seasonName("§5Facas"), "§7Skins e upgrades da faca.", "§eClique para abrir."));
        inv.setItem(25, category(Material.FIREWORK, seasonName("§dEfeitos"), "§7Todos os efeitos do Murder.", "§eClique para abrir."));
        inv.setItem(15, category(Material.NAME_TAG, seasonName("§bTítulos"), "§7Linha extra acima da cabeça.", "§eClique para abrir."));

        // Direita
        inv.setItem(16, category(Material.DIAMOND_HOE, seasonName("§aArmas"), "§7Skins da arma do Detetive.", "§eClique para abrir."));
        inv.setItem(23, category(Material.ENDER_CHEST, seasonName("§aGadgets"), "§7Brinquedos para o lobby.", "§eClique para abrir."));
        inv.setItem(24, category(Material.SKULL_ITEM, seasonName("§dEmotes"), "§7Carinhas animadas na cabeça.", "§eClique para abrir."));
        inv.setItem(32, category(Material.MONSTER_EGG, seasonName("§eCompanions"), "§7Companhias que te seguem no lobby.", "§eClique para abrir."));
        inv.setItem(33, category(Material.NOTE_BLOCK, seasonName("§5Sons"), "§7Sons cosméticos.", "§cEm breve."));


        // Hotbar
        inv.setItem(45, new ItemBuilder(Material.COMPASS)
                .name("§bPerfil")
                .lore("§7Veja seus cosméticos equipados.", "", "§eClique para abrir.")
                .build());

        inv.setItem(46, new ItemBuilder(Material.COAL)
                .name("§8Selecionados")
                .lore("§7Veja seus cosméticos equipados.", "", "§eClique para abrir.")
                .build());

        inv.setItem(48, new ItemBuilder(Material.CHEST)
                .name(seasonName("§b§lCaixas"))
                .lore("§7Caixas e recompensas.", "", "§eClique para abrir.")
                .build());

        inv.setItem(49, new ItemBuilder(Material.EMERALD)
                .name((plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive()) ? plugin.seasonalEventManager.getShopTitle() : "§6§lLoja")
                .lore("§7Coins: §e" + getCoins(p), "§7Nível: §b" + getLevel(p), "", "§eClique para abrir.")
                .build());

        inv.setItem(52, closeItem());

        inv.setItem(53, new ItemBuilder(Material.NETHER_STAR)
                .name("§eAjuda")
                .lore("§7Clique nos itens para comprar", "§7ou selecionar cosméticos.")
                .build());

        p.openInventory(inv);
    }

    private void openEffectsMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, EFFECTS_TITLE);
        decorate(inv);

        inv.setItem(10, category(Material.BLAZE_POWDER, "§dEfeitos de Faca", "§7Trilhas da faca arremessada.", "§eClique para abrir."));
        inv.setItem(12, category(Material.SKULL_ITEM, "§cEfeitos de Morte", "§7Ativam quando você morre.", "§eClique para abrir."));
        inv.setItem(14, category(Material.FIREWORK, "§6Efeitos de Vitória", "§7Ativam quando você vence.", "§eClique para abrir."));
        inv.setItem(16, category(Material.REDSTONE, "§cAuras", "§7Partículas no lobby.", "§eClique para abrir."));

        inv.setItem(22, backItem());

        p.openInventory(inv);
    }

    private ItemStack category(Material mat, String name, String line1, String line2) {
        return new ItemBuilder(mat).name(name).lore(line1, "", line2).removeAttributes().build();
    }

    private void decorate(Inventory inv) {
        ItemStack gray = glass((short) 7, " ");
        ItemStack black = glass((short) 15, " ");
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || i % 9 == 8) inv.setItem(i, gray);
        }
        inv.setItem(0, black); inv.setItem(8, black); inv.setItem(inv.getSize() - 9, black); inv.setItem(inv.getSize() - 1, black);
    }

    private ItemStack glass(short data, String name) {
        return new ItemBuilder(Material.STAINED_GLASS_PANE, 1, data).name(name).build();
    }

    private ItemStack backItem() {
        return new ItemBuilder(Material.ARROW).name("§eVoltar").lore("§7Clique para voltar ao menu principal.").build();
    }

    private ItemStack closeItem() {
        return new ItemBuilder(Material.BARRIER).name("§cFechar").lore("§7Clique para fechar.").build();
    }


    private boolean isShopItemName(String name) {
        if (name == null) return false;
        String clean = ChatColor.stripColor(name).toLowerCase();
        return clean.contains("loja") || clean.contains("cosmetico") || clean.contains("cosmeticos")
                || clean.contains("assombrada") || clean.contains("natalina") || clean.contains("pascoa")
                || clean.contains("evento");
    }

    private String seasonalId(SeasonalCosmetic cosmetic) {
        if (plugin.seasonalEventManager == null || !plugin.seasonalEventManager.isEventActive()) return "seasonal_none_" + cosmetic.getKey().toLowerCase();
        return "seasonal_" + plugin.seasonalEventManager.getActiveEventIdLower() + "_" + cosmetic.getKey().toLowerCase();
    }

    private String seasonalName(SeasonalCosmetic cosmetic) {
        String fallback = "§dCosmetico do Evento";
        if (cosmetic == SeasonalCosmetic.DEATH) fallback = "§dEfeito de Morte do Evento";
        if (cosmetic == SeasonalCosmetic.VICTORY) fallback = "§dEfeito de Vitoria do Evento";
        if (cosmetic == SeasonalCosmetic.AURA) fallback = "§dAura do Evento";
        if (cosmetic == SeasonalCosmetic.GADGET) fallback = "§dGadget do Evento";
        return plugin.seasonalEventManager == null ? fallback : plugin.seasonalEventManager.getCosmeticName(cosmetic.getKey(), fallback);
    }

    private int seasonalPrice(SeasonalCosmetic cosmetic) {
        return plugin.seasonalEventManager == null ? cosmetic.getDefaultPrice() : plugin.seasonalEventManager.getCosmeticPrice(cosmetic.getKey(), cosmetic.getDefaultPrice());
    }

    private boolean isCurrentSeason(String eventId) {
        if (eventId == null || eventId.isEmpty()) return false;
        return plugin.seasonalEventManager != null
                && plugin.seasonalEventManager.isEventActive()
                && plugin.seasonalEventManager.getActiveEventIdLower().equalsIgnoreCase(eventId);
    }

    private boolean visibleSeasonalKnife(Player p, KnifeSkinManager.KnifeSkin skin) {
        if (plugin.knifeSkinManager == null || !plugin.knifeSkinManager.isSeasonalSkin(skin)) return true;
        return isCurrentSeason(plugin.knifeSkinManager.getSeasonalEventId(skin)) || plugin.knifeSkinManager.ownsSkin(p, skin);
    }

    private boolean visibleSeasonalGun(Player p, GunSkinManager.GunSkin gun) {
        if (plugin.gunSkinManager == null || !plugin.gunSkinManager.isSeasonalSkin(gun)) return true;
        return isCurrentSeason(plugin.gunSkinManager.getSeasonalEventId(gun)) || plugin.gunSkinManager.owns(p, gun);
    }

    private boolean visibleSeasonalCosmetic(Player p, SeasonalCosmetic cosmetic) {
        if (plugin.seasonalEventManager == null) return ownsSeasonal(p, cosmetic);
        return plugin.seasonalEventManager.isEventActive() || ownsSeasonal(p, cosmetic);
    }

    private void openSeasonalMenu(Player p) {
        if (plugin.seasonalEventManager == null || !plugin.seasonalEventManager.isEventActive()) {
            p.sendMessage("§cNenhum evento sazonal ativo.");
            openMainMenu(p);
            return;
        }
        Inventory inv = Bukkit.createInventory(null, 27, "§8Loja › Evento");
        decorate(inv);
        inv.setItem(4, new ItemBuilder(Material.NETHER_STAR)
                .name(plugin.seasonalEventManager.getString("CosmeticCategoryName", "§dCosmeticos do Evento"))
                .lore("§7Evento atual: " + plugin.seasonalEventManager.getDisplayName(), "§7Itens limitados enquanto o evento estiver ativo.")
                .build());
        int[] slots = {10, 12, 14, 16};
        int i = 0;
        for (SeasonalCosmetic cosmetic : SeasonalCosmetic.values()) {
            inv.setItem(slots[i++], seasonalItem(p, cosmetic));
        }
        inv.setItem(22, backItem());
        p.openInventory(inv);
    }

    private ItemStack seasonalItem(Player p, SeasonalCosmetic cosmetic) {
        List<String> lore = new ArrayList<String>();
        lore.add("§8ID: " + seasonalId(cosmetic));
        lore.add("§7Raridade: " + cosmetic.getRarity().getDisplay());
        lore.add(cosmetic.getDescription());
        lore.add(" ");
        if (ownsSeasonal(p, cosmetic)) {
            lore.add(isSelectedSeasonal(p, cosmetic) ? "§a§lEQUIPADO" : "§eClique para equipar.");
        } else {
            lore.add("§7Preço: §e" + seasonalPrice(cosmetic) + " coins");
            lore.add(canAfford(p, seasonalPrice(cosmetic)) ? "§aClique para comprar." : "§cCoins insuficientes.");
        }
        return new ItemBuilder(cosmetic.getMaterial()).name(seasonalName(cosmetic)).lore(lore).removeAttributes().build();
    }

    private void openKnifeMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, KNIFE_TITLE);
        decorate(inv);
        inv.setItem(4, new ItemBuilder(Material.DIAMOND_AXE).name("§5§lFacas")
                .lore("§7Clique esquerdo: comprar/selecionar", "§7Clique direito: melhorar atributos", "", "§7Coins: §e" + getCoins(p))
                .removeAttributes().build());
        inv.setItem(45, backItem());
        inv.setItem(49, closeItem());

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        int i = 0;
        for (KnifeSkinManager.KnifeSkin skin : plugin.knifeSkinManager.getSkins()) {
            if (!visibleSeasonalKnife(p, skin)) continue;
            if (i >= slots.length) break;
            inv.setItem(slots[i++], knifeItem(p, skin));
        }
        p.openInventory(inv);
    }

    private ItemStack knifeItem(Player p, KnifeSkinManager.KnifeSkin skin) {
        List<String> lore = new ArrayList<String>();
        lore.add("§8ID: " + skin.getId());
        lore.add("§7Raridade: " + rarityFromPrice(skin.getPrice()).getDisplay());
        lore.add("§7Requer nível: §b" + skin.getLevel());
        lore.add(" ");
        lore.addAll(plugin.knifeSkinManager.buildSkinLore(p, skin));
        lore.add(" ");
        if (plugin.knifeSkinManager.ownsSkin(p, skin)) {
            lore.add(plugin.knifeSkinManager.getSelected(p) == skin ? "§a§lEQUIPADA" : "§eClique para equipar.");
            lore.add("§bClique direito para upgrades.");
        } else {
            lore.add("§7Preço: §e" + skin.getPrice() + " coins");
            lore.add(canAfford(p, skin.getPrice()) ? "§aClique para comprar." : "§cCoins insuficientes.");
        }
        return new ItemBuilder(skin.getMaterial(), 1, skin.getData()).name(skin.getDisplayName()).lore(lore).removeAttributes().build();
    }

    private void openTrailMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TRAIL_TITLE);
        decorate(inv);
        inv.setItem(4, new ItemBuilder(Material.BLAZE_POWDER).name("§d§lEfeitos de Faca")
                .lore("§7Trilhas que seguem a faca arremessada.", "", "§7Coins: §e" + getCoins(p)).build());
        inv.setItem(45, backItem()); inv.setItem(49, closeItem());
        int[] slots = {11,12,13,14,15,20,21,22,23,24,29,30,31,32,33};
        int i = 0;
        for (KnifeSkinManager.KnifeTrail trail : plugin.knifeSkinManager.getTrails()) {
            if (i >= slots.length) break;
            inv.setItem(slots[i++], trailItem(p, trail));
        }
        p.openInventory(inv);
    }

    private ItemStack trailItem(Player p, KnifeSkinManager.KnifeTrail trail) {
        List<String> lore = new ArrayList<String>();
        lore.add("§8ID: " + trail.getId());
        lore.add("§7Raridade: " + rarityFromPrice(trail.getPrice()).getDisplay());
        lore.add("§7Efeito visual da faca arremessada.");
        lore.add(" ");
        if (plugin.knifeSkinManager.ownsTrail(p, trail)) {
            lore.add(plugin.knifeSkinManager.getSelectedTrail(p) == trail ? "§a§lEQUIPADO" : "§eClique para equipar.");
            lore.add("§7Clique direito para preview.");
        } else {
            lore.add("§7Preço: §e" + trail.getPrice() + " coins");
            lore.add(canAfford(p, trail.getPrice()) ? "§aClique para comprar." : "§cCoins insuficientes.");
        }
        return new ItemBuilder(trail.getMaterial()).name(trail.getDisplayName()).lore(lore).removeAttributes().build();
    }

    private void openGenericMenu(Player p, String title, String category, Material icon, String display) {
        Inventory inv = Bukkit.createInventory(null, 54, title);
        decorate(inv);
        inv.setItem(4, new ItemBuilder(icon).name(display).lore("§7Comprar, selecionar e fazer preview.", "", "§7Coins: §e" + getCoins(p)).removeAttributes().build());
        inv.setItem(40, removeSelectedItem(category));
        inv.setItem(45, backItem()); inv.setItem(49, closeItem());

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        int i = 0;
        for (GenericCosmetic cosmetic : GenericCosmetic.values()) {
            if (!cosmetic.getCategory().equalsIgnoreCase(category)) continue;
            if (i >= slots.length) break;
            inv.setItem(slots[i++], genericItem(p, cosmetic));
        }
        for (SeasonalCosmetic cosmetic : SeasonalCosmetic.values()) {
            if (!cosmetic.getCategory().equalsIgnoreCase(category)) continue;
            if (!visibleSeasonalCosmetic(p, cosmetic)) continue;
            if (i >= slots.length) break;
            inv.setItem(slots[i++], seasonalItem(p, cosmetic));
        }
        p.openInventory(inv);
    }

    private ItemStack genericItem(Player p, GenericCosmetic cosmetic) {
        List<String> lore = new ArrayList<String>();
        lore.add("§8ID: " + cosmetic.getId());
        lore.add("§7Raridade: " + cosmetic.getRarity().getDisplay());
        lore.add("§7Requer nível: §b" + cosmetic.getLevel());
        lore.add(" ");
        lore.add(cosmetic.getDescription());
        lore.add(" ");
        if (ownsGeneric(p, cosmetic)) {
            lore.add(isSelectedGeneric(p, cosmetic) ? "§a§lEQUIPADO" : "§eClique para equipar.");
            lore.add("§7Clique direito para preview.");
        } else {
            lore.add("§7Preço: §e" + cosmetic.getPrice() + " coins");
            lore.add(getLevel(p) < cosmetic.getLevel() ? "§cNível insuficiente." : (canAfford(p, cosmetic.getPrice()) ? "§aClique para comprar." : "§cCoins insuficientes."));
        }
        if (cosmetic.getCategory().equalsIgnoreCase("emote")) {
            return createCustomHead(emoteTexture(cosmetic.getId()), cosmetic.getName(), lore);
        }
        if (cosmetic.getCategory().equalsIgnoreCase("companion") && cosmetic.getMaterial() == Material.SKULL_ITEM) {
            return createCustomHead(companionTexture(cosmetic.getId()), cosmetic.getName(), lore);
        }
        return new ItemBuilder(cosmetic.getMaterial()).name(cosmetic.getName()).lore(lore).removeAttributes().build();
    }

    private void openProfile(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, PROFILE_TITLE);
        decorate(inv);
        inv.setItem(10, new ItemBuilder(Material.DIAMOND_AXE).name("§5Faca Atual").lore("§7" + plugin.knifeSkinManager.getSelected(p).getDisplayName()).removeAttributes().build());
        if (plugin.hatAbilityManager != null) inv.setItem(17, profileLine("§6Chapéu", plugin.hatAbilityManager.getSelected(p).getDisplay(), Material.LEATHER_HELMET));
        if (plugin.gunSkinManager != null) inv.setItem(18, profileLine("§aArma", plugin.gunSkinManager.getSelected(p).getDisplay(), Material.DIAMOND_HOE));
        inv.setItem(11, new ItemBuilder(Material.BLAZE_POWDER).name("§dTrilha Atual").lore("§7" + plugin.knifeSkinManager.getSelectedTrail(p).getDisplayName()).build());
        inv.setItem(12, profileLine("§cEfeito de Morte", getSelectedName(p, "death"), Material.SKULL_ITEM));
        inv.setItem(13, profileLine("§6Vitória", getSelectedName(p, "victory"), Material.FIREWORK));
        inv.setItem(14, profileLine("§cAura", getSelectedName(p, "aura"), Material.REDSTONE));
        inv.setItem(15, profileLine("§bTítulo", getSelectedName(p, "title"), Material.NAME_TAG));
        inv.setItem(16, profileLine("§aGadget", getSelectedName(p, "gadget"), Material.ENDER_CHEST));
        inv.setItem(19, profileLine("§eChapéu Animado", getSelectedName(p, "animated_hat"), Material.GLOWSTONE));
        inv.setItem(20, profileLine("§bBalloon", getSelectedName(p, "balloon"), Material.LEASH));
        inv.setItem(21, profileLine("§dEmote", getSelectedName(p, "emote"), Material.SKULL_ITEM));
        inv.setItem(23, profileLine("§eCompanion", getSelectedName(p, "companion"), Material.MONSTER_EGG));
        inv.setItem(24, profileLine("§fWalk Trail", getSelectedName(p, "walk_trail"), Material.FEATHER));
        if (plugin.api != null) {
            int kills = plugin.api.getKills(p);
            int deaths = plugin.api.getDeaths(p);
            double kd = deaths <= 0 ? kills : ((double) kills / (double) deaths);
            inv.setItem(4, new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3).name("§b§l" + p.getName())
                    .lore("§7Nível: §b" + getLevel(p), "§7Coins: §e" + getCoins(p), "",
                          "§7Vitórias: §a" + plugin.api.getWins(p), "§7Derrotas: §c" + plugin.api.getLoses(p),
                          "§7Kills: §a" + kills, "§7Mortes: §c" + deaths, "§7K/D: §f" + String.format(java.util.Locale.US, "%.2f", kd),
                          "", "§7Murder: §f" + modeStat(p, "murder"), "§7AMONG US: §f" + modeStat(p, "among_us"), "§7TNT Tag: §f" + modeStat(p, "tnt_tag"))
                    .build());
        }
        inv.setItem(22, backItem());
        p.openInventory(inv);
    }

    private int modeStat(Player p, String mode) {
        if (plugin.data == null) return 0;
        return plugin.data.getConfig().getInt("ModeStats." + p.getUniqueId() + "." + mode + ".wins", 0);
    }

    private ItemStack profileLine(String name, String value, Material mat) {
        return new ItemBuilder(mat).name(name).lore("§7Selecionado: §f" + value).removeAttributes().build();
    }


    private void openHatMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, HAT_TITLE);
        decorate(inv);
        if (plugin.hatAbilityManager != null) {
            int slot = 10;
            for (HatAbilityManager.Hat hat : plugin.hatAbilityManager.getHats()) {
                if (slot >= 44) break;
                inv.setItem(slot, new ItemBuilder(hat.getMaterial(), 1, hat.getData())
                        .name(hat.getDisplay())
                        .lore(plugin.hatAbilityManager.buildLore(p, hat))
                        .removeAttributes()
                        .build());
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
        inv.setItem(49, backItem());
        p.openInventory(inv);
    }

    private void openGunMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, GUN_TITLE);
        decorate(inv);
        if (plugin.gunSkinManager != null) {
            int slot = 10;
            for (GunSkinManager.GunSkin gun : plugin.gunSkinManager.getSkins()) {
                if (!visibleSeasonalGun(p, gun)) continue;
                if (slot >= 44) break;
                inv.setItem(slot, new ItemBuilder(gun.getMaterial(), 1, gun.getData())
                        .name(gun.getDisplay())
                        .lore(plugin.gunSkinManager.buildLore(p, gun))
                        .removeAttributes()
                        .build());
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
        inv.setItem(49, backItem());
        p.openInventory(inv);
    }

    public void openKnifeUpgradeMenu(Player p, KnifeSkinManager.KnifeSkin skin) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6Upgrade Faca: " + skin.getId());
        decorate(inv);
        inv.setItem(4, new ItemBuilder(skin.getMaterial(), 1, skin.getData()).name(skin.getDisplayName()).lore("§7Coins: §e" + getCoins(p)).removeAttributes().build());
        inv.setItem(11, new ItemBuilder(Material.SUGAR).name("§eMelhorar Agilidade").lore(plugin.knifeSkinManager.buildUpgradeLore(p, skin, "agility")).build());
        inv.setItem(13, new ItemBuilder(Material.BOW).name("§eMelhorar Precisão").lore(plugin.knifeSkinManager.buildUpgradeLore(p, skin, "precision")).build());
        inv.setItem(15, new ItemBuilder(Material.FEATHER).name("§eMelhorar Velocidade").lore(plugin.knifeSkinManager.buildUpgradeLore(p, skin, "speed")).build());
        inv.setItem(22, new ItemBuilder(Material.ARROW).name("§eVoltar").build());
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        String name = clicked.getItemMeta().hasDisplayName() ? clicked.getItemMeta().getDisplayName() : "";
        String invName = e.getInventory().getName();
        MurderAPI api = plugin.api;

        if (invName.equals(MAIN_TITLE)) {
            e.setCancelled(true);

            if (name.contains("Facas")) {
                openKnifeMenu(p);
            } else if (name.contains("Chapéus")) {
                openHatMenu(p);
            } else if (name.contains("Armas")) {
                openGunMenu(p);
            } else if (name.contains("Efeitos")) {
                openEffectsMenu(p);
            } else if (name.contains("Títulos")) {
                openGenericMenu(p, TITLE_TITLE, "title", Material.NAME_TAG, "§b§lTítulos");
            } else if (name.contains("Gadgets")) {
                openGenericMenu(p, GADGET_TITLE, "gadget", Material.ENDER_CHEST, "§a§lGadgets");
            } else if (name.contains("Emotes")) {
                openGenericMenu(p, EMOTE_TITLE, "emote", Material.SKULL_ITEM, "§d§lEmotes");
            } else if (name.contains("Companions")) {
                openGenericMenu(p, COMPANION_TITLE, "companion", Material.MONSTER_EGG, "§e§lCompanions");
            } else if (name.contains("Balloons")) {
                openGenericMenu(p, BALLOON_TITLE, "balloon", Material.LEASH, "§b§lBalloons");
            } else if (name.contains("Chapéus Animados")) {
                openGenericMenu(p, ANIMATED_HAT_TITLE, "animated_hat", Material.GLOWSTONE, "§e§lChapéus Animados");
            } else if (name.contains("Walk Trails")) {
                openGenericMenu(p, WALK_TRAIL_TITLE, "walk_trail", Material.FEATHER, "§f§lWalk Trails");
            } else if (clicked.getType() == Material.NETHER_STAR && plugin.seasonalEventManager != null && plugin.seasonalEventManager.isEventActive()) {
                openSeasonalMenu(p);
            } else if (name.contains("Perfil")) {
                openProfile(p);
            } else if (clicked.getType() == Material.BARRIER) {
                p.closeInventory();
            }

            return;
        }

        if (invName.equals(EFFECTS_TITLE)) {
            e.setCancelled(true);

            if (isBackOrClose(p, clicked)) return;

            if (name.contains("Efeitos de Faca")) {
                openTrailMenu(p);
            } else if (name.contains("Efeitos de Morte")) {
                openGenericMenu(p, DEATH_TITLE, "death", Material.SKULL_ITEM, "§c§lEfeitos de Morte");
            } else if (name.contains("Vitória")) {
                openGenericMenu(p, VICTORY_TITLE, "victory", Material.FIREWORK, "§6§lEfeitos de Vitória");
            } else if (name.contains("Auras")) {
                openGenericMenu(p, AURA_TITLE, "aura", Material.REDSTONE, "§c§lAuras");
            }

            return;
        }

        if (invName.equals(KNIFE_TITLE)) {
            e.setCancelled(true);
            if (isBackOrClose(p, clicked)) return;
            for (KnifeSkinManager.KnifeSkin skin : plugin.knifeSkinManager.getSkins()) {
                if (clicked.getType() == skin.getMaterial() && name.equals(skin.getDisplayName())) {
                    handleKnifeClick(p, skin, api, e.isRightClick());
                    return;
                }
            }
            return;
        }


        if (invName.equals(HAT_TITLE)) {
            e.setCancelled(true);
            if (isBackOrClose(p, clicked)) return;
            if (plugin.hatAbilityManager == null) return;
            for (HatAbilityManager.Hat hat : plugin.hatAbilityManager.getHats()) {
                if (clicked.getType() == hat.getMaterial() && name.equals(hat.getDisplay())) {
                    handleHatClick(p, hat, api);
                    return;
                }
            }
            return;
        }

        if (invName.equals(GUN_TITLE)) {
            e.setCancelled(true);
            if (isBackOrClose(p, clicked)) return;
            if (plugin.gunSkinManager == null) return;
            for (GunSkinManager.GunSkin gun : plugin.gunSkinManager.getSkins()) {
                if (clicked.getType() == gun.getMaterial() && name.equals(gun.getDisplay())) {
                    handleGunClick(p, gun, api);
                    return;
                }
            }
            return;
        }

        if (invName.equals(TRAIL_TITLE)) {
            e.setCancelled(true);
            if (isBackOrClose(p, clicked)) return;
            for (KnifeSkinManager.KnifeTrail trail : plugin.knifeSkinManager.getTrails()) {
                if (clicked.getType() == trail.getMaterial() && name.equals(trail.getDisplayName())) {
                    handleTrailClick(p, trail, api, e.isRightClick());
                    return;
                }
            }
            return;
        }

        if (invName.equals(DEATH_TITLE) || invName.equals(VICTORY_TITLE) || invName.equals(AURA_TITLE) || invName.equals(TITLE_TITLE) || invName.equals(GADGET_TITLE) || invName.equals(EMOTE_TITLE) || invName.equals(COMPANION_TITLE) || invName.equals(BALLOON_TITLE) || invName.equals(ANIMATED_HAT_TITLE) || invName.equals(WALK_TRAIL_TITLE)) {
            e.setCancelled(true);
            if (isRemoveSelectedButton(clicked)) {
                String cat = categoryFromMenu(invName);
                clearSelectedGeneric(p, cat);
                success(p, (cat.equalsIgnoreCase("gadget") ? "Gadget" : cat.equalsIgnoreCase("emote") ? "Emote" : cat.equalsIgnoreCase("companion") ? "Companion" : "Cosmético") + " removido/desativado.");
                reopenGenericMenu(p, invName);
                return;
            }
            if (isBackOrClose(p, clicked)) return;
            SeasonalCosmetic seasonal = findSeasonalByItem(clicked);
            if (seasonal != null) {
                handleSeasonalClick(p, seasonal, api, invName);
                return;
            }
            GenericCosmetic cosmetic = findGenericByItem(clicked, invName);
            if (cosmetic != null) handleGenericClick(p, cosmetic, api, e.isRightClick(), invName);
            return;
        }

        if (invName.equals("§8Loja › Evento")) {
            e.setCancelled(true);
            if (isBackOrClose(p, clicked)) return;
            SeasonalCosmetic cosmetic = findSeasonalByItem(clicked);
            if (cosmetic != null) handleSeasonalClick(p, cosmetic, api);
            return;
        }

        if (invName.equals(PROFILE_TITLE)) {
            e.setCancelled(true);
            if (clicked.getType() == Material.ARROW) openMainMenu(p);
            return;
        }

        if (invName.startsWith("§6Upgrade Faca: ")) {
            e.setCancelled(true);
            String skinId = invName.replace("§6Upgrade Faca: ", "");
            KnifeSkinManager.KnifeSkin skin = plugin.knifeSkinManager.getSkinById(skinId);
            if (skin == null) { p.closeInventory(); return; }
            if (clicked.getType() == Material.ARROW) { openKnifeMenu(p); return; }
            String attr = null;
            if (clicked.getType() == Material.SUGAR) attr = "agility";
            if (clicked.getType() == Material.BOW) attr = "precision";
            if (clicked.getType() == Material.FEATHER) attr = "speed";
            if (attr != null) {
                if (plugin.knifeSkinManager.upgradeAttribute(p, skin, attr, api)) {
                    success(p, "Atributo melhorado com sucesso!");
                    openKnifeUpgradeMenu(p, skin);
                } else {
                    fail(p);
                }
            }
        }
    }


    private ItemStack removeSelectedItem(String category) {
        String label = "cosmético";
        if (category.equalsIgnoreCase("gadget")) label = "gadget";
        if (category.equalsIgnoreCase("emote")) label = "emote";
        if (category.equalsIgnoreCase("companion")) label = "companion";
        return new ItemBuilder(Material.BARRIER)
                .name("§cRemover " + label + " selecionado")
                .lore("§7Desativa o " + label + " atual.", "", "§eClique para remover.")
                .build();
    }

    private boolean isRemoveSelectedButton(ItemStack item) {
        if (item == null || item.getType() != Material.BARRIER || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        return item.getItemMeta().getDisplayName().startsWith("§cRemover ");
    }

    private void clearSelectedGeneric(Player p, String category) {
        if (plugin.data == null) return;
        plugin.data.getConfig().set(base(p, category) + "selected", "nenhum");
        plugin.data.save();
        try {
            if (plugin.getConfig().getBoolean("mysql") && plugin.sql != null) {
                String safeCategory = category.replace("'", "''");
                plugin.sql.update("CREATE TABLE IF NOT EXISTS MurderCosmetics (uuid VARCHAR(36) NOT NULL, category VARCHAR(32) NOT NULL, owned TEXT, selected VARCHAR(64), PRIMARY KEY(uuid, category));");
                plugin.sql.update("INSERT INTO MurderCosmetics (uuid, category, owned, selected) VALUES ('" + p.getUniqueId().toString() + "', '" + safeCategory + "', '', 'nenhum') ON DUPLICATE KEY UPDATE selected='nenhum';");
            }
        } catch (Throwable ignored) {}
    }

    private String categoryFromMenu(String menu) {
        if (menu.equals(DEATH_TITLE)) return "death";
        if (menu.equals(VICTORY_TITLE)) return "victory";
        if (menu.equals(AURA_TITLE)) return "aura";
        if (menu.equals(TITLE_TITLE)) return "title";
        if (menu.equals(GADGET_TITLE)) return "gadget";
        if (menu.equals(EMOTE_TITLE)) return "emote";
        if (menu.equals(COMPANION_TITLE)) return "companion";
        if (menu.equals(BALLOON_TITLE)) return "balloon";
        if (menu.equals(ANIMATED_HAT_TITLE)) return "animated_hat";
        if (menu.equals(WALK_TRAIL_TITLE)) return "walk_trail";
        return "";
    }

    private boolean isBackOrClose(Player p, ItemStack clicked) {
        if (clicked.getType() == Material.ARROW) { openMainMenu(p); return true; }
        if (clicked.getType() == Material.BARRIER) { p.closeInventory(); return true; }
        return false;
    }

    private void handleKnifeClick(Player p, KnifeSkinManager.KnifeSkin skin, MurderAPI api, boolean rightClick) {
        if (plugin.knifeSkinManager.ownsSkin(p, skin)) {
            if (rightClick) { openKnifeUpgradeMenu(p, skin); return; }
            plugin.knifeSkinManager.setSelected(p, skin);
            success(p, "Faca equipada: " + skin.getDisplayName());
            openKnifeMenu(p);
            return;
        }
        if (!buy(p, skin.getPrice(), api)) return;
        plugin.knifeSkinManager.unlockSkin(p, skin);
        plugin.knifeSkinManager.setSelected(p, skin);
        success(p, "Você comprou e equipou: " + skin.getDisplayName());
        openKnifeMenu(p);
    }


    private void handleHatClick(Player p, HatAbilityManager.Hat hat, MurderAPI api) {
        if (plugin.hatAbilityManager.owns(p, hat)) {
            plugin.hatAbilityManager.setSelected(p, hat);
            plugin.hatAbilityManager.playSelect(p);
            success(p, "Chapéu equipado: " + hat.getDisplay());
            openHatMenu(p);
            return;
        }
        if (!buy(p, hat.getPrice(), api)) return;
        plugin.hatAbilityManager.unlock(p, hat);
        plugin.hatAbilityManager.setSelected(p, hat);
        plugin.hatAbilityManager.playSelect(p);
        success(p, "Você comprou e equipou: " + hat.getDisplay());
        openHatMenu(p);
    }

    private void handleGunClick(Player p, GunSkinManager.GunSkin gun, MurderAPI api) {
        if (plugin.gunSkinManager.owns(p, gun)) {
            plugin.gunSkinManager.setSelected(p, gun);
            success(p, "Arma equipada: " + gun.getDisplay());
            openGunMenu(p);
            return;
        }
        if (!buy(p, gun.getPrice(), api)) return;
        plugin.gunSkinManager.unlock(p, gun);
        plugin.gunSkinManager.setSelected(p, gun);
        success(p, "Você comprou e equipou: " + gun.getDisplay());
        openGunMenu(p);
    }

    private void handleTrailClick(Player p, KnifeSkinManager.KnifeTrail trail, MurderAPI api, boolean rightClick) {
        if (rightClick) { previewTrail(p, trail); return; }
        if (plugin.knifeSkinManager.ownsTrail(p, trail)) {
            plugin.knifeSkinManager.setSelectedTrail(p, trail);
            success(p, "Efeito de faca equipado: " + trail.getDisplayName());
            openTrailMenu(p);
            return;
        }
        if (!buy(p, trail.getPrice(), api)) return;
        plugin.knifeSkinManager.unlockTrail(p, trail);
        plugin.knifeSkinManager.setSelectedTrail(p, trail);
        success(p, "Você comprou e equipou: " + trail.getDisplayName());
        openTrailMenu(p);
    }

    private void handleGenericClick(Player p, GenericCosmetic cosmetic, MurderAPI api, boolean rightClick, String currentMenu) {
        if (rightClick) { previewGeneric(p, cosmetic); return; }
        if (ownsGeneric(p, cosmetic)) {
            setSelectedGeneric(p, cosmetic);
            success(p, "Cosmético equipado: " + cosmetic.getName());
            reopenGenericMenu(p, currentMenu);
            return;
        }
        if (getLevel(p) < cosmetic.getLevel()) {
            p.sendMessage("§cVocê precisa de nível " + cosmetic.getLevel() + " para comprar esse cosmético.");
            fail(p);
            return;
        }
        if (!buy(p, cosmetic.getPrice(), api)) return;
        unlockGeneric(p, cosmetic);
        setSelectedGeneric(p, cosmetic);
        success(p, "Você comprou e equipou: " + cosmetic.getName());
        reopenGenericMenu(p, currentMenu);
    }

    private void reopenGenericMenu(Player p, String menu) {
        if (menu.equals(DEATH_TITLE)) openGenericMenu(p, DEATH_TITLE, "death", Material.SKULL_ITEM, "§c§lEfeitos de Morte");
        else if (menu.equals(VICTORY_TITLE)) openGenericMenu(p, VICTORY_TITLE, "victory", Material.FIREWORK, "§6§lEfeitos de Vitória");
        else if (menu.equals(AURA_TITLE)) openGenericMenu(p, AURA_TITLE, "aura", Material.REDSTONE, "§c§lAuras");
        else if (menu.equals(TITLE_TITLE)) openGenericMenu(p, TITLE_TITLE, "title", Material.NAME_TAG, "§b§lTítulos");
        else if (menu.equals(GADGET_TITLE)) openGenericMenu(p, GADGET_TITLE, "gadget", Material.ENDER_CHEST, "§a§lGadgets");
        else if (menu.equals(EMOTE_TITLE)) openGenericMenu(p, EMOTE_TITLE, "emote", Material.SKULL_ITEM, "§d§lEmotes");
        else if (menu.equals(COMPANION_TITLE)) openGenericMenu(p, COMPANION_TITLE, "companion", Material.MONSTER_EGG, "§e§lCompanions");
        else if (menu.equals(BALLOON_TITLE)) openGenericMenu(p, BALLOON_TITLE, "balloon", Material.LEASH, "§b§lBalloons");
        else if (menu.equals(ANIMATED_HAT_TITLE)) openGenericMenu(p, ANIMATED_HAT_TITLE, "animated_hat", Material.GLOWSTONE, "§e§lChapéus Animados");
        else if (menu.equals(WALK_TRAIL_TITLE)) openGenericMenu(p, WALK_TRAIL_TITLE, "walk_trail", Material.FEATHER, "§f§lWalk Trails");
    }

    private GenericCosmetic findGenericByItem(ItemStack item, String menu) {
        if (item == null || !item.hasItemMeta()) return null;
        String category = categoryFromMenu(menu);
        if (category.isEmpty()) return null;

        // O ID no lore é a identificação principal. Isso funciona também para
        // custom heads e itens cujo material visual muda por animação.
        if (item.getItemMeta().hasLore()) {
            for (String line : item.getItemMeta().getLore()) {
                if (line == null || !line.startsWith("§8ID: ")) continue;
                String id = line.substring("§8ID: ".length()).trim();
                for (GenericCosmetic cosmetic : GenericCosmetic.values()) {
                    if (cosmetic.getCategory().equalsIgnoreCase(category)
                            && cosmetic.getId().equalsIgnoreCase(id)) {
                        return cosmetic;
                    }
                }
            }
        }

        // Fallback para itens antigos sem ID no lore.
        String name = item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "";
        for (GenericCosmetic cosmetic : GenericCosmetic.values()) {
            if (cosmetic.getCategory().equalsIgnoreCase(category)
                    && cosmetic.getName().equals(name)) {
                return cosmetic;
            }
        }
        return null;
    }


    private SeasonalCosmetic findSeasonalByItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return null;
        String name = item.getItemMeta().getDisplayName();
        for (SeasonalCosmetic cosmetic : SeasonalCosmetic.values()) {
            if (item.getType() == cosmetic.getMaterial() && name.equals(seasonalName(cosmetic))) return cosmetic;
        }
        return null;
    }

    private boolean ownsSeasonal(Player p, SeasonalCosmetic cosmetic) {
        if (seasonalPrice(cosmetic) <= 0) return true;
        return plugin.data != null && plugin.data.getConfig().getBoolean(base(p, cosmetic.getCategory()) + "owned." + seasonalId(cosmetic), false);
    }

    private boolean isSelectedSeasonal(Player p, SeasonalCosmetic cosmetic) {
        return getSelectedId(p, cosmetic.getCategory()).equalsIgnoreCase(seasonalId(cosmetic));
    }

    private void unlockSeasonal(Player p, SeasonalCosmetic cosmetic) {
        if (plugin.data == null) return;
        plugin.data.getConfig().set(base(p, cosmetic.getCategory()) + "owned." + seasonalId(cosmetic), true);
        plugin.data.save();
    }

    private void selectSeasonal(Player p, SeasonalCosmetic cosmetic) {
        if (plugin.data == null) return;
        plugin.data.getConfig().set(base(p, cosmetic.getCategory()) + "selected", seasonalId(cosmetic));
        plugin.data.save();
    }

    private void handleSeasonalClick(Player p, SeasonalCosmetic cosmetic, MurderAPI api) {
        handleSeasonalClick(p, cosmetic, api, "§8Loja › Evento");
    }

    private void handleSeasonalClick(Player p, SeasonalCosmetic cosmetic, MurderAPI api, String currentMenu) {
        if (ownsSeasonal(p, cosmetic)) {
            selectSeasonal(p, cosmetic);
            success(p, "Cosmetico sazonal equipado: " + seasonalName(cosmetic));
            reopenSeasonalMenu(p, currentMenu);
            return;
        }
        if (!buy(p, seasonalPrice(cosmetic), api)) return;
        unlockSeasonal(p, cosmetic);
        selectSeasonal(p, cosmetic);
        success(p, "Voce comprou e equipou: " + seasonalName(cosmetic));
        reopenSeasonalMenu(p, currentMenu);
    }

    private void reopenSeasonalMenu(Player p, String currentMenu) {
        if (currentMenu.equals(DEATH_TITLE)) openGenericMenu(p, DEATH_TITLE, "death", Material.SKULL_ITEM, "§c§lEfeitos de Morte");
        else if (currentMenu.equals(VICTORY_TITLE)) openGenericMenu(p, VICTORY_TITLE, "victory", Material.FIREWORK, "§6§lEfeitos de Vitória");
        else if (currentMenu.equals(AURA_TITLE)) openGenericMenu(p, AURA_TITLE, "aura", Material.REDSTONE, "§c§lAuras");
        else if (currentMenu.equals(GADGET_TITLE)) openGenericMenu(p, GADGET_TITLE, "gadget", Material.ENDER_CHEST, "§a§lGadgets");
        else openMainMenu(p);
    }

    private boolean buy(Player p, int price, MurderAPI api) {
        if (price <= 0) return true;
        if (api.getCoins(p) < price) {
            p.sendMessage("§cVocê não tem coins suficientes. Necessário: " + price + ".");
            fail(p);
            return false;
        }
        boolean removed = api.removeCoins(p, price);
        if (!removed) {
            p.sendMessage("§cNão foi possível remover seus coins. Compra cancelada.");
            fail(p);
            return false;
        }
        return true;
    }

    private int getCoins(Player p) {
        return plugin.api == null ? 0 : plugin.api.getCoins(p);
    }

    private boolean canAfford(Player p, int price) {
        return price <= 0 || getCoins(p) >= price;
    }

    private int getLevel(Player p) {
        if (plugin.data == null) return 1;
        UUID uuid = p.getUniqueId();
        int level = plugin.data.getConfig().getInt("Levels." + uuid + ".level", 1);
        int level2 = plugin.data.getConfig().getInt("XP." + uuid + ".level", level);
        return Math.max(level, level2);
    }

    private Rarity rarityFromPrice(int price) {
        if (price >= 5000) return Rarity.LENDARIO;
        if (price >= 2000) return Rarity.EPICO;
        if (price >= 1000) return Rarity.RARO;
        return Rarity.COMUM;
    }

    private String base(Player p, String category) {
        return "Cosmetics." + p.getUniqueId() + "." + category + ".";
    }

    private boolean ownsGeneric(Player p, GenericCosmetic cosmetic) {
        if (cosmetic.getPrice() <= 0) return true;
        return plugin.data != null && plugin.data.getConfig().getBoolean(base(p, cosmetic.getCategory()) + "owned." + cosmetic.getId(), false);
    }

    private void unlockGeneric(Player p, GenericCosmetic cosmetic) {
        if (plugin.data == null) return;
        plugin.data.getConfig().set(base(p, cosmetic.getCategory()) + "owned." + cosmetic.getId(), true);
        plugin.data.save();
        saveGenericMysql(p, cosmetic, true);
    }

    private boolean isSelectedGeneric(Player p, GenericCosmetic cosmetic) {
        return getSelectedId(p, cosmetic.getCategory()).equalsIgnoreCase(cosmetic.getId());
    }

    private void setSelectedGeneric(Player p, GenericCosmetic cosmetic) {
        if (plugin.data == null) return;
        plugin.data.getConfig().set(base(p, cosmetic.getCategory()) + "selected", cosmetic.getId());
        plugin.data.save();
        saveGenericMysql(p, cosmetic, false);

        // Títulos são separados das tags: não mexem no chat, TAB nem prefixo da nametag.
        // Eles aparecem como uma linha holográfica acima da cabeça, selecionados apenas pela Loja.
        if (cosmetic.getCategory().equalsIgnoreCase("title")) {
            plugin.getConfig().set("shop-titles." + cosmetic.getId() + ".display", cosmetic.getName().replace("§", "&"));
            plugin.saveConfig();
            if (plugin.titleManager != null) plugin.titleManager.setTitle(p, cosmetic.getId(), cosmetic.getName());
        }
    }

    private String getSelectedId(Player p, String category) {
        if (plugin.data == null) return "nenhum";
        return plugin.data.getConfig().getString(base(p, category) + "selected", "nenhum");
    }

    private String getSelectedName(Player p, String category) {
        String id = getSelectedId(p, category);
        if (id == null || id.equalsIgnoreCase("nenhum")) return "Nenhum";
        for (GenericCosmetic cosmetic : GenericCosmetic.values()) {
            if (cosmetic.getCategory().equalsIgnoreCase(category) && cosmetic.getId().equalsIgnoreCase(id)) return cosmetic.getName();
        }
        return id;
    }

    private void saveGenericMysql(Player p, GenericCosmetic cosmetic, boolean ownedOnly) {
        try {
            if (plugin == null || !plugin.getConfig().getBoolean("mysql") || plugin.sql == null) return;
            String selected = getSelectedId(p, cosmetic.getCategory()).replace("'", "''");
            String id = cosmetic.getId().replace("'", "''");
            String category = cosmetic.getCategory().replace("'", "''");
            plugin.sql.update("CREATE TABLE IF NOT EXISTS MurderCosmetics (uuid VARCHAR(36) NOT NULL, category VARCHAR(32) NOT NULL, owned TEXT, selected VARCHAR(64), PRIMARY KEY(uuid, category));");
            plugin.sql.update("INSERT INTO MurderCosmetics (uuid, category, owned, selected) VALUES ('" + p.getUniqueId().toString() + "', '" + category + "', '" + id + "', '" + selected + "') ON DUPLICATE KEY UPDATE owned=CONCAT(IFNULL(owned,''), ',', '" + id + "'), selected='" + selected + "';");
        } catch (Throwable ignored) {}
    }

    private void previewTrail(Player p, KnifeSkinManager.KnifeTrail trail) {
        p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1f, 1.2f);
        if (trail == KnifeSkinManager.KnifeTrail.HEART) p.getWorld().playEffect(p.getLocation(), Effect.HEART, 0);
        else if (trail == KnifeSkinManager.KnifeTrail.CLOUD) p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 0);
        else p.getWorld().playEffect(p.getLocation(), Effect.CRIT, 0);
    }

    private void previewGeneric(Player p, GenericCosmetic cosmetic) {
        p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1f, 1.4f);
        if (cosmetic.getCategory().equals("death")) {
            if (cosmetic.getId().contains("lightning") || cosmetic.getId().contains("funeral")) p.getWorld().strikeLightningEffect(p.getLocation());
            else if (cosmetic.getId().contains("explosion")) p.getWorld().playEffect(p.getLocation(), Effect.EXPLOSION_HUGE, 0);
            else p.getWorld().playEffect(p.getLocation(), Effect.SMOKE, 0);
        } else if (cosmetic.getCategory().equals("victory")) {
            p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1f);
            p.getWorld().playEffect(p.getLocation(), Effect.FIREWORKS_SPARK, 0);
        } else if (cosmetic.getCategory().equals("aura")) {
            p.getWorld().playEffect(p.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        } else if (cosmetic.getCategory().equals("gadget")) {
            p.getWorld().playEffect(p.getLocation(), Effect.CLOUD, 0);
        } else if (cosmetic.getCategory().equals("emote")) {
            p.getWorld().playEffect(p.getLocation().clone().add(0, 2.2D, 0), Effect.HAPPY_VILLAGER, 0);
            p.sendMessage("§dPreview do emote: " + cosmetic.getName());
        } else if (cosmetic.getCategory().equals("companion")) {
            p.getWorld().playEffect(p.getLocation().clone().add(1.2D, 1.2D, 0), Effect.ENDER_SIGNAL, 0);
            p.getWorld().playEffect(p.getLocation().clone().add(1.2D, 1.0D, 0), Effect.HAPPY_VILLAGER, 0);
            p.sendMessage("§ePreview do companion: " + cosmetic.getName());
        } else {
            p.sendMessage("§bPreview do título: " + cosmetic.getName());
        }
    }


    private ItemStack createCustomHead(String textureValue, String name, List<String> lore) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", textureValue));
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (Throwable ignored) {
            meta.setOwner("MHF_Smile");
        }
        head.setItemMeta(meta);
        return head;
    }

    private String companionTexture(String id) {
        return companionTextureValue(id);
    }

    private String companionTextureValue(String id) {
        if (id == null) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNmU2OWE4NDVlMjI3ZjdjYTFmZGRlOGMzNTdjOGM4MjFlYmQ0YmE2MTkzODJlYTRhMWY4N2Q0YWU5NCJ9fX0=";
        if (id.equalsIgnoreCase("companion_mini_murder")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNmU2OWE4NDVlMjI3ZjdjYTFmZGRlOGMzNTdjOGM4MjFlYmQ0YmE2MTkzODJlYTRhMWY4N2Q0YWU5NCJ9fX0=";
        if (id.equalsIgnoreCase("companion_mini_detective")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDdiNGY4NGUxOWI1MmYzMTIxNzcxMmU3YmE5ZjUxZDU2ZGE1OWQyNDQ1YjRkN2YzOWVmNmMzMjNiODE2NiJ9fX0=";
        if (id.equalsIgnoreCase("companion_ghost")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNmU2OWE4NDVlMjI3ZjdjYTFmZGRlOGMzNTdjOGM4MjFlYmQ0YmE2MTkzODJlYTRhMWY4N2Q0YWU5NCJ9fX0=";
        if (id.equalsIgnoreCase("companion_dragon_fire")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIzNmQ3OWJhNDA3MDUzYzI1MWJkNGFhOTEzYTVlMjgxY2NkMjRkOGQ5ZWJlOTNkMjhiYjYyZjE0MGFkYzUifX19";
        if (id.equalsIgnoreCase("companion_dragon_ice")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNkY2VjZmY0ZjZiYTBiMjQ2Zjc3NzU3NTNkNzkxZjE2YzljNmRlNTUzNWQ3OTUyMzZhNGYzYzhjOWIyOTNiIn19fQ==";
        if (id.equalsIgnoreCase("companion_among_us")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNmU2OWE4NDVlMjI3ZjdjYTFmZGRlOGMzNTdjOGM4MjFlYmQ0YmE2MTkzODJlYTRhMWY4N2Q0YWU5NCJ9fX0=";
        if (id.equalsIgnoreCase("companion_koala")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ4N2U4MjlmMmQ0ODlmZjZlZTY2ODI2MzllMzM5YTIxMjNkMzQ0OWQyN2Y4YmRhNTE1MjhjNjA3NmZiOWYyYSJ9fX0=";
        if (id.equalsIgnoreCase("companion_panda")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3YjY4ZWQwMjE2MzJmNDA4ZmMyMjNlZjc5NTdjMjQ3ODZhZTUwOWE4NGU2ZjE4YTM3MWE1NWMzZDhjZjkwOSJ9fX0=";
        if (id.equalsIgnoreCase("companion_robot")) return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NlYmM5Nzc5OGMyZTM2MDU1MWNhYjNkZDVkYjZkNTM0OTdmZTYzMDQwOTQxYzlhYzQ5MWE1OWNiZjM4M2E3YSJ9fX0=";
        return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTExNmU2OWE4NDVlMjI3ZjdjYTFmZGRlOGMzNTdjOGM4MjFlYmQ0YmE2MTkzODJlYTRhMWY4N2Q0YWU5NCJ9fX0=";
    }

    private String emoteTexture(String id) {
        return textureValueFromHash(emotePreviewHash(id));
    }

    private String textureValueFromHash(String hash) {
        if (hash == null || hash.isEmpty()) return "";
        String json = "{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    private String emotePreviewHash(String id) {
        if (id == null) return "60c432cbc490a8af6e9dfeb28095c0a0ec79fff705fb184674d1e743bd05baa";
        if (id.equalsIgnoreCase("emote_cry")) return "45436e46e4885eeb9882089601e9852f6546ad7b2101cf47b4369156a44b7ba";
        if (id.equalsIgnoreCase("emote_rage")) return "973fd955ca4389b68642483e53e5e2f1fabafc2416fc8e95d43694b76c5a81";
        if (id.equalsIgnoreCase("emote_cool")) return "766b3eef3c726ecb816c43839189eeb8e36382e3e5fe41128372785185a322";
        if (id.equalsIgnoreCase("emote_heart")) return "fd26ae4b5793d087e62a2cf3f34359829d02869aae6626bfcff59de1469f51";
        return "60c432cbc490a8af6e9dfeb28095c0a0ec79fff705fb184674d1e743bd05baa";
    }

    private void success(Player p, String message) {
        p.sendMessage("§a" + message);
        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1.4f);
    }

    private void fail(Player p) {
        p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
    }
}
