# Bird Monitor

bird-monitor is an android app that makes periodic sound recordings and uploads them to the cloud, noting the time and location.
These recordings will be analysed to detect the volume of birdsong and how it changes over time, giving you an objective measure of the impact of predator control or reforestation.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/nz.org.cacophony.birdmonitor/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=nz.org.cacophony.birdmonitor)

## License

This project is licensed under the Affero General Public License
(https://www.gnu.org/licenses/agpl-3.0.en.html).

## Autoupdates

In order to allow bird monitor to auto update:
* The APK must be installed from the github releases page (https://github.com/TheCacophonyProject/bird-monitor/releases)
* Phone must be rooted
* Bird Monitor Privileged Extension must be installed as a system app, via TWRP (or other recovery image).
  https://github.com/TheCacophonyProject/Bird-Monitor-Privileged-Extension
* Playstore Play protect must be disabled, otherwise a warning popup will block auto update

## Releases

* Ensure all required changes have been merged into the master branch on Github.
* Ensure you have updated app/build.gradle version parameters eg. (ext.versionPatch)
* VersionCode and VersionName will be automatically generated
* VersionCode:
	- First 2 digits represent minSDKVersion
	- Third digit represents screen sizes
	- Followed by 2 Digits for Major, Minor and Patch versions
* Ensure all required changes have been merged into the master branch on Github.
* Ensure your local repository has the required release revision checked out.
* Tag the release (starting with a "v"), e.g.: `git tag -a v1.2.3 -m "1.2.3 release"`
* Push the tag to Github, e.g.: `git push origin v1.2.3`
* TravisCI will run the tests, create a release package and create a
  [Github Release](https://github.com/TheCacophonyProject/bird-monitor/releases)
* To publish to playstore, download unsigned aab bundle
* Sign aab using jarsigner `jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore <path to android keystore> Bird.Monitor-x.x.x.aab <keyalias>`
* Upload to google play beta track using Google Play Console
* When happy with testing realse to production from Google Play Console
* To update metadata run `fastlane update_meta`