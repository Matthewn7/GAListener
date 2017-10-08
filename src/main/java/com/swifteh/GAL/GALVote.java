package com.swifteh.GAL;

import java.util.List;

public class GALVote {
   public String key;
   public String message;
   public String broadcast;
   public List commands;

   public GALVote(String k, String m, String b, List c) {
      this.key = k;
      this.message = m;
      this.broadcast = b;
      this.commands = c;
   }
}
