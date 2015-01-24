package com.bendude56.goldenapple.punish;

import java.util.Comparator;

public class PunishmentTimeComparator implements Comparator<Punishment> {

    @Override
    public int compare(Punishment p1, Punishment p2) {
        return p1.getStartTime().compareTo(p2.getStartTime());
    }
    
}
