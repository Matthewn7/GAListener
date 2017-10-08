package com.swifteh.GAL;

import com.swifteh.GAL.GAL;
import com.swifteh.GAL.GALReward;
import com.swifteh.GAL.GALVote;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RewardTask extends BukkitRunnable {
   private GAL plugin;
   private List commands = new ArrayList();
   private String message = "";
   private String broadcast = "";
   private boolean shouldBroadcast = true;
   private Player player;
   private String username = "";

   public RewardTask(GAL plugin, GALVote vote, GALReward reward) {
      this.plugin = plugin;
      this.message = vote.message;
      this.broadcast = vote.broadcast;
      this.commands = vote.commands;
      if(reward.queued) {
         this.shouldBroadcast = plugin.broadcastQueue && !plugin.broadcastOffline;
      }

      this.username = reward.vote.getUsername();
      this.player = plugin.getServer().getPlayerExact(this.username);
      if(this.player != null) {
         this.username = this.player.getName();
      }

   }

   public void run() {
      int var2;
      int var3;
      if(this.broadcast.length() > 0 && this.shouldBroadcast) {

         for(Player command : Bukkit.getOnlinePlayers()) {

            if(this.plugin.broadcastRecent || command.getName().equalsIgnoreCase(this.username) || !this.plugin.lastVoted.containsKey(command.getName().toLowerCase()) || ((Long)this.plugin.lastVoted.get(command.getName().toLowerCase())).longValue() <= System.currentTimeMillis() - 86400000L) {
               String[] var8;
               int var7 = (var8 = this.broadcast.split("\\\\n")).length;

               for(int var6 = 0; var6 < var7; ++var6) {
                  String b = var8[var6];
                  command.sendMessage(b);
               }
            }
         }
      }

      String var10 = "";
      if(this.message.length() > 0 && this.player != null) {
         String[] var12;
         var3 = (var12 = this.message.split("\\\\n")).length;

         for(var2 = 0; var2 < var3; ++var2) {
            var10 = var12[var2];
            this.player.sendMessage(var10);
         }
      }

      Iterator var11 = this.commands.iterator();

      while(var11.hasNext()) {
         var10 = (String)var11.next();
         String finalVar1 = var10;
         this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable() {
            public void run() {
               RewardTask.this.plugin.getServer().dispatchCommand(RewardTask.this.plugin.getServer().getConsoleSender(), finalVar1);
            }
         });

         try {
            Thread.sleep(50L);
         } catch (InterruptedException var9) {
            ;
         }
      }

   }
}
