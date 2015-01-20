package com.bendude56.goldenapple;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;

public class SimplePerformanceMonitor implements PerformanceMonitor {
    private boolean active;
    private OutputStreamWriter out;
    
    public SimplePerformanceMonitor(GoldenApple instance) {
        this.active = instance.getConfig().getBoolean("global.performanceLogging", false);
        
        if (this.active) {
            try {
                this.out = new OutputStreamWriter(new FileOutputStream(instance.getDataFolder() + "/performance.log", true));
            } catch (Exception e) {
                GoldenApple.log(Level.WARNING, "Failed to start performance logging:");
                GoldenApple.log(Level.WARNING, e);
                this.active = false;
            }
        }
    }
    
    @Override
    public PerformanceEvent createForCommand(String module, String commandLabel, String[] args) {
        if (active) {
            String command = commandLabel;
            for (String arg : args) {
                command += " " + arg;
            }
            
            return new PerformanceEventCommand(module, command);
        } else {
            return new PerformanceEventDummy();
        }
    }
    
    @Override
    public PerformanceEvent createForEvent(String module, String event) {
        if (active) {
            return new PerformanceEventEvent(module, event);
        } else {
            return new PerformanceEventDummy();
        }
    }
    
    private void logEvent(PerformanceEvent e) {
        if (!active) {
            throw new UnsupportedOperationException();
        }
        
        try {
            synchronized (out) {
                if (e instanceof PerformanceEventCommand) {
                    PerformanceEventCommand eCommand = (PerformanceEventCommand) e;
                    out.write(eCommand.module + ": Command '" + eCommand.command + "' took " + (eCommand.getNanos() / 1000000.0) + "ms\n");
                } else if (e instanceof PerformanceEventEvent) {
                    PerformanceEventEvent eEvent = (PerformanceEventEvent) e;
                    out.write(eEvent.module + ": Event '" + eEvent.event + "' took " + (eEvent.getNanos() / 1000000.0) + "ms\n");
                }
                
                out.flush();
            }
        } catch (IOException ex) {
            GoldenApple.log(Level.WARNING, "Failed to log performance event:");
            GoldenApple.log(Level.WARNING, ex);
            this.active = false;
        }
    }
    
    public void close() {
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                out = null;
            }
        }
    }
    
    public class PerformanceEventDummy implements PerformanceEvent {
        @Override
        public void start() {
            // Do nothing
        }
        
        @Override
        public void stop() {
            // Do nothing
        }
        
        @Override
        public boolean isTracked() {
            return false;
        }
        
        @Override
        public long getNanos() {
            throw new UnsupportedOperationException();
        }
    }
    
    public class PerformanceEventCommand implements PerformanceEvent {
        public String module, command;
        public Long startTime = null, stopTime = null;
        
        public PerformanceEventCommand(String module, String command) {
            this.module = module;
            this.command = command;
        }
        
        @Override
        public void start() {
            if (startTime != null) {
                throw new UnsupportedOperationException();
            }
            
            startTime = System.nanoTime();
        }
        
        @Override
        public void stop() {
            if (startTime == null) {
                throw new UnsupportedOperationException();
            }
            
            stopTime = System.nanoTime();
            SimplePerformanceMonitor.this.logEvent(this);
        }
        
        @Override
        public boolean isTracked() {
            return true;
        }
        
        @Override
        public long getNanos() {
            return stopTime - startTime;
        }
    }
    
    public class PerformanceEventEvent implements PerformanceEvent {
        public String module, event;
        public Long startTime = null, stopTime = null;
        
        public PerformanceEventEvent(String module, String event) {
            this.module = module;
            this.event = event;
        }
        
        @Override
        public void start() {
            if (startTime != null) {
                throw new UnsupportedOperationException();
            }
            
            startTime = System.nanoTime();
        }
        
        @Override
        public void stop() {
            if (startTime == null) {
                throw new UnsupportedOperationException();
            }
            
            stopTime = System.nanoTime();
            SimplePerformanceMonitor.this.logEvent(this);
        }
        
        @Override
        public boolean isTracked() {
            return true;
        }
        
        @Override
        public long getNanos() {
            return stopTime - startTime;
        }
    }
}
