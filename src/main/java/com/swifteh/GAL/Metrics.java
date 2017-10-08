package com.swifteh.GAL;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitTask;

public class Metrics {
   private static final int REVISION = 7;
   private static final String BASE_URL = "http://report.mcstats.org";
   private static final String REPORT_URL = "/plugin/%s";
   private static final int PING_INTERVAL = 15;
   private final Plugin plugin;
   private final Set graphs = Collections.synchronizedSet(new HashSet());
   private final YamlConfiguration configuration;
   private final File configurationFile;
   private final String guid;
   private final boolean debug;
   private final Object optOutLock = new Object();
   private volatile BukkitTask task = null;

   public Metrics(Plugin plugin) throws IOException {
      if(plugin == null) {
         throw new IllegalArgumentException("Plugin cannot be null");
      } else {
         this.plugin = plugin;
         this.configurationFile = this.getConfigFile();
         this.configuration = YamlConfiguration.loadConfiguration(this.configurationFile);
         this.configuration.addDefault("opt-out", Boolean.valueOf(false));
         this.configuration.addDefault("guid", UUID.randomUUID().toString());
         this.configuration.addDefault("debug", Boolean.valueOf(false));
         if(this.configuration.get("guid", (Object)null) == null) {
            this.configuration.options().header("http://mcstats.org").copyDefaults(true);
            this.configuration.save(this.configurationFile);
         }

         this.guid = this.configuration.getString("guid");
         this.debug = this.configuration.getBoolean("debug", false);
      }
   }

   public Metrics.Graph createGraph(String name) {
      if(name == null) {
         throw new IllegalArgumentException("Graph name cannot be null");
      } else {
         Metrics.Graph graph = new Metrics.Graph(name, (Metrics.Graph)null);
         this.graphs.add(graph);
         return graph;
      }
   }

   public void addGraph(Metrics.Graph graph) {
      if(graph == null) {
         throw new IllegalArgumentException("Graph cannot be null");
      } else {
         this.graphs.add(graph);
      }
   }

   public boolean start() {
      Object var1 = this.optOutLock;
      synchronized(this.optOutLock) {
         if(this.isOptOut()) {
            return false;
         } else if(this.task != null) {
            return true;
         } else {
            this.task = this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(this.plugin, new Runnable() {
               private boolean firstPost = true;

               public void run() {
                  try {
                     synchronized(Metrics.this.optOutLock) {
                        if(Metrics.this.isOptOut() && Metrics.this.task != null) {
                           Metrics.this.task.cancel();
                           Metrics.this.task = null;
                           Iterator var3 = Metrics.this.graphs.iterator();

                           while(var3.hasNext()) {
                              Metrics.Graph graph = (Metrics.Graph)var3.next();
                              graph.onOptOut();
                           }
                        }
                     }

                     Metrics.this.postPlugin(!this.firstPost);
                     this.firstPost = false;
                  } catch (IOException var5) {
                     if(Metrics.this.debug) {
                        Bukkit.getLogger().log(Level.INFO, "[Metrics] " + var5.getMessage());
                     }
                  }

               }
            }, 0L, 18000L);
            return true;
         }
      }
   }

   public boolean isOptOut() {
      Object var1 = this.optOutLock;
      synchronized(this.optOutLock) {
         try {
            this.configuration.load(this.getConfigFile());
         } catch (IOException var3) {
            if(this.debug) {
               Bukkit.getLogger().log(Level.INFO, "[Metrics] " + var3.getMessage());
            }

            return true;
         } catch (InvalidConfigurationException var4) {
            if(this.debug) {
               Bukkit.getLogger().log(Level.INFO, "[Metrics] " + var4.getMessage());
            }

            return true;
         }

         return this.configuration.getBoolean("opt-out", false);
      }
   }

   public void enable() throws IOException {
      Object var1 = this.optOutLock;
      synchronized(this.optOutLock) {
         if(this.isOptOut()) {
            this.configuration.set("opt-out", Boolean.valueOf(false));
            this.configuration.save(this.configurationFile);
         }

         if(this.task == null) {
            this.start();
         }

      }
   }

   public void disable() throws IOException {
      Object var1 = this.optOutLock;
      synchronized(this.optOutLock) {
         if(!this.isOptOut()) {
            this.configuration.set("opt-out", Boolean.valueOf(true));
            this.configuration.save(this.configurationFile);
         }

         if(this.task != null) {
            this.task.cancel();
            this.task = null;
         }

      }
   }

   public File getConfigFile() {
      File pluginsFolder = this.plugin.getDataFolder().getParentFile();
      return new File(new File(pluginsFolder, "PluginMetrics"), "config.yml");
   }

   private void postPlugin(boolean isPing) throws IOException {
      PluginDescriptionFile description = this.plugin.getDescription();
      String pluginName = description.getName();
      boolean onlineMode = Bukkit.getServer().getOnlineMode();
      String pluginVersion = description.getVersion();
      String serverVersion = Bukkit.getVersion();
      int playersOnline = Bukkit.getServer().getOnlinePlayers().size();
      StringBuilder json = new StringBuilder(1024);
      json.append('{');
      appendJSONPair(json, "guid", this.guid);
      appendJSONPair(json, "plugin_version", pluginVersion);
      appendJSONPair(json, "server_version", serverVersion);
      appendJSONPair(json, "players_online", Integer.toString(playersOnline));
      String osname = System.getProperty("os.name");
      String osarch = System.getProperty("os.arch");
      String osversion = System.getProperty("os.version");
      String java_version = System.getProperty("java.version");
      int coreCount = Runtime.getRuntime().availableProcessors();
      if(osarch.equals("amd64")) {
         osarch = "x86_64";
      }

      appendJSONPair(json, "osname", osname);
      appendJSONPair(json, "osarch", osarch);
      appendJSONPair(json, "osversion", osversion);
      appendJSONPair(json, "cores", Integer.toString(coreCount));
      appendJSONPair(json, "auth_mode", onlineMode?"1":"0");
      appendJSONPair(json, "java_version", java_version);
      if(isPing) {
         appendJSONPair(json, "ping", "1");
      }

      if(this.graphs.size() > 0) {
         Set url = this.graphs;
         synchronized(this.graphs) {
            json.append(',');
            json.append('\"');
            json.append("graphs");
            json.append('\"');
            json.append(':');
            json.append('{');
            boolean connection = true;

            for(Iterator uncompressed = this.graphs.iterator(); uncompressed.hasNext(); connection = false) {
               Metrics.Graph compressed = (Metrics.Graph)uncompressed.next();
               StringBuilder os = new StringBuilder();
               os.append('{');
               Iterator response = compressed.getPlotters().iterator();

               while(response.hasNext()) {
                  Metrics.Plotter reader = (Metrics.Plotter)response.next();
                  appendJSONPair(os, reader.getColumnName(), Integer.toString(reader.getValue()));
               }

               os.append('}');
               if(!connection) {
                  json.append(',');
               }

               json.append(escapeJSON(compressed.getName()));
               json.append(':');
               json.append(os);
            }

            json.append('}');
         }
      }

      json.append('}');
      URL url1 = new URL("http://report.mcstats.org" + String.format("/plugin/%s", new Object[]{urlEncode(pluginName)}));
      URLConnection connection1;
      if(this.isMineshafterPresent()) {
         connection1 = url1.openConnection(Proxy.NO_PROXY);
      } else {
         connection1 = url1.openConnection();
      }

      byte[] uncompressed1 = json.toString().getBytes();
      byte[] compressed1 = gzip(json.toString());
      connection1.addRequestProperty("User-Agent", "MCStats/7");
      connection1.addRequestProperty("Content-Type", "application/json");
      connection1.addRequestProperty("Content-Encoding", "gzip");
      connection1.addRequestProperty("Content-Length", Integer.toString(compressed1.length));
      connection1.addRequestProperty("Accept", "application/json");
      connection1.addRequestProperty("Connection", "close");
      connection1.setDoOutput(true);
      if(this.debug) {
         System.out.println("[Metrics] Prepared request for " + pluginName + " uncompressed=" + uncompressed1.length + " compressed=" + compressed1.length);
      }

      OutputStream os1 = connection1.getOutputStream();
      os1.write(compressed1);
      os1.flush();
      BufferedReader reader1 = new BufferedReader(new InputStreamReader(connection1.getInputStream()));
      String response1 = reader1.readLine();
      os1.close();
      reader1.close();
      if(response1 != null && !response1.startsWith("ERR") && !response1.startsWith("7")) {
         if(response1.equals("1") || response1.contains("This is your first update this hour")) {
            Set var21 = this.graphs;
            synchronized(this.graphs) {
               Iterator iter = this.graphs.iterator();

               while(iter.hasNext()) {
                  Metrics.Graph graph = (Metrics.Graph)iter.next();
                  Iterator var25 = graph.getPlotters().iterator();

                  while(var25.hasNext()) {
                     Metrics.Plotter plotter = (Metrics.Plotter)var25.next();
                     plotter.reset();
                  }
               }
            }
         }

      } else {
         if(response1 == null) {
            response1 = "null";
         } else if(response1.startsWith("7")) {
            response1 = response1.substring(response1.startsWith("7,")?2:1);
         }

         throw new IOException(response1);
      }
   }

   public static byte[] gzip(String input) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzos = null;

      try {
         gzos = new GZIPOutputStream(baos);
         gzos.write(input.getBytes("UTF-8"));
      } catch (IOException var12) {
         var12.printStackTrace();
      } finally {
         if(gzos != null) {
            try {
               gzos.close();
            } catch (IOException var11) {
               ;
            }
         }

      }

      return baos.toByteArray();
   }

   private boolean isMineshafterPresent() {
      try {
         Class.forName("mineshafter.MineServer");
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   private static void appendJSONPair(StringBuilder json, String key, String value) throws UnsupportedEncodingException {
      boolean isValueNumeric = false;

      try {
         if(value.equals("0") || !value.endsWith("0")) {
            Double.parseDouble(value);
            isValueNumeric = true;
         }
      } catch (NumberFormatException var5) {
         isValueNumeric = false;
      }

      if(json.charAt(json.length() - 1) != 123) {
         json.append(',');
      }

      json.append(escapeJSON(key));
      json.append(':');
      if(isValueNumeric) {
         json.append(value);
      } else {
         json.append(escapeJSON(value));
      }

   }

   private static String escapeJSON(String text) {
      StringBuilder builder = new StringBuilder();
      builder.append('\"');

      for(int index = 0; index < text.length(); ++index) {
         char chr = text.charAt(index);
         switch(chr) {
         case '\b':
            builder.append("\\b");
            break;
         case '\t':
            builder.append("\\t");
            break;
         case '\n':
            builder.append("\\n");
            break;
         case '\r':
            builder.append("\\r");
            break;
         case '\"':
         case '\\':
            builder.append('\\');
            builder.append(chr);
            break;
         default:
            if(chr < 32) {
               String t = "000" + Integer.toHexString(chr);
               builder.append("\\u" + t.substring(t.length() - 4));
            } else {
               builder.append(chr);
            }
         }
      }

      builder.append('\"');
      return builder.toString();
   }

   private static String urlEncode(String text) throws UnsupportedEncodingException {
      return URLEncoder.encode(text, "UTF-8");
   }

   public static class Graph {
      private final String name;
      private final Set plotters;

      private Graph(String name) {
         this.plotters = new LinkedHashSet();
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public void addPlotter(Metrics.Plotter plotter) {
         this.plotters.add(plotter);
      }

      public void removePlotter(Metrics.Plotter plotter) {
         this.plotters.remove(plotter);
      }

      public Set getPlotters() {
         return Collections.unmodifiableSet(this.plotters);
      }

      public int hashCode() {
         return this.name.hashCode();
      }

      public boolean equals(Object object) {
         if(!(object instanceof Metrics.Graph)) {
            return false;
         } else {
            Metrics.Graph graph = (Metrics.Graph)object;
            return graph.name.equals(this.name);
         }
      }

      protected void onOptOut() {
      }

      // $FF: synthetic method
      Graph(String var1, Metrics.Graph var2) {
         this(var1);
      }
   }

   public abstract static class Plotter {
      private final String name;

      public Plotter() {
         this("Default");
      }

      public Plotter(String name) {
         this.name = name;
      }

      public abstract int getValue();

      public String getColumnName() {
         return this.name;
      }

      public void reset() {
      }

      public int hashCode() {
         return this.getColumnName().hashCode();
      }

      public boolean equals(Object object) {
         if(!(object instanceof Metrics.Plotter)) {
            return false;
         } else {
            Metrics.Plotter plotter = (Metrics.Plotter)object;
            return plotter.name.equals(this.name) && plotter.getValue() == this.getValue();
         }
      }
   }
}
