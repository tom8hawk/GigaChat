# GigaChat
Plugin was made by [OverwriteMC](https://github.com/Overwrite987) and forked by me (Groundbreaking)


## Features
- Optimized performance
- Supports SQLite database to save players' choices and load them on login
- Newbie protection (better to use [NewbieGuard](https://github.com/groundbreakingmc/NewbieGuard/releases))
- Supports multiple color formats (MINIMESSAGE / LEGACY / LEGACY_ADVANCED / VANILLA)
  - MINIMESSAGE: <color>text</color>
  - LEGACY: &color_or_style_key and &#rrggbb
  - LEGACY_ADVANCED: &#rgb (&#123 -> &#112233)
  - VANILLA: &color_or_style_key
- Adds the "/broadcast" command
- Adds private message commands
- Adds auto-messages
- Allows setting listener priority
- Configurable options for newbie time tracking method:
  - From first login
  - Total playtime
- Supports hover texts for chat messages and broadcasts
  - Separately configurable for administrators
- Chat clearing option
  - Does not clear for administrators
- Ability to disable the chat
- Configure commands and aliases for each module
- Option to disable the chat for yourself
- Ability to customize personal message sounds


## Commands
- /gigachat reload - reload plugin (Usage perm: gigachat.command.reload)
- /gigachat clearchat - clear chat (Usage perm: gigachat.command.clearchat)
  - Chat is not cleared for those who have permission: "gigachat.bypass.clearchat"
- /gigachat disablechat -  (Usage perm: gigachat.command.disablechat)
  - To bypass chat disabling you need to have permission: "gigachat.bypass.disablechat"
- /gigachat localspy - see all local messages (Usage perm: gigachat.command.localspy)
- /gigachat setpmsound <player> <sound/disable> - set sound for receiving pm for specified player (Usage perm: gigachat.command.setpmsound)
### Private Messages
- /pm <player> <message> - write private message to a player (Usage perm: gigachat.pm)
- /reply <message> - reply to last private message (Usage perm: gigachat.command.reply)
- /ignore <chat/private> <player> - enable/disable messages from a player in pm ore chat (Usage perm: gigachat.ignore.chat / gigachat.ignore.private)
  - If player has only one of both permissions, that he needs to use just "/ignore <player>", plugin itself will get how the sender will ignore specified player
- /socialspy - enable/disable spy mode (Usage perm: gigachat.command.socialspy)


## Permissions
- gigachat.adminhover - allows to see admin hover
- gigachat.bypass.cooldown.local - allows to bypass cooldown in local chat
- gigachat.bypass.cooldown.global - allows to bypass cooldown in global chat
- gigachat.bypass.cooldown.socialspy - allows to bypass cooldown for command "/socialspy" (Also aliases)
- gigachat.bypass.cooldown.broadcast - allows to bypass cooldown for command "/broadcast" (Also aliases)
- gigachat.bypass.cooldown.ignore - allows to bypass cooldown for command "/ignore" (Also aliases)
- gigachat.bypass.cooldown.pm - allows to bypass cooldown for command "/pm" and "/reply" (Also aliases)
- gigachat.bypass.chatnewbie - allows to white in the chat bypassing the newbie chat check
- gigachat.bypass.commandsnewbie - allows to white in the chat bypassing the newbie commands check
- gigachat.bypass.clearchat - allows to save chat history on "/gchat clearchat" command usage
- gigachat.bypass.disabledchat - allows to write in the chat bypassing disabled chat
- gigachat.command.ignore.chat - allows to use "/ignore chat <player>" command
- gigachat.command.ignore.private - allows to use "/ignore private <player>" command
- gigachat.command.socialspy - allows to use "/socialspy" command
- gigachat.command.reply - allows to use "/reply <message>" command
- gigachat.command.setpmsound - allows to use "/gchat setpmsound <player> <sound/disable>" command
- gigachat.command.disablechat - allows to use "/gchat disablechat" command
- gigachat.command.clearchat - allows to use "/gchat clearchat" command
- gigachat.command.localspy - allows to use "/gchat localspy" command
- gigachat.command.broadcast - allows to use "/broadcast <message>" command
- gigachat.automessages


## Permissions for colors/styles usage in chat
Allowed types: chat / private / broadcast
- gigachat.color.<type>.black
- gigachat.color.<type>.dark_blue
- gigachat.color.<type>.dark_green
- gigachat.color.<type>.dark_aqua
- gigachat.color.<type>.dark_red
- gigachat.color.<type>.dark_purple
- gigachat.color.<type>.gold
- gigachat.color.<type>.gray
- gigachat.color.<type>.dark_gray
- gigachat.color.<type>.blue
- gigachat.color.<type>.green
- gigachat.color.<type>.aqua
- gigachat.color.<type>.red
- gigachat.color.<type>.light_purple
- gigachat.color.<type>.yellow
- gigachat.color.<type>.white
- gigachat.style.<type>.obfuscated
- gigachat.style.<type>.bold
- gigachat.style.<type>.strikethrough
- gigachat.style.<type>.underline
- gigachat.style.<type>.italic
- gigachat.style.<type>.reset
- gigachat.<type>.hex