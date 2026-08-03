# VChat

**English:** Forge 1.20.1 mod: global/local chat, LuckPerms prefixes and rank sorting, custom tab list, command logging.

**Русский:** Forge 1.20.1 мод: глобальный/локальный чат, префиксы и сортировка LuckPerms, кастомный таб, логирование команд.

## Features / Возможности

### English
- **Global chat** — `/g <text>` or `!<text>` in chat, visible to all players
- **Local chat** — regular message, configurable radius (default 100 blocks)
- **LuckPerms prefixes** — automatically applied via scoreboard team
- **LuckPerms sorting** — configurable group order, `tab-order` meta and group-weight fallback
- **Custom tab list** — header and footer with placeholders: `%online%`, `%max%`, `%player%`
- **Welcome message** — personal message on join
- **Command logging** — all commands and chat logged to `[VChat]` in console
- **Fully configurable** — all settings in `config/vchat-tab.json`

### Русский
- **Глобальный чат** — `/g <текст>` или `!<текст>` в чате, видят все игроки
- **Локальный чат** — обычное сообщение, настраиваемый радиус (по умолчанию 100 блоков)
- **Префиксы LuckPerms** — автоматически подставляются через scoreboard team
- **Сортировка LuckPerms** — порядок групп из конфига, meta `tab-order` и fallback на weight
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
  "noOneHeardMessage": "&7No one heard you",
  "enableLuckPermsPrefixes": true,
  "enableTabSorting": true,
  "tabGroupOrder": {},
  "tabOrderMetaKey": "tab-order",
  "useLuckPermsWeightFallback": true,
  "higherWeightFirst": true,
  "defaultTabOrder": 9999,
  "tabUpdateIntervalTicks": 20
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
| `enableLuckPermsPrefixes` | bool | `true` | Show the resolved LuckPerms prefix |
| `enableTabSorting` | bool | `true` | Sort players in TAB using scoreboard teams |
| `tabGroupOrder` | object | `{}` | Explicit primary-group order; lower values appear first |
| `tabOrderMetaKey` | string | `tab-order` | LuckPerms meta key used when the group has no config override |
| `useLuckPermsWeightFallback` | bool | `true` | Use primary-group weight if no explicit or meta order exists |
| `higherWeightFirst` | bool | `true` | Put larger LuckPerms weights above smaller weights |
| `defaultTabOrder` | int | `9999` | Order for players without sorting data, clamped to `0..9999` |
| `tabUpdateIntervalTicks` | int | `20` | Prefix, order and TAB refresh interval; `20` ticks is about one second |

### LuckPerms TAB sorting / Сортировка TAB через LuckPerms

The first available source wins / Используется первый найденный источник:

1. `tabGroupOrder` override for the player's primary group / настройка primary group в `tabGroupOrder`.
2. LuckPerms meta value configured by `tabOrderMetaKey` (default: `tab-order`).
3. Primary-group weight when `useLuckPermsWeightFallback` is enabled.
4. `defaultTabOrder`.

Lower order values appear first. Group weights are reversed when `higherWeightFirst` is `true`, so a larger weight appears higher.

Меньшее значение порядка отображается выше. При `higherWeightFirst: true` больший LuckPerms weight располагается выше.

Example / Пример:

```json
"tabGroupOrder": {
  "owner": 0,
  "admin": 100,
  "moderator": 200,
  "default": 9999
}
```

Players with the same order use a nickname-derived secondary key (rare shortened-name collisions get a stable UUID suffix). VChat updates a scoreboard team only when the resolved prefix or order changes. Existing configs are automatically extended with missing fields on `/vchat reload`.

Для игроков с одинаковым порядком используется вторичный ключ из ника; редкие коллизии сокращённых ников получают стабильный UUID-суффикс. VChat обновляет scoreboard-команду только при изменении итогового префикса или порядка. При `/vchat reload` недостающие поля автоматически добавляются в существующий конфиг.

## Installation / Установка

1. Download `VChat-1.x.x.jar` from [releases](https://github.com/GoldNiks/VChat/releases)
2. Put in `mods/` folder on the server
3. Restart the server
