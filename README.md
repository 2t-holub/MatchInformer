# MatchInformer
Telegram bot that sends notifications about the start and end of competitive CS:GO matches.

The following technologies are used in this project: Apache Maven, Selenium, Jsoup, JDBC, Telegram API.

The bot sends a list of team players and statistics of the best players at the beginning of the match and the result of battle after it. 
Bot messages contain a lot of information thanks to screenshots of hltv.com website taken with Selenium in Chrome browser.

When the match starts, this bot sends the following message:

![Output sample](https://github.com/2t-holub/MatchInformer/blob/main/images/start.png)

At the end of the match user receives 2 messages:

![Output sample](https://github.com/2t-holub/MatchInformer/blob/main/images/result.png)
