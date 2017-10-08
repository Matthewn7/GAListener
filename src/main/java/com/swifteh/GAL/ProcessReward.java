package com.swifteh.GAL;

import com.swifteh.GAL.GAL;
import com.swifteh.GAL.GALReward;
import com.swifteh.GAL.GALVote;
import com.swifteh.GAL.RewardEvent;
import com.swifteh.GAL.RewardTask;
import com.swifteh.GAL.VoteType;
import com.vexsoftware.votifier.model.Vote;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProcessReward extends BukkitRunnable {
   private GAL plugin;
   private Logger log;
   private SecureRandom random = new SecureRandom();
   private GALReward reward;
   private int votetotal;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$swifteh$GAL$VoteType;

   public ProcessReward(GAL plugin, GALReward reward, int votetotal) {
      this.plugin = plugin;
      this.reward = reward;
      this.log = plugin.log;
      this.votetotal = votetotal;
   }

   public void run() {
      try {
         String e = this.reward.vote.getUsername();
         Player player = this.plugin.getServer().getPlayerExact(e);
         if(player != null) {
            e = player.getName();
         }

         GALVote vote = null;
         int lucky = 0;
         boolean isFakeVote = this.reward.vote.getAddress().equals("fakeVote.local");
         if(isFakeVote) {
            String[] message = this.reward.vote.getServiceName().split("\\|");
            if(message.length > 1) {
               this.reward.vote.setServiceName(message[0]);

               try {
                  lucky = Integer.parseInt(message[1]);
               } catch (NumberFormatException var11) {
                  ;
               }
            }
         }

         String message2;
         String broadcast1;
         GALVote message3;
         Iterator broadcast2;
         switch($SWITCH_TABLE$com$swifteh$GAL$VoteType()[this.reward.type.ordinal()]) {
         case 1:
         case 3:
            GALVote broadcast;
            Iterator commands;
            if(this.plugin.luckyVote && isFakeVote && lucky > 0) {
               message2 = String.valueOf(lucky);
               commands = this.plugin.galVote.get(VoteType.LUCKY).iterator();

               while(commands.hasNext()) {
                  broadcast = (GALVote)commands.next();

                  try {
                     if(lucky == Integer.parseInt(broadcast.key)) {
                        this.log.info("Player: " + e + " was lucky with number " + message2);
                        (new ProcessReward(this.plugin, new GALReward(VoteType.LUCKY, message2, this.reward.vote, false), this.votetotal)).runTaskAsynchronously(this.plugin);
                        break;
                     }
                  } catch (NumberFormatException var15) {
                     ;
                  }
               }
            } else if(this.plugin.luckyVote && player != null) {
               int message1 = 0;
               commands = this.plugin.galVote.get(VoteType.LUCKY).iterator();

               while(commands.hasNext()) {
                  broadcast = (GALVote)commands.next();
                  boolean event = false;

                  int event1;
                  try {
                     event1 = Integer.parseInt(broadcast.key);
                  } catch (NumberFormatException var14) {
                     continue;
                  }

                  if(event1 > 0 && event1 > message1 && this.random.nextInt(event1) == 0) {
                     message1 = event1;
                  }
               }

               if(message1 > 0) {
                  broadcast1 = String.valueOf(message1);
                  Iterator event2 = this.plugin.galVote.get(VoteType.LUCKY).iterator();

                  while(event2.hasNext()) {
                     GALVote commands1 = (GALVote)event2.next();

                     try {
                        if(message1 == Integer.parseInt(commands1.key)) {
                           this.log.info("Player: " + e + " was lucky with number " + broadcast1);
                           (new ProcessReward(this.plugin, new GALReward(VoteType.LUCKY, broadcast1, this.reward.vote, false), this.votetotal)).runTaskAsynchronously(this.plugin);
                           break;
                        }
                     } catch (NumberFormatException var13) {
                        ;
                     }
                  }
               }
            }

            if(this.plugin.cumulativeVote) {
               broadcast2 = this.plugin.galVote.get(VoteType.CUMULATIVE).iterator();

               while(broadcast2.hasNext()) {
                  message3 = (GALVote)broadcast2.next();

                  try {
                     if(this.votetotal == Integer.parseInt(message3.key)) {
                        (new ProcessReward(this.plugin, new GALReward(VoteType.CUMULATIVE, message3.key, this.reward.vote, false), this.votetotal)).runTaskAsynchronously(this.plugin);
                        if(player != null) {
                           this.log.info("Player: " + e + " has voted " + this.votetotal + " times");
                        } else {
                           this.log.info("Offline Player: " + e + " has voted " + this.votetotal + " times");
                        }
                        break;
                     }
                  } catch (NumberFormatException var12) {
                     ;
                  }
               }
            }
         case 2:
         }

         switch($SWITCH_TABLE$com$swifteh$GAL$VoteType()[this.reward.type.ordinal()]) {
         case 1:
            broadcast2 = this.plugin.galVote.get(VoteType.NORMAL).iterator();

            while(broadcast2.hasNext()) {
               message3 = (GALVote)broadcast2.next();
               if(this.reward.key.equalsIgnoreCase(message3.key)) {
                  vote = message3;
                  break;
               }
            }

            if(vote == null) {
               broadcast2 = this.plugin.galVote.get(VoteType.NORMAL).iterator();

               while(broadcast2.hasNext()) {
                  message3 = (GALVote)broadcast2.next();
                  if(message3.key.equalsIgnoreCase("default")) {
                     vote = message3;
                     break;
                  }
               }

               if(vote == null) {
                  this.log.severe("Default service not found, check your config!");
               }
            }
            break;
         case 2:
            broadcast2 = this.plugin.galVote.get(VoteType.LUCKY).iterator();

            while(broadcast2.hasNext()) {
               message3 = (GALVote)broadcast2.next();
               if(this.reward.key.equals(message3.key)) {
                  vote = message3;
                  break;
               }
            }

            if(vote == null) {
               this.log.severe("Lucky config key \'" + this.reward.key + "\' not found, check your config!");
            }
            break;
         case 3:
            broadcast2 = this.plugin.galVote.get(VoteType.PERMISSION).iterator();

            while(broadcast2.hasNext()) {
               message3 = (GALVote)broadcast2.next();
               if(this.reward.key.equalsIgnoreCase(message3.key)) {
                  vote = message3;
                  break;
               }
            }

            if(vote == null) {
               this.log.severe("Perm config key \'" + this.reward.key + "\' not found, check your config!");
            }
            break;
         case 4:
            broadcast2 = this.plugin.galVote.get(VoteType.CUMULATIVE).iterator();

            while(broadcast2.hasNext()) {
               message3 = (GALVote)broadcast2.next();
               if(this.reward.key.equals(message3.key)) {
                  vote = message3;
                  break;
               }
            }

            if(vote == null) {
               this.log.severe("Cumulative config key \'" + this.reward.key + "\' not found, check your config!");
            }
         }

         if(vote == null) {
            return;
         }

         message2 = this.plugin.formatMessage(vote.message, new Vote[]{this.reward.vote});
         broadcast1 = this.plugin.formatMessage(vote.broadcast, new Vote[]{this.reward.vote});
         ArrayList commands2 = new ArrayList();
         Iterator ex = vote.commands.iterator();

         while(ex.hasNext()) {
            String event3 = (String)ex.next();
            commands2.add(this.plugin.formatMessage(event3, new Vote[]{this.reward.vote}));
         }

         final RewardEvent event4 = new RewardEvent(this.reward.type, new GALVote(this.reward.key, message2, broadcast1, commands2));
         this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable() {
            public void run() {
               ProcessReward.this.plugin.getServer().getPluginManager().callEvent(event4);
            }
         });
         if(!event4.isCancelled()) {
            vote = event4.getVote();
            (new RewardTask(this.plugin, vote, this.reward)).runTaskAsynchronously(this.plugin);
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$swifteh$GAL$VoteType() {
      int[] var10000 = $SWITCH_TABLE$com$swifteh$GAL$VoteType;
      if($SWITCH_TABLE$com$swifteh$GAL$VoteType != null) {
         return var10000;
      } else {
         int[] var0 = new int[VoteType.values().length];

         try {
            var0[VoteType.CUMULATIVE.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            var0[VoteType.LUCKY.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            var0[VoteType.NORMAL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            var0[VoteType.PERMISSION.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         $SWITCH_TABLE$com$swifteh$GAL$VoteType = var0;
         return var0;
      }
   }
}
