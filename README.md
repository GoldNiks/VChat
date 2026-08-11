# VChat

Приватный серверный мод ValorCraft для Forge 1.20.1: локальный и глобальный чат, настраиваемый TAB, LuckPerms и FTB Teams.

## Возможности

- Локальный чат работает только в текущем измерении и заданном радиусе.
- Глобальный чат: `/g <сообщение>` или `!сообщение`.
- После `!` пробел необязателен: `!текст` и `! текст` отображаются одинаково.
- Prefix, suffix, primary group и weight LuckPerms.
- Форматы локального чата, глобального чата и строки игрока в TAB.
- Сортировка TAB по weight основной группы.
- HEX и стили с отдельными permissions.
- Антиспам, ограничение длины и блокировка повторов.
- Упоминания `@Ник` с подсветкой и звуком.
- Постоянный `/ignore`, сохраняемый в `config/VMods/VChat/vchat-ignore.json`.
- Безопасное логирование команд без аргументов по умолчанию.
- Диагностика LuckPerms и форматирования через `/vchat debug`.
- Цветная информация FTB Teams при наведении курсора на ник в чате.
- Текущий этап развития игрока (LV, MV, UV и т.п.) по завершённым главам FTB Quests в суффиксе TAB или чата.
- Скрытие голов Chat Heads в ванильных сообщениях смерти без изменения их текста.
- Discord получает чистый текст без Minecraft/HEX-кодов; `@Ник` остаётся читаемым.
- Сообщение всем игрокам при самом первом входе нового игрока на сервер (порог входа хранится в `config/vchat-firstjoin.json`).
- Мост чата с Discord: webhook-и (глобальный чат, вход/выход, статус сервера) и бот (Discord → игра). Заменяет отдельный мод MC Chat Link.

## Команды

| Команда | Кто может использовать | Описание |
|---|---|---|
| `/g <сообщение>` | Все | Глобальный чат |
| `/ignore <игрок>` | Все | Добавить или убрать игрока из ignore-списка |
| `/ignore` | Все | Показать подсказку и размер ignore-списка |
| `/ignore clear` | Все | Очистить ignore-список, включая офлайн-игроков |
| `/vchat reload` | Уровень 2 | Перечитать конфиг и обновить TAB |
| `/vchat announce <текст>` | Уровень 2 | Отправить объявление всем игрокам |
| `/vchat status` | Уровень 2 | Показать активные настройки |
| `/vchat debug <игрок>` | Уровень 2 | Показать group, weight, prefix, suffix и permissions |

## Конфиг

При первом запуске создаётся `config/VMods/VChat/vchat-config.json5`. JSON5 поддерживает комментарии `//`; каждое поле в сгенерированном файле подписано по-русски. VChat работает только внутри собственной подпапки и не очищает/не перезаписывает общий каталог `config/VMods`, поэтому рядом безопасно могут храниться настройки других модов серии V.

Основная структура:

```json5
{
  "configVersion": 11,

  "tab": {
    "header": "...",
    "footer": "...",
    "joinMessage": "Добро пожаловать на ValorCraft!",
    "firstJoinMessage": "Игрок &f<name> &7присоединился к серверу впервые. Приветствуем!",
    "playerFormat": "<prefix>&f<name><suffix>&r",
    "updateIntervalTicks": 20
  },

  "chat": {
    "localRadius": 100,
    "enableGlobal": true,
    "enableLocal": true,
    "globalCommand": "g",
    "globalFormat": "&e[G] <prefix>&f<name><suffix>&7: &f<message>",
    "localFormat": "&7[L] <prefix>&f<name><suffix>&7: &f<message>",
    "notifyWhenNoOneHeard": true,
    "noOneHeardMessage": "&7Вас никто не услышал",
    "globalDisabledMessage": "&cГлобальный чат сейчас отключён",
    "localDisabledMessage": "&cЛокальный чат сейчас отключён",

    "playerFormatting": {
      "enabled": true,
      "colorsForEveryone": false,
      "hexForEveryone": false,
      "stylesForEveryone": false,
      "obfuscatedForEveryone": false
    },

    "antiSpam": {
      "enabled": true,
      "maxMessageLength": 256,
      "cooldownMillis": 500,
      "blockRepeatedMessages": true,
      "repeatWindowSeconds": 15,
      "tooLongMessage": "&cСообщение слишком длинное. Максимум: <max> символов",
      "tooFastMessage": "&cНе так быстро. Подождите ещё <seconds> сек.",
      "repeatedMessage": "&cНе повторяйте одно и то же сообщение",
      "emptyMessage": "&cСообщение не может быть пустым"
    },

    "mentions": {
      "enabled": true,
      "highlightFormat": "&e&l@<name>&r&f",
      "playSound": true,
      "sound": "minecraft:entity.experience_orb.pickup",
      "volume": 0.8,
      "pitch": 1.2
    },

    "ignore": {
      "enabled": true,
      "addedMessage": "&7Вы больше не видите сообщения игрока &f<name>",
      "removedMessage": "&7Вы снова видите сообщения игрока &f<name>",
      "disabledMessage": "&cСистема игнорирования отключена",
      "cannotIgnoreSelfMessage": "&cНельзя игнорировать самого себя",
      "usageMessage": "&7Использование: &f/ignore <игрок>&7 или &f/ignore clear&7. В списке: &f<count>",
      "clearedMessage": "&7Список игнорирования очищен. Удалено игроков: &f<count>",
      "commandCooldownMillis": 1000,
      "saveIntervalMillis": 1000,
      "cooldownMessage": "&cНе так быстро. Подождите перед повторным изменением списка"
    },

    "logging": {
      "logChatMessages": true,
      "logCommands": true,
      "includeCommandArguments": false,
      "redactedCommands": ["login", "l", "register", "reg", "changepassword", "cp", "password", "2fa"]
    }
  },

  "luckPerms": {
    "showPrefixes": true,
    "showSuffixes": true,
    "sortTabByWeight": true,
    "higherWeightFirst": true
  },

  "ftbTeams": {
    "showTeamOnNameHover": true,
    "showTeamName": true,
    "showPlayerRank": true,
    "showMemberCount": true,
    "hideHoverWithoutTeam": true,
    "teamLabel": "&7Команда: &f",
    "rankLabel": "&7Роль: &f",
    "membersLabel": "&7Участников: &f",
    "noTeamText": "&7Игрок не состоит в команде"
  },

  "deathMessages": {
    "enabled": true,
    "hidePlayerHeads": true
  },

  "stages": {
    "enabled": true,
    "appendToSuffix": true,
    "separator": " ",
    "chapters": [
      { "chapter": "questsstoneage", "tag": "&7Stone Age" },
      { "chapter": "questssteam_age", "tag": "&7Steam" },
      { "chapter": "lv__low_voltage", "tag": "&aLV" },
      { "chapter": "mv__medium_voltage", "tag": "&bMV" },
      { "chapter": "hv__high_voltage", "tag": "&eHV" },
      { "chapter": "ev__extreme_voltage", "tag": "&dEV" },
      { "chapter": "iv__insane_voltage", "tag": "&5IV" },
      { "chapter": "luv__ludicrous_voltage", "tag": "&dLuV" },
      { "chapter": "zpm__zero_point_module", "tag": "&fZPM" },
      { "chapter": "uv__ultimate_voltage", "tag": "&cUV" }
    ]
  },

  "discord": {
    "enabled": true,
    "relayChatToDiscord": true,
    "chatWebhookUrl": "https://discord.com/api/webhooks/...",
    "statusWebhookUrl": "https://discord.com/api/webhooks/...",
    "relayServerStatus": true,
    "serverName": "ValorCraft",
    "webhookUsername": "ValorCraft",
    "webhookAvatarUrl": "",
    "gameToDiscordFormat": "**{player}**: {message}",
    "joinFormat": "**{player}** вошёл на сервер",
    "leaveFormat": "**{player}** вышел с сервера",
    "serverStartedFormat": "🟢 Сервер запущен | {server}",
    "serverStoppedFormat": "🔴 Сервер остановлен | {server}",

    "botEnabled": false,
    "botToken": "",
    "botChannelId": 0,
    "relayDiscordToGame": true,
    "discordToGameFormat": "&8[Discord] &7{username}&8: &f{message}"
  },

  "announcements": {
    "enabled": true,
    "intervalSeconds": 600,
    "messages": [
      "&eСайт сервера: &f[valorcraft.ru](https://valorcraft.ru) &7| &f[Правила](https://valorcraft.ru/rules)",
      "&eDiscord: &f[discord.gg](https://discord.gg/mzCtnkJA7S)",
      "&eНужна помощь? &fЗадай вопрос администрации через &a/ask"
    ]
  }
  }
}
```

После изменения выполните `/vchat reload`. Изменение `globalCommand` требует полного перезапуска, потому что команда регистрируется при запуске сервера.

Конфиги старых версий автоматически обновляются до `configVersion: 13` с сохранением настроек. При первом запуске этой версии файлы VChat сначала копируются из ошибочного пути `<корень>/VMods/VChat/`, затем — из старой папки `config/`, но только если одноимённого файла в `config/VMods/VChat/` ещё нет. После этого создаётся служебный маркер `.legacy-config-migrated`, и миграция больше не повторяется. Старые файлы не удаляются и остаются резервной копией; файлы других модов не копируются и не изменяются. Последняя проверенная конфигурация хранится в `vchat-config.json5.last-good`: при синтаксической ошибке `/vchat reload` отклонит новый файл и продолжит использовать рабочие настройки, а после перезапуска сможет восстановиться из этой копии.

## FTB Teams hover

Если FTB Teams установлен, при наведении на `<name>` или `<display_name>` в сообщении показываются цветное название команды, роль игрока и количество участников. Личные одиночные команды FTB Teams скрываются при `hideHoverWithoutTeam: true`. Интеграция необязательная: без FTB Teams VChat продолжает работать без подсказки.

## FTB Quests: этап развития игрока

Если FTB Quests установлен и `stages.enabled: true`, VChat определяет текущий этап игрока по главам квестов: показывается последняя глава из списка `stages.chapters`, все обязательные квесты которой игрок полностью выполнил. При `appendToSuffix: true` тег этапа автоматически дописывается в конец `<suffix>` в TAB и чате. Иначе этап можно вывести вручную через `<stage>`. Интеграция необязательная: без FTB Quests VChat работает как раньше.

## Сообщения о смерти и Chat Heads

При `deathMessages.enabled: true` и `hidePlayerHeads: true` VChat добавляет невидимый разделитель в имена жертвы и игрока-убийцы только на время формирования ванильного death-компонента. Chat Heads перестаёт ошибочно считать системную строку сообщением игрока. Перевод, причина смерти, имя оружия и hover предмета сохраняются. Настройку `handleSystemMessages` в Chat Heads можно оставить включённой.

## Placeholders

В `globalFormat`, `localFormat` и `playerFormat` доступны:

| Placeholder | Значение |
|---|---|
| `<prefix>` | Prefix LuckPerms с цветами |
| `<suffix>` | Suffix LuckPerms с цветами |
| `<name>` | Настоящий ник |
| `<display_name>` | Текущее отображаемое имя |
| `<group>` | Primary group LuckPerms |
| `<world>` | Текущее измерение |
| `<channel>` | `global`, `local` или `tab` |
| `<message>` | Текст сообщения |
| `<stage>` | Текущий этап развития игрока по главам FTB Quests (тег из `stages.chapters`) |
| `<balance>` | Баланс игрока из VEconomy (пусто, если мод не установлен) |
| `<tps>` | Текущий TPS сервера |

В header/footer: `%online%`, `%max%`, `%player%`, `%tps%`. В `firstJoinMessage` доступен `<name>`.

## VEconomy: баланс игрока

Если VEconomy установлен, `<balance>` в `playerFormat` и форматах чата показывает баланс игрока в том же виде, что и команда VEconomy (символ валюты, разделители, склонение). Интеграция необязательная и работает через публичный API VEconomy: без мода плейсхолдер заменяется пустой строкой.

## TPS

`%tps%` в header/footer и `<tps>` в `playerFormat`/форматах чата показывают текущий TPS сервера (в среднем за 200 тиков, округление до десятых). При полной нагрузке — `20.0`, при лагах значение уменьшается.

## Автообъявления

Секция `announcements` в конфиге: при `enabled: true` VChat каждые `intervalSeconds` секунд показывает случайную фразу из `messages` (без повторов подряд). Фразы поддерживают `&-цвета` и кликабельные ссылки `[текст](https://url)` — ссылка подсвечивается, открывается по клику, URL виден при наведении. Ручной запуск — `/vchat announce <текст>` (уровень 2).

Замените мод-объявления: если анонсы крутил отдельный мод (например, секция `[broadcast]` в `reportmod-common.toml`), просто отключите его и впишите свои фразы в `messages`.

## Цвета и permissions

Поддерживаются `&6`, `&l`, `#RRGGBB`, `&#RRGGBB`, `&%23RRGGBB` и прописные варианты вроде `&A`.

| Permission | Возможность |
|---|---|
| `vchat.format.color` | Цвета `&0`–`&f` |
| `vchat.format.hex` | HEX-цвета |
| `vchat.format.style` | Bold, italic, underline, strikethrough, reset |
| `vchat.format.obfuscated` | Эффект `&k` |
| `vchat.antispam.bypass` | Обход cooldown, повторов и ограничения длины |

Параметры `ForEveryone: true` разрешают соответствующее оформление без permission. Операторы уровня 2 имеют все разрешения форматирования и обходят антиспам.

## Логирование

Безопасное значение по умолчанию — `includeCommandArguments: false`: в лог попадает название команды, но не её аргументы. Команды из `redactedCommands` скрывают аргументы всегда, даже если глобальная запись аргументов включена. Поддерживаются также namespaced-варианты вроде `/tiauth:login`.

## Установка

1. Поместить `VChat-1.6.5.jar` в `mods/` сервера Forge 1.20.1.
2. Установить LuckPerms, если нужны префиксы, permissions и сортировка.
3. Перезапустить сервер.
4. Настроить `config/VMods/VChat/vchat-config.json5` и выполнить `/vchat reload`.
