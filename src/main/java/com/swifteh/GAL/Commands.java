package com.swifteh.GAL;

import com.google.common.collect.ListMultimap;
import com.swifteh.GAL.GAL;
import com.swifteh.GAL.GALReward;
import com.swifteh.GAL.GALVote;
import com.swifteh.GAL.VoteType;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
   private GAL plugin;

   public Commands(GAL plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
      String cmdName = command.getName();
      String message;
      String message1;
      int lucky;
      String var18;
      if(cmdName.equalsIgnoreCase("gal")) {
         message = args.length > 0?args[0]:null;
         var18 = args.length > 1?args[1]:null;
         message1 = args.length > 2?args[2]:null;
         if(message == null) {
            sender.sendMessage("- /gal reload | clearqueue | cleartotals | forcequeue | total <player> <total> | clear <player> | top [count] | votes <player> | broadcast <message>");
            return true;
         } else if(message.equalsIgnoreCase("reload")) {
            if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
               return false;
            } else {
               this.plugin.reload();
               sender.sendMessage("Reloaded " + this.plugin.getDescription().getFullName());
               this.plugin.log.info("Reloaded " + this.plugin.getDescription().getFullName());
               return true;
            }
         } else if(message.equalsIgnoreCase("cleartotals")) {
            if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
               return false;
            } else {
               this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                  public void run() {
                     Commands.this.plugin.db.modifyQuery("DELETE FROM `" + Commands.this.plugin.dbPrefix + "GALTotals`;");
                  }
               });
               this.plugin.voteTotals.clear();
               sender.sendMessage("Reset vote totals");
               return true;
            }
         } else if(message.equalsIgnoreCase("clearqueue")) {
            if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
               return false;
            } else {
               this.plugin.queuedVotes.clear();
               this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                  public void run() {
                     Commands.this.plugin.db.modifyQuery("DELETE FROM `" + Commands.this.plugin.dbPrefix + "GALQueue`;");
                  }
               });
               this.plugin.queuedVotes.clear();
               sender.sendMessage("Cleared vote queue");
               return true;
            }
         } else if(message.equalsIgnoreCase("forcequeue")) {
            if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
               return false;
            } else {
               this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                  public void run() {
                     if(Commands.this.plugin.queuedVotes.isEmpty()) {
                        sender.sendMessage("There are no queued votes!");
                     } else {
                        sender.sendMessage("Processing " + Commands.this.plugin.queuedVotes.size() + " votes...");
                        synchronized(Commands.this.plugin.queuedVotes) {
                           Iterator i = Commands.this.plugin.queuedVotes.entries().iterator();

                           while(true) {
                              if(!i.hasNext()) {
                                 break;
                              }

                              Entry entry = (Entry)i.next();
                              Vote vote = ((GALReward)entry.getValue()).vote;
                              Commands.this.plugin.log.info("Forcing queued vote for " + vote.getUsername() + " on " + vote.getServiceName());
                              Commands.this.plugin.processReward(new GALReward((VoteType)entry.getKey(), vote.getServiceName(), vote, false));
                              i.remove();
                           }
                        }

                        Commands.this.plugin.db.modifyQuery("DELETE FROM `" + Commands.this.plugin.dbPrefix + "GALQueue`;");
                     }
                  }
               });
               return true;
            }
         } else if(message.equalsIgnoreCase("top")) {
            if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
               return false;
            } else {
               lucky = 10;

               try {
                  lucky = var18 == null?10:Integer.parseInt(var18);
               } catch (NumberFormatException var13) {
                  ;
               }

               this.voteTop(sender, lucky);
               return true;
            }
         } else {
            final int var20;
            int var25;
            final String var23  = "";
            String var2;
            if(message.equalsIgnoreCase("total")) {
               if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
                  return false;
               } else if(message1 == null) {
                  sender.sendMessage("- /gal total <player> <total>");
                  return true;
               } else {
                  var2 = var18.replaceAll("[^a-zA-Z0-9_\\-]", "");
                  var2 = var2.substring(0, Math.min(var2.length(), 16));
                  boolean var22 = false;

                  try {
                     var25 = Integer.parseInt(message1);
                  } catch (NumberFormatException var14) {
                     sender.sendMessage("- /gal total <player> <total>");
                     return true;
                  }

                  String finalVar = var2;
                  int finalVar2 = var25;
                  this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                     public void run() {
                        Commands.this.plugin.db.setVotes(finalVar, finalVar2, false);
                     }
                  });
                  this.plugin.voteTotals.put(var2.toLowerCase(), Integer.valueOf(var25));
                  this.plugin.lastVoted.put(var2.toLowerCase(), Long.valueOf(System.currentTimeMillis()));
                  sender.sendMessage("Setting " + var2 + "\'s total votes to: " + var25);
                  return true;
               }
            } else if(message.equalsIgnoreCase("clear")) {
               if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
                  return false;
               } else if(var18 == null) {
                  sender.sendMessage("- /gal clear <player>");
                  return true;
               } else {
                  var18 = var18.replaceAll("[^a-zA-Z0-9_\\-]", "");
                  var2 = var18.substring(0, Math.min(var18.length(), 16));
                  this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
                     public void run() {
                        Commands.this.plugin.db.modifyQuery("DELETE FROM `" + Commands.this.plugin.dbPrefix + "GALQueue` WHERE LOWER(`IGN`) = \'" + var23.toLowerCase() + "\'");
                     }
                  });
                  ListMultimap var21 = this.plugin.queuedVotes;
                  synchronized(this.plugin.queuedVotes) {
                     Iterator var24 = this.plugin.queuedVotes.entries().iterator();

                     while(var24.hasNext()) {
                        Entry entry = (Entry)var24.next();
                        if(((GALReward)entry.getValue()).vote.getUsername().equalsIgnoreCase(var23)) {
                           var24.remove();
                        }
                     }
                  }

                  sender.sendMessage("Clearing " + var23 + "\'s queued votes");
                  return true;
               }
            } else if(!message.equalsIgnoreCase("broadcast")) {
               if(message.equalsIgnoreCase("votes")) {
                  if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
                     return false;
                  } else if(var18 == null) {
                     sender.sendMessage("- /gal votes <player>");
                     return true;
                  } else {
                     lucky = 0;
                     if(this.plugin.voteTotals.containsKey(var18.toLowerCase())) {
                        lucky = ((Integer)this.plugin.voteTotals.get(var18.toLowerCase())).intValue();
                     }

                     sender.sendMessage("Player: " + var18 + " has " + lucky + " votes");
                     return true;
                  }
               } else {
                  sender.sendMessage("- /gal reload | clearqueue | cleartotals | total <player> <total> | clear <player> | top [count]");
                  return true;
               }
            } else if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
               return false;
            } else if(args.length <= 1) {
               return false;
            } else {
               StringBuilder var19 = new StringBuilder();

               for(var25 = 1; var25 < args.length; ++var25) {
                  var19.append(args[var25]).append(" ");
               }

               this.plugin.getServer().broadcastMessage(this.plugin.formatMessage(var19.toString().trim(), new Vote[0]));
               return true;
            }

         }
      } else if(cmdName.equalsIgnoreCase("fakevote")) {
         if(!sender.isOp() && !sender.hasPermission("gal.admin")) {
            return false;
         } else {
            message = args.length > 0?args[0]:null;
            var18 = args.length > 1?args[1]:null;
            message1 = args.length > 2?args[2]:null;
            if(message == null) {
               sender.sendMessage("- /fakevote <player> [servicename] [luckynumber]");
               return true;
            } else {
               lucky = 0;
               if(message1 != null) {
                  try {
                     lucky = Integer.parseInt(message1);
                  } catch (NumberFormatException var15) {
                     ;
                  }
               }

               Vote fakeVote = new Vote();
               fakeVote.setUsername(message);
               StringBuilder service = new StringBuilder();
               service.append(var18 == null?"fakeVote":var18);
               if(message1 != null) {
                  service.append("|").append(lucky);
               }

               fakeVote.setServiceName(service.toString());
               fakeVote.setAddress("fakeVote.local");
               fakeVote.setTimeStamp(String.valueOf(System.currentTimeMillis()));
               this.plugin.getServer().getPluginManager().callEvent(new VotifierEvent(fakeVote));
               sender.sendMessage("sent fake vote!");
               this.plugin.log.info("Sent fake vote: " + fakeVote.toString());
               return true;
            }
         }
      } else {
         Iterator arg2;
         if(cmdName.equalsIgnoreCase("vote")) {
            if(!this.plugin.voteCommand) {
               return false;
            } else {
               arg2 = this.plugin.voteMessage.iterator();

               while(arg2.hasNext()) {
                  message = (String)arg2.next();
                  sender.sendMessage(this.plugin.formatMessage(message, sender));
               }

               return true;
            }
         } else if(!cmdName.equalsIgnoreCase("rewards")) {
            if(cmdName.equalsIgnoreCase("votetop")) {
               if(!sender.isOp() && !sender.hasPermission("gal.admin") && !sender.hasPermission("gal.top")) {
                  return false;
               } else {
                  this.voteTop(sender, 10);
                  return true;
               }
            } else {
               return false;
            }
         } else if(this.plugin.rewardCommand && this.plugin.cumulativeVote) {
            arg2 = this.plugin.rewardHeader.iterator();

            while(arg2.hasNext()) {
               message = (String)arg2.next();
               sender.sendMessage(this.plugin.formatMessage(message, sender));
            }

            arg2 = this.plugin.galVote.get(VoteType.CUMULATIVE).iterator();

            while(arg2.hasNext()) {
               GALVote var17 = (GALVote)arg2.next();
               if(this.plugin.rewardMessages.containsKey(var17.key)) {
                  message1 = this.plugin.rewardFormat.replace("{TOTAL}", var17.key).replace("{REWARD}", (CharSequence)this.plugin.rewardMessages.get(var17.key));
                  sender.sendMessage(this.plugin.formatMessage(message1, sender));
               }
            }

            arg2 = this.plugin.rewardFooter.iterator();

            while(arg2.hasNext()) {
               message = (String)arg2.next();
               sender.sendMessage(this.plugin.formatMessage(message, sender));
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void voteTop(final CommandSender sender, final int count) {
      this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, new Runnable() {
         public void run() {
            int place = 1;
            Map votes = Commands.this.plugin.db.getVoteTop(count);
            Iterator var4 = Commands.this.plugin.votetopHeader.iterator();

            while(var4.hasNext()) {
               String entry = (String)var4.next();
               sender.sendMessage(Commands.this.plugin.formatMessage(entry, sender));
            }

            for(var4 = votes.entrySet().iterator(); var4.hasNext(); ++place) {
               Entry var8 = (Entry)var4.next();
               String user = (String)var8.getKey();
               int total = ((Integer)var8.getValue()).intValue();
               if(Commands.this.plugin.users.containsKey(user.toLowerCase())) {
                  user = (String)Commands.this.plugin.users.get(user.toLowerCase());
               }

               String message = Commands.this.plugin.votetopFormat.replace("{POSITION}", String.valueOf(place)).replace("{TOTAL}", String.valueOf(total)).replace("{username}", user);
               sender.sendMessage(Commands.this.plugin.formatMessage(message, new Vote[0]));
            }

         }
      });
   }
}
