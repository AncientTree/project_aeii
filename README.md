# Project Structure

* **campaigns** / All campaign language texts go here.
* **languages** / All general game language texts go here.
* **fonts** / All the fonts used to render texts.

# Language File Naming Policy

Use `[locale code].lang` for language file name. e.g. `ru_RU.lang` represents for Russian.

For all supported locale codes please refer to [this page](http://www.oracle.com/technetwork/java/javase/java8locales-2095355.html).

# Translation Guide

To begin translation, yout need to know a couple of things. So let's begin.

### How Language Files Work in Game?

For each language there is a master file in the `languages` folder which defines the font to be used for this language and contains all the general texts. Without this master file the new language most likely won't work. For campaign texts anyway, the game will try to read the specified language texts for each scenario. If they do not exist game will use the English texts as default for that campaign. So you know, once the master file is in place, you can translate campaign texts any time you want.

### Language File Structure

Simply speaking, language files just contain `<key>=<text>` pairs. For master file though, there's a font specified at the top of the file which defines the font to be used for that language. One notable thing is that, the keys (which are on the left) are not supposed to be modified, and you are only meant to translate the right part.

### How to Submit Translation?

For those already know how to use GitHub I will say no more, just fork the project, make the translation and open PRs to `master` branch. If you are not familiar with GitHub work flow, I recommend you take a look at [this tutorial](https://guides.github.com/activities/forking/). Anyway if you find yourself just not willing to figure out how GitHub works, you can download the project and send me your translation through email (with your name of course).

### Where Translation Works Need to Go?

1. If you want to add a missing language support to the game.
2. If there are any non-translated texts in the language files.
3. If you found any translation errors.

### Keep in Mind

1. Reserve an empty line at the top of each language file, or it will cause problem for the game to load it.
2. Do not replace placeholders (`%s`, `%d`, etc)in the language file and you need to keep their order during translation.
3. You can use `\n` to create a new line for messages and paragraphs (not recommended for labels).
