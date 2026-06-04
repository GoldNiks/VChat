# VChat

**English:** Forge 1.20.1 mod: global/local chat with configurable prefixes, custom tab list, command logging.

**Русский:** Forge 1.20.1 мод: глобальный/локальный чат с настраиваемыми префиксами, кастомный таб, логирование команд.

## Features / Возможности

### English
- **Global chat** — `/g <text>` or `!<text>` in chat, visible to all players
- **Local chat** — regular message, configurable radius (default 100 blocks)
- **LuckPerms prefixes** — automatically applied via scoreboard team
- **Custom tab list** — header and footer with placeholders: `%online%`, `%max%`, `%player%`
- **Welcome message** — personal message on join
- **Command logging** — all commands and chat logged to `[VChat]` in console
- **Fully configurable** — all settings in `config/vchat-tab.json`

### Русский
- **Глобальный чат** — `/g <текст>` или `!<текст>` в чате, видят все игроки
- **Локальный чат** — обычное сообщение, настраиваемый радиус (по умолчанию 100 блоков)
- **Префиксы LuckPerms** — автоматически подставляются через scoreboard team
- **Кастомный таб** — заголовок и футер с плейсхолдерами: `%online%`, `%max%`, `%player%`
- **Приветствие** — личное сообщение при входе на сервер
- **Логирование** — все команды и чат пишутся в `[VChat]` в консоли
- **Полная настройка** — все параметры в `config/vchat-tab.json`

## Commands / Команды

| English | Русский |
|---|---|
| `/g <message>` — global chat | `/g <сообщение>` — глобальный чат |
| `/vchat reload` — reload config | `/vchat reload` — перезагрузить конфиг |
| `/vchat status` — show current settings | `/vchat status` — показать текущие настройки |

## Config: `config/vchat-tab.json`

```json
{
  "header": "\n&6&l&nVChat\n\n&7Players: &a%online%\n\n&7&m-----------------",
  "footer": "&7&m-----------------\n\n&7Balance: &e0",
  "joinMessage": "&aWelcome to &6&l&nVChat&a!",
  "localChatRadius": 100,
  "enableGlobalChat": true,
  "enableLocalChat": true,
  "globalCommand": "g",
  "globalChatFormat": "&e[G] &7<name>: &f<message>",
  "localChatFormat": "&7[L] &7<name>: &f<message>",
  "mentionNoOneHeard": true,
  "noOneHeardMessage": "&7No one heard you"
}
```

### Placeholders / Плейсхолдеры

| Placeholder | Description / Описание |
|---|---|
| `%online%` | Online player count |
| `%max%` | Max players |
| `%player%` | Player name |
| `<name>` | Player name (in chat formats) |
| `<message>` | Message text (in chat formats) |

### Config fields / Поля конфига

| Field | Type | Default | Description |
|---|---|---|---|
| `header` | string | `&6&l&nVChat...` | Tab header |
| `footer` | string | `&7&m-...` | Tab footer |
| `joinMessage` | string | `&aWelcome...` | Join message |
| `localChatRadius` | int | `100` | Local chat radius (blocks) |
| `enableGlobalChat` | bool | `true` | Enable/disable global chat |
| `enableLocalChat` | bool | `true` | Enable/disable local chat |
| `globalCommand` | string | `g` | Command for global chat |
| `globalChatFormat` | string | `&e[G]...` | Global message format |
| `localChatFormat` | string | `&7[L]...` | Local message format |
| `mentionNoOneHeard` | bool | `true` | Notify if no one hears local |
| `noOneHeardMessage` | string | `&7No one...` | "No one heard" text |

## Installation / Установка

1. Download `VChat-1.x.x.jar` from [releases](https://github.com/GoldNiks/VChat/releases)
2. Put in `mods/` folder on the server
3. Restart the server
