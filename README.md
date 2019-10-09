# meetTodayBot
Telegram bot for socializing that lets you find, join and create groups and events.

## Usage
The simplest and most popular case so far is a group for going to lunch.
* Find the bot in Telegram: https://t.me/ TODO
* /start 
* /groups
* /events

# Collaboration
# How to run
* Specify env variables: BOT_TOKEN, BOT_USERNAME (get from BotFather)
* run Application.kt
* For local testing, activate spring profile `test_local` - some initial data and user activity simulation
  * application.yml: spring.profiles.active: test_local
  * or env variable SPRING_PROFILES_ACTIVE = test_local

## tech stack
- Kotlin
- Spring Boot, Data
- Telegrambots
