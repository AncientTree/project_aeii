# Project Structure

* **campaigns** / All campaign language texts go here.
* **languages** / All general game language texts go here.
* **fonts** / All the fonts used to render texts.

# Translation Guide

To begin translation, yout need to know a couple of things. So let's begin.

### How To Create A Translation?

Login into your in-game online account, there will be a button to let you create a new translation. (iOS not supported for now)

### Language File Structure

Simply speaking, language files only contain `<key>=<text>` pairs. The keys (which are on the left) are not supposed to be modified, and you are only meant to translate the right part. But good thing is that, with in-game translation tool, it will take care of the keys for you so you can focus on translating the right part.

### How to Submit Translation?

Login into your in-game online account, if you have already created your translation the game will let you upload it to the server as a language pack. But if you have not translated enough entries (keys) the server might reject your upload.

### Keep in Mind

* **DO NOT** replace placeholders (`%s`, `%d`, etc) and you need to **keep their order** during translation.
* You can use `\n` to create a new line for messages and paragraphs (not recommended for labels).
