package com.bendude56.goldenapple.punish;

import java.sql.Timestamp;

import com.bendude56.goldenapple.GoldenApple;
import com.bendude56.goldenapple.LocalizationManager.Locale;
import com.bendude56.goldenapple.User;
import com.bendude56.goldenapple.permissions.IPermissionUser;
import com.bendude56.goldenapple.permissions.PermissionManager;

public abstract class Punishment {
    protected long id;
    protected long targetId, adminId;
    protected String reason;
    protected Timestamp startTime;
    protected RemainingTime length;
    protected boolean voided, permanent;
    
    public abstract boolean update();
    public abstract boolean insert();
    
    public long getId() {
        return id;
    }
    
    public long getTargetId() {
        return targetId;
    }
    
    public IPermissionUser getTarget() {
        return PermissionManager.getInstance().getUser(targetId);
    }
    
    public long getAdminId() {
        return adminId;
    }
    
    public IPermissionUser getAdmin() {
        return (adminId == -1) ? User.getConsoleUser() : PermissionManager.getInstance().getUser(adminId);
    }
    
    public String getReason() {
        return reason;
    }
    
    public Timestamp getStartTime() {
        return startTime;
    }
    
    public Timestamp getEndTime() {
        return RemainingTime.add(startTime, length);
    }
    
    public RemainingTime getDuration() {
        return length;
    }
    
    public RemainingTime getRemainingDuration() {
        return new RemainingTime((RemainingTime.add(startTime, length).getTime() - System.currentTimeMillis()) / 1000);
    }
    
    public boolean isVoided() {
        return voided;
    }
    
    public boolean isPermanent() {
        return permanent;
    }
    
    public boolean isExpired() {
        return voided || (!permanent && System.currentTimeMillis() > RemainingTime.add(startTime, length).getTime());
    }
    
    public void voidPunishment() {
        voided = true;
    }
    
    public static class RemainingTime {
        private long secondsLeft;
        
        public RemainingTime(long seconds) {
            this.secondsLeft = seconds;
        }
        
        public RemainingTime(int days, int hours, int minutes, int seconds) {
            this.secondsLeft = seconds + (minutes * 60) + (hours * 3600) + (days * 86400);
        }
        
        public long getTotalSeconds() {
            return secondsLeft;
        }
        
        public int getDays() {
            return (int) Math.floor(secondsLeft / 86400D);
        }
        
        public int getHours() {
            return (int) ((long) Math.floor(secondsLeft / 3600D) % 24);
        }
        
        public int getMinutes() {
            return (int) ((long) Math.floor(secondsLeft / 60D) % 60);
        }
        
        public int getSeconds() {
            return (int) (secondsLeft % 60);
        }
        
        public String toString(IPermissionUser target) {
            return toString((target == null) ? GoldenApple.getInstance().getLocalizationManager().getDefaultLocale() : GoldenApple.getInstance().getLocalizationManager().getLocale(target));
        }
        
        public String toString(Locale l) {
            String result = "";
            int days = getDays(), hours = getHours(), minutes = getMinutes(), seconds = getSeconds();
            
            if (days > 0) {
                result += l.getMessage("shared.time.days", days) + ", ";
            }
            
            if (hours > 0) {
                result += l.getMessage("shared.time.hours", hours) + ", ";
            }
            
            if (minutes > 0) {
                result += l.getMessage("shared.time.minutes", minutes) + ", ";
            }
            
            if (seconds > 0) {
                result += l.getMessage("shared.time.seconds", seconds) + ", ";
            }
            
            if (result.length() > 0) {
                result = result.substring(0, result.length() - 2);
            }
            
            return result;
        }
        
        public String toStringDefault() {
            return toString((IPermissionUser) null);
        }
        
        @Override
        @Deprecated
        public String toString() {
            return toString(GoldenApple.getInstance().getLocalizationManager().getDefaultLocale());
        }
        
        public static RemainingTime parseTime(String input) throws NumberFormatException {
            long seconds = 0;
            
            while (input.length() > 0) {
                int i;
                for (i = 0; i <= input.length(); i++) {
                    if (i == input.length()) {
                        seconds += Integer.parseInt(input.substring(0, i));
                        input = "";
                        break;
                    } else if (!Character.isDigit((input.charAt(i)))) {
                        Character c = Character.toLowerCase(input.charAt(i));
                        if (c == 'd') {
                            seconds += Integer.parseInt(input.substring(0, i)) * 86400;
                        } else if (c == 'h') {
                            seconds += Integer.parseInt(input.substring(0, i)) * 3600;
                        } else if (c == 'm') {
                            seconds += Integer.parseInt(input.substring(0, i)) * 60;
                        } else if (c == 's') {
                            seconds += Integer.parseInt(input.substring(0, i));
                        } else {
                            throw new NumberFormatException();
                        }
                        
                        input = input.substring(i + 1);
                        break;
                    }
                }
            }
            
            return new RemainingTime(seconds);
        }
        
        public static RemainingTime timeBetween(Timestamp time1, Timestamp time2) {
            return new RemainingTime((time1.getTime() - time2.getTime()) / 1000);
        }
        
        public static Timestamp add(Timestamp time, RemainingTime timeAdd) {
            return new Timestamp(time.getTime() + timeAdd.getTotalSeconds() * 1000);
        }
    }
}
