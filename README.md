# About

**Ancient Empires Reloaded** is a remastered version of the game **Ancient Empires II** on Symbian platform. This remastered version provides supports for Desktop, Android and iOS platfroms. New features are listed below.
* New tiles
* New & renewed units
* New abilities
* New & renewed status
* New campaigns
* Map editor
* Online map sharing
* Online multiplayer mode

Our [website](http://toyknight.net/aeii)

# Licence

The game's code is under GNU General Public License version 3. You can use the assets of this game without any permission or restriction.

# Contribute a new language support

Follow the steps below to contribute a new language support:

1. Fork the project to your repository.
2. Install [Github Desktop](https://desktop.github.com/) and check out the forked project.
3. Find your country's locale ID from [JDK 6 and JRE 6 Supported Locales](http://www.oracle.com/technetwork/java/javase/locales-137662.html).
4. Grab a TrueType font which supports your country's language, put it under `project_aeii/android/assets/fonts/`. We recommend that you rename the font's filename to your country's locale ID.
5. Go to `project_aeii/android/assets/lang/`, duplicate `en_US.dat` file, rename it to your country's locale ID and translate all the texts after `=`. Note that for the first line, just change `en_US.ttf` to the font filename you are using.
6. Save the language file in UTF-8 format and you are done. Submit a pull request and we will add you to credits once your changes are merged.

**Note:** if you want to see how the font looks in the game, please check the following section.

# Compile & run the project

This project is being developed using [IntellilJ IDEA](https://www.jetbrains.com/idea/), however if you're planning to use [Android Studio](https://developer.android.com/studio/index.html) it will also be fine.
Check out the project from your Github repository after forking it. If you have already checked out the project using Github Desktop then simply import the project from your local repository.
After gradle build is done go for `Build -> Make Project`. Then you can create launchers to launch the game.

* Desktop Launcher **main class:** `net.toyknight.aeii.desktop.DesktopLauncher`, **module:** `desktop`, **working directory:** `<project directory>\project_aeii\android\assets`
* Android Launcher **module:** `android`.
* iOS Launcher **main class:** `net.toyknight.aeii.IOSLauncher`, **module:** `ios`.

**Note:** You will need to run gradle task `desktop:dist` before launch desktop version.

