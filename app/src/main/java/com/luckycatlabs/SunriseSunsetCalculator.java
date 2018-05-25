/*
 * Copyright 2008-2009 Mike Reedell / LuckyCatLabs.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luckycatlabs;

import com.luckycatlabs.calculator.SolarEventCalculator;
import com.luckycatlabs.dto.Location;

import java.util.Calendar;



/**
 * Public interface for getting the various types of sunrise/sunset.
 */
public class SunriseSunsetCalculator {

    private final Location location;

    private final SolarEventCalculator calculator;

    /**
     * Constructs a new <code>SunriseSunsetCalculator</code> with the given <code>Location</code>
     * 
     * @param location
     *            <code>Location</code> object containing the Latitude/Longitude of the location to compute
     *            the sunrise/sunset for.
     * @param timeZoneIdentifier
     *            String identifier for the timezone to compute the sunrise/sunset times in. In the form
     *            "America/New_York". Please see the zi directory under the JDK installation for supported
     *            time zones.
     */
    public SunriseSunsetCalculator(Location location, String timeZoneIdentifier) {
        this.location = location;
        this.calculator = new SolarEventCalculator(location, timeZoneIdentifier);
    }

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     * 
     * @param date
     *            <code>Calendar</code> object containing the date to compute the official sunrise for.
     * @return the official sunrise time as a Calendar
     */
    public Calendar getOfficialSunriseCalendarForDate(Calendar date) {
        return calculator.computeSunriseCalendar(Zenith.OFFICIAL, date);
    }

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     * 
     * @param date
     *            <code>Calendar</code> object containing the date to compute the official sunset for.
     * @return the official sunset time as a Calendar
     */
    public Calendar getOfficialSunsetCalendarForDate(Calendar date) {
        return calculator.computeSunsetCalendar(Zenith.OFFICIAL, date);
    }

}
