package com.swifteh.GAL;

import com.swifteh.GAL.GALVote;
import com.swifteh.GAL.VoteType;
import java.util.List;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RewardEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled;
   private VoteType type;
   private String message;
   private String broadcast;
   private List commands;
   private String key;

   public RewardEvent(VoteType type, GALVote vote) {
      this.type = type;
      this.message = vote.message;
      this.broadcast = vote.broadcast;
      this.commands = vote.commands;
      this.key = vote.key;
   }

   public VoteType getType() {
      return this.type;
   }

   public String getKey() {
      return this.key;
   }

   public List getCommandList() {
      return this.commands;
   }

   public String getBroadcastMessage() {
      return this.broadcast;
   }

   public String getPlayerMessage() {
      return this.message;
   }

   public void setCommandList(List commands) {
      this.commands = commands;
   }

   public void setBroadcastMessage(String broadcast) {
      this.broadcast = broadcast;
   }

   public void setPlayerMessage(String message) {
      this.message = message;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public GALVote getVote() {
      return new GALVote(this.key, this.message, this.broadcast, this.commands);
   }
}
