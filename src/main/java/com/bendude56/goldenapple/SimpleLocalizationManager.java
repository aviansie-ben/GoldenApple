package com.bendude56.goldenapple;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public class SimpleLocalizationManager implements LocalizationManager {
    private HashMap<String, Locale> locales = new HashMap<String, Locale>();
    private String defaultLocale;
    
    public SimpleLocalizationManager(ClassLoader loader, File customLocaleDirectory) {
        this.defaultLocale = GoldenApple.getInstanceMainConfig().getString("message.defaultLocale", "en-US");
        
        loadLocales(loader, customLocaleDirectory);
        
        if (!locales.containsKey(defaultLocale)) {
            if (locales.containsKey("en-US")) {
                GoldenApple.log(Level.WARNING, "Default locale '" + defaultLocale + "' not found! Will use en-US instead...");
            } else {
                throw new RuntimeException("Neither default locale '" + defaultLocale + "', nor locale 'en-US' were found!");
            }
        }
    }
    
    private YamlConfiguration loadJarYaml(ClassLoader loader, String fileName) throws IOException, InvalidConfigurationException {
        InputStream input = loader.getResourceAsStream(fileName);
        
        if (input == null) {
            GoldenApple.log(Level.WARNING, "Locale file '" + fileName + "' not found in JAR!");
            return null;
        }
        
        InputStreamReader r = new InputStreamReader(input, "UTF-8");
        
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(r);
            
            return yaml;
        } finally {
            r.close();
            input.close();
        }
    }
    
    private YamlConfiguration loadFileYaml(File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);
        
        return yaml;
    }
    
    private void loadLocales(ClassLoader loader, File customLocaleDirectory) {
        List<String> jarLocales = new ArrayList<String>();
        
        try {
            InputStream input = loader.getResourceAsStream("locale/locales.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            
            try {
                String localeFile;
                
                while ((localeFile = br.readLine()) != null) {
                    jarLocales.add(localeFile);
                }
            } finally {
                br.close();
                input.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading locale list from JAR", e);
        }
        
        for (String localeFile : jarLocales) {
            if (!localeFile.endsWith(".i18n.yml")) {
                GoldenApple.log(Level.WARNING, "Ignoring bad locale file '" + localeFile + "' in locales.txt!");
                continue;
            }
            
            YamlConfiguration jarDescription = null;
            YamlConfiguration customDescription = null;
            
            try {
                jarDescription = loadJarYaml(loader, "locale/" + localeFile);
            } catch (IOException | InvalidConfigurationException e) {
                GoldenApple.log(Level.WARNING, "Error while loading locale in JAR from '" + localeFile + "':");
                GoldenApple.log(Level.WARNING, e);
            }
            
            File customLocaleFile = new File(customLocaleDirectory, localeFile);
            
            if (customLocaleFile.isFile()) {
                try {
                    customDescription = loadFileYaml(customLocaleFile);
                } catch (IOException | InvalidConfigurationException e) {
                    GoldenApple.log(Level.WARNING, "Error while loading custom locale file '" + localeFile + "':");
                    GoldenApple.log(Level.WARNING, e);
                }
            }
            
            if (jarDescription != null || customDescription != null) {
                try {
                    YamlLocale l = new YamlLocale(jarDescription, customDescription);
                    String name = localeFile.substring(0, localeFile.length() - 9);
                    
                    if (!l.getShortName().equals(name)) {
                        GoldenApple.log(Level.WARNING, "Locale file '" + localeFile + "' contains locale '" + l.getShortName() + "'? Ignoring locale...");
                    } else {
                        locales.put(name, l);
                    }
                } catch (InvalidConfigurationException e) {
                    GoldenApple.log(Level.WARNING, "Failed to load locale from file '" + localeFile + "':");
                    GoldenApple.log(Level.WARNING, e);
                }
            }
        }
        
        if (customLocaleDirectory.isDirectory()) {
            for (String localeFile : customLocaleDirectory.list()) {
                if (!localeFile.endsWith(".i18n.yml") || locales.containsKey(localeFile.substring(0, localeFile.length() - 9))) {
                    continue;
                }
                
                YamlConfiguration customDescription = null;
                File customLocaleFile = new File(customLocaleDirectory, localeFile);
                
                if (customLocaleFile.isFile()) {
                    try {
                        customDescription = loadFileYaml(customLocaleFile);
                    } catch (IOException | InvalidConfigurationException e) {
                        GoldenApple.log(Level.WARNING, "Error while loading custom locale file '" + localeFile + "':");
                        GoldenApple.log(Level.WARNING, e);
                    }
                }
                
                if (customDescription != null) {
                    try {
                        YamlLocale l = new YamlLocale(null, customDescription);
                        String name = localeFile.substring(0, localeFile.length() - 9);
                        
                        if (!l.getShortName().equals(name)) {
                            GoldenApple.log(Level.WARNING, "Locale file '" + localeFile + "' contains locale '" + l.getShortName() + "'? Ignoring locale...");
                        } else {
                            locales.put(name, l);
                        }
                    } catch (InvalidConfigurationException e) {
                        GoldenApple.log(Level.WARNING, "Failed to load locale from file '" + localeFile + "':");
                        GoldenApple.log(Level.WARNING, e);
                    }
                }
            }
        }
    }
    
    @Override
    public Locale getDefaultLocale() {
        return locales.get(defaultLocale);
    }
    
    @Override
    public boolean isLocalePresent(String localeName) {
        return locales.containsKey(localeName);
    }
    
    @Override
    public Locale getLocale(String localeName) {
        return locales.get(localeName);
    }
    
    @Override
    public Locale getLocale(IPermissionUser user) {
        if (PermissionManager.getInstance() != null) {
            Locale l = getLocale(user.getVariableString("goldenapple.locale"));
            
            if (l != null) {
                return l;
            } else {
                return getDefaultLocale();
            }
        } else {
            return getDefaultLocale();
        }
    }
    
    @Override
    public Collection<Locale> getLocales() {
        return Collections.unmodifiableCollection(locales.values());
    }
    
    @Override
    public void reloadLocales() {
        throw new UnsupportedOperationException("Not implemented!");
    }
    
    public class YamlLocale implements Locale {
        private YamlConfiguration jarDefinition;
        private YamlConfiguration customDefinition;
        
        private String shortName;
        private String longName;
        private List<String> authors;
        
        private List<String> fallbackLanguages;
        private java.util.Locale javaLocale;
        
        public YamlLocale(YamlConfiguration jarDefinition, YamlConfiguration customDefinition) throws InvalidConfigurationException {
            this.jarDefinition = jarDefinition;
            this.customDefinition = customDefinition;
            
            if ((shortName = getStringValue("meta.shortName")) == null) {
                throw new InvalidConfigurationException("Metadata missing from locale file: meta.shortName");
            }
            
            if ((longName = getStringValue("meta.longName")) == null) {
                throw new InvalidConfigurationException("Metadata missing from locale file: meta.longName");
            }
            
            if ((authors = getStringListValue("meta.authors")) == null) {
                authors = new ArrayList<String>();
            }
            
            if ((fallbackLanguages = getStringListValue("options.fallbackLanguages")) == null) {
                fallbackLanguages = new ArrayList<String>();
            }
            
            String javaLocale;
            
            if ((javaLocale = getStringValue("options.javaLocale")) == null) {
                this.javaLocale = java.util.Locale.getDefault();
            } else {
                this.javaLocale = java.util.Locale.forLanguageTag(javaLocale);
            }
        }
        
        private String getStringValue(String name) {
            if (customDefinition != null && customDefinition.isString(name)) {
                return customDefinition.getString(name);
            } else if (jarDefinition != null && jarDefinition.isString(name)) {
                return jarDefinition.getString(name);
            } else {
                return null;
            }
        }
        
        private List<String> getStringListValue(String name) {
            if (customDefinition != null && customDefinition.isList(name)) {
                return (List<String>) customDefinition.getList(name);
            } else if (jarDefinition != null && jarDefinition.isList(name)) {
                return (List<String>) jarDefinition.getList(name);
            } else {
                return null;
            }
        }
        
        private boolean isStringList(String name) {
            if (customDefinition != null && customDefinition.contains(name)) {
                return customDefinition.isList(name);
            } else if (jarDefinition != null && jarDefinition.contains(name)) {
                return jarDefinition.isList(name);
            } else {
                return false;
            }
        }
        
        private boolean isString(String name) {
            if (customDefinition != null && customDefinition.contains(name)) {
                return customDefinition.isString(name);
            } else if (jarDefinition != null && jarDefinition.contains(name)) {
                return jarDefinition.isString(name);
            } else {
                return false;
            }
        }
        
        @Override
        public String getShortName() {
            return shortName;
        }
        
        @Override
        public String getLongName() {
            return longName;
        }
        
        @Override
        public List<String> getAuthors() {
            return Collections.unmodifiableList(authors);
        }
        
        @Override
        public List<String> getFallbackLocales() {
            return Collections.unmodifiableList(fallbackLanguages);
        }
        
        @Override
        public java.util.Locale getJavaLocale() {
            return javaLocale;
        }
        
        private String getRawString(String name, String rawName) {
            if (isStringList(rawName)) {
                StringBuilder result = new StringBuilder();
                
                for (String message : getStringListValue(rawName)) {
                    if (result.length() > 0) {
                        result.append("\n&f");
                    }
                    
                    result.append(message);
                }
                
                return result.toString();
            } else if (isString(rawName)) {
                return getStringValue(rawName);
            } else {
                for (String fallbackLanguage : fallbackLanguages) {
                    if (SimpleLocalizationManager.this.isLocalePresent(fallbackLanguage)) {
                        String result = SimpleLocalizationManager.this.getLocale(fallbackLanguage).getRawMessage(name);
                        
                        if (result != null) {
                            return result;
                        }
                    }
                }
                
                GoldenApple.log(Level.WARNING, "Locale " + shortName + " is missing string " + name + "!");
                return name;
            }
        }
        
        @Override
        public String getRawMessage(String messageName) {
            if (messageName.startsWith("mail:")) {
                return getRawString(messageName, "mail." + messageName.substring(5));
            } else if (messageName.startsWith("msg:")) {
                return getRawString(messageName, "messages." + messageName.substring(4));
            } else {
                return getRawString(messageName, "messages." + messageName);
            }
        }
        
        @Override
        public String processMessage(String message, Object... args) {
            for (int i = args.length - 1; i >= 0; i--) {
                message = message.replace("%" + (i + 1), args[i].toString());
            }
            
            for (ChatColor color : ChatColor.values()) {
                message = message.replace("&" + color.getChar(), color.toString());
            }
            
            return message;
        }
        
        @Override
        public String getMessage(String messageName, Object... args) {
            return processMessage(getRawMessage(messageName), args);
        }
    }
}
