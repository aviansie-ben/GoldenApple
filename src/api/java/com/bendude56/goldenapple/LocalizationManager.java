package com.bendude56.goldenapple;

import java.util.Collection;
import java.util.List;

import com.bendude56.goldenapple.permissions.IPermissionUser;

public interface LocalizationManager {
    public Locale getDefaultLocale();
    
    public boolean isLocalePresent(String localeName);
    public Locale getLocale(String localeName);
    public Locale getLocale(IPermissionUser user);
    
    public Collection<Locale> getLocales();
    
    public void reloadLocales();
    
    public interface Locale {
        public String getShortName();
        public String getLongName();
        public List<String> getAuthors();
        
        public List<String> getFallbackLocales();
        public java.util.Locale getJavaLocale();
        
        public String getRawMessage(String messageName);
        public String processMessage(String message, Object... args);
        
        public String getMessage(String messageName, Object... args);
    }
}
