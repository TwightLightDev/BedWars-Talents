package org.twightlight.talents.utils;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ItemBuilder {
   private ItemStack is;
   private String skullOwner;

   public ItemBuilder(XMaterial material) {
      this.is = material.isSupported() ? material.parseItem() : new ItemStack(Material.STONE);
   }

   public ItemBuilder(XMaterial material, int amount) {
      this.is = material.isSupported() ? material.parseItem() : new ItemStack(Material.STONE);
      this.is.setAmount(amount);
   }

   public ItemBuilder(String skullOwnerNms) {
      this(XMaterial.PLAYER_HEAD);
      this.setSkullOwnerNMS(skullOwnerNms);
   }

   /** @deprecated */
   @Deprecated
   public ItemBuilder(int id) {
      this.is = new ItemStack(id, 1);
   }

   public ItemBuilder(ItemBuilder builder, boolean clone) {
      this(builder.toItemStack(), clone);
   }

   public ItemBuilder(ItemBuilder builder) {
      this(builder.toItemStack(), true);
   }

   /** @deprecated */
   @Deprecated
   public ItemBuilder(Material m) {
      this((Material)m, 1);
   }

   public ItemBuilder(ItemStack is, boolean clone) {
      this.is = clone ? is.clone() : is;
   }

   public ItemBuilder(ItemStack is) {
      this(is, true);
   }

   public ItemBuilder(Material m, int amount) {
      this.is = new ItemStack(m, amount);
   }

   public ItemBuilder(Material m, int amount, byte durability) {
      this.is = new ItemStack(m, amount, (short)durability);
   }

   public ItemBuilder clone() {
      return new ItemBuilder(this.is);
   }

   public ItemBuilder setDurability(short dur) {
      this.is.setDurability(dur);
      return this;
   }

   public ItemBuilder setDurability(byte dur) {
      this.is.setDurability((short)dur);
      return this;
   }

   public ItemBuilder setType(Material m) {
      this.is.setType(m);
      return this;
   }

   public ItemBuilder setName(String name) {
      ItemMeta im = this.is.getItemMeta();
      im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder setAmount(int amount) {
      this.is.setAmount(amount);
      return this;
   }

   public List<String> getLore() {
      return this.is.getItemMeta().getLore();
   }

   public ItemBuilder setLore(String... lore) {
      ItemMeta im = this.is.getItemMeta();
      List<String> color = new ArrayList();
      String[] var4 = lore;
      int var5 = lore.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String l = var4[var6];
         color.add(ChatColor.translateAlternateColorCodes('&', l));
      }

      im.setLore(color);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder setLore(List<String> lore) {
      ItemMeta im = this.is.getItemMeta();
      List<String> color = new ArrayList();
      Iterator var4 = lore.iterator();

      while(var4.hasNext()) {
         String l = (String)var4.next();
         color.add(ChatColor.translateAlternateColorCodes('&', l));
      }

      im.setLore(color);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder removeLore() {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList(im.getLore());
      lore.clear();
      im.setLore(lore);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder setPotionEffect(PotionEffect effect) {
      try {
         PotionMeta meta = (PotionMeta)this.is.getItemMeta();
         meta.setMainEffect(effect.getType());
         meta.addCustomEffect(effect, false);
         this.is.setItemMeta(meta);
         return this;
      } catch (ClassCastException var3) {
         return this;
      }
   }

   public ItemBuilder hideAttributes() {
      ItemMeta im = this.is.getItemMeta();
      im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
      im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_PLACED_ON});
      im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addItemFlag(ItemFlag itemFlag) {
      ItemMeta im = this.is.getItemMeta();
      im.addItemFlags(new ItemFlag[]{itemFlag});
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder removeItemFlag(ItemFlag itemFlag) {
      ItemMeta im = this.is.getItemMeta();
      im.removeItemFlags(new ItemFlag[]{itemFlag});
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
      this.is.addUnsafeEnchantment(ench, level);
      return this;
   }

   public ItemBuilder createPotion(boolean splash) {
      ItemStack potion = new ItemStack(Material.POTION);
      PotionMeta meta = (PotionMeta)potion.getItemMeta();
      PotionEffect main = null;

      PotionEffect current;
      for(Iterator var5 = ((PotionMeta)this.is.getItemMeta()).getCustomEffects().iterator(); var5.hasNext(); meta.addCustomEffect(current, true)) {
         current = (PotionEffect)var5.next();
         if (main == null) {
            main = current;
         }
      }

      if (main == null) {
         return this;
      } else {
         potion.setAmount(this.is.getAmount());
         potion.setItemMeta(meta);
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
         meta.setMainEffect(main.getType());
         Potion po = new Potion(PotionType.getByEffect(main.getType()));
         po.setSplash(splash);
         po.apply(potion);
         return new ItemBuilder(potion);
      }
   }

   public ItemBuilder setSplash(boolean splash) {
      try {
         Potion potion = Potion.fromItemStack(this.is);
         potion.setSplash(splash);
         this.is = potion.toItemStack(this.is.getAmount());
         return this;
      } catch (ClassCastException var3) {
         return this;
      }
   }

   public ItemBuilder setSkullOwnerNMS(String url, UUID uuid) {
      try {
         byte[] decodedBytes = Base64.getDecoder().decode(url.getBytes(StandardCharsets.UTF_8));
         String decoded = (new String(decodedBytes)).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "").replace("\"}}}", "");
         if (!decoded.contains(".")) {
            return this;
         }

         SkullMeta headMeta = (SkullMeta)this.is.getItemMeta();
         GameProfile profile = new GameProfile(uuid, (String)null);
         byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", decoded).getBytes());
         profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

         try {
            Field profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
            this.skullOwner = decoded;
         } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException var9) {
            var9.printStackTrace();
         }

         this.is.setItemMeta(headMeta);
      } catch (Exception var10) {
         var10.printStackTrace();
      }

      return this;
   }

   public ItemBuilder setSkullOwnerNMS(String url) {
      return this.setSkullOwnerNMS(url, UUID.randomUUID());
   }

   public ItemBuilder removeEnchantment(Enchantment ench) {
      this.is.removeEnchantment(ench);
      return this;
   }

   public ItemBuilder addEnchant(Enchantment ench, int level) {
      ItemMeta im = this.is.getItemMeta();
      im.addEnchant(ench, level, true);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addBookEnchant(Enchantment ench, int level) {
      EnchantmentStorageMeta im = (EnchantmentStorageMeta)this.is.getItemMeta();
      im.addStoredEnchant(ench, level, true);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addEnchantGlow(Enchantment ench, int level) {
      ItemMeta im = this.is.getItemMeta();
      im.addEnchant(ench, level, true);
      im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
      this.is.addEnchantments(enchantments);
      return this;
   }

   public ItemBuilder setInfinityDurability() {
      this.is.setDurability((short)32767);
      return this;
   }

   public ItemBuilder setBannerColor(DyeColor color) {
      ItemMeta im = this.is.getItemMeta();
      BannerMeta metaBan = (BannerMeta)im;
      metaBan.setBaseColor(color);
      this.is.setItemMeta(metaBan);
      return this;
   }

   public ItemBuilder setFireworkCharge(Color color) {
      ItemMeta im = this.is.getItemMeta();
      FireworkEffectMeta metaFw = (FireworkEffectMeta)im;
      FireworkEffect effect = FireworkEffect.builder().withColor(color).build();
      metaFw.setEffect(effect);
      this.is.setItemMeta(metaFw);
      return this;
   }

   public ItemBuilder addLoreLines(List<String> line) {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList();
      if (im.hasLore()) {
         lore = new ArrayList(im.getLore());
      }

      Iterator var4 = line.iterator();

      while(var4.hasNext()) {
         String s = (String)var4.next();
         if (s != null) {
            lore.add(ChatColor.translateAlternateColorCodes('&', s));
         }
      }

      im.setLore(lore);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addLoreLines(String... line) {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList();
      if (im.hasLore()) {
         lore = new ArrayList(im.getLore());
      }

      String[] var4 = line;
      int var5 = line.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String s = var4[var6];
         lore.add(ChatColor.translateAlternateColorCodes('&', s));
      }

      im.setLore(lore);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder removeLoreLine(String line) {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList(im.getLore());
      if (!lore.contains(line)) {
         return this;
      } else {
         lore.remove(line);
         im.setLore(lore);
         this.is.setItemMeta(im);
         return this;
      }
   }

   public ItemBuilder removeLoreLine(int index) {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList(im.getLore());
      if (index >= 0 && index <= lore.size()) {
         lore.remove(index);
         im.setLore(lore);
         this.is.setItemMeta(im);
         return this;
      } else {
         return this;
      }
   }

   public ItemBuilder addLoreLine(String line) {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList();
      if (im.hasLore()) {
         lore = new ArrayList(im.getLore());
      }

      lore.add(ChatColor.translateAlternateColorCodes('&', line));
      im.setLore(lore);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder addLoreLine(String line, int pos) {
      ItemMeta im = this.is.getItemMeta();
      List<String> lore = new ArrayList(im.getLore());
      lore.set(pos, line);
      im.setLore(lore);
      this.is.setItemMeta(im);
      return this;
   }

   public ItemBuilder setDyeColor(DyeColor color) {
      this.is.setDurability((short)color.getDyeData());
      return this;
   }

   /** @deprecated */
   @Deprecated
   public ItemBuilder setWoolColor(DyeColor color) {
      if (!this.is.getType().equals(Material.WOOL)) {
         return this;
      } else {
         this.is.setDurability((short)color.getDyeData());
         return this;
      }
   }

   public ItemBuilder setLeatherArmorColor(Color color) {
      try {
         LeatherArmorMeta im = (LeatherArmorMeta)this.is.getItemMeta();
         im.setColor(color);
         this.is.setItemMeta(im);
      } catch (ClassCastException var3) {
      }

      return this;
   }

   public ItemStack toItemStack() {
      return this.is;
   }

   public String getSkullOwner() {
      return this.skullOwner;
   }

   public ItemBuilder setSkullOwner(String owner) {
      try {
         SkullMeta im = (SkullMeta)this.is.getItemMeta();
         im.setOwner(owner);
         this.is.setItemMeta(im);
      } catch (ClassCastException var3) {
      }

      return this;
   }

   public ItemBuilder setSkullOwnerNMS(SkullData data) {
      try {
         String url = data.getTexture();
         if (data.getType() == SkullDataType.NAME) {
            return this.setSkullOwner(url);
         }

         SkullMeta headMeta = (SkullMeta)this.is.getItemMeta();
         GameProfile profile = new GameProfile(UUID.randomUUID(), (String)null);
         if (data.getType() == SkullDataType.URL) {
            byte[] decodedBytes = Base64.getDecoder().decode(url);
            String decoded = (new String(decodedBytes)).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "").replace("\"}}}", "");
            byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{\"url\":\"%s\"}}}", decoded).getBytes());
            profile.getProperties().put("textures", new Property("textures", new String(encodedData)));

            try {
               Field profileField = headMeta.getClass().getDeclaredField("profile");
               profileField.setAccessible(true);
               profileField.set(headMeta, profile);
               this.skullOwner = decoded;
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException var10) {
               var10.printStackTrace();
            }
         } else if (data.getType() == SkullDataType.TEXTURE) {
            profile.getProperties().put("textures", new Property("textures", data.getTexture()));

            try {
               Field profileField = headMeta.getClass().getDeclaredField("profile");
               profileField.setAccessible(true);
               profileField.set(headMeta, profile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException var9) {
               var9.printStackTrace();
            }
         }

         this.is.setItemMeta(headMeta);
      } catch (ClassCastException var11) {
      }

      return this;
   }

   public ItemBuilder unbreakable() {
      this.is.getItemMeta().spigot().setUnbreakable(true);
      this.is.getItemMeta().addItemFlags(new ItemFlag[]{ItemFlag.HIDE_UNBREAKABLE});
      return this;
   }

   public static ItemBuilder parse(YamlConfiguration yml, String path) {
      XMaterial material = XMaterial.valueOf(yml.getString(path + ".material", "BEDROCK"));
      ItemBuilder builder = new ItemBuilder(material);
      if (yml.contains(path + ".name")) {
         builder = builder.setName(yml.getString(path + ".name"));
      }

      if (yml.contains(path + ".data")) {
         builder = builder.setDurability(Short.parseShort(yml.getString(path + ".data")));
      }

      if (yml.contains(path + ".dye_color")) {
         builder = builder.setDyeColor(DyeColor.valueOf(yml.getString(path + ".dye_color")));
      }

      if (yml.contains(path + ".head_url")) {
         builder = builder.setSkullOwnerNMS(yml.getString(path + ".head_url"));
      }

      if (yml.contains(path + ".texture")) {
          builder = builder.setSkullOwnerNMS(yml.getString(path + ".texture"));
      }

      if (yml.contains(path + ".unbreakable") && yml.getBoolean(path + ".unbreakable")) {
         builder = builder.unbreakable();
      }

      if (yml.contains(path + ".amount")) {
         builder = builder.setAmount(Integer.parseInt(yml.getString(path + ".amount")));
      }

      if (yml.contains(path + ".lore")) {
         builder = builder.setLore(yml.getStringList(path + ".lore"));
      }

      if (yml.contains(path + ".enchantments")) {
         Map<Enchantment, Integer> enchsMap = new HashMap();
         yml.getStringList(path + ".enchantments").forEach((linex) -> {
            String[] elements = linex.split(":", 2);
            enchsMap.put(Enchantment.getByName(elements[0]), Integer.parseInt(elements[1]));
         });
         builder = builder.addEnchantments(enchsMap);
      }

      String line;
      if (yml.contains(path + ".flags")) {
         for(Iterator var6 = yml.getStringList(path + ".flags").iterator(); var6.hasNext(); builder = builder.addItemFlag(ItemFlag.valueOf(line))) {
            line = (String)var6.next();
         }
      }

      return builder;
   }

   public static ItemBuilder parse(ConfigurationSection yml, String path) {
      XMaterial material = XMaterial.valueOf(yml.getString(path + ".material", "BEDROCK"));
      ItemBuilder builder = new ItemBuilder(material);
      if (yml.contains(path + ".name")) {
         builder = builder.setName(yml.getString(path + ".name"));
      }

      if (yml.contains(path + ".data")) {
         builder = builder.setDurability(Short.parseShort(yml.getString(path + ".data")));
      }

      if (yml.contains(path + ".dye_color")) {
         builder = builder.setDyeColor(DyeColor.valueOf(yml.getString(path + ".dye_color")));
      }

      if (yml.contains(path + ".head_url")) {
         builder = builder.setSkullOwnerNMS(yml.getString(path + ".head_url"));
      }

       if (yml.contains(path + ".texture")) {
           builder = builder.setSkullOwnerNMS(yml.getString(path + ".texture"));
       }

      if (yml.contains(path + ".unbreakable") && yml.getBoolean(path + ".unbreakable")) {
         builder = builder.unbreakable();
      }

      if (yml.contains(path + ".amount")) {
         builder = builder.setAmount(Integer.parseInt(yml.getString(path + ".amount")));
      }

      if (yml.contains(path + ".lore")) {
         builder = builder.setLore(yml.getStringList(path + ".lore"));
      }

      if (yml.contains(path + ".enchantments")) {
         Map<Enchantment, Integer> enchsMap = new HashMap();
         yml.getStringList(path + ".enchantments").forEach((linex) -> {
            String[] elements = linex.split(":", 2);
            enchsMap.put(Enchantment.getByName(elements[0]), Integer.parseInt(elements[1]));
         });
         builder = builder.addEnchantments(enchsMap);
      }

      String line;
      if (yml.contains(path + ".flags")) {
         for(Iterator var6 = yml.getStringList(path + ".flags").iterator(); var6.hasNext(); builder = builder.addItemFlag(ItemFlag.valueOf(line))) {
            line = (String)var6.next();
         }
      }

      return builder;
   }

   public static class SkullData {
      private final String texture;
      private final SkullDataType type;

      public SkullData(String texture, SkullDataType type) {
         this.texture = texture;
         this.type = type;
      }

      public String getTexture() {
         return this.texture;
      }

      public SkullDataType getType() {
         return this.type;
      }
   }

   public static enum SkullDataType {
      NAME,
      URL,
      TEXTURE;
   }
}
