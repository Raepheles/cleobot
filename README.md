# IMPORTANT DISCLAIMER

This is a very old discord bot I used to host privately. I decided the make it public in case people would want to see its source code. The data in config file no longer exists such as token, channel ids etc. This file should have been in gitignore (oh well...) but I suppose I didn't care about it in the past since it was a private repo. The application most likely will not run since there has been a lot of changes to the internal discord api also in the near future message commands will be completely deprecated and bots will have to move onto slash commands. There are a lot of bad practices I've done in this repository. Nonetheless this was an important project in my past that helped me learn a lot about Java (although I'm a filthy javascript developer now).

# Cleo Bot

Cleo is a bot for mobile game called King's Raid. It can post data about the game, post certain notifications related to the game, post game news from its [website](https://www.plug.game/kingsraid/1030449) etc. You can use `%help` command to see full command list.

# How to use
You need [maven](https://maven.apache.org/) to bundle the source code into a jar file. After installing maven run the command `mvn package`. It will create {version}.jar and {version}-jar-with-dependencies.jar. Run the 2nd file using `java -jar {version}-jar-with-dependencies.jar`. Replace {version} with the version name you have downloaded such as `v0.5-beta`, `v0.62-beta`, `v1.0` etc.
