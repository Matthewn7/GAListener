package com.swifteh.GAL;

import com.swifteh.GAL.VoteType;
import com.vexsoftware.votifier.model.Vote;

public class GALReward {
   public VoteType type;
   public String key;
   public Vote vote;
   public boolean queued;

   public GALReward(VoteType t, String k, Vote v, boolean q) {
      this.type = t;
      this.key = k;
      this.vote = v;
      this.queued = q;
   }
}
