package com.bendude56.goldenapple;

public interface PerformanceMonitor {
	public PerformanceEvent createForCommand(String module, String commandLabel, String[] args);
	public PerformanceEvent createForEvent(String module, String event);
	
	public interface PerformanceEvent {
		public void start();
		public void stop();
		
		public boolean isTracked();
		public long getNanos();
	}
}
