/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.measure;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.apache.log4j.Logger;

/**
 *
 * Times - Measures and stores wallclock time, user time and system time.
 *
 * @author Dr. Lee Sau-dan
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class Times {
    private static final Logger log = Logger.getLogger(Times.class);

    private static double SIGFIG = 1000.0;

    private static final ThreadMXBean threadMX =
	ManagementFactory.getThreadMXBean();
    static {
	if (!threadMX.isCurrentThreadCpuTimeSupported())
	    log.warn("getCurrentThreadCpuTime() is not supported");
    }

    private long cpuTime, userTime, wallclockTime;

    /**
     * Constructor by the given Times object
     * @param that a Times object to be copied
     */
    public Times(Times that){
    	this.cpuTime = that.cpuTime;
    	this.userTime = that.userTime;
    	this.wallclockTime = that.wallclockTime;
    }

    /**
     * Constructor by the given cpuTime, userTime and wallclockTime
     * @param cpuTime the cpu time
     * @param userTime the user time
     * @param wallclockTime the wallclock time
     */
    public Times(long cpuTime, long userTime, long wallclockTime) {
		this.cpuTime = cpuTime;
		this.userTime = userTime;
		this.wallclockTime = wallclockTime;
    }

    /**
     * Constructor fot initializing to current time
     *
     */
    public Times(){
    	this(true);
    }

    /**
     * Constructor with update parameter
     * @param update true to initialize to current time, false to initialize a zero time
     */
    public Times(boolean update){
    	this(0,0,0);
    	if(update) setToCurrent();
    }

    /**
     * Get the cpu time of the system (user time + system time) in nanoseconds
     * @return the cpu time in nanoseconds
     */
    public long getCpuTime() {
    	return cpuTime;
    }

    /**
     * Get the user time of the system in nanoseconds
     * @return the user time in nanoseconds
     */
    public long getUserTime() {
    	return userTime;
    }

    /**
     * Get the system time of the system in nanoseconds
     * @return the system time in nanoseconds
     */
    public long getSystemTime(){
    	return cpuTime - userTime;
    }

    /**
     * Get Wallclock Time in nanosecond
     * @return the wallclock time in nanoseconds
     */
    public long getWallclockTime() {
    	return wallclockTime;
    }

    /**
     * Get Wallclock Time in nseconds
     * @return the wallclock time in seconds
     */
    public double getWallclockTimeInSeconds() {
    	return Math.rint(wallclockTime/SIGFIG*SIGFIG)/SIGFIG;
    }


    /**
     * Get the cpu time of the system (user time + system time)
     * @return the cpu time in seconds
     */
    public double getCpuTimeInSeconds() {
    	return Math.rint(cpuTime/SIGFIG*SIGFIG)/SIGFIG;
    }

    /**
     * Get the user time of the system
     * @return the user time in seconds
     */
    public double  getUserTimeInSeconds() {
    	return Math.rint(userTime/SIGFIG*SIGFIG)/SIGFIG;
    }

    /**
     * Get the system time of the system
     * @return the system time in seconds
     */
    public double getSystemTimeInSeconds(){
    	return Math.rint((getCpuTimeInSeconds() - getUserTimeInSeconds())*SIGFIG)/SIGFIG;
    }


    /**
     * Set the Times to current time
     *
     */
    public void setToCurrent() {
		cpuTime = threadMX.getCurrentThreadCpuTime();
		userTime = threadMX.getCurrentThreadUserTime();
		wallclockTime = System.nanoTime();
    }

    /**
     * Substract the current times and the given times and store the dufference in current Times object
     * @param times the given times
     */
    public void subtract(Times times) {
		cpuTime -= times.cpuTime;
		userTime -= times.userTime;
		wallclockTime -= times.wallclockTime;
    }

    /**
     * Find the difference of the current time and another time and store in a new Time
     * @param oldTimes the reference times
     * @return the Time object that records the difference
     */
    public Times difference(Times oldTimes){
    	Times difference = new Times(this);
    	difference.subtract(oldTimes);
    	return difference;
    }

    /**
     *  Print wallclock time, user time and system time in seconds
     *
     */
    public void printTime(){
    	System.out.println("real\t" + getWallclockTimeInSeconds());
    	System.out.println("user\t" + getUserTimeInSeconds());
    	System.out.println("system\t" + getSystemTimeInSeconds());
    }
}
