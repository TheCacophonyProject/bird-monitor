/**
 * Overview
 * The Cacophonometer app periodically records audio and uploads the recordings to The Cacophony server.
 * <p>
 * The original version of this app was developed by Cameron Ryan-Pears, then reworked by Tim Hunt
 * with input from Menno Finlay-Smits, and more to come from other members of The Cacophony Project.
 * <p>
 * The MainActivity class creates the opening screen that is shown to the user.  It allows the user
 * to set the mode of operation,  and also access the settings and vitals
 * screens for further configuration such as setting - GPS, frequency of recordings/uploads.
 *<p>
 * Recordings are scheduled using  Android alarms, which the Android OS is responsible for
 * firing.  The alarms send an intent to the 'StartRecordingRecieiver' class which in turn uses
 * either a Service or Thread to call the doRecord method in the RecordAndUpload class.
 * <p>
 * As some(all?) versions of Android do not keep alarms after a reboot, the Cacophonometer app also
 * recreates all necessary alarms on reboot using the BootReceiver class so enabling recording to
 * restart without having to open the app.
 *
 */
package nz.org.cacophony.cacophonometer;

